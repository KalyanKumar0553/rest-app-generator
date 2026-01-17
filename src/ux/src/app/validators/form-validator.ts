export interface ValidationRule {
  required?: boolean;
  email?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  custom?: (value: any) => boolean;
  message?: string;
}

export interface ValidationRules {
  [key: string]: ValidationRule[];
}

export interface ValidationErrors {
  [key: string]: string;
}

export class FormValidator {
  static validate(formData: any, rules: ValidationRules): ValidationErrors {
    const errors: ValidationErrors = {};

    Object.keys(rules).forEach(field => {
      const value = formData[field];
      const fieldRules = rules[field];

      for (const rule of fieldRules) {
        const error = this.validateField(value, rule, field);
        if (error) {
          errors[field] = error;
          break;
        }
      }
    });

    return errors;
  }

  static validateField(value: any, rule: ValidationRule, fieldName: string): string | null {
    if (rule.required && this.isEmpty(value)) {
      return rule.message || `${this.formatFieldName(fieldName)} is required`;
    }

    if (!this.isEmpty(value)) {
      if (rule.email && !this.isValidEmail(value)) {
        return rule.message || 'Please enter a valid email address';
      }

      if (rule.minLength && value.length < rule.minLength) {
        return rule.message || `${this.formatFieldName(fieldName)} must be at least ${rule.minLength} characters`;
      }

      if (rule.maxLength && value.length > rule.maxLength) {
        return rule.message || `${this.formatFieldName(fieldName)} must not exceed ${rule.maxLength} characters`;
      }

      if (rule.pattern && !rule.pattern.test(value)) {
        return rule.message || `${this.formatFieldName(fieldName)} format is invalid`;
      }

      if (rule.custom && !rule.custom(value)) {
        return rule.message || `${this.formatFieldName(fieldName)} is invalid`;
      }
    }

    return null;
  }

  static validateSingleField(value: any, rules: ValidationRule[], fieldName: string): string | null {
    for (const rule of rules) {
      const error = this.validateField(value, rule, fieldName);
      if (error) {
        return error;
      }
    }
    return null;
  }

  static isEmpty(value: any): boolean {
    return value === null || value === undefined || value === '' ||
           (typeof value === 'string' && value.trim() === '');
  }

  static isValidEmail(email: string): boolean {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailPattern.test(email);
  }

  static formatFieldName(fieldName: string): string {
    return fieldName
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  static hasErrors(errors: ValidationErrors): boolean {
    return Object.keys(errors).length > 0;
  }

  static clearErrors(errors: ValidationErrors, field?: string): ValidationErrors {
    if (field) {
      const newErrors = { ...errors };
      delete newErrors[field];
      return newErrors;
    }
    return {};
  }
}

export const CommonValidationRules = {
  email: [
    { required: true, message: 'Email is required' },
    { email: true, message: 'Please enter a valid email address' }
  ],
  password: [
    { required: true, message: 'Password is required' },
    { minLength: 6, message: 'Password must be at least 6 characters' }
  ],
  confirmPassword: (password: string) => [
    { required: true, message: 'Please confirm your password' },
    {
      custom: (value: string) => value === password,
      message: 'Passwords do not match'
    }
  ],
  otp: [
    { required: true, message: 'OTP is required' },
    {
      pattern: /^\d{6}$/,
      message: 'OTP must be 6 digits'
    }
  ]
};
