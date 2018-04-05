package org.eltex.softwlc.stacollector.entity;

import com.datastax.driver.core.DataType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.cassandra.mapping.CassandraType;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.*;

@Table
public class StaData implements Serializable {

    @PrimaryKey
    @JsonIgnore
    @CassandraType(type = DataType.Name.UUID)
    private UUID id;

    private String apDomain;
    private String apMac;
    private String staMac;

    @CassandraType(type = DataType.Name.TINYINT)
    private Short rssi;

    @CassandraType(type = DataType.Name.TIMESTAMP)
    private Date lastSeen = new Date();

    @CassandraType(type = DataType.Name.TINYINT)
    private Short band;

    public StaData() {
    }

    public StaData(UUID id, String apDomain, String apMac, String staMac, Short rssi, Date lastSeen, Short band) {
        this.id = id;
        this.apDomain = apDomain;
        this.apMac = apMac;
        this.staMac = staMac;
        this.rssi = rssi;
        this.lastSeen = lastSeen;
        this.band = band;
    }

    public StaData(String apDomain, String apMac, String staMac, int rssi, int band) {
        this.id = UUID.randomUUID();
        this.apDomain = apDomain;
        this.apMac = apMac;
        this.staMac = staMac;
        this.rssi = (short) rssi;
        //        this.lastSeen = lastSeen;
        this.band = (short) band;
    }

    @JsonCreator
    public StaData(@JsonProperty("apDomain") String apDomain, @JsonProperty("apMac") String apMac,
                   @JsonProperty("staMac") String staMac, @JsonProperty("rssi") Short rssi,
                   @JsonProperty("band") Short band) {
        this.id = UUID.randomUUID();

        this.apDomain = apDomain;
        this.apMac = apMac;
        this.staMac = staMac;
        this.rssi = rssi;
        this.band = band;
    }

    public UUID getId() {
        return id;
    }

    public String getApDomain() {
        return apDomain;
    }

    public void setApDomain(String apDomain) {
        this.apDomain = apDomain;
    }

    public String getApMac() {
        return apMac;
    }

    public void setApMac(String apMac) {
        this.apMac = apMac;
    }

    public String getStaMac() {
        return staMac;
    }

    public void setStaMac(String staMac) {
        this.staMac = staMac;
    }

    public short getRssi() {
        return rssi;
    }

    public void setRssi(Short rssi) {
        this.rssi = rssi;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public short getBand() {
        return band;
    }

    public void setBand(short band) {
        this.band = band;
    }

    @Override
    public String toString() {
        return "StaData{" + "id=" + id + ", apDomain='" + apDomain + '\'' + ", apMac='" + apMac + '\'' + ", staMac='"
                + staMac + '\'' + ", rssi=" + rssi + ", lastSeen=" + lastSeen + ", band=" + band + '}';
    }
}
