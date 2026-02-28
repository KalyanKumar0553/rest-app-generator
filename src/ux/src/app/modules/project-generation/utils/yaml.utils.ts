export function convertObjectToYaml(value: any): string {
  return toYaml(value, 0).trim() + '\n';
}

function toYaml(value: any, indent: number): string {
  const indentation = '  '.repeat(indent);

  if (value === null || value === undefined) {
    return 'null';
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return '[]';
    }
    return value.map(item => {
      if (isScalar(item)) {
        return `${indentation}- ${formatScalar(item)}`;
      }
      const nested = toYaml(item, indent + 1);
      return `${indentation}-\n${nested}`;
    }).join('\n');
  }

  if (typeof value === 'object') {
    const entries = Object.entries(value).filter(([, v]) => v !== undefined);
    if (entries.length === 0) {
      return '{}';
    }
    return entries.map(([key, val]) => {
      if (isScalar(val)) {
        return `${indentation}${key}: ${formatScalar(val)}`;
      }
      if (Array.isArray(val) && val.length === 0) {
        return `${indentation}${key}: []`;
      }
      if (typeof val === 'object' && val !== null && !Array.isArray(val) && Object.keys(val).length === 0) {
        return `${indentation}${key}: {}`;
      }
      return `${indentation}${key}:\n${toYaml(val, indent + 1)}`;
    }).join('\n');
  }

  return formatScalar(value);
}

function isScalar(value: any): boolean {
  return value === null || ['string', 'number', 'boolean'].includes(typeof value);
}

function formatScalar(value: any): string {
  if (value === null || value === undefined) {
    return 'null';
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  const stringValue = String(value);
  if (stringValue === '') {
    return '""';
  }
  if (/^[a-zA-Z0-9_\-./]+$/.test(stringValue)) {
    return stringValue;
  }
  return `"${stringValue.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
}
