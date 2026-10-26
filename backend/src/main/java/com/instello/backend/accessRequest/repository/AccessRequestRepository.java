package com.instello.backend.accessRequest.repository;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.UUID;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID>, JpaSpecificationExecutor<AccessRequest> {

    boolean existsByEmailIgnoreCaseAndStatusIn(String email, Collection<AccessRequestStatus> statuses);
}
