import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { SearchableMultiSelectComponent } from '../../../../components/searchable-multi-select/searchable-multi-select.component';

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
  imports: [CommonModule, MatExpansionModule, SearchableMultiSelectComponent],
  templateUrl: './actuator-config.component.html',
  styleUrls: ['./actuator-config.component.css']
})
export class ActuatorConfigComponent implements OnChanges {
  @Input() endpointsByConfiguration: Record<string, string[]> = {};
  @Input() configurationOptions: string[] = ['default'];
  @Output() endpointsByConfigurationChange = new EventEmitter<Record<string, string[]>>();

  readonly endpointOptions: ActuatorEndpointOption[] = ACTUATOR_ENDPOINT_OPTIONS;
  readonly endpointOptionValues = this.endpointOptions.map((option) => option.value);
  resolvedEndpointsByConfiguration: Record<string, string[]> = {};
  availableEndpointOptionsByConfiguration: Record<string, string[]> = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['endpointsByConfiguration'] || changes['configurationOptions']) {
      this.syncResolvedEndpoints();
    }
  }

  trackByConfiguration(_: number, config: string): string {
    return config;
  }

  getProfileTitle(config: string): string {
    const normalized = String(config ?? '').trim().toLowerCase();
    if (normalized === 'default') {
      return 'default';
    }
    return config;
  }

  onEndpointsChange(configuration: string, endpoints: string[]): void {
    const key = String(configuration ?? '').trim().toLowerCase() || 'default';
    const allowedOptions = this.availableEndpointOptionsByConfiguration[key] ?? this.endpointOptionValues;
    const cleaned = Array.from(
      new Set(
        (Array.isArray(endpoints) ? endpoints : [])
          .map((item) => String(item ?? '').trim().toLowerCase())
          .filter(Boolean)
          .filter((item) => allowedOptions.includes(item))
      )
    );
    const nextConfigMap: Record<string, string[]> = {
      ...this.resolvedEndpointsByConfiguration,
      [key]: cleaned
    };

    const defaultSelected = new Set(nextConfigMap['default'] ?? []);
    Object.keys(nextConfigMap).forEach((profile) => {
      if (profile === 'default') {
        return;
      }
      nextConfigMap[profile] = (nextConfigMap[profile] ?? []).filter((item) => !defaultSelected.has(item));
    });

    this.resolvedEndpointsByConfiguration = nextConfigMap;
    this.updateAvailableOptions();
    this.endpointsByConfigurationChange.emit(nextConfigMap);
  }

  private syncResolvedEndpoints(): void {
    const normalizedOptions = (this.configurationOptions ?? [])
      .map((item) => String(item ?? '').trim().toLowerCase())
      .filter(Boolean);

    const uniqueOptions = Array.from(new Set(normalizedOptions));
    if (!uniqueOptions.includes('default')) {
      uniqueOptions.unshift('default');
    }

    const source = this.endpointsByConfiguration ?? {};
    const next: Record<string, string[]> = {};
    const sourceNormalized: Record<string, string[]> = {};
    uniqueOptions.forEach((config) => {
      const selected = source[config];
      sourceNormalized[config] = Array.isArray(selected)
        ? selected
            .map((item) => String(item ?? '').trim().toLowerCase())
            .filter((item) => item && this.endpointOptionValues.includes(item))
        : [];
    });

    next['default'] = sourceNormalized['default'].length
      ? sourceNormalized['default']
      : [...DEFAULT_ACTUATOR_ENDPOINTS];

    const defaultSelected = new Set(next['default']);
    uniqueOptions
      .filter((config) => config !== 'default')
      .forEach((config) => {
        next[config] = (sourceNormalized[config] ?? []).filter((item) => !defaultSelected.has(item));
      });

    this.resolvedEndpointsByConfiguration = next;
    this.updateAvailableOptions();

    if (!this.areMapsEqual(sourceNormalized, next, uniqueOptions)) {
      this.endpointsByConfigurationChange.emit(next);
    }
  }

  private updateAvailableOptions(): void {
    const defaultSelected = new Set(this.resolvedEndpointsByConfiguration['default'] ?? []);
    const optionsByProfile: Record<string, string[]> = {};
    Object.keys(this.resolvedEndpointsByConfiguration).forEach((config) => {
      if (config === 'default') {
        optionsByProfile[config] = [...this.endpointOptionValues];
        return;
      }
      optionsByProfile[config] = this.endpointOptionValues.filter((item) => !defaultSelected.has(item));
    });
    this.availableEndpointOptionsByConfiguration = optionsByProfile;
  }

  private areMapsEqual(
    left: Record<string, string[]>,
    right: Record<string, string[]>,
    keys: string[]
  ): boolean {
    return keys.every((key) => {
      const a = left[key] ?? [];
      const b = right[key] ?? [];
      if (a.length !== b.length) {
        return false;
      }
      return a.every((item, index) => item === b[index]);
    });
  }
}
