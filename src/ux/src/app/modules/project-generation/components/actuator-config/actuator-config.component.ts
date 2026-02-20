import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import {
  SearchConfig,
  SearchSortComponent,
  SearchSortEvent,
  SortOption
} from '../../../../components/search-sort/search-sort.component';

export interface ActuatorEndpointOption {
  value: string;
  label: string;
}

export const ACTUATOR_ENDPOINT_OPTIONS: ActuatorEndpointOption[] = [
  { value: 'health', label: 'Health' },
  { value: 'shutdown', label: 'Shutdown' },
  { value: 'metrics', label: 'Metrics' },
  { value: 'info', label: 'Info' },
  { value: 'env', label: 'Environment' },
  { value: 'beans', label: 'Beans' },
  { value: 'mappings', label: 'Mappings' },
  { value: 'loggers', label: 'Loggers' },
  { value: 'threaddump', label: 'Thread Dump' },
  { value: 'heapdump', label: 'Heap Dump' },
  { value: 'prometheus', label: 'Prometheus' },
  { value: 'conditions', label: 'Conditions' },
  { value: 'configprops', label: 'Config Props' },
  { value: 'caches', label: 'Caches' },
  { value: 'scheduledtasks', label: 'Scheduled Tasks' }
];

export const DEFAULT_ACTUATOR_ENDPOINTS = ['health', 'metrics', 'info'];

@Component({
  selector: 'app-actuator-config',
  standalone: true,
  imports: [CommonModule, MatExpansionModule, MatCheckboxModule, SearchSortComponent],
  templateUrl: './actuator-config.component.html',
  styleUrls: ['./actuator-config.component.css']
})
export class ActuatorConfigComponent {
  @Input() selectedEndpoints: string[] = [];
  @Output() selectedEndpointsChange = new EventEmitter<string[]>();

  readonly endpointOptions: ActuatorEndpointOption[] = ACTUATOR_ENDPOINT_OPTIONS;
  readonly searchConfig: SearchConfig = {
    placeholder: 'Search actuator endpoints...',
    properties: ['label']
  };
  readonly sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'label', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'label', direction: 'desc' },
    { label: 'Selected first', property: 'selected', direction: 'asc' },
    { label: 'Unselected first', property: 'selected', direction: 'desc' }
  ];

  showFilters = false;
  searchTerm = '';
  sortOption: SortOption | null = null;

  get endpointRows(): ActuatorEndpointOption[][] {
    const visibleOptions = this.filteredEndpointOptions();
    const rows: ActuatorEndpointOption[][] = [];
    for (let index = 0; index < visibleOptions.length; index += 2) {
      rows.push(visibleOptions.slice(index, index + 2));
    }
    return rows;
  }

  get selectedCount(): number {
    return this.selectedEndpoints.length;
  }

  get allSelected(): boolean {
    return this.selectedEndpoints.length === this.endpointOptions.length;
  }

  toggleAll(checked: boolean): void {
    const next = checked ? this.endpointOptions.map((option) => option.value) : [];
    this.selectedEndpointsChange.emit(next);
  }

  toggleEndpoint(endpoint: string, checked: boolean): void {
    const normalized = String(endpoint || '').trim().toLowerCase();
    if (!normalized) {
      return;
    }
    const next = checked
      ? Array.from(new Set([...this.selectedEndpoints, normalized]))
      : this.selectedEndpoints.filter((item) => item !== normalized);
    this.selectedEndpointsChange.emit(next);
  }

  isSelected(endpoint: string): boolean {
    return this.selectedEndpoints.includes(String(endpoint || '').trim().toLowerCase());
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
    if (!this.showFilters) {
      this.searchTerm = '';
      this.sortOption = null;
    }
  }

  onSearchSortChange(event: SearchSortEvent): void {
    this.searchTerm = String(event?.searchTerm ?? '').trim().toLowerCase();
    this.sortOption = event?.sortOption ?? null;
  }

  private filteredEndpointOptions(): ActuatorEndpointOption[] {
    let items = [...this.endpointOptions];

    if (this.searchTerm) {
      items = items.filter((item) => item.label.toLowerCase().includes(this.searchTerm));
    }

    if (!this.sortOption) {
      return items;
    }

    const direction = this.sortOption.direction === 'asc' ? 1 : -1;
    const property = this.sortOption.property;
    return items.sort((left, right) => {
      if (property === 'selected') {
        const leftValue = this.isSelected(left.value) ? 1 : 0;
        const rightValue = this.isSelected(right.value) ? 1 : 0;
        return (rightValue - leftValue) * direction;
      }
      return left.label.localeCompare(right.label) * direction;
    });
  }
}
