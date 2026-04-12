export const ENTITY_FIELD_TYPE_OPTIONS: string[] = [
  'String',
  'Long',
  'Integer',
  'Double',
  'Float',
  'Boolean',
  'LocalDate',
  'LocalDateTime',
  'BigDecimal',
  'UUID',
  'byte[]'
];

export const DTO_FIELD_TYPE_OPTIONS: string[] = [
  'String',
  'Int',
  'Long',
  'Double',
  'Decimal',
  'Boolean',
  'Date',
  'Time',
  'DateTime',
  'Instant',
  'UUID',
  'Json',
  'Binary',
  'List<String>',
  'List<Long>',
  'List<Integer>'
];

export const BACKEND_FIELD_TYPE_OPTIONS: string[] = Array.from(
  new Set([...ENTITY_FIELD_TYPE_OPTIONS, ...DTO_FIELD_TYPE_OPTIONS])
);
