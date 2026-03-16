package com.src.main.service;

import java.util.List;

import com.src.main.dto.MavenDependencyDTO;


public interface DependencyResolver {

	List<MavenDependencyDTO> resolveForMaven(List<String> idsOrGavs, String bootVersion, boolean includeOpenApi);
	List<String> resolveForGradle(List<String> idsOrGavs, String bootVersion, boolean includeOpenApi);
}
