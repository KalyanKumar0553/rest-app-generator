package com.src.main.sm.executor.model;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.LayeredSpecSupport;

/**
 * Entry point for model (entity) generation.
 *
 * <p>Delegates entity file rendering to {@link ModelGenerator} and MongoDB
 * infrastructure files to {@link MongoSupportFileGenerator}, keeping each
 * component focused on a single responsibility.
 */
@Service
public class ModelGenerationService {

    private final TemplateEngine templateEngine;
    private final MongoSupportFileGenerator mongoSupportFileGenerator;

    public ModelGenerationService(TemplateEngine templateEngine,
                                   MongoSupportFileGenerator mongoSupportFileGenerator) {
        this.templateEngine = templateEngine;
        this.mongoSupportFileGenerator = mongoSupportFileGenerator;
    }

    public void generate(Map<String, Object> yaml, Path root,
                          String basePackage, GenerationLanguage language) throws Exception {
        new ModelGenerator(templateEngine, basePackage, language).generate(yaml, root);

        if (isNoSql(yaml)) {
            AppSpecDTO spec = new ObjectMapper().convertValue(yaml, AppSpecDTO.class);
            boolean domainLayout = "domain".equalsIgnoreCase(
                    StringUtils.firstNonBlank(spec.getPackages(), "technical"));
            mongoSupportFileGenerator.generate(spec, root, basePackage, domainLayout, language);
        }
    }

    private static boolean isNoSql(Map<String, Object> yaml) {
        if (yaml == null) return false;
        Object dbType = LayeredSpecSupport.resolveDatabaseType(yaml);
        if (dbType != null && "NOSQL".equalsIgnoreCase(String.valueOf(dbType).trim())) return true;
        Object dbCode = LayeredSpecSupport.resolveDatabaseCode(yaml);
        return dbCode != null && "MONGODB".equalsIgnoreCase(String.valueOf(dbCode).trim());
    }
}
