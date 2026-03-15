package com.src.main.sm.executor.dto;

public class LombokDtoBoilerplateStrategy implements DtoBoilerplateStrategy {

	@Override
	public void apply(DtoBoilerplateContext context) {
		context.setUseLombok(true);
		context.getImports().add("lombok.Getter");
		context.getImports().add("lombok.Setter");
		context.getClassAnnotations().add("@Getter");
		context.getClassAnnotations().add("@Setter");
		if (context.isGenerateNoArgsConstructor()) {
			context.getImports().add("lombok.NoArgsConstructor");
			context.getClassAnnotations().add("@NoArgsConstructor");
		}
		if (context.isGenerateAllArgsConstructor()) {
			context.getImports().add("lombok.AllArgsConstructor");
			context.getClassAnnotations().add("@AllArgsConstructor");
		}
		if (context.isGenerateBuilder()) {
			context.getImports().add("lombok.Builder");
			context.getClassAnnotations().add("@Builder");
		}
		if (context.isGenerateToString()) {
			context.getImports().add("lombok.ToString");
			context.getClassAnnotations().add("@ToString");
		}
		if (context.isGenerateEquals() || context.isGenerateHashCode()) {
			context.getImports().add("lombok.EqualsAndHashCode");
			context.getClassAnnotations().add("@EqualsAndHashCode");
		}
	}
}
