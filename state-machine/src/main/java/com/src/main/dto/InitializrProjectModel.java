package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
    private boolean includeLombok;
    private boolean angularIntegration;
    
}
