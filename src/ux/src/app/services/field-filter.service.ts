import { Injectable } from '@angular/core';
import { SortOption } from '../components/search-sort/search-sort.component';
import { Field } from '../modules/project-generation/components/field-item/field-item.component';

export interface VisibleField {
  field: Field;
  index: number;
}

@Injectable({
  providedIn: 'root'
})
export class FieldFilterService {
  getVisibleFields(
    fields: Field[],
    searchTerm: string,
    sortOption: SortOption | null
  ): VisibleField[] {
    const normalizedSearch = searchTerm.trim().toLowerCase();
    const withIndex = fields.map((field, index) => ({ field, index }));

    let results = withIndex;
    if (normalizedSearch) {
      results = withIndex.filter(({ field }) => this.matchesFieldSearch(field, normalizedSearch));
    }

    if (sortOption) {
      const { property, direction } = sortOption;
      results = [...results].sort((a, b) => {
        const aValue = this.getSortValue(a.field, property);
        const bValue = this.getSortValue(b.field, property);
        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return direction === 'asc' ? comparison : -comparison;
      });
    }

    return results;
  }

  private matchesFieldSearch(field: Field, searchTerm: string): boolean {
    const constraints = field.constraints ?? [];
    const constraintTokens = constraints
      .map(constraint => `${constraint.name ?? ''} ${constraint.value ?? ''} ${constraint.value2 ?? ''}`.trim())
      .filter(Boolean)
      .join(' ');
    const legacyConstraints = [
      field.primaryKey ? 'primary key' : '',
      field.required ? 'required' : '',
      field.unique ? 'unique' : ''
    ]
      .filter(Boolean)
      .join(' ');

    const tokens = [
      field.name,
      field.type,
      field.maxLength ? String(field.maxLength) : '',
      constraintTokens,
      legacyConstraints
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return tokens.includes(searchTerm);
  }

  private getSortValue(field: Field, property: string): string | number {
    switch (property) {
      case 'name':
        return field.name?.toLowerCase() ?? '';
      case 'type':
        return field.type?.toLowerCase() ?? '';
      case 'constraintCount':
        return this.getConstraintCount(field);
      case 'maxLength':
        return field.maxLength ?? 0;
      default:
        return '';
    }
  }

  private getConstraintCount(field: Field): number {
    const listCount = field.constraints?.length ?? 0;
    if (listCount > 0) {
      return listCount;
    }
    return Number(Boolean(field.primaryKey)) + Number(Boolean(field.required)) + Number(Boolean(field.unique));
  }
}
