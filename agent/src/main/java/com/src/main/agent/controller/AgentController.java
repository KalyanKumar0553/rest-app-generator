package com.src.main.agent.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.agent.dto.request.AgentMessageRequestDto;
import com.src.main.agent.dto.request.AgentSessionCreateRequestDto;
import com.src.main.agent.dto.request.AgentSpecSaveRequestDto;
import com.src.main.agent.dto.response.AgentMessageResponseDto;
import com.src.main.agent.dto.response.AgentSessionResponseDto;
import com.src.main.agent.dto.response.AgentSessionSummaryDto;
import com.src.main.agent.dto.response.AgentSpecSaveResponseDto;
import com.src.main.agent.service.AgentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

	private final AgentService agentService;

	public AgentController(AgentService agentService) {
		this.agentService = agentService;
	}

	@PostMapping("/sessions")
	public ResponseEntity<AgentSessionResponseDto> createSession(
			@Valid @RequestBody AgentSessionCreateRequestDto request, Principal principal) {
		AgentSessionResponseDto response = agentService.createSession(request, principal.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/sessions")
	public ResponseEntity<List<AgentSessionSummaryDto>> listSessions(Principal principal) {
		return ResponseEntity.ok(agentService.listSessions(principal.getName()));
	}

	@GetMapping("/sessions/{sessionId}")
	public ResponseEntity<AgentSessionResponseDto> getSession(
			@PathVariable("sessionId") UUID sessionId, Principal principal) {
		return ResponseEntity.ok(agentService.getSession(sessionId, principal.getName()));
	}

	@GetMapping("/sessions/{sessionId}/messages")
	public ResponseEntity<List<AgentMessageResponseDto>> getMessages(
			@PathVariable("sessionId") UUID sessionId, Principal principal) {
		return ResponseEntity.ok(agentService.getMessages(sessionId, principal.getName()));
	}

	@PostMapping("/sessions/{sessionId}/messages")
	public ResponseEntity<AgentMessageResponseDto> sendMessage(
			@PathVariable("sessionId") UUID sessionId,
			@Valid @RequestBody AgentMessageRequestDto request, Principal principal) {
		AgentMessageResponseDto response = agentService.sendMessage(sessionId, request, principal.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/sessions/{sessionId}/generate")
	public ResponseEntity<AgentSessionResponseDto> generateSpec(
			@PathVariable("sessionId") UUID sessionId, Principal principal) {
		return ResponseEntity.ok(agentService.generateSpec(sessionId, principal.getName()));
	}

	@PostMapping("/sessions/{sessionId}/save")
	public ResponseEntity<AgentSpecSaveResponseDto> saveAsProject(
			@PathVariable("sessionId") UUID sessionId,
			@RequestBody(required = false) AgentSpecSaveRequestDto request, Principal principal) {
		AgentSpecSaveResponseDto response = agentService.saveAsProject(sessionId, request, principal.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/sessions/{sessionId}/cancel")
	public ResponseEntity<Void> cancelSession(
			@PathVariable("sessionId") UUID sessionId, Principal principal) {
		agentService.cancelSession(sessionId, principal.getName());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/sessions/{sessionId}")
	public ResponseEntity<Void> deleteSession(
			@PathVariable("sessionId") UUID sessionId, Principal principal) {
		agentService.deleteSession(sessionId, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
