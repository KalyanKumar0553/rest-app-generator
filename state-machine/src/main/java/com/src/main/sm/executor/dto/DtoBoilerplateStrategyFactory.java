package com.src.main.sm.executor.dto;

import com.src.main.sm.executor.common.BoilerplateStyle;

public final class DtoBoilerplateStrategyFactory {

	private static final DtoBoilerplateStrategy PLAIN_STRATEGY = new PlainDtoBoilerplateStrategy();

	private DtoBoilerplateStrategyFactory() {
	}

	public static DtoBoilerplateStrategy forStyle(BoilerplateStyle style) {
		return PLAIN_STRATEGY;
	}
}
