package com.medibrary.api.repository;

import com.medibrary.api.entity.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugRepository extends JpaRepository<Drug, String> {
    List<Drug> findTop20ByNameContainingIgnoreCase(String name);
    List<Drug> findTop20ByShapeAndColor(String shape, String color);
    List<Drug> findTop20ByShape(String shape);
    List<Drug> findTop20ByColor(String color);
}
