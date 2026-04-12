import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtifactAdminService,
  ArtifactApp,
  ArtifactAppPayload,
  ArtifactAppVersion
} from '../../../../services/artifact-admin.service';
import { UserService, UserRoles } from '../../../../services/user.service';
import { ToastService } from '../../../../services/toast.service';
import { LoadingOverlayComponent } from '../../../../components/shared/loading-overlay/loading-overlay.component';

interface ArtifactAppFormModel {
  id: string | null;
  code: string;
  name: string;
  description: string;
  status: string;
  generatorLanguage: string;
  buildTool: string;
  enabledPacks: string[];
  configJson: string;
}

interface PackOption {
  key: string;
  label: string;
  description: string;
}

@Component({
  selector: 'app-artifacts-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingOverlayComponent],
  templateUrl: './artifacts-panel.component.html',
  styleUrls: ['./artifacts-panel.component.css']
})
export class ArtifactsPanelComponent implements OnInit {
  readonly packOptions: PackOption[] = [
    { key: 'crm', label: 'CRM', description: 'Leads, contacts, opportunities, and customer-facing pipeline management.' },
    { key: 'order', label: 'Order', description: 'Order intake, validation, payment state, and fulfillment orchestration.' },
    { key: 'inventory', label: 'Inventory', description: 'Warehouses, reservations, stock movement, and available-to-promise control.' },
    { key: 'shipping', label: 'Shipping', description: 'Shipment planning, package tracking, carrier flows, and delivery events.' }
  ];

  readonly statusOptions = ['DRAFT', 'ACTIVE', 'ARCHIVED'];
  readonly generatorOptions = ['java', 'node'];
  readonly buildToolOptions = ['maven', 'gradle', 'npm'];

  userPermissions: string[] = [];
  artifacts: ArtifactApp[] = [];
  versions: ArtifactAppVersion[] = [];
  selectedArtifactId: string | null = null;
  versionCodeInput = '';

  isLoading = false;
  isSaving = false;
  isLoadingVersions = false;
  isSnapshotting = false;
  isPublishing = false;

  readonly form: ArtifactAppFormModel = this.createEmptyForm();

  constructor(
    private userService: UserService,
    private artifactAdminService: ArtifactAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.userService.getUserRoles().subscribe({
      next: (rolesData: UserRoles) => {
        this.userPermissions = rolesData.permissions || [];
        if (this.canReadArtifacts()) {
          this.loadArtifacts();
        }
      },
      error: () => {
        this.toastService.error('Failed to load user permissions.');
      }
    });
  }

  canReadArtifacts(): boolean {
    return this.userPermissions.includes('artifact.app.read');
  }

  canManageArtifacts(): boolean {
    return this.userPermissions.includes('artifact.app.manage');
  }

  canPublishArtifacts(): boolean {
    return this.userPermissions.includes('artifact.app.publish');
  }

  loadArtifacts(selectId?: string | null): void {
    if (!this.canReadArtifacts()) {
      return;
    }
    this.isLoading = true;
    this.artifactAdminService.listApps().subscribe({
      next: (apps) => {
        this.artifacts = Array.isArray(apps) ? apps : [];
        this.isLoading = false;
        const targetId = selectId
          || this.selectedArtifactId
          || this.artifacts[0]?.id
          || null;
        if (targetId) {
          this.selectArtifact(targetId);
        } else {
          this.startNewArtifact();
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to load artifacts.');
      }
    });
  }

  selectArtifact(appId: string): void {
    this.selectedArtifactId = appId;
    this.isLoading = true;
    this.artifactAdminService.getApp(appId).subscribe({
      next: (app) => {
        this.isLoading = false;
        this.applyAppToForm(app);
        this.loadVersions(app.id);
      },
      error: (error) => {
        this.isLoading = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to load artifact details.');
      }
    });
  }

  startNewArtifact(): void {
    this.selectedArtifactId = null;
    this.versions = [];
    this.versionCodeInput = '';
    Object.assign(this.form, this.createEmptyForm());
  }

  togglePack(packKey: string, checked: boolean): void {
    const next = new Set(this.form.enabledPacks);
    if (checked) {
      next.add(packKey);
      if (packKey === 'inventory' || packKey === 'shipping') {
        next.add('order');
      }
    } else {
      next.delete(packKey);
      if (packKey === 'order') {
        next.delete('inventory');
        next.delete('shipping');
      }
    }
    this.form.enabledPacks = this.packOptions
      .map((option) => option.key)
      .filter((key) => next.has(key));
    this.syncConfigModules();
  }

  saveArtifact(): void {
    if (!this.canManageArtifacts()) {
      return;
    }
    const payload = this.buildPayload();
    if (!payload) {
      return;
    }
    this.isSaving = true;
    const request$ = this.form.id
      ? this.artifactAdminService.updateApp(this.form.id, payload)
      : this.artifactAdminService.createApp(payload);
    request$.subscribe({
      next: (artifact) => {
        this.isSaving = false;
        this.toastService.success(this.form.id ? 'Artifact updated.' : 'Artifact created.');
        this.loadArtifacts(artifact.id);
      },
      error: (error) => {
        this.isSaving = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to save artifact.');
      }
    });
  }

  createVersionSnapshot(): void {
    if (!this.form.id || !this.canManageArtifacts()) {
      return;
    }
    this.isSnapshotting = true;
    this.artifactAdminService.createVersion(this.form.id, this.versionCodeInput.trim() || null).subscribe({
      next: (version) => {
        this.isSnapshotting = false;
        this.versionCodeInput = version.versionCode;
        this.toastService.success(`Snapshot ${version.versionCode} created.`);
        this.loadVersions(this.form.id!);
        this.loadArtifacts(this.form.id);
      },
      error: (error) => {
        this.isSnapshotting = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to create artifact snapshot.');
      }
    });
  }

  publishArtifact(versionCode?: string): void {
    if (!this.form.id || !this.canPublishArtifacts()) {
      return;
    }
    this.isPublishing = true;
    this.artifactAdminService.publishApp(this.form.id, versionCode || this.versionCodeInput.trim() || null).subscribe({
      next: (artifact) => {
        this.isPublishing = false;
        this.toastService.success(`Artifact published${artifact.publishedVersion ? ` as ${artifact.publishedVersion}` : ''}.`);
        this.loadArtifacts(artifact.id);
      },
      error: (error) => {
        this.isPublishing = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to publish artifact.');
      }
    });
  }

  loadVersions(appId: string): void {
    this.isLoadingVersions = true;
    this.artifactAdminService.listVersions(appId).subscribe({
      next: (versions) => {
        this.versions = Array.isArray(versions) ? versions : [];
        this.isLoadingVersions = false;
      },
      error: (error) => {
        this.isLoadingVersions = false;
        this.toastService.error(error?.error?.errorMsg || 'Failed to load artifact versions.');
      }
    });
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return 'Never';
    }
    return new Date(value).toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(',', '');
  }

  private buildPayload(): ArtifactAppPayload | null {
    let parsedConfig: Record<string, any>;
    try {
      parsedConfig = JSON.parse(this.form.configJson || '{}');
    } catch {
      this.toastService.error('Configuration JSON is invalid.');
      return null;
    }
    return {
      code: this.form.code.trim(),
      name: this.form.name.trim(),
      description: this.form.description.trim() || null,
      status: this.form.status,
      generatorLanguage: this.form.generatorLanguage,
      buildTool: this.form.buildTool,
      enabledPacks: [...this.form.enabledPacks],
      config: parsedConfig
    };
  }

  private applyAppToForm(app: ArtifactApp): void {
    this.form.id = app.id;
    this.form.code = app.code || '';
    this.form.name = app.name || '';
    this.form.description = app.description || '';
    this.form.status = app.status || 'DRAFT';
    this.form.generatorLanguage = app.generatorLanguage || 'java';
    this.form.buildTool = app.buildTool || 'maven';
    this.form.enabledPacks = Array.isArray(app.enabledPacks) ? [...app.enabledPacks] : [];
    this.form.configJson = JSON.stringify(app.config || this.defaultConfig(this.form.enabledPacks), null, 2);
  }

  private syncConfigModules(): void {
    let config: Record<string, any>;
    try {
      config = JSON.parse(this.form.configJson || '{}');
    } catch {
      config = this.defaultConfig(this.form.enabledPacks);
    }
    const currentModules = config['modules'];
    const modules = typeof currentModules === 'object' && currentModules
      ? { ...currentModules }
      : {};
    for (const option of this.packOptions) {
      modules[option.key] = {
        ...(modules[option.key] || {}),
        enabled: this.form.enabledPacks.includes(option.key)
      };
    }
    config['modules'] = modules;
    this.form.configJson = JSON.stringify(config, null, 2);
  }

  private defaultConfig(enabledPacks: string[]): Record<string, any> {
    const modules = this.packOptions.reduce<Record<string, any>>((acc, option) => {
      acc[option.key] = { enabled: enabledPacks.includes(option.key) };
      return acc;
    }, {});
    return {
      foundation: {
        customer: true,
        product: true,
        address: true,
        warehouse: enabledPacks.includes('inventory') || enabledPacks.includes('shipping')
      },
      modules,
      workflows: {}
    };
  }

  private createEmptyForm(): ArtifactAppFormModel {
    const enabledPacks = ['crm', 'order'];
    return {
      id: null,
      code: '',
      name: '',
      description: '',
      status: 'DRAFT',
      generatorLanguage: 'java',
      buildTool: 'maven',
      enabledPacks,
      configJson: JSON.stringify(this.defaultConfig(enabledPacks), null, 2)
    };
  }
}
