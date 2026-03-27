package com.shubhasamagri.repository;

import com.shubhasamagri.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndPoojaKitId(Long userId, Long kitId);
    void deleteByUserId(Long userId);
}
