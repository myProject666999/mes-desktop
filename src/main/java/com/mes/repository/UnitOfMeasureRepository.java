package com.mes.repository;

import com.mes.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    Optional<UnitOfMeasure> findByUnitCode(String unitCode);

    boolean existsByUnitCode(String unitCode);

    @Query("SELECT u FROM UnitOfMeasure u WHERE " +
           "(:unitCode IS NULL OR u.unitCode LIKE %:unitCode%) AND " +
           "(:unitName IS NULL OR u.unitName LIKE %:unitName%)")
    List<UnitOfMeasure> findByConditions(@Param("unitCode") String unitCode,
                                         @Param("unitName") String unitName);

    List<UnitOfMeasure> findByIsEnabledTrue();

    List<UnitOfMeasure> findByIsPrimaryTrue();
}
