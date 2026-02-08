import { Injectable } from '@angular/core';

export interface DdlField {
  name: string;
  type: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
}

export interface DdlEntity {
  name: string;
  fields: DdlField[];
}

@Injectable({
  providedIn: 'root'
})
export class DdlImportService {
  parse(sql: string): DdlEntity[] {
    if (!sql || !sql.trim()) {
      return [];
    }

    const entities: DdlEntity[] = [];
    const createTableRegex = /create\s+table\s+([`"\[]?)([a-zA-Z0-9_]+)\1\s*\(([^;]*?)\)\s*;/gi;
    let match: RegExpExecArray | null = null;

    while ((match = createTableRegex.exec(sql)) !== null) {
      const tableName = match[2];
      const columnsBlock = match[3];
      const fields = this.parseColumns(columnsBlock);
      if (fields.length === 0) {
        continue;
      }
      entities.push({
        name: this.toPascalCase(tableName),
        fields
      });
    }

    return entities;
  }

  private parseColumns(columnsBlock: string): DdlField[] {
    const lines = columnsBlock
      .split(/,\s*(?![^()]*\))/)
      .map(line => line.trim())
      .filter(line => line.length > 0);

    const fields: DdlField[] = [];

    for (const line of lines) {
      if (/^constraint\b/i.test(line) || /^primary\s+key\b/i.test(line) || /^unique\b/i.test(line)) {
        continue;
      }

      const columnMatch = /^([`"\[]?)([a-zA-Z0-9_]+)\1\s+([a-zA-Z0-9_]+)(\s*\((\d+)\))?/i.exec(line);
      if (!columnMatch) {
        continue;
      }

      const rawName = columnMatch[2];
      const rawType = columnMatch[3];
      const length = columnMatch[5] ? Number(columnMatch[5]) : undefined;
      const normalizedType = this.mapType(rawType);
      const isPrimaryKey = /\bprimary\s+key\b/i.test(line);

      fields.push({
        name: this.toCamelCase(rawName),
        type: normalizedType,
        maxLength: length,
        primaryKey: isPrimaryKey,
        required: /\bnot\s+null\b/i.test(line)
      });
    }

    return fields;
  }

  private mapType(rawType: string): string {
    const type = rawType.toLowerCase();
    if (type.includes('bigint')) {
      return 'Long';
    }
    if (type.includes('int')) {
      return 'Integer';
    }
    if (type.includes('decimal') || type.includes('numeric')) {
      return 'BigDecimal';
    }
    if (type.includes('date')) {
      return 'LocalDate';
    }
    if (type.includes('time')) {
      return 'Instant';
    }
    if (type.includes('bool')) {
      return 'Boolean';
    }
    return 'String';
  }

  private toCamelCase(value: string): string {
    return value
      .replace(/^[`"\[]?|[`"\]]$/g, '')
      .toLowerCase()
      .replace(/[_\s]+([a-z0-9])/g, (_, char) => char.toUpperCase());
  }

  private toPascalCase(value: string): string {
    const camel = this.toCamelCase(value);
    return camel.charAt(0).toUpperCase() + camel.slice(1);
  }
}
