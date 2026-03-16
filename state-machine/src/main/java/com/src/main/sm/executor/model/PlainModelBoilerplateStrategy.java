package com.src.main.sm.executor.model;

public class PlainModelBoilerplateStrategy implements ModelBoilerplateStrategy {

	@Override
	public void apply(ModelBoilerplateContext context) {
		context.setUseLombok(false);
	}
}

