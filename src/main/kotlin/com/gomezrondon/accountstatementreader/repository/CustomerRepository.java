package com.gomezrondon.accountstatementreader.repository;

import com.gomezrondon.accountstatementreader.service.Consolidado;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CustomerRepository extends MongoRepository<Consolidado, String> {

    Consolidado findByStrDate(String strDate);
}
