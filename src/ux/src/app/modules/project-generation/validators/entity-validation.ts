import { ValidationRule } from '../../../services/validator.service';
import { Field } from '../components/field-item/field-item.component';
import { areConstraintsValid } from '../constants/field-constraints';

const FIELD_NAME_PATTERN = /^[a-zA-Z0-9]+$/;

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
      type: 'pattern',
      regex: FIELD_NAME_PATTERN,
      message: 'Field name must be alphanumeric without spaces.',
      messageType: 'warning'
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
