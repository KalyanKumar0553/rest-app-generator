export const VALIDATION_MESSAGES = {
  projectGroupRequired: 'Project group is required.',
  projectGroupInvalid: 'Project group must be a valid Maven groupId (lowercase dot-separated segments).',
  projectNameRequired: 'Project name is required.',
  projectNameInvalidFolder: 'Project name must contain only letters, numbers, dots, underscores, or hyphens.',
  artifactIdInvalid: 'Project name generates an invalid artifact id.',
  constraintInvalid: 'Please complete all constraint values before saving.',
  atLeastOneProperty: 'At least one property is required to save a data object.',
  mapperNameRequired: 'Mapper name is required.',
  mapperNameInvalid: 'Mapper name must be a valid Java class name (PascalCase).',
  mapperNameDuplicate: 'Mapper name already exists.',
  fromModelRequired: 'From model is required.',
  toModelRequired: 'To model is required.',
  sourceTargetDifferent: 'From model and To model must be different.',
  atLeastOneMapping: 'At least one mapping row is required.',
  allRowsMapped: 'All mapping rows must be mapped.',
  baseEndpointRequired: 'Base endpoint is required.'
} as const;

