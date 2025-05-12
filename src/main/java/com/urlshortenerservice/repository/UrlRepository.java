package com.urlshortenerservice.repository;

import com.urlshortenerservice.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Url findByHash(String hash);

    Page<Url> findByCreatedAtBefore(LocalDateTime expirationDate, Pageable pageable);
}
