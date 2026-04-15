package com.mes.service;

import com.mes.entity.UnitOfMeasure;
import com.mes.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public UnitOfMeasureService(UnitOfMeasureRepository unitOfMeasureRepository) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    public List<UnitOfMeasure> findAll() {
        return unitOfMeasureRepository.findAll();
    }

    public Optional<UnitOfMeasure> findById(Long id) {
        return unitOfMeasureRepository.findById(id);
    }

    public Optional<UnitOfMeasure> findByUnitCode(String unitCode) {
        return unitOfMeasureRepository.findByUnitCode(unitCode);
    }

    public List<UnitOfMeasure> findByConditions(String unitCode, String unitName) {
        return unitOfMeasureRepository.findByConditions(
                (unitCode != null && !unitCode.isEmpty()) ? unitCode : null,
                (unitName != null && !unitName.isEmpty()) ? unitName : null
        );
    }

    public List<UnitOfMeasure> findEnabledUnits() {
        return unitOfMeasureRepository.findByIsEnabledTrue();
    }

    @Transactional
    public UnitOfMeasure create(UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasureRepository.existsByUnitCode(unitOfMeasure.getUnitCode())) {
            throw new RuntimeException("单位编码已存在: " + unitOfMeasure.getUnitCode());
        }
        return unitOfMeasureRepository.save(unitOfMeasure);
    }

    @Transactional
    public UnitOfMeasure update(UnitOfMeasure unitOfMeasure) {
        Optional<UnitOfMeasure> existing = unitOfMeasureRepository.findById(unitOfMeasure.getId());
        if (existing.isEmpty()) {
            throw new RuntimeException("计量单位不存在: " + unitOfMeasure.getId());
        }

        Optional<UnitOfMeasure> byCode = unitOfMeasureRepository.findByUnitCode(unitOfMeasure.getUnitCode());
        if (byCode.isPresent() && !byCode.get().getId().equals(unitOfMeasure.getId())) {
            throw new RuntimeException("单位编码已存在: " + unitOfMeasure.getUnitCode());
        }

        return unitOfMeasureRepository.save(unitOfMeasure);
    }

    @Transactional
    public void delete(Long id) {
        unitOfMeasureRepository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        unitOfMeasureRepository.deleteAllById(ids);
    }

    public boolean existsByUnitCode(String unitCode) {
        return unitOfMeasureRepository.existsByUnitCode(unitCode);
    }
}
