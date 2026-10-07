package com.example.shortener;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByCode(String code);
    boolean existsByCode(String code);
    List<ShortLink> findTop20ByOrderByCreatedAtDesc();

    // Single atomic UPDATE: no lost clicks under concurrent redirects
    @Modifying
    @Query("update ShortLink s set s.clicks = s.clicks + 1 where s.id = :id")
    void incrementClicks(@Param("id") Long id);
}
