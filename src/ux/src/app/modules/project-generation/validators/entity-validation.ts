import { ValidationRule } from '../../../services/validator.service';
import { Field } from '../components/field-item/field-item.component';
import { areConstraintsValid } from '../constants/field-constraints';
import { findReservedJavaOrDatabaseKeyword, isValidJavaIdentifier } from './naming-validation';

export interface EntityNameValidationParams {
  entityName: string;
  existingEntities: Array<{ name: string }>;
  editEntityName?: string | null;
  fieldName?: string;
  label?: string;
  setError: (message: string) => void;
}

export interface FieldValidationParams {
  field: Field;
  duplicateField: boolean;
  hasOtherPrimaryKey: boolean;
  enforcePrimaryKey?: boolean;
  setError: (message: string) => void;
}

export interface FieldListRulesOptions {
  requirePrimaryKey?: boolean;
  itemLabel?: string;
}

export const buildEntityNameRules = (params: EntityNameValidationParams): ValidationRule[] => {
  const fieldName = params.fieldName ?? 'entityName';
  const label = params.label ?? 'Entity';
  const duplicateEntity = params.existingEntities.find((entity, index) => {
    const normalizedName = params.entityName.trim().toLowerCase();
    const entityName = entity.name.toLowerCase();
    if (entityName !== normalizedName) {
      return false;
    }
    if (params.editEntityName && params.editEntityName.trim().toLowerCase() === normalizedName) {
      return false;
    }
    return true;
  });
  const reservedKeyword = findReservedJavaOrDatabaseKeyword(params.entityName);

  return [
    {
      field: fieldName,
      setError: params.setError,
      constraints: [
        {
          type: 'required',
          message: `${label} name is required.`,
          messageType: 'error'
        },
        {
          type: 'custom',
          predicate: (value) => isValidJavaIdentifier(String(value ?? '')),
          message: `${label} name must be a valid Java identifier and not a Java keyword.`,
          messageType: 'error'
        },
        {
          type: 'custom',
          predicate: () => !reservedKeyword,
          message: `Your input contains the keyword "${reservedKeyword}", which cannot be used in java/database context.`,
          messageType: 'error'
        },
        {
          type: 'unique',
          value: Boolean(duplicateEntity),
          message: `${label} "${params.entityName}" already exists.`,
          messageType: 'warning'
        }
      ]
    }
  ];
};

export const buildFieldListRules = (options: FieldListRulesOptions = {}): ValidationRule[] => {
  const requirePrimaryKey = options.requirePrimaryKey ?? true;
  const itemLabel = options.itemLabel ?? 'field';

  const constraints: ValidationRule['constraints'] = [
    {
      type: 'custom',
      predicate: (value) => Array.isArray(value) && value.length > 0,
      message: `At least one ${itemLabel} is required.`,
      messageType: 'warning'
    }
  ];

  if (requirePrimaryKey) {
    constraints.push({
      type: 'custom',
      predicate: (value) =>
        Array.isArray(value) && value.some((field: Field) => Boolean(field?.primaryKey)),
      message: 'At least one primary key is required to save an entity.',
      messageType: 'warning'
    });
  }

  return [
    {
      field: 'fields',
      constraints
    }
  ];
};

export const buildFieldRules = (params: FieldValidationParams): ValidationRule[] => {
  const enforcePrimaryKey = params.enforcePrimaryKey ?? true;
  const constraints: ValidationRule['constraints'] = [
    {
      type: 'required',
      message: 'Field name is required.',
      messageType: 'error'
    },
    {
      type: 'custom',
      predicate: (value) => isValidJavaIdentifier(String(value ?? '')),
      message: 'Field name must be a valid Java identifier and not a Java keyword.',
      messageType: 'error'
    },
    {
      type: 'unique',
      value: params.duplicateField,
      message: `Field "${params.field.name}" already exists in this entity.`,
      messageType: 'warning'
    }
  ];

  if (enforcePrimaryKey) {
    constraints.push({
      type: 'custom',
      predicate: () => !(params.field.primaryKey && params.hasOtherPrimaryKey),
      message: 'Only one primary key is allowed per entity.',
      messageType: 'error'
    });
  }

  return [
    {
      field: 'name',
      setError: params.setError,
      constraints
    },
    {
      field: 'constraints',
      constraints: [
        {
          type: 'custom',
          predicate: (value) => areConstraintsValid(Array.isArray(value) ? value : []),
          message: 'Constraints contain invalid or missing values.',
          messageType: 'error'
        }
      ]
    }
  ];
};
