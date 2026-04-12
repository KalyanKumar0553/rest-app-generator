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

    this.validateOnlyCreateTableStatements(sql);

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

  private validateOnlyCreateTableStatements(sql: string): void {
    const withoutBlockComments = sql.replace(/\/\*[\s\S]*?\*\//g, ' ');
    const withoutLineComments = withoutBlockComments
      .replace(/--.*$/gm, ' ')
      .replace(/#.*$/gm, ' ');

    const statements = withoutLineComments
      .split(';')
      .map(statement => statement.trim())
      .filter(Boolean);

    const invalidStatement = statements.find(
      statement => !/^create\s+table\b/i.test(statement)
    );

    if (invalidStatement) {
      throw new Error('Only CREATE TABLE SQL queries are allowed for import.');
    }
  }

  private parseColumns(columnsBlock: string): DdlField[] {
    const lines = columnsBlock
      .split(/,\s*(?![^()]*\))/)
      .map(line => line.trim())
      .filter(line => line.length > 0);

    const primaryKeyColumns = this.extractPrimaryKeyColumns(lines);
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
      const normalizedRawName = this.normalizeIdentifier(rawName);
      const isPrimaryKey = /\bprimary\s+key\b/i.test(line) || primaryKeyColumns.has(normalizedRawName);

      fields.push({
        name: this.toCamelCase(rawName),
        type: normalizedType,
        maxLength: length,
        primaryKey: isPrimaryKey,
        required: /\bnot\s+null\b/i.test(line)
      });
    }

    return this.ensurePrimaryKeyField(fields);
  }

  private extractPrimaryKeyColumns(lines: string[]): Set<string> {
    const primaryKeyColumns = new Set<string>();

    for (const line of lines) {
      const tablePrimaryKeyMatch = /(?:^constraint\s+[a-zA-Z0-9_`"\[\]]+\s+)?primary\s+key\s*\(([^)]+)\)/i.exec(line);
      if (!tablePrimaryKeyMatch) {
        continue;
      }

      const rawColumns = tablePrimaryKeyMatch[1]
        .split(',')
        .map(column => this.normalizeIdentifier(column))
        .filter(Boolean);

      rawColumns.forEach(column => primaryKeyColumns.add(column));
    }

    return primaryKeyColumns;
  }

  private ensurePrimaryKeyField(fields: DdlField[]): DdlField[] {
    if (fields.some(field => Boolean(field.primaryKey))) {
      return fields;
    }

    const idField = fields.find(field => this.normalizeIdentifier(field.name) === 'id');
    if (idField) {
      idField.primaryKey = true;
      idField.required = true;
      return fields;
    }

    return [
      {
        name: 'id',
        type: 'Long',
        primaryKey: true,
        required: true
      },
      ...fields
    ];
  }

  private normalizeIdentifier(value: string): string {
    return value
      .trim()
      .replace(/^[`"\[]+|[`"\]]+$/g, '')
      .toLowerCase();
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
