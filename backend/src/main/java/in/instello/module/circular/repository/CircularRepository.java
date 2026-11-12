package in.instello.module.circular.repository;

import in.instello.module.circular.entity.Circular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircularRepository extends JpaRepository<Circular, String> {}
