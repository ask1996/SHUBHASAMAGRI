package com.shubhasamagri.repository;

import com.shubhasamagri.entity.PoojaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PoojaItemRepository extends JpaRepository<PoojaItem, Long> {
    List<PoojaItem> findByIsAvailableTrue();
}
