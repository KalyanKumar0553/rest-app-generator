export function trimmed(value: unknown): string {
  return String(value ?? '').trim();
}

export function hasNumber(value: unknown): boolean {
  if (value === null || value === undefined) {
    return false;
  }
  return !Number.isNaN(Number(value));
}

export function toNumberIfPossible(value: string): string | number {
  const numeric = Number(value);
  return Number.isNaN(numeric) ? value : numeric;
}

export function toBooleanIfPossible(value: string): string | boolean {
  const normalized = value.toLowerCase();
  if (normalized === 'true') {
    return true;
  }
  if (normalized === 'false') {
    return false;
  }
  return value;
}

export function normalizeType(type: unknown): string {
  const value = trimmed(type);
  const typeMap: Record<string, string> = {
    Int: 'Integer',
    Decimal: 'BigDecimal',
    Date: 'LocalDate',
    Time: 'LocalTime',
    DateTime: 'OffsetDateTime',
    Json: 'String'
  };
  return typeMap[value] || value || 'String';
}

export function toSnakeCase(value: string): string {
  return value
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .replace(/[\s\-]+/g, '_')
    .replace(/__+/g, '_')
    .toLowerCase();
}

export function toArtifactId(value: string): string {
  return value
    .trim()
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/[\s_]+/g, '-')
    .replace(/-+/g, '-')
    .toLowerCase();
}

export function toDatabaseCode(value: unknown): string {
  const raw = trimmed(value) || '';
  const normalized = raw.toLowerCase();
  if (['NONE', 'OTHER', 'MSSQL', 'MYSQL', 'MARIADB', 'ORACLE', 'POSTGRES', 'MONGODB', 'DERBY', 'H2', 'HSQL'].includes(raw.toUpperCase())) {
    return raw.toUpperCase();
  }
  switch (normalized) {
    case 'none':
      return 'NONE';
    case 'other':
      return 'OTHER';
    case 'mssql':
    case 'mssql server':
    case 'sql server':
      return 'MSSQL';
    case 'mysql':
      return 'MYSQL';
    case 'mariadb':
      return 'MARIADB';
    case 'oracle':
      return 'ORACLE';
    case 'postgres':
    case 'postgresql':
      return 'POSTGRES';
    case 'mongodb':
    case 'mongo':
      return 'MONGODB';
    case 'derby':
    case 'apache derby':
      return 'DERBY';
    case 'h2':
    case 'h2 database':
      return 'H2';
    case 'hsql':
    case 'hypersql':
    case 'hsql database':
      return 'HSQL';
    default:
      return 'POSTGRES';
  }
}

export function resolveDatabaseType(type: unknown, databaseCode: unknown): 'SQL' | 'NOSQL' | 'NONE' {
  const normalizedType = trimmed(type)?.toUpperCase();
  if (normalizedType === 'NONE') {
    return 'NONE';
  }
  if (normalizedType === 'NOSQL') {
    return 'NOSQL';
  }
  if (normalizedType === 'SQL') {
    return 'SQL';
  }

  const normalizedDb = toDatabaseCode(databaseCode);
  if (normalizedDb === 'NONE') {
    return 'NONE';
  }
  if (normalizedDb === 'MONGODB') {
    return 'NOSQL';
  }
  return 'SQL';
}

export function normalizeProfileName(value: unknown): string | null {
  if (value === null || value === undefined) {
    return null;
  }
  const t = String(value).trim();
  if (!t) {
    return null;
  }
  return t.toLowerCase();
}

export function isValidProfileName(profile: string): boolean {
  return /^[a-z0-9._-]+$/.test(profile);
}

export function isNotNullConstraint(constraint: any): boolean {
  if (typeof constraint === 'string') {
    return constraint === 'NotNull';
  }
  if (!constraint || typeof constraint !== 'object') {
    return false;
  }
  const [firstKey] = Object.keys(constraint);
  return firstKey === 'NotNull';
}
