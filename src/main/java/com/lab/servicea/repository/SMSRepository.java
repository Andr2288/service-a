package com.lab.servicea.repository;

/*
    @project   service-a
    @class     SMSRepository
    @version   1.0.0
    @since     22.10.2025 - 23:34
*/

import com.lab.servicea.model.SMS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SMSRepository extends JpaRepository<SMS, String> {
    Page<SMS> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
