package com.src.main.dto;

import java.util.List;

import lombok.Data;

@Data
public class IdBlockDTO {
    private String name;
    private String capitalizedName;
    private String type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}