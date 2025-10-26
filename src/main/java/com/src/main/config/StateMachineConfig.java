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
import com.src.main.service.StepExecutorFactory;
import com.src.main.sm.Events;
import com.src.main.sm.States;

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
				// Run DTO generation
				.stateEntry(States.DTO_GENERATION, runStep(States.DTO_GENERATION, Events.DTO_DONE, Events.DTO_FAIL))
				// NEW: generate application-<profile>.properties files
				.stateEntry(States.APPLICATION_FILES,
						runStep(States.APPLICATION_FILES, Events.APPFILES_DONE, Events.APPFILES_FAIL))
				// Scaffold the rest of the project
				.stateEntry(States.SCAFFOLD, runStep(States.SCAFFOLD, Events.SCAFFOLD_DONE, Events.SCAFFOLD_FAIL))
				.state(States.DONE).state(States.ERROR);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<States, Events> t) throws Exception {
		t
				// DTO -> APP FILES
				.withExternal().source(States.DTO_GENERATION).target(States.APPLICATION_FILES).event(Events.DTO_DONE)
				.and()
				// DTO -> ERROR
				.withExternal().source(States.DTO_GENERATION).target(States.ERROR).event(Events.DTO_FAIL).and()

				// APP FILES -> SCAFFOLD
				.withExternal().source(States.APPLICATION_FILES).target(States.SCAFFOLD).event(Events.APPFILES_DONE)
				.and()
				// APP FILES -> ERROR
				.withExternal().source(States.APPLICATION_FILES).target(States.ERROR).event(Events.APPFILES_FAIL).and()

				// SCAFFOLD -> DONE
				.withExternal().source(States.SCAFFOLD).target(States.DONE).event(Events.SCAFFOLD_DONE).and()
				// SCAFFOLD -> ERROR
				.withExternal().source(States.SCAFFOLD).target(States.ERROR).event(Events.SCAFFOLD_FAIL).and()

				// Any explicit fail -> ERROR (defensive)
				.withExternal().source(States.DTO_GENERATION).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.APPLICATION_FILES).target(States.ERROR).event(Events.FAIL).and()
				.withExternal().source(States.SCAFFOLD).target(States.ERROR).event(Events.FAIL);
	}

	private Action<States, Events> fireStartOnEntry() {
		return ctx -> {
			// Only fire START if request indicated it; otherwise ignore
			Boolean autostart = (Boolean) ctx.getExtendedState().getVariables().getOrDefault("autostart", Boolean.TRUE);
			if (Boolean.TRUE.equals(autostart)) {
				ctx.getStateMachine().sendEvent(Events.START);
			}
		};
	}

	/**
	 * Generic action that runs a StepExecutor via the Factory, merges outputs, and
	 * sends success/failure event
	 */
	private Action<States, Events> runStep(States state, Events success, Events fail) {
		return (StateContext<States, Events> ctx) -> {
			// Only execute when a START-ish event puts us here.
			// For the initial state (DTO_GENERATION), we allow event==null as well.
			if (ctx.getEvent() == null && state != States.DTO_GENERATION)
				return;

			try {
				var executor = factory.forState(state);
				StepResult res = executor.execute(ctx.getExtendedState());
				if (res.success()) {
					// Merge payload into ExtendedState to carry forward
					res.payload().forEach((k, v) -> ctx.getExtendedState().getVariables().put(k, v));
					ctx.getStateMachine().sendEvent(MessageBuilder.withPayload(success).build());
				} else {
					ctx.getExtendedState().getVariables().put("error", res.message());
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
