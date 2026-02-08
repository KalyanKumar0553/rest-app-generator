export type ConstraintValueMode = 'none' | 'single' | 'double';
type ConstraintValidationMode = 'none' | 'first' | 'both' | 'atLeastOne';

export interface FieldConstraint {
  name: string;
  value?: string;
  value2?: string;
}

type ConstraintFieldGroup = 'all' | 'string' | 'number' | 'date' | 'boolean' | 'binary' | 'collection' | 'object';

export interface ConstraintInputConfig {
  label: string;
  placeholder: string;
}

export interface ConstraintDefinition {
  name: string;
  groups: ConstraintFieldGroup[];
  valueMode: ConstraintValueMode;
  validationMode?: ConstraintValidationMode;
  firstInput?: ConstraintInputConfig;
  secondInput?: ConstraintInputConfig;
}

const FIELD_GROUP_BY_TYPE: Record<string, ConstraintFieldGroup> = {
  String: 'string',
  Enum: 'string',
  UUID: 'string',
  Int: 'number',
  Long: 'number',
  Integer: 'number',
  Double: 'number',
  Float: 'number',
  Decimal: 'number',
  BigDecimal: 'number',
  Date: 'date',
  Time: 'date',
  DateTime: 'date',
  Instant: 'date',
  LocalDate: 'date',
  LocalDateTime: 'date',
  Boolean: 'boolean',
  Binary: 'binary',
  'byte[]': 'binary',
  Json: 'object'
};

const CONSTRAINT_DEFINITIONS: ConstraintDefinition[] = [
  { name: 'NotNull', groups: ['all'], valueMode: 'none' },
  { name: 'Null', groups: ['all'], valueMode: 'none' },
  { name: 'NotBlank', groups: ['string'], valueMode: 'none' },
  { name: 'NotEmpty', groups: ['string', 'binary', 'collection'], valueMode: 'none' },
  { name: 'Email', groups: ['string'], valueMode: 'none' },
  {
    name: 'Size',
    groups: ['string', 'binary', 'collection'],
    valueMode: 'double',
    validationMode: 'atLeastOne',
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
    validationMode: 'first',
    firstInput: { label: 'Value', placeholder: 'Enter decimal minimum value' },
    secondInput: { label: 'Inclusive', placeholder: 'Optional (true/false), default true' }
  },
  {
    name: 'DecimalMax',
    groups: ['number'],
    valueMode: 'double',
    validationMode: 'first',
    firstInput: { label: 'Value', placeholder: 'Enter decimal maximum value' },
    secondInput: { label: 'Inclusive', placeholder: 'Optional (true/false), default true' }
  },
  {
    name: 'Digits',
    groups: ['number'],
    valueMode: 'double',
    validationMode: 'both',
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
  { name: 'FutureOrPresent', groups: ['date'], valueMode: 'none' },
  { name: 'Positive', groups: ['number'], valueMode: 'none' },
  { name: 'PositiveOrZero', groups: ['number'], valueMode: 'none' },
  { name: 'Negative', groups: ['number'], valueMode: 'none' },
  { name: 'NegativeOrZero', groups: ['number'], valueMode: 'none' },
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
  const fieldGroup = resolveFieldGroup(fieldType);
  if (!fieldGroup) {
    return SORTED_CONSTRAINT_DEFINITIONS.map(definition => definition.name);
  }

  return SORTED_CONSTRAINT_DEFINITIONS
    .filter(definition => definition.groups.includes('all') || definition.groups.includes(fieldGroup))
    .map(definition => definition.name);
};

const resolveFieldGroup = (fieldType: string): ConstraintFieldGroup | undefined => {
  const normalized = (fieldType ?? '').trim();
  if (!normalized) {
    return undefined;
  }
  if (/^List\s*<.+>$/.test(normalized) || normalized.endsWith('[]')) {
    return 'collection';
  }
  return FIELD_GROUP_BY_TYPE[normalized];
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
  const definition = getConstraintDefinition(constraint?.name);
  const validationMode = definition?.validationMode ?? (
    definition?.valueMode === 'double' ? 'both' : definition?.valueMode === 'single' ? 'first' : 'none'
  );
  if (validationMode === 'none') {
    return true;
  }

  const first = constraint?.value?.trim();
  const second = constraint?.value2?.trim();

  if (validationMode === 'first') {
    return Boolean(first);
  }

  if (validationMode === 'both') {
    return Boolean(first) && Boolean(second);
  }

  if (validationMode === 'atLeastOne') {
    return Boolean(first) || Boolean(second);
  }

  if (!first) {
    return false;
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

  const formatError = items
    .map(getConstraintFormatError)
    .find(Boolean);
  if (formatError) {
    return formatError;
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

const isInteger = (value: string | undefined): boolean => /^-?\d+$/.test((value ?? '').trim());
const isNumber = (value: string | undefined): boolean => /^-?\d+(\.\d+)?$/.test((value ?? '').trim());
const isBoolean = (value: string | undefined): boolean => /^(true|false)$/i.test((value ?? '').trim());

export const getConstraintFormatError = (constraint: FieldConstraint | undefined | null): string | null => {
  if (!constraint?.name?.trim()) {
    return null;
  }

  const name = constraint.name.trim();
  const value = constraint.value?.trim();
  const value2 = constraint.value2?.trim();

  if (name === 'Size') {
    if (value && !isInteger(value)) {
      return 'Size min must be an integer.';
    }
    if (value2 && !isInteger(value2)) {
      return 'Size max must be an integer.';
    }
  }

  if (name === 'Min' || name === 'Max') {
    if (value && !isNumber(value)) {
      return `${name} value must be numeric.`;
    }
  }

  if (name === 'DecimalMin' || name === 'DecimalMax') {
    if (value && !isNumber(value)) {
      return `${name} value must be numeric.`;
    }
    if (value2 && !isBoolean(value2)) {
      return `${name} inclusive must be true or false.`;
    }
  }

  if (name === 'Digits') {
    if (value && !isInteger(value)) {
      return 'Digits integer must be an integer.';
    }
    if (value2 && !isInteger(value2)) {
      return 'Digits fraction must be an integer.';
    }
  }

  return null;
};
