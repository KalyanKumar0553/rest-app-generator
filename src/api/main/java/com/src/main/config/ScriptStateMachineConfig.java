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
import org.springframework.statemachine.guard.Guard;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.ScriptEvents;
import com.src.main.sm.config.ScriptStates;
import com.src.main.sm.config.ScriptStepExecutorFactory;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;

@Configuration
@EnableStateMachineFactory(name = "scriptStateMachineFactory")
public class ScriptStateMachineConfig extends EnumStateMachineConfigurerAdapter<ScriptStates, ScriptEvents> {

	private static final Logger log = LoggerFactory.getLogger(ScriptStateMachineConfig.class);
	private final ScriptStepExecutorFactory factory;

	public ScriptStateMachineConfig(ScriptStepExecutorFactory factory) {
		this.factory = factory;
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<ScriptStates, ScriptEvents> config) throws Exception {
		config.withConfiguration().autoStartup(false);
	}

	@Override
	public void configure(StateMachineStateConfigurer<ScriptStates, ScriptEvents> states) throws Exception {
		states.withStates()
				.initial(ScriptStates.SCRIPT_ROUTER, fireStartOnEntry())
				.stateEntry(ScriptStates.NODE_GENERATION, runStep(ScriptStates.NODE_GENERATION, ScriptEvents.NODE_DONE, ScriptEvents.NODE_FAIL))
				.stateEntry(ScriptStates.PYTHON_GENERATION, runStep(ScriptStates.PYTHON_GENERATION, ScriptEvents.PYTHON_DONE, ScriptEvents.PYTHON_FAIL))
				.state(ScriptStates.DONE)
				.state(ScriptStates.ERROR);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<ScriptStates, ScriptEvents> t) throws Exception {
		t
				.withExternal().source(ScriptStates.SCRIPT_ROUTER).target(ScriptStates.NODE_GENERATION).event(ScriptEvents.START)
				.guard(languageGuard(GenerationLanguage.NODE))
				.and()
				.withExternal().source(ScriptStates.SCRIPT_ROUTER).target(ScriptStates.PYTHON_GENERATION).event(ScriptEvents.START)
				.guard(languageGuard(GenerationLanguage.PYTHON))
				.and()
				.withExternal().source(ScriptStates.SCRIPT_ROUTER).target(ScriptStates.ERROR).event(ScriptEvents.START)
				.and()
				.withExternal().source(ScriptStates.NODE_GENERATION).target(ScriptStates.DONE).event(ScriptEvents.NODE_DONE)
				.and()
				.withExternal().source(ScriptStates.NODE_GENERATION).target(ScriptStates.ERROR).event(ScriptEvents.NODE_FAIL)
				.and()
				.withExternal().source(ScriptStates.PYTHON_GENERATION).target(ScriptStates.DONE).event(ScriptEvents.PYTHON_DONE)
				.and()
				.withExternal().source(ScriptStates.PYTHON_GENERATION).target(ScriptStates.ERROR).event(ScriptEvents.PYTHON_FAIL)
				.and()
				.withExternal().source(ScriptStates.SCRIPT_ROUTER).target(ScriptStates.ERROR).event(ScriptEvents.FAIL)
				.and()
				.withExternal().source(ScriptStates.NODE_GENERATION).target(ScriptStates.ERROR).event(ScriptEvents.FAIL)
				.and()
				.withExternal().source(ScriptStates.PYTHON_GENERATION).target(ScriptStates.ERROR).event(ScriptEvents.FAIL);
	}

	private Guard<ScriptStates, ScriptEvents> languageGuard(GenerationLanguage... expected) {
		return ctx -> {
			Object yamlRaw = ctx.getExtendedState().getVariables().get("yaml");
			@SuppressWarnings("unchecked")
			GenerationLanguage resolved = GenerationLanguageResolver.resolveFromYaml(
					yamlRaw instanceof java.util.Map<?, ?> map ? (java.util.Map<String, Object>) map : null);
			for (GenerationLanguage candidate : expected) {
				if (candidate == resolved) {
					return true;
				}
			}
			return false;
		};
	}

	private Action<ScriptStates, ScriptEvents> fireStartOnEntry() {
		return ctx -> {
			Boolean autostart = (Boolean) ctx.getExtendedState().getVariables().getOrDefault("autostart", Boolean.TRUE);
			if (Boolean.TRUE.equals(autostart)) {
				ctx.getStateMachine().sendEvent(ScriptEvents.START);
			}
		};
	}

	private Action<ScriptStates, ScriptEvents> runStep(ScriptStates state, ScriptEvents success, ScriptEvents fail) {
		return (StateContext<ScriptStates, ScriptEvents> ctx) -> {
			if (ctx.getEvent() == null) {
				return;
			}
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
				log.error("Script step '{}' failed", state, ex);
				ctx.getExtendedState().getVariables().put("error", ex.getMessage());
				ctx.getStateMachine().sendEvent(MessageBuilder.withPayload(ScriptEvents.FAIL).build());
			}
		};
	}
}
