package com.src.main.sm.executor.common;

import java.util.Map;

/**
 * Contract for all code-generation units. Each unit encapsulates the data
 * needed to render one source file from a Mustache template.
 */
public interface GenerationUnit {

    /** Returns the Mustache context map used when rendering the template. */
    Map<String, Object> toTemplateModel();
}
