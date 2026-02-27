package com.src.main.sm.executor.crud;

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

	public void generate(Path root, List<CrudGenerationUnit> units, GenerationLanguage language) throws Exception {
		for (CrudGenerationUnit unit : units) {
			repositoryGenerator.generate(root, unit, language);
		}
	}
}
