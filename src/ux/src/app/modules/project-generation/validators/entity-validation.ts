import { ValidationRule } from '../../../services/validator.service';
import { Field } from '../components/field-item/field-item.component';

const FIELD_NAME_PATTERN = /^[a-zA-Z0-9]+$/;

export interface EntityNameValidationParams {
  entityName: string;
  existingEntities: Array<{ name: string }>;
  editEntityName?: string | null;
  setError: (message: string) => void;
}

export interface FieldValidationParams {
  field: Field;
  duplicateField: boolean;
  hasOtherPrimaryKey: boolean;
  setError: (message: string) => void;
}

export const buildEntityNameRules = (params: EntityNameValidationParams): ValidationRule[] => {
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
      field: 'entityName',
      setError: params.setError,
      constraints: [
        {
          type: 'required',
          message: 'Entity name is required.',
          messageType: 'error'
        },
        {
          type: 'unique',
          value: Boolean(duplicateEntity),
          message: `Entity "${params.entityName}" already exists.`,
          messageType: 'warning'
        }
      ]
    }
  ];
};

export const buildFieldListRules = (): ValidationRule[] => [
  {
    field: 'fields',
    constraints: [
      {
        type: 'custom',
        predicate: (value) => Array.isArray(value) && value.length > 0,
        message: 'At least one field is required to save an entity.',
        messageType: 'warning'
      }
    ]
  }
];

export const buildFieldRules = (params: FieldValidationParams): ValidationRule[] => [
  {
    field: 'name',
    setError: params.setError,
    constraints: [
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
      },
      {
        type: 'custom',
        predicate: () => !(params.field.primaryKey && params.hasOtherPrimaryKey),
        message: 'Only one primary key is allowed per entity.',
        messageType: 'error'
      }
    ]
  },
  {
    field: 'constraints',
    constraints: [
      {
        type: 'custom',
        predicate: (value) => {
          const constraints = Array.isArray(value) ? value : [];
          const invalidConstraint = constraints.find(
            constraint => !constraint?.name?.trim() || !constraint?.value?.trim()
          );
          return !invalidConstraint;
        },
        message: 'Constraint name and value are required.',
        messageType: 'error'
      }
    ]
  }
];
