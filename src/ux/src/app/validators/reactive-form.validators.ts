import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = (control.value || '').trim();
    if (!value) {
      return null;
    }
    return value.length >= 8 ? null : { passwordStrength: true };
  };
}

export function matchingFieldsValidator(sourceKey: string, targetKey: string, errorKey: string = 'fieldsMismatch'): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const source = group.get(sourceKey);
    const target = group.get(targetKey);
    if (!source || !target) {
      return null;
    }
    const mismatch = !!source.value && !!target.value && source.value !== target.value;
    const currentErrors = target.errors || {};
    if (mismatch) {
      target.setErrors({ ...currentErrors, [errorKey]: true });
      return { [errorKey]: true };
    }
    if (currentErrors[errorKey]) {
      const { [errorKey]: _, ...rest } = currentErrors;
      target.setErrors(Object.keys(rest).length ? rest : null);
    }
    return null;
  };
}
