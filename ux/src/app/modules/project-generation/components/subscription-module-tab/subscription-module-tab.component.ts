import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';

type SubscriptionPlanDraft = {
  code: string;
  name: string;
  description: string;
  monthlyPrice: number;
  yearlyPrice: number;
  trialDays: number;
  featuresInput: string;
  rolesInput: string;
};

type SubscriptionConfigDraft = {
  currency: string;
  defaultPlanCode: string;
  allowTrial: boolean;
  plans: SubscriptionPlanDraft[];
};

@Component({
  selector: 'app-subscription-module-tab',
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
  templateUrl: './subscription-module-tab.component.html',
  styleUrls: ['./subscription-module-tab.component.css']
})
export class SubscriptionModuleTabComponent implements OnInit {
  config: SubscriptionConfigDraft = this.buildDefaultConfig();

  constructor(private readonly projectGenerationState: ProjectGenerationStateService) {}

  ngOnInit(): void {
    const savedConfig = this.projectGenerationState.getModuleConfigsSnapshot()['subscription'];
    this.config = this.normalizeConfig(savedConfig);
  }

  addPlan(): void {
    this.config = {
      ...this.config,
      plans: [
        ...this.config.plans,
        {
          code: '',
          name: '',
          description: '',
          monthlyPrice: 0,
          yearlyPrice: 0,
          trialDays: 0,
          featuresInput: '',
          rolesInput: ''
        }
      ]
    };
    this.persist();
  }

  removePlan(index: number): void {
    const nextPlans = this.config.plans.filter((_, planIndex) => planIndex !== index);
    this.config = {
      ...this.config,
      plans: nextPlans
    };
    if (!nextPlans.some((plan) => plan.code === this.config.defaultPlanCode)) {
      this.config.defaultPlanCode = nextPlans[0]?.code ?? '';
    }
    this.persist();
  }

  onConfigChange(): void {
    this.persist();
  }

  trackByIndex(index: number): number {
    return index;
  }

  private persist(): void {
    const normalizedPlans = this.config.plans
      .map((plan) => ({
        code: plan.code.trim().toUpperCase(),
        name: plan.name.trim(),
        description: plan.description.trim(),
        monthlyPrice: Number(plan.monthlyPrice || 0),
        yearlyPrice: Number(plan.yearlyPrice || 0),
        trialDays: Number(plan.trialDays || 0),
        features: this.toList(plan.featuresInput),
        roles: this.toList(plan.rolesInput)
      }))
      .filter((plan) => plan.code && plan.name);
    this.projectGenerationState.updateModuleConfig('subscription', {
      currency: this.config.currency.trim() || 'INR',
      defaultPlanCode: this.config.defaultPlanCode.trim().toUpperCase() || normalizedPlans[0]?.code || 'FREE',
      allowTrial: this.config.allowTrial,
      plans: normalizedPlans
    });
  }

  private normalizeConfig(rawConfig: Record<string, any> | undefined): SubscriptionConfigDraft {
    const defaults = this.buildDefaultConfig();
    if (!rawConfig || typeof rawConfig !== 'object') {
      this.persist();
      return defaults;
    }
    const plans = Array.isArray(rawConfig['plans']) && rawConfig['plans'].length
      ? rawConfig['plans'].map((plan) => ({
          code: String((plan as Record<string, unknown>)['code'] ?? '').trim().toUpperCase(),
          name: String((plan as Record<string, unknown>)['name'] ?? '').trim(),
          description: String((plan as Record<string, unknown>)['description'] ?? '').trim(),
          monthlyPrice: Number((plan as Record<string, unknown>)['monthlyPrice'] ?? 0),
          yearlyPrice: Number((plan as Record<string, unknown>)['yearlyPrice'] ?? 0),
          trialDays: Number((plan as Record<string, unknown>)['trialDays'] ?? 0),
          featuresInput: this.fromList((plan as Record<string, unknown>)['features']),
          rolesInput: this.fromList((plan as Record<string, unknown>)['roles'])
        }))
      : defaults.plans;
    const normalized: SubscriptionConfigDraft = {
      currency: String(rawConfig['currency'] ?? defaults.currency).trim() || defaults.currency,
      defaultPlanCode: String(rawConfig['defaultPlanCode'] ?? plans[0]?.code ?? defaults.defaultPlanCode).trim().toUpperCase(),
      allowTrial: typeof rawConfig['allowTrial'] === 'boolean' ? rawConfig['allowTrial'] : defaults.allowTrial,
      plans
    };
    this.projectGenerationState.updateModuleConfig('subscription', {
      currency: normalized.currency,
      defaultPlanCode: normalized.defaultPlanCode,
      allowTrial: normalized.allowTrial,
      plans: normalized.plans.map((plan) => ({
        code: plan.code,
        name: plan.name,
        description: plan.description,
        monthlyPrice: plan.monthlyPrice,
        yearlyPrice: plan.yearlyPrice,
        trialDays: plan.trialDays,
        features: this.toList(plan.featuresInput),
        roles: this.toList(plan.rolesInput)
      }))
    });
    return normalized;
  }

  private buildDefaultConfig(): SubscriptionConfigDraft {
    return {
      currency: 'INR',
      defaultPlanCode: 'FREE',
      allowTrial: true,
      plans: [
        {
          code: 'FREE',
          name: 'Free',
          description: 'Starter plan for low-volume or evaluation use.',
          monthlyPrice: 0,
          yearlyPrice: 0,
          trialDays: 0,
          featuresInput: 'core-api',
          rolesInput: 'ROLE_USER'
        },
        {
          code: 'PRO',
          name: 'Pro',
          description: 'Paid plan with analytics and team workflows.',
          monthlyPrice: 499,
          yearlyPrice: 4999,
          trialDays: 14,
          featuresInput: 'core-api, analytics, priority-support',
          rolesInput: 'ROLE_USER, ROLE_MANAGER'
        }
      ]
    };
  }

  private toList(rawValue: string): string[] {
    return rawValue
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }

  private fromList(rawValue: unknown): string {
    return Array.isArray(rawValue)
      ? rawValue.map((item) => String(item ?? '').trim()).filter(Boolean).join(', ')
      : '';
  }
}
