import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';

type SwaggerConfigDraft = {
  title: string;
  description: string;
  version: string;
  docsPath: string;
  openApiPath: string;
  enableUi: boolean;
};

@Component({
  selector: 'app-swagger-module-tab',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, ShippedModuleConfigPanelComponent],
  templateUrl: './swagger-module-tab.component.html',
  styleUrls: ['./swagger-module-tab.component.css']
})
export class SwaggerModuleTabComponent implements OnInit {
  config: SwaggerConfigDraft = this.buildDefaultConfig();

  constructor(private readonly projectGenerationState: ProjectGenerationStateService) {}

  ngOnInit(): void {
    const savedConfig = this.projectGenerationState.getModuleConfigsSnapshot()['swagger'];
    this.config = this.normalizeConfig(savedConfig);
  }

  onConfigChange(): void {
    this.projectGenerationState.updateModuleConfig('swagger', {
      title: this.config.title.trim() || 'Generated API',
      description: this.config.description.trim() || 'Generated API documentation.',
      version: this.config.version.trim() || '1.0.0',
      docsPath: this.normalizePath(this.config.docsPath, '/swagger'),
      openApiPath: this.normalizePath(this.config.openApiPath, '/openapi.json'),
      enableUi: this.config.enableUi
    });
  }

  private normalizeConfig(rawConfig: Record<string, any> | undefined): SwaggerConfigDraft {
    const defaults = this.buildDefaultConfig();
    const normalized: SwaggerConfigDraft = {
      title: String(rawConfig?.['title'] ?? defaults.title).trim() || defaults.title,
      description: String(rawConfig?.['description'] ?? defaults.description).trim() || defaults.description,
      version: String(rawConfig?.['version'] ?? defaults.version).trim() || defaults.version,
      docsPath: this.normalizePath(rawConfig?.['docsPath'], defaults.docsPath),
      openApiPath: this.normalizePath(rawConfig?.['openApiPath'], defaults.openApiPath),
      enableUi: typeof rawConfig?.['enableUi'] === 'boolean' ? rawConfig['enableUi'] : defaults.enableUi
    };
    this.projectGenerationState.updateModuleConfig('swagger', normalized);
    return normalized;
  }

  private buildDefaultConfig(): SwaggerConfigDraft {
    return {
      title: 'Generated API',
      description: 'Generated API documentation.',
      version: '1.0.0',
      docsPath: '/swagger',
      openApiPath: '/openapi.json',
      enableUi: true
    };
  }

  private normalizePath(rawValue: unknown, fallback: string): string {
    const trimmed = String(rawValue ?? '').trim();
    if (!trimmed) {
      return fallback;
    }
    return trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
  }
}
