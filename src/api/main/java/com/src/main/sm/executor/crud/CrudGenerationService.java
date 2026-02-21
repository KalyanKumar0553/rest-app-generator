package com.src.main.sm.executor.crud;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CrudGenerationService {

	private final CrudRepositoryGenerator repositoryGenerator;

	public CrudGenerationService(CrudRepositoryGenerator repositoryGenerator) {
		this.repositoryGenerator = repositoryGenerator;
	}

	public void generate(Path root, List<CrudGenerationUnit> units) throws Exception {
		for (CrudGenerationUnit unit : units) {
			repositoryGenerator.generate(root, unit);
		}
	}
}
