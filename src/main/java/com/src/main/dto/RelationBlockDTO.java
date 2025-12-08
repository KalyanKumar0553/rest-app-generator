package com.src.main.dto;

import java.util.List;

public class RelationBlockDTO {
    private String name;
    private String capitalizedName;
    private String declarationType;
    private String targetType;
    private List<String> annotations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapitalizedName() {
        return capitalizedName;
    }

    public void setCapitalizedName(String capitalizedName) {
        this.capitalizedName = capitalizedName;
    }

    public String getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(String declarationType) {
        this.declarationType = declarationType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}