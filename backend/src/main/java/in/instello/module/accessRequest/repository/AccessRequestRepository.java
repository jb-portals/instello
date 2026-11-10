package in.instello.module.accessRequest.repository;

import in.instello.module.accessRequest.entity.AccessRequest;
import in.instello.module.accessRequest.entity.AccessRequestStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccessRequestRepository
    extends JpaRepository<AccessRequest, String>, JpaSpecificationExecutor<AccessRequest> {

  boolean existsByEmailIgnoreCaseAndStatusIn(
      String email, Collection<AccessRequestStatus> statuses);
}
