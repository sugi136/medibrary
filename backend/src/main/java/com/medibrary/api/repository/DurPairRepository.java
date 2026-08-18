package com.medibrary.api.repository;

import com.medibrary.api.entity.DurPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DurPairRepository extends JpaRepository<DurPair, Long> {
    Optional<DurPair> findByDrugAIdAndDrugBId(String drugIdA, String drugIdB);

    @Query("""
            select pair from DurPair pair
            join fetch pair.drugA
            join fetch pair.drugB
            where pair.drugA.id = :drugId or pair.drugB.id = :drugId
            """)
    List<DurPair> findAllInvolvingDrug(@Param("drugId") String drugId);

    @Query("""
            select pair from DurPair pair
            join fetch pair.drugA
            join fetch pair.drugB
            where pair.drugA.id in :drugIds and pair.drugB.id in :drugIds
            """)
    List<DurPair> findAllWithinDrugIds(@Param("drugIds") Collection<String> drugIds);
}
