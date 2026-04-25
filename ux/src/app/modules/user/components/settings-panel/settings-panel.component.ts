import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import {
  AdminService,
  ConfigProperty,
  PluginModule,
  PluginModulePayload,
  PluginModuleVersionPayload,
  ProjectTabDefinitionAdmin,
  ProjectTabDefinitionPayload
} from '../../../../services/admin.service';
import { ToastService } from '../../../../services/toast.service';
import { UserService, UserRoles } from '../../../../services/user.service';
import { getApiUserMessage } from '../../../../utils/api-error.utils';

interface PluginModuleFormModel {
  id: string | null;
  code: string;
  name: string;
  description: string;
  category: string;
  enabled: boolean;
  enableConfig: boolean;
  generatorTargetsInput: string;
}

interface PluginModuleVersionFormModel {
  versionCode: string;
  changelog: string;
  artifactFile: File | null;
}

interface ProjectTabDefinitionFormModel {
  id: string | null;
  key: string;
  label: string;
  icon: string;
  componentKey: string;
  order: number;
  generatorLanguage: string;
  enabled: boolean;
}

@Component({
  selector: 'app-settings-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule],
  templateUrl: './settings-panel.component.html',
  styleUrls: ['./settings-panel.component.css']
})
export class SettingsPanelComponent implements OnInit {
  private static readonly AI_LABS_FEATURE_KEY = 'app.feature.ai-labs.enabled';
  private static readonly AI_LABS_USAGE_LIMIT_KEY = 'app.feature.ai-labs.usage-limit';
  private static readonly PLUGIN_MODULES_FEATURE_KEY = 'app.feature.plugin-modules.enabled';
  private static readonly NEWSLETTER_MAX_RETRY_KEY = 'app.newsletter.max-email-retry-attempts';
  private static readonly SHIPPED_MODULE_CODES = new Set(['rbac', 'auth', 'state-machine', 'subscription']);
  userRoles: string[] = [];
  userPermissions: string[] = [];
  featureConfigs: ConfigProperty[] = [];
  pluginModules: PluginModule[] = [];
  projectTabDefinitions: ProjectTabDefinitionAdmin[] = [];
  isLoadingFeatureConfigs = false;
  isLoadingPluginModules = false;
  isLoadingProjectTabDefinitions = false;
  isSavingAiLabsFeature = false;
  isSavingPluginModulesFeature = false;
  isSavingPluginModule = false;
  isPublishingPluginModule = false;
  isSavingProjectTabDefinition = false;
  isDeletingProjectTabDefinition = false;
  pluginModuleForm: PluginModuleFormModel = this.createEmptyPluginModuleForm();
  pluginModuleVersionForm: PluginModuleVersionFormModel = this.createEmptyPluginModuleVersionForm();
  projectTabDefinitionForm: ProjectTabDefinitionFormModel = this.createEmptyProjectTabDefinitionForm();
  selectedProjectTabLanguage = 'java';
  selectedPluginModuleForVersionUpload: PluginModule | null = null;

  constructor(
    private userService: UserService,
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.userService.getUserRoles().subscribe({
      next: (rolesData: UserRoles) => {
        this.userRoles = rolesData.roles || [];
        this.userPermissions = rolesData.permissions || [];
        if (this.canManageFeatureFlags()) {
          this.loadFeatureConfigs();
        }
        if (this.canManagePluginModules()) {
          this.loadPluginModules();
        }
        if (this.canManageProjectTabLayouts()) {
          this.loadProjectTabDefinitions();
        }
      },
      error: () => {
        this.toastService.error('Failed to load user roles');
      }
    });
  }

  hasPermission(permission: string): boolean {
    return this.userPermissions.includes(permission);
  }

  canManageFeatureFlags(): boolean {
    return this.hasPermission('config.feature.read') || this.hasPermission('config.feature.manage');
  }

  canEditFeatureFlags(): boolean {
    return this.hasPermission('config.feature.manage');
  }

  canManagePluginModules(): boolean {
    return this.hasPermission('plugin.module.read') || this.hasPermission('plugin.module.manage') || this.hasPermission('plugin.module.publish');
  }

  canEditPluginModules(): boolean {
    return this.hasPermission('plugin.module.manage');
  }

  canPublishPluginModules(): boolean {
    return this.hasPermission('plugin.module.publish');
  }

  canManageProjectTabLayouts(): boolean {
    return this.hasPermission('project.tab.layout.read') || this.hasPermission('project.tab.layout.manage');
  }

  canEditProjectTabLayouts(): boolean {
    return this.hasPermission('project.tab.layout.manage');
  }

  loadFeatureConfigs(): void {
    if (!this.canManageFeatureFlags()) {
      return;
    }
    this.isLoadingFeatureConfigs = true;
    this.adminService.getConfigFeatures().subscribe({
      next: (features) => {
        this.featureConfigs = Array.isArray(features) ? features : [];
        this.isLoadingFeatureConfigs = false;
      },
      error: () => {
        this.isLoadingFeatureConfigs = false;
        this.toastService.error('Failed to load feature settings.');
      }
    });
  }

  isAiLabsEnabled(): boolean {
    return this.getFeatureCurrentValue(SettingsPanelComponent.AI_LABS_FEATURE_KEY) === 'true';
  }

  getAiLabsUsageLimitValue(): string {
    return this.getFeatureCurrentValue(SettingsPanelComponent.AI_LABS_USAGE_LIMIT_KEY) || '5';
  }

  getAiLabsUsageLimitLabel(): string {
    const value = this.getAiLabsUsageLimitValue();
    const matched = this.getAiLabsUsageLimitOptions().find((option) => option.valueKey === value);
    return matched?.valueLabel || value;
  }

  getAiLabsUsageLimitOptions() {
    return this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.AI_LABS_USAGE_LIMIT_KEY)?.values || [];
  }

  getNewsletterRetryLimitValue(): string {
    return this.getFeatureCurrentValue(SettingsPanelComponent.NEWSLETTER_MAX_RETRY_KEY) || '3';
  }

  getNewsletterRetryLimitLabel(): string {
    const value = this.getNewsletterRetryLimitValue();
    const matched = this.getNewsletterRetryLimitOptions().find((option) => option.valueKey === value);
    return matched?.valueLabel || value;
  }

  getNewsletterRetryLimitOptions() {
    return this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.NEWSLETTER_MAX_RETRY_KEY)?.values || [];
  }

  isPluginModulesEnabled(): boolean {
    return this.getFeatureCurrentValue(SettingsPanelComponent.PLUGIN_MODULES_FEATURE_KEY) === 'true';
  }

  updateAiLabsFeature(enabled: boolean): void {
    const feature = this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.AI_LABS_FEATURE_KEY);
    if (!feature || !this.canEditFeatureFlags()) {
      return;
    }
    this.isSavingAiLabsFeature = true;
    this.adminService.updateConfigFeatureValue({
      category: feature.category,
      propertyKey: feature.propertyKey,
      valueKey: enabled ? 'true' : 'false'
    }).subscribe({
      next: (updatedFeature) => {
        this.isSavingAiLabsFeature = false;
        this.featureConfigs = this.featureConfigs.map((item) =>
          item.propertyKey === updatedFeature.propertyKey ? updatedFeature : item
        );
        this.toastService.success('AI Labs setting updated.');
      },
      error: (error) => {
        this.isSavingAiLabsFeature = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to update AI Labs setting.'));
      }
    });
  }

  updateAiLabsUsageLimit(valueKey: string): void {
    const feature = this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.AI_LABS_USAGE_LIMIT_KEY);
    if (!feature || !this.canEditFeatureFlags()) {
      return;
    }
    this.isSavingAiLabsFeature = true;
    this.adminService.updateConfigFeatureValue({
      category: feature.category,
      propertyKey: feature.propertyKey,
      valueKey
    }).subscribe({
      next: (updatedFeature) => {
        this.isSavingAiLabsFeature = false;
        this.featureConfigs = this.featureConfigs.map((item) =>
          item.propertyKey === updatedFeature.propertyKey ? updatedFeature : item
        );
        this.toastService.success('AI Labs usage limit updated.');
      },
      error: (error) => {
        this.isSavingAiLabsFeature = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to update AI Labs usage limit.'));
      }
    });
  }

  updatePluginModulesFeature(enabled: boolean): void {
    const feature = this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.PLUGIN_MODULES_FEATURE_KEY);
    if (!feature || !this.canEditFeatureFlags()) {
      return;
    }
    this.isSavingPluginModulesFeature = true;
    this.adminService.updateConfigFeatureValue({
      category: feature.category,
      propertyKey: feature.propertyKey,
      valueKey: enabled ? 'true' : 'false'
    }).subscribe({
      next: (updatedFeature) => {
        this.isSavingPluginModulesFeature = false;
        this.featureConfigs = this.featureConfigs.map((item) =>
          item.propertyKey === updatedFeature.propertyKey ? updatedFeature : item
        );
        this.toastService.success('Plugin Modules setting updated.');
      },
      error: (error) => {
        this.isSavingPluginModulesFeature = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to update Plugin Modules setting.'));
      }
    });
  }

  updateNewsletterRetryLimit(valueKey: string): void {
    const feature = this.featureConfigs.find((item) => item.propertyKey === SettingsPanelComponent.NEWSLETTER_MAX_RETRY_KEY);
    if (!feature || !this.canEditFeatureFlags()) {
      return;
    }
    this.isSavingAiLabsFeature = true;
    this.adminService.updateConfigFeatureValue({
      category: feature.category,
      propertyKey: feature.propertyKey,
      valueKey
    }).subscribe({
      next: (updatedFeature) => {
        this.isSavingAiLabsFeature = false;
        this.featureConfigs = this.featureConfigs.map((item) =>
          item.propertyKey === updatedFeature.propertyKey ? updatedFeature : item
        );
        this.toastService.success('Newsletter retry limit updated.');
      },
      error: (error) => {
        this.isSavingAiLabsFeature = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to update newsletter retry limit.'));
      }
    });
  }

  loadPluginModules(): void {
    if (!this.canManagePluginModules()) {
      return;
    }
    this.isLoadingPluginModules = true;
    this.adminService.getPluginModules().subscribe({
      next: (modules) => {
        this.pluginModules = Array.isArray(modules) ? modules : [];
        this.isLoadingPluginModules = false;
      },
      error: () => {
        this.isLoadingPluginModules = false;
        this.toastService.error('Failed to load plugin modules.');
      }
    });
  }

  loadProjectTabDefinitions(): void {
    if (!this.canManageProjectTabLayouts()) {
      return;
    }
    this.isLoadingProjectTabDefinitions = true;
    this.adminService.getProjectTabDefinitions().subscribe({
      next: (definitions) => {
        this.projectTabDefinitions = Array.isArray(definitions) ? definitions : [];
        this.isLoadingProjectTabDefinitions = false;
      },
      error: () => {
        this.isLoadingProjectTabDefinitions = false;
        this.toastService.error('Failed to load project tab layouts.');
      }
    });
  }

  getVisibleProjectTabDefinitions(): ProjectTabDefinitionAdmin[] {
    return this.projectTabDefinitions
      .filter((tab) => String(tab.generatorLanguage || '').trim().toLowerCase() === this.selectedProjectTabLanguage)
      .slice()
      .sort((left, right) => {
        if (left.order === right.order) {
          return left.key.localeCompare(right.key);
        }
        return left.order - right.order;
      });
  }

  editProjectTabDefinition(tab: ProjectTabDefinitionAdmin): void {
    this.projectTabDefinitionForm = {
      id: tab.id,
      key: tab.key,
      label: tab.label,
      icon: tab.icon,
      componentKey: tab.componentKey,
      order: tab.order,
      generatorLanguage: tab.generatorLanguage,
      enabled: tab.enabled
    };
    this.selectedProjectTabLanguage = String(tab.generatorLanguage || 'java').trim().toLowerCase() || 'java';
  }

  resetProjectTabDefinitionForm(): void {
    this.projectTabDefinitionForm = this.createEmptyProjectTabDefinitionForm(this.selectedProjectTabLanguage);
  }

  saveProjectTabDefinition(): void {
    if (!this.canEditProjectTabLayouts()) {
      return;
    }
    const payload = this.buildProjectTabDefinitionPayload();
    if (!payload.key.trim()) {
      this.toastService.error('Tab key is required.');
      return;
    }
    if (!payload.label.trim()) {
      this.toastService.error('Tab label is required.');
      return;
    }
    if (!payload.icon.trim()) {
      this.toastService.error('Tab icon is required.');
      return;
    }
    if (!payload.componentKey.trim()) {
      this.toastService.error('Component key is required.');
      return;
    }
    this.isSavingProjectTabDefinition = true;
    const request$ = this.projectTabDefinitionForm.id
      ? this.adminService.updateProjectTabDefinition(this.projectTabDefinitionForm.id, payload)
      : this.adminService.createProjectTabDefinition(payload);
    request$.subscribe({
      next: () => {
        this.isSavingProjectTabDefinition = false;
        this.toastService.success(this.projectTabDefinitionForm.id ? 'Project tab updated.' : 'Project tab created.');
        this.resetProjectTabDefinitionForm();
        this.loadProjectTabDefinitions();
      },
      error: (error) => {
        this.isSavingProjectTabDefinition = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to save project tab definition.'));
      }
    });
  }

  deleteProjectTabDefinition(tab: ProjectTabDefinitionAdmin): void {
    if (!this.canEditProjectTabLayouts() || this.isDeletingProjectTabDefinition) {
      return;
    }
    this.isDeletingProjectTabDefinition = true;
    this.adminService.deleteProjectTabDefinition(tab.id).subscribe({
      next: () => {
        this.isDeletingProjectTabDefinition = false;
        this.toastService.success('Project tab deleted.');
        if (this.projectTabDefinitionForm.id === tab.id) {
          this.resetProjectTabDefinitionForm();
        }
        this.loadProjectTabDefinitions();
      },
      error: (error) => {
        this.isDeletingProjectTabDefinition = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to delete project tab definition.'));
      }
    });
  }

  editPluginModule(module: PluginModule): void {
    this.pluginModuleForm = {
      id: module.id,
      code: module.code,
      name: module.name,
      description: module.description || '',
      category: module.category || '',
      enabled: module.enabled,
      enableConfig: !!module.enableConfig,
      generatorTargetsInput: (module.generatorTargets || []).join(', ')
    };
    this.selectedPluginModuleForVersionUpload = this.canUploadPluginVersions(module) ? module : null;
  }

  preparePluginVersionUpload(module: PluginModule): void {
    if (!this.canUploadPluginVersions(module)) {
      return;
    }
    this.selectedPluginModuleForVersionUpload = module;
    this.pluginModuleVersionForm = this.createEmptyPluginModuleVersionForm();
  }

  canUploadPluginVersions(module: PluginModule | null | undefined): boolean {
    const code = String(module?.code ?? '').trim().toLowerCase();
    return code.length > 0 && !SettingsPanelComponent.SHIPPED_MODULE_CODES.has(code);
  }

  onPluginArtifactSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.pluginModuleVersionForm.artifactFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  resetPluginModuleForm(): void {
    this.pluginModuleForm = this.createEmptyPluginModuleForm();
  }

  resetPluginVersionForm(): void {
    this.pluginModuleVersionForm = this.createEmptyPluginModuleVersionForm();
    this.selectedPluginModuleForVersionUpload = null;
  }

  savePluginModule(): void {
    if (!this.canEditPluginModules()) {
      return;
    }
    const payload = this.buildPluginModulePayload();
    if (!payload.code.trim()) {
      this.toastService.error('Plugin module code is required.');
      return;
    }
    if (!payload.name.trim()) {
      this.toastService.error('Plugin module name is required.');
      return;
    }
    if (payload.generatorTargets.length === 0) {
      this.toastService.error('At least one generator target is required.');
      return;
    }
    if (!this.pluginModuleForm.id) {
      if (!this.pluginModuleVersionForm.versionCode.trim()) {
        this.toastService.error('Initial plugin version is required.');
        return;
      }
      if (!this.pluginModuleVersionForm.artifactFile) {
        this.toastService.error('Initial plugin zip file is required.');
        return;
      }
    }
    this.isSavingPluginModule = true;
    const request$ = this.pluginModuleForm.id
      ? this.adminService.updatePluginModule(this.pluginModuleForm.id, payload)
      : this.createPluginModuleWithInitialVersion(payload);
    request$.subscribe({
      next: () => {
        this.isSavingPluginModule = false;
        this.toastService.success(this.pluginModuleForm.id ? 'Plugin module updated.' : 'Plugin module created.');
        this.resetPluginModuleForm();
        this.resetPluginVersionForm();
        this.loadPluginModules();
      },
      error: (error) => {
        this.isSavingPluginModule = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to save plugin module.'));
      }
    });
  }

  uploadPluginModuleVersion(): void {
    if (!this.canEditPluginModules() || !this.selectedPluginModuleForVersionUpload) {
      return;
    }
    const payload = this.buildPluginModuleVersionPayload();
    if (!payload.versionCode.trim()) {
      this.toastService.error('Plugin version is required.');
      return;
    }
    if (!this.pluginModuleVersionForm.artifactFile) {
      this.toastService.error('Plugin zip file is required.');
      return;
    }
    this.isSavingPluginModule = true;
    this.adminService.uploadPluginModuleVersion(
      this.selectedPluginModuleForVersionUpload.id,
      payload,
      this.pluginModuleVersionForm.artifactFile
    ).subscribe({
      next: () => {
        this.isSavingPluginModule = false;
        this.toastService.success('Plugin module version uploaded.');
        this.resetPluginVersionForm();
        this.loadPluginModules();
      },
      error: (error) => {
        this.isSavingPluginModule = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to upload plugin module version.'));
      }
    });
  }

  publishPluginVersion(module: PluginModule, versionId: string): void {
    if (!this.canPublishPluginModules()) {
      return;
    }
    this.isPublishingPluginModule = true;
    this.adminService.publishPluginModuleVersion(module.id, versionId).subscribe({
      next: () => {
        this.isPublishingPluginModule = false;
        this.toastService.success('Plugin version published.');
        this.loadPluginModules();
      },
      error: (error) => {
        this.isPublishingPluginModule = false;
        this.toastService.error(getApiUserMessage(error, 'Failed to publish plugin version.'));
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(',', '');
  }

  private createEmptyPluginModuleForm(): PluginModuleFormModel {
    return {
      id: null,
      code: '',
      name: '',
      description: '',
      category: '',
      enabled: true,
      enableConfig: false,
      generatorTargetsInput: 'java'
    };
  }

  private createEmptyPluginModuleVersionForm(): PluginModuleVersionFormModel {
    return {
      versionCode: '',
      changelog: '',
      artifactFile: null
    };
  }

  private createEmptyProjectTabDefinitionForm(generatorLanguage = this.selectedProjectTabLanguage): ProjectTabDefinitionFormModel {
    return {
      id: null,
      key: '',
      label: '',
      icon: 'public',
      componentKey: '',
      order: 10,
      generatorLanguage: generatorLanguage || 'java',
      enabled: true
    };
  }

  private buildPluginModulePayload(): PluginModulePayload {
    return {
      code: this.pluginModuleForm.code.trim(),
      name: this.pluginModuleForm.name.trim(),
      description: this.pluginModuleForm.description.trim() || null,
      category: this.pluginModuleForm.category.trim() || null,
      enabled: this.pluginModuleForm.enabled,
      enableConfig: this.pluginModuleForm.enableConfig,
      generatorTargets: this.pluginModuleForm.generatorTargetsInput
        .split(',')
        .map((value) => value.trim().toLowerCase())
        .filter((value, index, self) => value.length > 0 && self.indexOf(value) === index)
    };
  }

  private buildPluginModuleVersionPayload(): PluginModuleVersionPayload {
    return {
      versionCode: this.pluginModuleVersionForm.versionCode.trim(),
      changelog: this.pluginModuleVersionForm.changelog.trim() || null
    };
  }

  private createPluginModuleWithInitialVersion(payload: PluginModulePayload) {
    return this.adminService.createPluginModule(
      payload,
      this.buildPluginModuleVersionPayload(),
      this.pluginModuleVersionForm.artifactFile as File
    );
  }

  private getFeatureCurrentValue(propertyKey: string): string {
    return this.featureConfigs.find((item) => item.propertyKey === propertyKey)?.currentValueKey || '';
  }

  private buildProjectTabDefinitionPayload(): ProjectTabDefinitionPayload {
    return {
      key: this.projectTabDefinitionForm.key.trim(),
      label: this.projectTabDefinitionForm.label.trim(),
      icon: this.projectTabDefinitionForm.icon.trim(),
      componentKey: this.projectTabDefinitionForm.componentKey.trim(),
      order: Number(this.projectTabDefinitionForm.order || 0),
      generatorLanguage: this.projectTabDefinitionForm.generatorLanguage.trim().toLowerCase(),
      enabled: this.projectTabDefinitionForm.enabled
    };
  }
}
