package com.src.main.sm.executor.node;

import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component
public class NodeEnumExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			for (NodeEnumDefinition enumDefinition : context.enums()) {
				NodeGenerationSupport.writeFile(context.root(), "src/enums/" + enumDefinition.name() + ".ts", renderEnum(enumDefinition));
				NodeGenerationSupport.writeFile(context.root(),
						"src/validation/enums/" + enumDefinition.name() + ".schema.ts",
						NodeValidationSupport.renderEnumSchema(enumDefinition));
			}
			return NodeGenerationSupport.success("Node enums generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_ENUM_GENERATION", ex.getMessage());
		}
	}

	private String renderEnum(NodeEnumDefinition enumDefinition) {
		String constants = enumDefinition.constants().stream()
				.map(constant -> "  " + constant + " = \"" + constant + "\"")
				.collect(Collectors.joining(",\n"));
		return "export enum " + enumDefinition.name() + " {\n" + constants + "\n}\n";
	}
}
