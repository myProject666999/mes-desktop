package com.mes.service;

import com.mes.entity.Menu;
import com.mes.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> findAll() {
        return menuRepository.findAll();
    }

    public List<Menu> findEnabledMenus() {
        return menuRepository.findByEnabledTrueOrderBySortOrder();
    }

    public List<Menu> findByRoleId(Long roleId) {
        return menuRepository.findByRoleId(roleId);
    }

    public List<Menu> findByUserId(Long userId) {
        return menuRepository.findByUserId(userId);
    }

    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    public Optional<Menu> findByCode(String code) {
        return menuRepository.findByCode(code);
    }

    @Transactional
    public Menu create(Menu menu) {
        if (menuRepository.existsByCode(menu.getCode())) {
            throw new RuntimeException("菜单编码已存在: " + menu.getCode());
        }
        return menuRepository.save(menu);
    }

    @Transactional
    public Menu update(Menu menu) {
        Optional<Menu> existing = menuRepository.findById(menu.getId());
        if (existing.isEmpty()) {
            throw new RuntimeException("菜单不存在: " + menu.getId());
        }

        Optional<Menu> byCode = menuRepository.findByCode(menu.getCode());
        if (byCode.isPresent() && !byCode.get().getId().equals(menu.getId())) {
            throw new RuntimeException("菜单编码已存在: " + menu.getCode());
        }

        return menuRepository.save(menu);
    }

    @Transactional
    public void delete(Long id) {
        menuRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return menuRepository.existsByCode(code);
    }
}
