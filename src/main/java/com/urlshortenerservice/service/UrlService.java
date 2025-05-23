package com.urlshortenerservice.service;

import com.urlshortenerservice.entity.Url;
import com.urlshortenerservice.exception.UrlNotFoundException;
import com.urlshortenerservice.repository.UrlRepository;
import com.urlshortenerservice.service.cache.HashCache;
import com.urlshortenerservice.service.cache.UrlCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlCache urlCache;
    private final HashCache hashCache;

    @Value("${base.short-url}")
    private String baseShortUrl;

    @Transactional(readOnly = true)
    public String getLongUrl(String hash) {
        String longUrl = urlCache.getLongUrl(hash);
        if (longUrl != null) {
            log.info("Found long URL in Redis for hash {}", hash);
            return longUrl;
        }

        Url urlEntity = urlRepository.findByHash(hash);
        if (urlEntity != null) {
            longUrl = urlEntity.getUrl();
            urlCache.saveUrlMapping(hash, longUrl);
            log.info("Found long URL in DB for hash {} and updated cache", hash);
            return longUrl;
        }

        throw new UrlNotFoundException("No URL found for hash: " + hash);
    }

    @Transactional
    public String createShortUrl(String longUrl) {
        String hash = hashCache.getHash();
        String shortUrl = baseShortUrl + hash;
        Url newUrl = Url.builder()
                .url(longUrl)
                .hash(hash)
                .build();

        urlRepository.save(newUrl);
        urlCache.saveUrlMapping(hash, longUrl);

        log.info("Created a short URL {} for a long URL {}", shortUrl, longUrl);
        return shortUrl;
    }

    public Page<Url> getPageExpiredUrls(LocalDateTime expirationDate, Pageable pageable) {
        return urlRepository.findByCreatedAtBefore(expirationDate, pageable);
    }

    @Transactional
    public void deleteUrls(List<Url> urls) {
        urlRepository.deleteAll(urls);
    }
}
