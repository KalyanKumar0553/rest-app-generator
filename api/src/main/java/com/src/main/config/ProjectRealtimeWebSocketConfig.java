package com.src.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.src.main.websocket.ProjectRealtimeWebSocketHandler;

@Configuration
@EnableWebSocket
public class ProjectRealtimeWebSocketConfig implements WebSocketConfigurer {

	private final ProjectRealtimeWebSocketHandler projectRealtimeWebSocketHandler;

	public ProjectRealtimeWebSocketConfig(ProjectRealtimeWebSocketHandler projectRealtimeWebSocketHandler) {
		this.projectRealtimeWebSocketHandler = projectRealtimeWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(projectRealtimeWebSocketHandler, "/ws/projects/*")
				.setAllowedOriginPatterns("*");
	}
}
