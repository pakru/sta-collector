package com.example.demo.config;

import com.datastax.driver.core.Session;
import com.datastax.spark.connector.cql.CassandraConnector;
import com.example.demo.SpringSpark2Application;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource({ "classpath:application.properties"})
public class SparkConfig {
    @Autowired
    private Environment env;

    @Bean
    public SparkConf sparkConf() {

        return new SparkConf()
                .setAppName("testSpark")
                //                .setSparkHome(sparkHome)
                //                .setMaster("local[*]")
                //                .setMaster("spark://192.168.26.127:7077")
//                .setMaster("spark://192.168.26.208:7077")
                .setMaster(env.getProperty(AppConfig.SPARKHOST))
//                .setJars(new String[]{
//                        "/home/pavel/STORAGE/tester/projects/tests/spring-spark2/target/spring-spark2-0.1-SNAPSHOT.jar",
//                        "/home/pavel/.m2/repository/org/apache/spark/spark-sql_2.11/2.2.1/spark-sql_2.11-2.2.1.jar",
//                        "/home/pavel/.m2/repository/com/datastax/spark/spark-cassandra-connector_2.11/2.0.5/spark-cassandra-connector_2.11-2.0.5.jar"})
                .set("spark.cassandra.connection.host",env.getProperty(AppConfig.CONTACTPOINTS));
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkConf());
    }

    @Bean
    public Session cassandraSession() {
        CassandraConnector connector = CassandraConnector.apply(sparkConf());
        return connector.openSession();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
