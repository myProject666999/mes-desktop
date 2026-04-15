package com.mes.dto;

import com.mes.entity.UnitOfMeasure;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class UnitOfMeasureDTO {
    private Long id;
    private String unitCode;
    private String unitName;
    private Boolean isPrimary;
    private Boolean isEnabled;
    private String description;
    private String createdAt;
    private String updatedAt;

    public static UnitOfMeasureDTO fromEntity(UnitOfMeasure entity) {
        UnitOfMeasureDTO dto = new UnitOfMeasureDTO();
        dto.setId(entity.getId());
        dto.setUnitCode(entity.getUnitCode());
        dto.setUnitName(entity.getUnitName());
        dto.setIsPrimary(entity.getIsPrimary());
        dto.setIsEnabled(entity.getIsEnabled());
        dto.setDescription(entity.getDescription());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(formatter));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(formatter));
        }

        return dto;
    }

    public String getIsPrimaryText() {
        return Boolean.TRUE.equals(isPrimary) ? "是" : "否";
    }

    public String getIsEnabledText() {
        return Boolean.TRUE.equals(isEnabled) ? "启用" : "禁用";
    }
}
