import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';

type WorkflowTransitionDraft = {
  from: string;
  event: string;
  to: string;
};

type WorkflowConfigDraft = {
  workflowName: string;
  initialState: string;
  allowSelfTransitions: boolean;
  states: string[];
  transitions: WorkflowTransitionDraft[];
};

@Component({
  selector: 'app-state-machine-module-tab',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    ShippedModuleConfigPanelComponent
  ],
  templateUrl: './state-machine-module-tab.component.html',
  styleUrls: ['./state-machine-module-tab.component.css']
})
export class StateMachineModuleTabComponent implements OnInit {
  config: WorkflowConfigDraft = this.buildDefaultConfig();
  stateInput = '';

  constructor(private readonly projectGenerationState: ProjectGenerationStateService) {}

  ngOnInit(): void {
    const savedConfig = this.projectGenerationState.getModuleConfigsSnapshot()['state-machine'];
    this.config = this.normalizeConfig(savedConfig);
  }

  addState(): void {
    const nextState = this.stateInput.trim();
    if (!nextState || this.config.states.includes(nextState)) {
      return;
    }
    this.config = {
      ...this.config,
      states: [...this.config.states, nextState]
    };
    if (!this.config.initialState) {
      this.config.initialState = nextState;
    }
    this.stateInput = '';
    this.persist();
  }

  removeState(state: string): void {
    this.config = {
      ...this.config,
      states: this.config.states.filter((item) => item !== state),
      transitions: this.config.transitions.filter((item) => item.from !== state && item.to !== state)
    };
    if (this.config.initialState === state) {
      this.config.initialState = this.config.states[0] ?? '';
    }
    this.persist();
  }

  addTransition(): void {
    this.config = {
      ...this.config,
      transitions: [
        ...this.config.transitions,
        {
          from: this.config.states[0] ?? '',
          event: '',
          to: this.config.states[0] ?? ''
        }
      ]
    };
    this.persist();
  }

  removeTransition(index: number): void {
    this.config = {
      ...this.config,
      transitions: this.config.transitions.filter((_, transitionIndex) => transitionIndex !== index)
    };
    this.persist();
  }

  onConfigChange(): void {
    this.persist();
  }

  trackByIndex(index: number): number {
    return index;
  }

  private persist(): void {
    this.projectGenerationState.updateModuleConfig('state-machine', {
      workflowName: this.config.workflowName.trim() || 'default-workflow',
      initialState: this.config.initialState.trim() || this.config.states[0] || 'draft',
      allowSelfTransitions: this.config.allowSelfTransitions,
      states: this.config.states.map((state) => ({ id: state, label: this.toStateLabel(state) })),
      transitions: this.config.transitions
        .map((transition) => ({
          from: transition.from.trim(),
          event: transition.event.trim(),
          to: transition.to.trim()
        }))
        .filter((transition) => transition.from && transition.event && transition.to)
    });
  }

  private normalizeConfig(rawConfig: Record<string, any> | undefined): WorkflowConfigDraft {
    const defaults = this.buildDefaultConfig();
    if (!rawConfig || typeof rawConfig !== 'object') {
      this.persist();
      return defaults;
    }
    const states = Array.isArray(rawConfig['states'])
      ? rawConfig['states']
          .map((state) => typeof state === 'string'
            ? state.trim()
            : String((state as Record<string, unknown>)?.['id'] ?? '').trim())
          .filter(Boolean)
      : defaults.states;
    const transitions = Array.isArray(rawConfig['transitions'])
      ? rawConfig['transitions']
          .map((transition) => ({
            from: String((transition as Record<string, unknown>)?.['from'] ?? '').trim(),
            event: String((transition as Record<string, unknown>)?.['event'] ?? '').trim(),
            to: String((transition as Record<string, unknown>)?.['to'] ?? '').trim()
          }))
          .filter((transition) => transition.from || transition.event || transition.to)
      : defaults.transitions;
    const normalized: WorkflowConfigDraft = {
      workflowName: String(rawConfig['workflowName'] ?? defaults.workflowName).trim() || defaults.workflowName,
      initialState: String(rawConfig['initialState'] ?? states[0] ?? defaults.initialState).trim() || defaults.initialState,
      allowSelfTransitions: typeof rawConfig['allowSelfTransitions'] === 'boolean'
        ? rawConfig['allowSelfTransitions']
        : defaults.allowSelfTransitions,
      states: states.length ? states : defaults.states,
      transitions
    };
    this.projectGenerationState.updateModuleConfig('state-machine', {
      workflowName: normalized.workflowName,
      initialState: normalized.initialState,
      allowSelfTransitions: normalized.allowSelfTransitions,
      states: normalized.states.map((state) => ({ id: state, label: this.toStateLabel(state) })),
      transitions: normalized.transitions
    });
    return normalized;
  }

  private buildDefaultConfig(): WorkflowConfigDraft {
    return {
      workflowName: 'default-workflow',
      initialState: 'draft',
      allowSelfTransitions: false,
      states: ['draft', 'approved', 'archived'],
      transitions: [
        { from: 'draft', event: 'approve', to: 'approved' },
        { from: 'approved', event: 'archive', to: 'archived' }
      ]
    };
  }

  private toStateLabel(state: string): string {
    return state
      .split(/[-_\s]+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
