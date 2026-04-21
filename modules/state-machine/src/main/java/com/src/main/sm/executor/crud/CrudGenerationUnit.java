package com.src.main.sm.executor.crud;

import java.util.LinkedHashMap;
import java.util.Map;

import com.src.main.sm.executor.common.GenerationUnit;

/**
 * Encapsulates everything needed to generate one CRUD repository interface.
 * Implements {@link GenerationUnit} so the template model is co-located with
 * the data it describes.
 */
public class CrudGenerationUnit implements GenerationUnit {

    private final String entityName;
    private final String idName;
    private final String idType;
    private final String idTypeImport;
    private final String modelPackage;
    private final String repositoryPackage;
    private final String repositoryClass;
    private final boolean noSql;

    public CrudGenerationUnit(String entityName, String idName, String idType,
                               String idTypeImport, String modelPackage,
                               String repositoryPackage, boolean noSql) {
        this.entityName = entityName;
        this.idName = idName;
        this.idType = idType;
        this.idTypeImport = idTypeImport;
        this.modelPackage = modelPackage;
        this.repositoryPackage = repositoryPackage;
        this.repositoryClass = entityName + "Repository";
        this.noSql = noSql;
    }

    public String getRepositoryPackage() { return repositoryPackage; }
    public String getRepositoryClass()   { return repositoryClass; }

    @Override
    public Map<String, Object> toTemplateModel() {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("entityName",        entityName);
        model.put("idName",            idName);
        model.put("idType",            idType);
        model.put("idTypeImport",      idTypeImport);
        model.put("modelPackage",      modelPackage);
        model.put("repositoryPackage", repositoryPackage);
        model.put("repositoryClass",   repositoryClass);
        model.put("noSql",             noSql);
        return model;
    }
}
