package com.src.main.workflow.engine;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.src.main.model.workflow.WorkflowExecutorPoolEntity;
import com.src.main.repository.WorkflowExecutorPoolRepository;

@Component
public class WorkflowExecutorPoolRegistry implements DisposableBean {

	private static final int MIN_POOL_SIZE = 1;
	private static final int MAX_POOL_SIZE_CAP = 64;
	private static final int MAX_QUEUE_CAPACITY = 1000;
	private static final int MAX_KEEP_ALIVE_SECONDS = 3600;

	private final WorkflowExecutorPoolRepository workflowExecutorPoolRepository;
	private final Map<String, ThreadPoolTaskExecutor> executorsByCode = new ConcurrentHashMap<>();

	public WorkflowExecutorPoolRegistry(WorkflowExecutorPoolRepository workflowExecutorPoolRepository) {
		this.workflowExecutorPoolRepository = workflowExecutorPoolRepository;
	}

	public Future<?> submit(String poolCode, Runnable task) {
		return executor(poolCode).submit(task);
	}

	public <T> Future<T> submit(String poolCode, Callable<T> task) {
		return executor(poolCode).submit(task);
	}

	public boolean exists(String poolCode) {
		return workflowExecutorPoolRepository.findByPoolCodeAndActiveTrue(poolCode).isPresent();
	}

	private ThreadPoolTaskExecutor executor(String poolCode) {
		return executorsByCode.computeIfAbsent(poolCode, this::buildExecutor);
	}

	private ThreadPoolTaskExecutor buildExecutor(String poolCode) {
		WorkflowExecutorPoolEntity pool = workflowExecutorPoolRepository.findByPoolCodeAndActiveTrue(poolCode)
				.orElseThrow(() -> new IllegalArgumentException("No active workflow executor pool with code " + poolCode));
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(bound(pool.getCorePoolSize(), MIN_POOL_SIZE, MAX_POOL_SIZE_CAP));
		executor.setMaxPoolSize(bound(pool.getMaxPoolSize(), MIN_POOL_SIZE, MAX_POOL_SIZE_CAP));
		executor.setQueueCapacity(bound(pool.getQueueCapacity(), 0, MAX_QUEUE_CAPACITY));
		executor.setKeepAliveSeconds(bound(pool.getKeepAliveSeconds(), 0, MAX_KEEP_ALIVE_SECONDS));
		executor.setThreadNamePrefix(pool.getPoolName().replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase() + "-");
		executor.initialize();
		return executor;
	}

	private int bound(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public void destroy() {
		executorsByCode.values().forEach(ThreadPoolTaskExecutor::shutdown);
		executorsByCode.clear();
	}
}
