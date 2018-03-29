package com.example.demo.repo;

import com.example.demo.entity.StaData;
import org.springframework.data.cassandra.repository.CassandraRepository;
//import org.springframework.stereotype.Repository;

public interface StaDataRepo extends CassandraRepository<StaData> {

}
