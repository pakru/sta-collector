package org.eltex.softwlc.stacollector.repo;

import org.eltex.softwlc.stacollector.entity.StaData;

import org.springframework.data.cassandra.repository.CassandraRepository;
//import org.springframework.stereotype.Repository;

public interface StaDataRepo extends CassandraRepository<StaData> {

}
