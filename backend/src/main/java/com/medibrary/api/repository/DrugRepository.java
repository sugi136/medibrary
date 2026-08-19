package com.medibrary.api.repository;

import com.medibrary.api.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugRepository extends JpaRepository<Drug, String> {
    Page<Drug> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Drug> findByShapeAndColor(String shape, String color, Pageable pageable);

    Page<Drug> findByShape(String shape, Pageable pageable);

    Page<Drug> findByColor(String color, Pageable pageable);
}
