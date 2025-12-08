package com.src.main.dto;

import java.util.ArrayList;
import java.util.List;

public class AuditingBlockDTO {
    private boolean enabled;
    private List<String> annotations = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}