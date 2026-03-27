package com.shubhasamagri.repository;

import com.shubhasamagri.entity.Occasion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccasionRepository extends JpaRepository<Occasion, Long> {
    List<Occasion> findByIsActiveTrue();
    Optional<Occasion> findByName(String name);
}
