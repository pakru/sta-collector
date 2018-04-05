package org.eltex.softwlc.stacollector.controller;

import org.eltex.softwlc.stacollector.config.AppConfig;
import org.eltex.softwlc.stacollector.entity.StaData;
import org.eltex.softwlc.stacollector.repo.StaDataRepo;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import scala.Tuple2;

import java.util.*;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.column;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapColumnTo;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;

@RestController
public class RestApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StaDataRepo staDataDao;

    @Autowired
    private JavaSparkContext sc;

    @Autowired
    Environment environment;

    @RequestMapping(value = "/addmac")
    private String addMac(@RequestParam(name = "domain", defaultValue = "n/a") String domainName,
                          @RequestParam(name = "apmac") String apMac,
                          @RequestParam(name = "stamac") String staMac,
                          @RequestParam(name = "rssi", defaultValue = "0") int rssi,
                          @RequestParam(name = "band", defaultValue = "n/a") String band){

        if (apMac != null && staMac != null){
            StaData sta = new StaData(domainName, apMac, staMac, rssi, Integer.parseInt(band));
            staDataDao.insert(sta);
        } else
            staDataDao.insert(new StaData("New Domain", "00:11:22:33:44:55", "01:23:44:55:66:77", -50,
                    5));
        return "OK";
    }

    @ExceptionHandler({Exception.class})
    private String handleException(Exception ex){
        ex.printStackTrace();
        return "Exception: " + ex.toString();
    }

    // TODO!  Выбрасывает exception при пустой базе
    @RequestMapping("/getStat")
    private String getAvg() {
        JavaRDD<Long> dataRssiStaRDD = CassandraJavaUtil.javaFunctions(sc)
                .cassandraTable(environment.getProperty(AppConfig.KEYSPACE), "stadata", mapColumnTo(Long.class))
                .select("rssi");

        JavaRDD<StaData> dataStaRDD = CassandraJavaUtil.javaFunctions(sc)
                .cassandraTable(environment.getProperty(AppConfig.KEYSPACE), "stadata", mapRowTo(StaData.class))
                .select(column("id"),
                        column("apdomain").as("apDomain"),
                        column("apmac").as("apMac"),
                        column("band"),
                        column("lastseen").as("lastSeen"),
                        column("rssi"),
                        column("stamac").as("staMac"));

        Long staCount = dataStaRDD.map(StaData::getStaMac).distinct().count();
        Long apCount =  dataStaRDD.map(StaData::getApMac).distinct().count();

        JavaRDD<StaData> sta2GRDD = dataStaRDD.filter(v -> v.getBand() == 2);
        JavaRDD<StaData> sta5GRDD = dataStaRDD.filter(v -> v.getBand() == 5);

        Long rssi2GSum = sta2GRDD.map(staData -> (long) staData.getRssi()).reduce((x, y) -> x + y);
        Long rssi5GSum = sta5GRDD.map(staData -> (long) staData.getRssi()).reduce((x, y) -> x + y);

        Long sta2GCount = sta2GRDD.map(StaData::getStaMac).count();
        Long sta5GCount = sta5GRDD.map(StaData::getStaMac).count();

//        Long rssiSum = dataRssiStaRDD.reduce((x, y) -> x + y);
        Long totalEntries = dataRssiStaRDD.count();

        float avg2G = rssi2GSum / sta2GCount;
        float avg5G = rssi5GSum / sta5GCount;

        StringBuilder resultStat = new StringBuilder();
        resultStat.append(" --- STAT --- \n Total Entries in table: " + totalEntries +
                "\n Total unic AP MAC: " + apCount +
                "\n Total unic STA MAC: " + staCount +
                "\n    2.4G: " + sta2GCount + "; Avg RSSI: " + avg2G +
                "\n    5G: "   + sta5GCount + "; Avg RSSI: " + avg5G);

        return resultStat.toString();
    }


    @RequestMapping("/getApUsage")
    private Map<String, Set<String>> getApUsage(@RequestParam(name = "apmac", defaultValue = "") String apMac){

        JavaRDD<StaData> dataStaRDD = CassandraJavaUtil.javaFunctions(sc)
                .cassandraTable(environment.getProperty(AppConfig.KEYSPACE), "stadata", mapRowTo(StaData.class))
                .select(column("id"),
                        column("apdomain").as("apDomain"),
                        column("apmac").as("apMac"),
                        column("band"),
                        column("lastseen").as("lastSeen"),
                        column("rssi"),
                        column("stamac").as("staMac"));

        JavaPairRDD<String,String> pairStaRDD = dataStaRDD.mapToPair(data ->
                new Tuple2<>(data.getApMac(),data.getStaMac()));               // делаем пару MAC AP - MAC STA

        JavaPairRDD<String, Set<String>> apResultRDD = pairStaRDD.combineByKey(
                (org.apache.spark.api.java.function.Function<String, Set<String>>) v1 -> {Set<String> res = new HashSet<>();
                    res.add(v1);
                    return res;},  // create combiner

                (Function2<Set<String>, String, Set<String>>) (val1, val2) -> {
                    val1.add(val2);
                    return val1;}, // merge value

                (Function2<Set<String>, Set<String>, Set<String>>)(val1, val2) -> { Set<String> res = new HashSet<>();
                    res.addAll(val1);
                    res.addAll(val2);
                    return res;}); // merge combiner

        Map<String, Set<String>> apResultMap;
        if (apMac.isEmpty())
            apResultMap = apResultRDD.collectAsMap();
        else {
            apResultMap = apResultRDD.filter(t -> t._1.equals(apMac)).collectAsMap();
        }

        return apResultMap;
    }

    @RequestMapping("/getStaUsage")
    private Map<String, Set<String>> getStaUsage(@RequestParam(name = "stamac", defaultValue = "") String staMac){
        JavaRDD<StaData> dataStaRDD = CassandraJavaUtil.javaFunctions(sc)
                .cassandraTable(environment.getProperty(AppConfig.KEYSPACE), "stadata", mapRowTo(StaData.class))
                .select(column("id"),
                        column("apdomain").as("apDomain"),
                        column("apmac").as("apMac"),
                        column("band"),
                        column("lastseen").as("lastSeen"),
                        column("rssi"),
                        column("stamac").as("staMac"));

        JavaPairRDD<String,String> pairStaRDD = dataStaRDD.mapToPair(data ->
                new Tuple2<>(data.getStaMac(),data.getApMac()));               // делаем пару MAC STA - MAC AP

        JavaPairRDD<String, Set<String>> apResultRDD = pairStaRDD.combineByKey(
                (org.apache.spark.api.java.function.Function<String, Set<String>>) v1 -> {Set<String> res = new HashSet<>();
                    res.add(v1);
                    return res;},  // create combiner

                (Function2<Set<String>, String, Set<String>>) (val1, val2) -> {
                    val1.add(val2);
                    return val1;}, // merge value

                (Function2<Set<String>, Set<String>, Set<String>>)(val1, val2) -> { Set<String> res = new HashSet<>();
                    res.addAll(val1);
                    res.addAll(val2);
                    return res;}); // merge combiner

        Map<String, Set<String>> apResultMap;

        if (staMac.isEmpty())
            apResultMap = apResultRDD.collectAsMap();
        else {
            apResultMap = apResultRDD.filter(t -> t._1.equals(staMac)).collectAsMap();
        }
        return apResultMap;
    }

}
