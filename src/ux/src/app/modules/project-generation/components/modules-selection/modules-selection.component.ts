import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  SearchConfig,
  SearchSortComponent,
  SearchSortEvent,
  SortOption
} from '../../../../components/search-sort/search-sort.component';

export interface ShippableModuleCard {
  key: string;
  title: string;
  description: string;
}

export interface PluginModuleSelection {
  pluginId: string;
  versionId: string;
  code: string;
  name: string;
  versionCode: string;
}

export interface PluginModuleCard {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  category?: string | null;
  enabled: boolean;
  currentPublishedVersionId?: string | null;
  versions: Array<{
    id: string;
    versionCode: string;
    published: boolean;
  }>;
}

@Component({
  selector: 'app-modules-selection',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCheckboxModule, MatIconModule, MatTooltipModule, SearchSortComponent],
  templateUrl: './modules-selection.component.html',
  styleUrls: ['./modules-selection.component.css']
})
export class ModulesSelectionComponent implements OnChanges {
  @Input() modules: ShippableModuleCard[] = [];
  @Input() selectedModuleKeys: string[] = [];
  @Input() pluginModules: PluginModuleCard[] = [];
  @Input() selectedPlugins: PluginModuleSelection[] = [];
  @Output() selectedModuleKeysChange = new EventEmitter<string[]>();
  @Output() selectedPluginsChange = new EventEmitter<PluginModuleSelection[]>();

  filteredModules: ShippableModuleCard[] = [];
  filteredPluginModules: PluginModuleCard[] = [];
  private currentSearchTerm = '';
  private currentSortOption: SortOption | null = null;

  readonly modulesHelpText = 'Choose the shipped platform modules to copy into the generated project and expose for follow-up configuration.';
  readonly searchConfig: SearchConfig = {
    placeholder: 'Search modules by name or description...',
    properties: ['title', 'description', 'key']
  };
  readonly sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'title', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'title', direction: 'desc' }
  ];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['modules'] || changes['pluginModules']) {
      this.applySearchAndSort();
    }
  }

  isSelected(moduleKey: string): boolean {
    return this.selectedModuleKeys.includes(moduleKey);
  }

  onSearchSortChange(event: SearchSortEvent): void {
    this.currentSearchTerm = event.searchTerm || '';
    this.currentSortOption = event.sortOption || null;
    this.applySearchAndSort();
  }

  toggleModule(moduleKey: string, checked: boolean): void {
    const next = new Set(this.selectedModuleKeys);
    if (checked) {
      next.add(moduleKey);
    } else {
      next.delete(moduleKey);
    }
    this.selectedModuleKeysChange.emit(Array.from(next));
  }

  isPluginSelected(pluginId: string): boolean {
    return this.selectedPlugins.some((item) => item.pluginId === pluginId);
  }

  getPluginSelectedVersionId(plugin: PluginModuleCard): string {
    return this.selectedPlugins.find((item) => item.pluginId === plugin.id)?.versionId
      || plugin.currentPublishedVersionId
      || plugin.versions[0]?.id
      || '';
  }

  togglePlugin(plugin: PluginModuleCard, checked: boolean): void {
    const next = this.selectedPlugins.filter((item) => item.pluginId !== plugin.id);
    if (checked) {
      const selectedVersionId = this.getPluginSelectedVersionId(plugin);
      const version = plugin.versions.find((item) => item.id === selectedVersionId) || plugin.versions[0];
      if (version) {
        next.push({
          pluginId: plugin.id,
          versionId: version.id,
          code: plugin.code,
          name: plugin.name,
          versionCode: version.versionCode
        });
      }
    }
    this.selectedPluginsChange.emit(next);
  }

  onPluginVersionChange(plugin: PluginModuleCard, versionId: string): void {
    const version = plugin.versions.find((item) => item.id === versionId);
    if (!version) {
      return;
    }
    const next = this.selectedPlugins.filter((item) => item.pluginId !== plugin.id);
    next.push({
      pluginId: plugin.id,
      versionId: version.id,
      code: plugin.code,
      name: plugin.name,
      versionCode: version.versionCode
    });
    this.selectedPluginsChange.emit(next);
  }

  private applySearchAndSort(): void {
    let next = Array.isArray(this.modules) ? [...this.modules] : [];
    let nextPlugins = Array.isArray(this.pluginModules) ? [...this.pluginModules] : [];

    if (this.currentSearchTerm.trim()) {
      const query = this.currentSearchTerm.trim().toLowerCase();
      next = next.filter((module) =>
        [module.title, module.description, module.key]
          .some((value) => String(value ?? '').toLowerCase().includes(query))
      );
      nextPlugins = nextPlugins.filter((plugin) =>
        [plugin.name, plugin.description, plugin.code, plugin.category]
          .some((value) => String(value ?? '').toLowerCase().includes(query))
      );
    }

    if (this.currentSortOption) {
      const { direction } = this.currentSortOption;
      next.sort((left, right) => {
        const leftValue = left.title.toLowerCase();
        const rightValue = right.title.toLowerCase();
        const result = leftValue.localeCompare(rightValue);
        return direction === 'asc' ? result : -result;
      });
      nextPlugins.sort((left, right) => {
        const leftValue = left.name.toLowerCase();
        const rightValue = right.name.toLowerCase();
        const result = leftValue.localeCompare(rightValue);
        return direction === 'asc' ? result : -result;
      });
    }

    this.filteredModules = next;
    this.filteredPluginModules = nextPlugins;
  }
}
