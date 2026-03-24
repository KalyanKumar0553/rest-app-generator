import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
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

@Component({
  selector: 'app-modules-selection',
  standalone: true,
  imports: [CommonModule, MatCheckboxModule, MatIconModule, MatTooltipModule, SearchSortComponent],
  templateUrl: './modules-selection.component.html',
  styleUrls: ['./modules-selection.component.css']
})
export class ModulesSelectionComponent implements OnChanges {
  @Input() modules: ShippableModuleCard[] = [];
  @Input() selectedModuleKeys: string[] = [];
  @Output() selectedModuleKeysChange = new EventEmitter<string[]>();

  filteredModules: ShippableModuleCard[] = [];
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
    if (changes['modules']) {
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

  private applySearchAndSort(): void {
    let next = Array.isArray(this.modules) ? [...this.modules] : [];

    if (this.currentSearchTerm.trim()) {
      const query = this.currentSearchTerm.trim().toLowerCase();
      next = next.filter((module) =>
        [module.title, module.description, module.key]
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
    }

    this.filteredModules = next;
  }
}
