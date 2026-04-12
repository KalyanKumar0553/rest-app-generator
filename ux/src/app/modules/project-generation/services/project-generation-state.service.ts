import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { ACTUATOR_ENDPOINT_OPTIONS, DEFAULT_ACTUATOR_ENDPOINTS } from '../components/actuator-config/actuator-config.component';

export interface ActuatorState {
  configurationOptions: string[];
  endpointsByConfiguration: Record<string, string[]>;
}

@Injectable({ providedIn: 'root' })
export class ProjectGenerationStateService {
  private readonly actuatorStateSubject = new BehaviorSubject<ActuatorState>({
    configurationOptions: ['default'],
    endpointsByConfiguration: { default: [...DEFAULT_ACTUATOR_ENDPOINTS] }
  });

  readonly actuatorState$ = this.actuatorStateSubject.asObservable().pipe(
    distinctUntilChanged((prev, curr) => this.areActuatorStatesEqual(prev, curr))
  );

  readonly actuatorConfigurationOptions$ = this.actuatorState$.pipe(
    map(state => state.configurationOptions)
  );

  readonly actuatorEndpointsByConfiguration$ = this.actuatorState$.pipe(
    map(state => state.endpointsByConfiguration)
  );

  setProfiles(profiles: string[]): void {
    const normalizedProfiles = Array.from(
      new Set(
        (profiles ?? [])
          .map(item => String(item ?? '').trim().toLowerCase())
          .filter(Boolean)
          .filter(item => item !== 'default')
      )
    );
    const configurationOptions = ['default', ...normalizedProfiles];
    const current = this.actuatorStateSubject.value;
    const sanitized = this.sanitizeConfigurations(current.endpointsByConfiguration, configurationOptions);
    this.nextIfChanged({
      configurationOptions,
      endpointsByConfiguration: sanitized
    });
  }

  setActuatorConfigurations(configurations: Record<string, string[]>): void {
    const current = this.actuatorStateSubject.value;
    const sanitized = this.sanitizeConfigurations(configurations, current.configurationOptions);
    this.nextIfChanged({
      ...current,
      endpointsByConfiguration: sanitized
    });
  }

  getActuatorStateSnapshot(): ActuatorState {
    return this.actuatorStateSubject.value;
  }

  private nextIfChanged(next: ActuatorState): void {
    const current = this.actuatorStateSubject.value;
    if (!this.areActuatorStatesEqual(current, next)) {
      this.actuatorStateSubject.next(next);
    }
  }

  private sanitizeConfigurations(
    rawConfigurations: Record<string, string[]> | unknown,
    configurationOptions: string[]
  ): Record<string, string[]> {
    const allowedConfigurations = new Set(configurationOptions);
    const result: Record<string, string[]> = {
      default: [...DEFAULT_ACTUATOR_ENDPOINTS]
    };

    if (!rawConfigurations || typeof rawConfigurations !== 'object') {
      return result;
    }

    Object.entries(rawConfigurations as Record<string, unknown>).forEach(([rawKey, rawValue]) => {
      const key = String(rawKey ?? '').trim().toLowerCase();
      if (!key || !allowedConfigurations.has(key)) {
        return;
      }
      result[key] = this.sanitizeEndpoints(rawValue);
    });

    configurationOptions.forEach((config) => {
      if (!result[config]?.length) {
        result[config] = [...DEFAULT_ACTUATOR_ENDPOINTS];
      }
    });

    return result;
  }

  private sanitizeEndpoints(rawEndpoints: unknown): string[] {
    const allowed = new Set(ACTUATOR_ENDPOINT_OPTIONS.map(option => option.value));
    if (!Array.isArray(rawEndpoints)) {
      return [...DEFAULT_ACTUATOR_ENDPOINTS];
    }
    const unique = Array.from(
      new Set(
        rawEndpoints
          .map(item => String(item ?? '').trim().toLowerCase())
          .filter(item => item && allowed.has(item))
      )
    );
    return unique.length ? unique : [...DEFAULT_ACTUATOR_ENDPOINTS];
  }

  private areActuatorStatesEqual(a: ActuatorState, b: ActuatorState): boolean {
    if (a.configurationOptions.length !== b.configurationOptions.length) {
      return false;
    }
    for (let index = 0; index < a.configurationOptions.length; index += 1) {
      if (a.configurationOptions[index] !== b.configurationOptions[index]) {
        return false;
      }
    }

    const keysA = Object.keys(a.endpointsByConfiguration).sort();
    const keysB = Object.keys(b.endpointsByConfiguration).sort();
    if (keysA.length !== keysB.length) {
      return false;
    }
    for (let index = 0; index < keysA.length; index += 1) {
      if (keysA[index] !== keysB[index]) {
        return false;
      }
      const valuesA = a.endpointsByConfiguration[keysA[index]] ?? [];
      const valuesB = b.endpointsByConfiguration[keysB[index]] ?? [];
      if (valuesA.length !== valuesB.length) {
        return false;
      }
      for (let valueIndex = 0; valueIndex < valuesA.length; valueIndex += 1) {
        if (valuesA[valueIndex] !== valuesB[valueIndex]) {
          return false;
        }
      }
    }

    return true;
  }
}
