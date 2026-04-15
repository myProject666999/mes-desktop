package com.mes.repository;

import com.mes.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT m FROM Menu m JOIN m.roles r WHERE r.id = :roleId AND m.enabled = true ORDER BY m.sortOrder")
    List<Menu> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT DISTINCT m FROM Menu m JOIN m.roles r JOIN r.users u WHERE u.id = :userId AND m.enabled = true ORDER BY m.sortOrder")
    List<Menu> findByUserId(@Param("userId") Long userId);

    List<Menu> findByEnabledTrueOrderBySortOrder();
}
