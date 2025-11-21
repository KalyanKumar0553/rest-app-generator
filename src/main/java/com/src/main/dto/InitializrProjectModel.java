package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitializrProjectModel {

    private String groupId;
    private String artifactId;
    private String version;
    private String name;
    private String description;
    private String packaging;
    private String generator;
    private String jdkVersion;
    private String bootVersion;
    private boolean includeOpenapi;
    private boolean angularIntegration;
    @Override
    public String toString() {
        return "InitializrProjectModel{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", packaging='" + packaging + '\'' +
                ", generator='" + generator + '\'' +
                ", jdkVersion='" + jdkVersion + '\'' +
                ", bootVersion='" + bootVersion + '\'' +
                ", includeOpenapi=" + includeOpenapi +
                ", angularIntegration=" + angularIntegration +
                '}';
    }
}