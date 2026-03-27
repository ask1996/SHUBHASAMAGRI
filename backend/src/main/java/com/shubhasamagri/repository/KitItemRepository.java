package com.shubhasamagri.repository;

import com.shubhasamagri.entity.KitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KitItemRepository extends JpaRepository<KitItem, Long> {
    List<KitItem> findByPoojaKitId(Long kitId);
}
