package com.src.main.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.PluginModuleResponseDTO;
import com.src.main.service.PluginModuleService;

@RestController
@RequestMapping("/api/plugin-modules")
public class PluginModuleController {

	private final PluginModuleService pluginModuleService;

	public PluginModuleController(PluginModuleService pluginModuleService) {
		this.pluginModuleService = pluginModuleService;
	}

	@GetMapping("/published")
	@PreAuthorize("isAuthenticated()")
	public List<PluginModuleResponseDTO> listPublished(
			@RequestParam(value = "generator", required = false) String generator) {
		return pluginModuleService.getPublishedModules(generator);
	}
}
