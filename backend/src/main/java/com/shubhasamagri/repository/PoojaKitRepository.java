package com.shubhasamagri.repository;

import com.shubhasamagri.entity.PoojaKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PoojaKitRepository extends JpaRepository<PoojaKit, Long> {
    List<PoojaKit> findByIsActiveTrue();
    List<PoojaKit> findByOccasionId(Long occasionId);
    List<PoojaKit> findByOccasionIdAndIsActiveTrue(Long occasionId);
}
