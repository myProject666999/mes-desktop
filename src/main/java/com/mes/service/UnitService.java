package com.mes.service;

import com.mes.entity.Unit;
import com.mes.repository.UnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnitService {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public List<Unit> findAll() {
        return unitRepository.findAll();
    }

    public List<Unit> search(String code, String name) {
        return unitRepository.findByCodeContainingAndNameContaining(
                (code == null || code.isBlank()) ? null : code,
                (name == null || name.isBlank()) ? null : name
        );
    }

    public Unit findById(Long id) {
        return unitRepository.findById(id).orElse(null);
    }

    public Unit create(Unit unit) {
        return unitRepository.save(unit);
    }

    public Unit update(Unit unit) {
        return unitRepository.save(unit);
    }

    public void delete(Long id) {
        unitRepository.deleteById(id);
    }

    public void deleteAll(List<Long> ids) {
        ids.forEach(unitRepository::deleteById);
    }
}
