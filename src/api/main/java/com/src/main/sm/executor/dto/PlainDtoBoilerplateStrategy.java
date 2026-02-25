package com.src.main.sm.executor.dto;

public class PlainDtoBoilerplateStrategy implements DtoBoilerplateStrategy {

	@Override
	public void apply(DtoBoilerplateContext context) {
		context.setUseLombok(false);
	}
}

