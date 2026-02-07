export type ConstraintValueMode = 'none' | 'single' | 'double';

export interface FieldConstraint {
  name: string;
  value?: string;
  value2?: string;
}

type ConstraintFieldGroup = 'all' | 'string' | 'number' | 'date' | 'boolean' | 'binary';

export interface ConstraintInputConfig {
  label: string;
  placeholder: string;
}

export interface ConstraintDefinition {
  name: string;
  groups: ConstraintFieldGroup[];
  valueMode: ConstraintValueMode;
  firstInput?: ConstraintInputConfig;
  secondInput?: ConstraintInputConfig;
}

const FIELD_GROUP_BY_TYPE: Record<string, ConstraintFieldGroup> = {
  String: 'string',
  Enum: 'string',
  UUID: 'string',
  Long: 'number',
  Integer: 'number',
  Double: 'number',
  Float: 'number',
  BigDecimal: 'number',
  LocalDate: 'date',
  LocalDateTime: 'date',
  Boolean: 'boolean',
  'byte[]': 'binary'
};

const CONSTRAINT_DEFINITIONS: ConstraintDefinition[] = [
  { name: 'NotNull', groups: ['all'], valueMode: 'none' },
  { name: 'NotBlank', groups: ['string'], valueMode: 'none' },
  { name: 'Email', groups: ['string'], valueMode: 'none' },
  {
    name: 'Size',
    groups: ['string', 'binary'],
    valueMode: 'double',
    firstInput: { label: 'Min', placeholder: 'Enter minimum size' },
    secondInput: { label: 'Max', placeholder: 'Enter maximum size' }
  },
  {
    name: 'Min',
    groups: ['number'],
    valueMode: 'single',
    firstInput: { label: 'Value', placeholder: 'Enter minimum value' }
  },
  {
    name: 'Max',
    groups: ['number'],
    valueMode: 'single',
    firstInput: { label: 'Value', placeholder: 'Enter maximum value' }
  },
  {
    name: 'DecimalMin',
    groups: ['number'],
    valueMode: 'double',
    firstInput: { label: 'Value', placeholder: 'Enter decimal minimum value' },
    secondInput: { label: 'Inclusive', placeholder: 'Enter true or false' }
  },
  {
    name: 'Digits',
    groups: ['number'],
    valueMode: 'double',
    firstInput: { label: 'Integer', placeholder: 'Enter integer digits' },
    secondInput: { label: 'Fraction', placeholder: 'Enter fraction digits' }
  },
  {
    name: 'Pattern',
    groups: ['string'],
    valueMode: 'single',
    firstInput: { label: 'Regex', placeholder: 'Enter regex pattern' }
  },
  { name: 'Past', groups: ['date'], valueMode: 'none' },
  { name: 'PastOrPresent', groups: ['date'], valueMode: 'none' },
  { name: 'Future', groups: ['date'], valueMode: 'none' },
  { name: 'Positive', groups: ['number'], valueMode: 'none' },
  { name: 'AssertTrue', groups: ['boolean'], valueMode: 'none' },
  { name: 'AssertFalse', groups: ['boolean'], valueMode: 'none' },
  { name: 'Valid', groups: ['all'], valueMode: 'none' }
];

const SORTED_CONSTRAINT_DEFINITIONS = [...CONSTRAINT_DEFINITIONS].sort((a, b) =>
  a.name.localeCompare(b.name)
);

export const getConstraintDefinition = (name: string | undefined | null): ConstraintDefinition | null => {
  if (!name?.trim()) {
    return null;
  }
  return CONSTRAINT_DEFINITIONS.find(definition => definition.name === name.trim()) ?? null;
};

export const getConstraintOptionsForFieldType = (fieldType: string): string[] => {
  const fieldGroup = FIELD_GROUP_BY_TYPE[fieldType];
  if (!fieldGroup) {
    return SORTED_CONSTRAINT_DEFINITIONS.map(definition => definition.name);
  }

  return SORTED_CONSTRAINT_DEFINITIONS
    .filter(definition => definition.groups.includes('all') || definition.groups.includes(fieldGroup))
    .map(definition => definition.name);
};

export const getConstraintValueMode = (name: string | undefined | null): ConstraintValueMode => {
  const definition = getConstraintDefinition(name);
  return definition?.valueMode ?? 'none';
};

export const getConstraintInputConfig = (
  name: string | undefined | null,
  inputIndex: 1 | 2
): ConstraintInputConfig | null => {
  const definition = getConstraintDefinition(name);
  if (!definition) {
    return inputIndex === 1
      ? { label: 'Value', placeholder: 'Enter value' }
      : null;
  }
  return inputIndex === 1
    ? definition.firstInput ?? { label: 'Value', placeholder: 'Enter value' }
    : definition.secondInput ?? null;
};

export const hasRequiredConstraintValues = (constraint: FieldConstraint): boolean => {
  const mode = getConstraintValueMode(constraint?.name);
  if (mode === 'none') {
    return true;
  }

  const first = constraint?.value?.trim();
  if (!first) {
    return false;
  }

  if (mode === 'double') {
    return Boolean(constraint?.value2?.trim());
  }

  return true;
};

export const getConstraintValidationError = (constraints: FieldConstraint[] | undefined | null): string | null => {
  const items = Array.isArray(constraints) ? constraints : [];

  const invalidName = items.find(constraint => !constraint?.name?.trim());
  if (invalidName) {
    return 'Constraint name is required.';
  }

  const names = items
    .map(constraint => constraint?.name?.trim())
    .filter(Boolean) as string[];
  if (new Set(names).size !== names.length) {
    return 'Duplicate constraints are not allowed for the same field.';
  }

  const missingValueConstraint = items.find(constraint => !hasRequiredConstraintValues(constraint));
  if (missingValueConstraint?.name) {
    return `Value is required for ${missingValueConstraint.name}.`;
  }

  return null;
};

export const areConstraintsValid = (constraints: FieldConstraint[] | undefined | null): boolean =>
  getConstraintValidationError(constraints) === null;

export const normalizeConstraintValuesForMode = (constraint: FieldConstraint): void => {
  const mode = getConstraintValueMode(constraint?.name);
  if (mode === 'none') {
    constraint.value = '';
    constraint.value2 = '';
    return;
  }

  if (mode === 'single') {
    constraint.value2 = '';
    return;
  }

  if (!constraint.value2?.trim()) {
    constraint.value2 = '';
  }
};
