package com.mes.repository;

import com.mes.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    @Query("SELECT u FROM Unit u WHERE (:code IS NULL OR u.code LIKE %:code%) AND (:name IS NULL OR u.name LIKE %:name%)")
    List<Unit> findByCodeContainingAndNameContaining(@Param("code") String code, @Param("name") String name);
}
