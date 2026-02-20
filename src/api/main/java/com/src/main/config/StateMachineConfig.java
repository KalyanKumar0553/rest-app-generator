package com.src.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.Events;
import com.src.main.sm.config.States;
import com.src.main.sm.config.StepExecutorFactory;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

	private static final Logger log = LoggerFactory.getLogger(StateMachineConfig.class);
	private final StepExecutorFactory factory;

	public StateMachineConfig(StepExecutorFactory factory) {
		this.factory = factory;
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
		config.withConfiguration().autoStartup(false);
	}

	@Override
	public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
		states.withStates().initial(States.DTO_GENERATION, fireStartOnEntry())
				.stateEntry(States.DTO_GENERATION, runStep(States.DTO_GENERATION, Events.DTO_DONE, Events.DTO_FAIL))
				.stateEntry(States.ENUM_GENERATION,
						runStep(States.ENUM_GENERATION, Events.ENUM_DONE, Events.ENUM_FAIL))
				.stateEntry(States.MODEL_GENERATION,
						runStep(States.MODEL_GENERATION, Events.MODEL_DONE, Events.MODEL_FAIL))
				.stateEntry(States.SWAGGER_GENERATION,
						runStep(States.SWAGGER_GENERATION, Events.SWAGGER_DONE, Events.SWAGGER_FAIL))
				.stateEntry(States.REST_GENERATION,
						runStep(States.REST_GENERATION, Events.REST_DONE, Events.REST_FAIL))
				.stateEntry(States.ACTUATOR_CONFIGURATION,
						runStep(States.ACTUATOR_CONFIGURATION, Events.ACTUATOR_DONE, Events.ACTUATOR_FAIL))
				.stateEntry(States.APPLICATION_FILES,
						runStep(States.APPLICATION_FILES, Events.APPFILES_DONE, Events.APPFILES_FAIL))
				.stateEntry(States.DOCKER_GENERATION,
						runStep(States.DOCKER_GENERATION, Events.DOCKER_DONE, Events.DOCKER_FAIL))
				.stateEntry(States.SCAFFOLD, runStep(States.SCAFFOLD, Events.SCAFFOLD_DONE, Events.SCAFFOLD_FAIL))
				.state(States.DONE).state(States.ERROR);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<States, Events> t) throws Exception {
		t
				// DTO → ENUM on success; DTO → ERROR on fail
				.withExternal().source(States.DTO_GENERATION).target(States.ENUM_GENERATION).event(Events.DTO_DONE)
				.and().withExternal().source(States.DTO_GENERATION).target(States.ERROR).event(Events.DTO_FAIL).and()

				// ENUM → MODEL on success; ENUM → ERROR on fail
				.withExternal().source(States.ENUM_GENERATION).target(States.MODEL_GENERATION).event(Events.ENUM_DONE)
				.and().withExternal().source(States.ENUM_GENERATION).target(States.ERROR).event(Events.ENUM_FAIL).and()

				// MODEL → SWAGGER GENERATION on success; MODEL → ERROR on fail
				.withExternal().source(States.MODEL_GENERATION).target(States.SWAGGER_GENERATION)
				.event(Events.MODEL_DONE).and().withExternal().source(States.MODEL_GENERATION).target(States.ERROR)
				.event(Events.MODEL_FAIL).and()

				// SWAGGER GENERATION → REST GENERATION / ERROR
				.withExternal().source(States.SWAGGER_GENERATION).target(States.REST_GENERATION)
				.event(Events.SWAGGER_DONE).and().withExternal().source(States.SWAGGER_GENERATION).target(States.ERROR)
				.event(Events.SWAGGER_FAIL).and()

				// REST GENERATION → ACTUATOR CONFIG / ERROR
				.withExternal().source(States.REST_GENERATION).target(States.ACTUATOR_CONFIGURATION)
					.event(Events.REST_DONE).and().withExternal().source(States.REST_GENERATION).target(States.ERROR)
					.event(Events.REST_FAIL).and()

				// ACTUATOR CONFIG → APP FILES / ERROR
				.withExternal().source(States.ACTUATOR_CONFIGURATION).target(States.APPLICATION_FILES)
					.event(Events.ACTUATOR_DONE).and().withExternal().source(States.ACTUATOR_CONFIGURATION)
					.target(States.ERROR).event(Events.ACTUATOR_FAIL).and()

				// APP FILES → DOCKER / ERROR
				.withExternal().source(States.APPLICATION_FILES).target(States.DOCKER_GENERATION).event(Events.APPFILES_DONE)
				.and().withExternal().source(States.APPLICATION_FILES).target(States.ERROR).event(Events.APPFILES_FAIL)
				.and()

				// DOCKER → SCAFFOLD / ERROR
				.withExternal().source(States.DOCKER_GENERATION).target(States.SCAFFOLD).event(Events.DOCKER_DONE)
				.and().withExternal().source(States.DOCKER_GENERATION).target(States.ERROR).event(Events.DOCKER_FAIL)
				.and()

				// SCAFFOLD → DONE / ERROR
				.withExternal().source(States.SCAFFOLD).target(States.DONE).event(Events.SCAFFOLD_DONE).and()
				.withExternal().source(States.SCAFFOLD).target(States.ERROR).event(Events.SCAFFOLD_FAIL).and()

				// Global FAIL shortcuts → ERROR
				.withExternal().source(States.DTO_GENERATION).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.ENUM_GENERATION).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.MODEL_GENERATION).target(States.ERROR).event(Events.FAIL).and()
					.withExternal().source(States.SWAGGER_GENERATION).target(States.ERROR).event(Events.FAIL).and()
					.withExternal().source(States.REST_GENERATION).target(States.ERROR).event(Events.FAIL).and()
					.withExternal().source(States.ACTUATOR_CONFIGURATION).target(States.ERROR).event(Events.FAIL).and()
					.withExternal().source(States.APPLICATION_FILES).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.DOCKER_GENERATION).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.SCAFFOLD).target(States.ERROR).event(Events.FAIL);
	}

	private Action<States, Events> fireStartOnEntry() {
		return ctx -> {
			Boolean autostart = (Boolean) ctx.getExtendedState().getVariables().getOrDefault("autostart", Boolean.TRUE);
			if (Boolean.TRUE.equals(autostart)) {
				ctx.getStateMachine().sendEvent(Events.START);
			}
		};
	}

	private Action<States, Events> runStep(States state, Events success, Events fail) {
		return (StateContext<States, Events> ctx) -> {
			if (ctx.getEvent() == null && state != States.DTO_GENERATION)
				return;

			try {
				var executor = factory.forState(state);
				StepResult res = executor.execute(ctx.getExtendedState());
				if (res.isSuccess()) {
					res.getDetails().forEach((k, v) -> ctx.getExtendedState().getVariables().put(k, v));
					ctx.getStateMachine().sendEvent(MessageBuilder.withPayload(success).build());
				} else {
					ctx.getExtendedState().getVariables().put("error", res.getMessage());
					ctx.getStateMachine().sendEvent(MessageBuilder.withPayload(fail).build());
				}
			} catch (Exception ex) {
				log.error("Step '{}' failed", state, ex);
				ctx.getExtendedState().getVariables().put("error", ex.getMessage());
				ctx.getStateMachine().sendEvent(MessageBuilder.withPayload(Events.FAIL).build());
			}
		};
	}
}
