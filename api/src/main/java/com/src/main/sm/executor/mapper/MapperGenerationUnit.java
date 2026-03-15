package com.src.main.sm.executor.mapper;

import java.util.List;

public record MapperGenerationUnit(
		String packageName,
		String className,
		String sourceSimpleName,
		String targetSimpleName,
		String sourceFqcn,
		String targetFqcn,
		List<MapperMappingLine> forwardMappings,
		List<MapperMappingLine> reverseMappings) {
}
