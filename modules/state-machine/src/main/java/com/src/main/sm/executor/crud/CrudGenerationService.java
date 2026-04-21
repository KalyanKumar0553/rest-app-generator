package com.src.main.sm.executor.crud;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.common.GenerationLanguage;

@Service
public class CrudGenerationService {

    private final CrudRepositoryGenerator repositoryGenerator;

    public CrudGenerationService(CrudRepositoryGenerator repositoryGenerator) {
        this.repositoryGenerator = repositoryGenerator;
    }

    public void generate(Path root, List<CrudGenerationUnit> units, GenerationLanguage language) throws IOException {
        try {
            units.stream().forEach(unit -> {
                try {
                    repositoryGenerator.generate(root, unit, language);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
