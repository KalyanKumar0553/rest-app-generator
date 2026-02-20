import { ValidationRule } from '../../../services/validator.service';

const JAVA_IDENTIFIER_PATTERN = /^[A-Za-z_$][A-Za-z0-9_$]*$/;
const JAVA_TYPE_NAME_PATTERN = /^[A-Z][A-Za-z0-9]*$/;
const JAVA_ENUM_CONSTANT_PATTERN = /^[A-Z0-9_]+$/;
const MAVEN_GROUP_ID_PATTERN = /^[a-z0-9]+(?:[._-][a-z0-9]+)*(?:\.[a-z0-9]+(?:[._-][a-z0-9]+)*)*$/;
const MAVEN_ARTIFACT_ID_PATTERN = /^[a-z0-9]+(?:[._-][a-z0-9]+)*$/;

const JAVA_KEYWORDS = new Set([
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class', 'const', 'continue',
  'default', 'do', 'double', 'else', 'enum', 'extends', 'final', 'finally', 'float', 'for', 'goto', 'if',
  'implements', 'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package', 'private',
  'protected', 'public', 'return', 'short', 'static', 'strictfp', 'super', 'switch', 'synchronized', 'this',
  'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while', 'true', 'false', 'null'
]);

const DATABASE_RESERVED_KEYWORDS = new Set([
  'add', 'alter', 'and', 'as', 'between', 'by', 'case', 'column', 'constraint', 'create', 'database',
  'default', 'delete', 'desc', 'distinct', 'drop', 'exec', 'exists', 'from', 'group', 'having', 'in',
  'index', 'insert', 'into', 'is', 'join', 'key', 'like', 'limit', 'not', 'null', 'on', 'or', 'order',
  'primary', 'procedure', 'replace', 'schema', 'select', 'set', 'table', 'top', 'truncate', 'union',
  'unique', 'update', 'values', 'view', 'where'
]);

export const isValidJavaIdentifier = (value: string): boolean => {
  const trimmed = String(value ?? '').trim();
  return JAVA_IDENTIFIER_PATTERN.test(trimmed) && !JAVA_KEYWORDS.has(trimmed);
};

export const findReservedJavaOrDatabaseKeyword = (value: string): string | null => {
  const normalized = String(value ?? '').trim().toLowerCase();
  if (!normalized) {
    return null;
  }
  if (JAVA_KEYWORDS.has(normalized) || DATABASE_RESERVED_KEYWORDS.has(normalized)) {
    return normalized;
  }
  return null;
};

export const isValidJavaTypeName = (value: string): boolean => {
  const trimmed = String(value ?? '').trim();
  return JAVA_TYPE_NAME_PATTERN.test(trimmed) && isValidJavaIdentifier(trimmed);
};

export const isValidJavaEnumConstantName = (value: string): boolean => {
  const trimmed = String(value ?? '').trim();
  if (!JAVA_ENUM_CONSTANT_PATTERN.test(trimmed)) {
    return false;
  }
  return isValidJavaIdentifier(trimmed);
};

export const isValidMavenGroupId = (value: string): boolean => {
  const trimmed = String(value ?? '').trim();
  return MAVEN_GROUP_ID_PATTERN.test(trimmed);
};

export const isValidMavenArtifactId = (value: string): boolean => {
  const trimmed = String(value ?? '').trim();
  return MAVEN_ARTIFACT_ID_PATTERN.test(trimmed);
};

export interface JavaNameRuleParams {
  fieldName: string;
  label: string;
  setError?: (message: string) => void;
}

export const buildJavaNameRules = (params: JavaNameRuleParams): ValidationRule[] => [
  {
    field: params.fieldName,
    setError: params.setError,
    constraints: [
      {
        type: 'required',
        message: `${params.label} is required.`,
        messageType: 'error'
      },
      {
        type: 'custom',
        predicate: (value) => isValidJavaIdentifier(String(value ?? '')),
        message: `${params.label} must be a valid Java identifier and not a Java keyword.`,
        messageType: 'error'
      }
    ]
  }
];

export interface MavenRuleParams {
  groupField: string;
  artifactField: string;
  setGroupError?: (message: string) => void;
  setArtifactError?: (message: string) => void;
}

export const buildMavenNamingRules = (params: MavenRuleParams): ValidationRule[] => [
  {
    field: params.groupField,
    setError: params.setGroupError,
    constraints: [
      {
        type: 'required',
        message: 'Project group is required.',
        messageType: 'error'
      },
      {
        type: 'custom',
        predicate: (value) => isValidMavenGroupId(String(value ?? '')),
        message: 'Project group must be a valid Maven groupId (lowercase dot-separated segments).',
        messageType: 'error'
      }
    ]
  },
  {
    field: params.artifactField,
    setError: params.setArtifactError,
    constraints: [
      {
        type: 'required',
        message: 'Artifact id is required.',
        messageType: 'error'
      },
      {
        type: 'custom',
        predicate: (value) => isValidMavenArtifactId(String(value ?? '')),
        message: 'Artifact id must use lowercase letters, digits, and separators (., _, -).',
        messageType: 'error'
      }
    ]
  }
];
