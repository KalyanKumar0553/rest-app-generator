import { Injectable } from '@angular/core';
import { ToastService } from './toast.service';

export type ToastLevel = 'error' | 'warning' | 'info' | 'success';

export interface ValidationConstraint {
  type: 'required' | 'pattern' | 'unique' | 'custom';
  regex?: RegExp;
  value?: boolean;
  predicate?: (value: any, form: any) => boolean;
  message?: string;
  messageType?: ToastLevel;
}

export interface ValidationRule {
  field: string;
  message?: string;
  messageType?: ToastLevel;
  constraints: ValidationConstraint[];
  setError?: (message: string) => void;
}

export interface ValidationOptions {
  stopOnFirst?: boolean;
  silent?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ValidatorService {
  constructor(private toastService: ToastService) {}

  validate(form: any, rules: ValidationRule[], options: ValidationOptions = {}): boolean {
    const stopOnFirst = options.stopOnFirst !== false;
    const silent = options.silent === true;
    let isValid = true;

    for (const rule of rules) {
      const value = form?.[rule.field];
      for (const constraint of rule.constraints) {
        if (this.checkConstraint(value, constraint, form)) {
          continue;
        }

        const message = constraint.message ?? rule.message ?? `${rule.field} is invalid.`;
        if (rule.setError) {
          rule.setError(message);
        }
        if (!silent) {
          this.notify(constraint.messageType ?? rule.messageType, message);
        }
        isValid = false;
        if (stopOnFirst) {
          return false;
        }
      }
    }

    return isValid;
  }

  private checkConstraint(value: any, constraint: ValidationConstraint, form: any): boolean {
    switch (constraint.type) {
      case 'required':
        return typeof value === 'string' ? value.trim().length > 0 : Boolean(value);
      case 'pattern':
        if (!value || (typeof value === 'string' && value.trim().length === 0)) {
          return true;
        }
        return constraint.regex ? constraint.regex.test(value) : true;
      case 'unique':
        return !constraint.value;
      case 'custom':
        return constraint.predicate ? constraint.predicate(value, form) : true;
      default:
        return true;
    }
  }

  private notify(type: ToastLevel | undefined, message: string): void {
    if (!type) {
      return;
    }
    switch (type) {
      case 'success':
        this.toastService.success(message);
        break;
      case 'warning':
        this.toastService.warning(message);
        break;
      case 'info':
        this.toastService.info(message);
        break;
      default:
        this.toastService.error(message);
        break;
    }
  }
}
