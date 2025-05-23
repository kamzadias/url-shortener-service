package com.urlshortenerservice.repository;

import com.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {

    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_hash_number_seq') FROM generate_series(1, :amount)
            """)
    List<Long> getUniqueNumbers(@Param("amount") int amount);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM hash WHERE id IN (
                        SELECT id FROM hash ORDER BY id ASC LIMIT :amount
                    ) RETURNING *
            """)
    List<Hash> getHashBatch(@Param("amount") long amount);
}
