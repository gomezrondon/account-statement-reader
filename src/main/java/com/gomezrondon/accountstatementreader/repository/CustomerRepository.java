package com.gomezrondon.accountstatementreader.repository;

import com.gomezrondon.accountstatementreader.service.Consolidado;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface CustomerRepository extends MongoRepository<Consolidado, String> {

    Consolidado findByCreationDateBetween(LocalDateTime desde, LocalDateTime hasta);
}
