package com.urlshortenerservice.service;

import com.urlshortenerservice.entity.Hash;
import com.urlshortenerservice.repository.HashRepository;
import com.urlshortenerservice.utils.Base62Encoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashServiceTest {

    @Mock
    private HashRepository hashRepository;
    @Mock
    private Base62Encoder base62Encoder;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Spy
    @InjectMocks
    private HashService hashService;

    @Test
    public void generateBatch_ShouldCallRepositoryAndSaveHashes() throws Exception {
        List<Long> uniqueNumbers = Arrays.asList(1L, 2L, 3L);
        when(hashRepository.getUniqueNumbers(anyInt())).thenReturn(uniqueNumbers);

        List<String> encodedHashes = Arrays.asList("a", "b", "c");
        when(base62Encoder.encode(uniqueNumbers)).thenReturn(encodedHashes);

        CompletableFuture<Void> future = hashService.generateBatch();
        future.get();
        verify(jdbcTemplate, times(1)).batchUpdate(
                eq("INSERT INTO hash (hash) VALUES (?)"),
                eq(encodedHashes),
                eq(encodedHashes.size()),
                any()
        );
    }

    @Test
    public void getHashes_WhenEnoughHashesAvailable_ShouldReturnAll() {
        List<Hash> batch = Arrays.asList(
                new Hash(null, "hash1"),
                new Hash(null, "hash2"),
                new Hash(null, "hash3")
        );
        when(hashRepository.getHashBatch(3)).thenReturn(batch);

        List<String> result = hashService.getHashes(3);
        assertThat(result).containsExactly("hash1", "hash2", "hash3");
        verify(hashRepository, times(1)).getHashBatch(3);
        verify(hashRepository, never()).saveAll(any());
    }

    @Test
    public void getHashes_WhenNotEnoughHashesAvailable_ShouldGenerateAdditionalAndReturnCombined() throws Exception {
        List<Hash> initialBatch = Arrays.asList(
                new Hash(null, "hash1"),
                new Hash(null, "hash2")
        );
        List<Hash> additionalBatch = Arrays.asList(
                new Hash(null, "hash3"),
                new Hash(null, "hash4")
        );
        when(hashRepository.getHashBatch(eq(4L))).thenReturn(initialBatch);
        when(hashRepository.getHashBatch(eq(2L))).thenReturn(additionalBatch);

        doReturn(CompletableFuture.completedFuture(null)).when(hashService).generateBatch();

        List<String> result = hashService.getHashes(4);
        assertThat(result).containsExactly("hash1", "hash2", "hash3", "hash4");
        verify(hashService, times(1)).generateBatch();
    }
}