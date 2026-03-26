package com.src.main.sm.executor.model;

import com.src.main.sm.executor.common.BoilerplateStyle;

public final class ModelBoilerplateStrategyFactory {

	private static final ModelBoilerplateStrategy PLAIN_STRATEGY = new PlainModelBoilerplateStrategy();

	private ModelBoilerplateStrategyFactory() {
	}

	public static ModelBoilerplateStrategy forStyle(BoilerplateStyle style) {
		return PLAIN_STRATEGY;
	}
}
