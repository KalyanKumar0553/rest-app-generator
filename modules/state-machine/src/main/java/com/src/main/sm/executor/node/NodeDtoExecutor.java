package com.src.main.sm.executor.node;

import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component("nodeDtoExecutor")
public class NodeDtoExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			for (NodeDtoDefinition dtoDefinition : context.dtos()) {
				String folder = "response".equals(dtoDefinition.dtoType()) ? "response" : "request";
				NodeGenerationSupport.writeFile(context.root(), "src/dto/" + folder + "/" + dtoDefinition.name() + ".ts", renderDto(dtoDefinition));
				NodeGenerationSupport.writeFile(context.root(),
						"src/validation/dto/" + folder + "/" + dtoDefinition.name() + ".schema.ts",
						NodeValidationSupport.renderDtoSchema(context, dtoDefinition));
			}
			return NodeGenerationSupport.success("Node DTOs generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_DTO_GENERATION", ex.getMessage());
		}
	}

	private String renderDto(NodeDtoDefinition dtoDefinition) {
		String fields = dtoDefinition.fields().stream()
				.map(field -> "  " + field.name() + (field.optional() ? "?: " : ": ") + field.tsType() + ";")
				.collect(Collectors.joining("\n"));
		return "export interface " + dtoDefinition.name() + " {\n" + fields + (fields.isBlank() ? "" : "\n") + "}\n";
	}
}
