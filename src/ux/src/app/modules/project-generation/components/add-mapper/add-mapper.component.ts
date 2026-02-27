import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { findReservedJavaOrDatabaseKeyword, isValidJavaTypeName } from '../../validators/naming-validation';
import { VALIDATION_MESSAGES } from '../../constants/validation-messages';

interface ModelField {
  name: string;
  type: string;
}

interface MapperModel {
  name: string;
  fields: ModelField[];
}

export interface MapperFieldMapping {
  sourceField: string;
  targetField: string;
}

export interface MapperDefinition {
  name: string;
  fromModel: string;
  toModel: string;
  mappings: MapperFieldMapping[];
}

@Component({
  selector: 'app-add-mapper',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './add-mapper.component.html',
  styleUrls: ['./add-mapper.component.css']
})
export class AddMapperComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() editMapper: MapperDefinition | null = null;
  @Input() existingMappers: MapperDefinition[] = [];
  @Input() dataObjects: Array<{ name: string; fields?: Array<{ name?: string; type?: string }> }> = [];
  @Input() entities: Array<{ name: string; fields?: Array<{ name?: string; type?: string }> }> = [];

  @Output() save = new EventEmitter<MapperDefinition>();
  @Output() cancel = new EventEmitter<void>();

  mapperName = '';
  fromModel = '';
  toModel = '';

  mapperNameError = '';
  fromModelError = '';
  toModelError = '';
  mappingsError = '';

  sourceModels: MapperModel[] = [];
  targetModels: MapperModel[] = [];
  sourceFields: ModelField[] = [];
  targetFields: ModelField[] = [];
  visibleSourceFieldNames: string[] = [];

  mappedTargetBySource: Record<string, string> = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      this.initializeForm();
    }

    if ((changes['dataObjects'] || changes['entities']) && this.isOpen) {
      this.buildModelOptions();
      this.refreshFieldOptions();
    }
  }

  onSave(): void {
    if (!this.validate()) {
      return;
    }

    const visibleSources = this.getVisibleSourceFields();
    const mappings: MapperFieldMapping[] = visibleSources
      .map((source) => ({
        sourceField: source.name,
        targetField: this.mappedTargetBySource[source.name] ?? ''
      }))
      .filter((item) => item.targetField);

    this.save.emit({
      name: this.mapperName.trim(),
      fromModel: this.fromModel,
      toModel: this.toModel,
      mappings
    });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onMapperNameChange(): void {
    if (this.mapperNameError) {
      this.mapperNameError = '';
    }
  }

  onFromModelChange(): void {
    this.fromModelError = '';
    this.refreshFieldOptions();
    this.autoMapCompatibleFields();
  }

  onToModelChange(): void {
    this.toModelError = '';
    this.refreshFieldOptions();
    this.autoMapCompatibleFields();
  }

  onTargetFieldChange(): void {
    if (this.mappingsError) {
      this.mappingsError = '';
    }
  }

  clearMapping(sourceFieldName: string): void {
    this.mappedTargetBySource[sourceFieldName] = '';
  }

  removeMappingRow(sourceFieldName: string): void {
    this.visibleSourceFieldNames = this.visibleSourceFieldNames.filter((name) => name !== sourceFieldName);
    this.clearMapping(sourceFieldName);
    this.onTargetFieldChange();
  }

  addMappingRow(afterSourceFieldName?: string): void {
    const existing = new Set(this.visibleSourceFieldNames);
    const next = this.sourceFields.find((field) => !existing.has(field.name));
    if (!next) {
      return;
    }
    if (!afterSourceFieldName) {
      this.visibleSourceFieldNames = [...this.visibleSourceFieldNames, next.name];
      return;
    }
    const insertAt = this.visibleSourceFieldNames.findIndex((name) => name === afterSourceFieldName);
    if (insertAt < 0) {
      this.visibleSourceFieldNames = [...this.visibleSourceFieldNames, next.name];
      return;
    }
    const updated = [...this.visibleSourceFieldNames];
    updated.splice(insertAt + 1, 0, next.name);
    this.visibleSourceFieldNames = updated;
  }

  canAddMappingRow(): boolean {
    return this.visibleSourceFieldNames.length < this.sourceFields.length;
  }

  getVisibleSourceFields(): ModelField[] {
    const allowed = new Set(this.sourceFields.map((field) => field.name));
    const orderedVisible = this.visibleSourceFieldNames.filter((name) => allowed.has(name));
    return orderedVisible
      .map((name) => this.sourceFields.find((field) => field.name === name))
      .filter((field): field is ModelField => !!field);
  }

  getCompatibleTargetFields(source: ModelField): ModelField[] {
    return this.targetFields.filter((target) => this.areTypesCompatible(source.type, target.type));
  }

  private initializeForm(): void {
    this.mapperNameError = '';
    this.fromModelError = '';
    this.toModelError = '';
    this.mappingsError = '';

    this.buildModelOptions();

    if (this.editMapper) {
      this.mapperName = this.editMapper.name;
      this.fromModel = this.editMapper.fromModel;
      this.toModel = this.editMapper.toModel;
      this.mappedTargetBySource = (this.editMapper.mappings ?? []).reduce((acc, item) => {
        if (item?.sourceField) {
          acc[item.sourceField] = item.targetField;
        }
        return acc;
      }, {} as Record<string, string>);
    } else {
      this.mapperName = '';
      this.fromModel = '';
      this.toModel = '';
      this.mappedTargetBySource = {};
    }

    this.refreshFieldOptions();
    this.autoMapCompatibleFields();
  }

  private buildModelOptions(): void {
    const dtoModels: MapperModel[] = (Array.isArray(this.dataObjects) ? this.dataObjects : [])
      .map((item) => ({
        name: String(item?.name ?? '').trim(),
        fields: (Array.isArray(item?.fields) ? item.fields : [])
          .map((field) => ({
            name: String(field?.name ?? '').trim(),
            type: String(field?.type ?? '').trim()
          }))
          .filter((field) => field.name && field.type)
      }))
      .filter((item) => item.name);

    const entityModels: MapperModel[] = (Array.isArray(this.entities) ? this.entities : [])
      .map((item) => ({
        name: String(item?.name ?? '').trim(),
        fields: (Array.isArray(item?.fields) ? item.fields : [])
          .map((field) => ({
            name: String(field?.name ?? '').trim(),
            type: String(field?.type ?? '').trim()
          }))
          .filter((field) => field.name && field.type)
      }))
      .filter((item) => item.name);

    this.sourceModels = dtoModels;

    const seen = new Set<string>();
    this.targetModels = [...dtoModels, ...entityModels].filter((item) => {
      const key = item.name.toLowerCase();
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  private refreshFieldOptions(): void {
    this.sourceFields = this.getModelFields(this.fromModel, this.sourceModels);
    this.targetFields = this.getModelFields(this.toModel, this.targetModels);

    const validSourceNames = new Set(this.sourceFields.map((field) => field.name));
    const validTargetNames = new Set(this.targetFields.map((field) => field.name));

    this.mappedTargetBySource = Object.entries(this.mappedTargetBySource).reduce((acc, [source, target]) => {
      if (validSourceNames.has(source) && validTargetNames.has(target)) {
        acc[source] = target;
      }
      return acc;
    }, {} as Record<string, string>);

    const mappedSources = Object.keys(this.mappedTargetBySource).filter((source) => validSourceNames.has(source));
    const existingVisible = this.visibleSourceFieldNames.filter((source) => validSourceNames.has(source));
    const initialVisible = existingVisible.length
      ? existingVisible
      : mappedSources.length
        ? mappedSources
        : this.sourceFields.map((field) => field.name);
    this.visibleSourceFieldNames = Array.from(new Set(initialVisible));
  }

  private autoMapCompatibleFields(): void {
    if (!this.sourceFields.length || !this.targetFields.length) {
      return;
    }

    const targetByLowerName = new Map(
      this.targetFields.map((item) => [item.name.toLowerCase(), item])
    );

    this.sourceFields.forEach((source) => {
      if (this.mappedTargetBySource[source.name]) {
        return;
      }
      const match = targetByLowerName.get(source.name.toLowerCase());
      if (match && this.areTypesCompatible(source.type, match.type)) {
        this.mappedTargetBySource[source.name] = match.name;
      }
    });
  }

  private getModelFields(modelName: string, models: MapperModel[]): ModelField[] {
    const selected = models.find((item) => item.name === modelName);
    return selected?.fields ?? [];
  }

  private areTypesCompatible(sourceType: string, targetType: string): boolean {
    const normalizedSource = this.normalizeType(sourceType);
    const normalizedTarget = this.normalizeType(targetType);
    return normalizedSource === normalizedTarget;
  }

  private normalizeType(type: string): string {
    const value = String(type ?? '').trim();
    if (!value) {
      return '';
    }

    const listMatch = value.match(/^List\s*<\s*(.+)\s*>$/i);
    if (listMatch) {
      return `LIST<${this.normalizeType(listMatch[1])}>`;
    }

    const upper = value.toUpperCase();
    switch (upper) {
      case 'INT':
      case 'INTEGER':
        return 'INTEGER';
      case 'LONG':
        return 'LONG';
      case 'DOUBLE':
        return 'DOUBLE';
      case 'FLOAT':
        return 'FLOAT';
      case 'DECIMAL':
      case 'BIGDECIMAL':
        return 'BIGDECIMAL';
      case 'BOOL':
      case 'BOOLEAN':
        return 'BOOLEAN';
      case 'STRING':
        return 'STRING';
      case 'DATE':
      case 'LOCALDATE':
        return 'DATE';
      case 'DATETIME':
      case 'LOCALDATETIME':
      case 'INSTANT':
        return 'DATETIME';
      case 'TIME':
        return 'TIME';
      case 'UUID':
        return 'UUID';
      case 'BINARY':
      case 'BYTE[]':
        return 'BINARY';
      default:
        return value;
    }
  }

  private validate(): boolean {
    this.mapperNameError = '';
    this.fromModelError = '';
    this.toModelError = '';
    this.mappingsError = '';

    const name = this.mapperName.trim();
    if (!name) {
      this.mapperNameError = VALIDATION_MESSAGES.mapperNameRequired;
      this.focusNameInput();
      return false;
    }

    if (!isValidJavaTypeName(name) || Boolean(findReservedJavaOrDatabaseKeyword(name))) {
      this.mapperNameError = VALIDATION_MESSAGES.mapperNameInvalid;
      this.focusNameInput();
      return false;
    }

    const duplicate = this.existingMappers.find((item) => {
      if (this.editMapper && item.name === this.editMapper.name) {
        return false;
      }
      return String(item?.name ?? '').trim().toLowerCase() === name.toLowerCase();
    });

    if (duplicate) {
      this.mapperNameError = VALIDATION_MESSAGES.mapperNameDuplicate;
      this.focusNameInput();
      return false;
    }

    if (!this.fromModel) {
      this.fromModelError = VALIDATION_MESSAGES.fromModelRequired;
      return false;
    }

    if (!this.toModel) {
      this.toModelError = VALIDATION_MESSAGES.toModelRequired;
      return false;
    }

    if (this.fromModel === this.toModel) {
      this.toModelError = VALIDATION_MESSAGES.sourceTargetDifferent;
      return false;
    }

    const visibleSources = this.getVisibleSourceFields().map((field) => field.name);
    if (visibleSources.length === 0) {
      this.mappingsError = VALIDATION_MESSAGES.atLeastOneMapping;
      return false;
    }

    const allRowsMapped = visibleSources.every((source) => {
      const mappedTarget = this.mappedTargetBySource[source];
      return typeof mappedTarget === 'string' && mappedTarget.trim().length > 0;
    });
    if (!allRowsMapped) {
      this.mappingsError = VALIDATION_MESSAGES.allRowsMapped;
      return false;
    }

    return true;
  }

  private focusNameInput(): void {
    setTimeout(() => {
      const nameInput = document.querySelector('.mapper-name-row .name-field input') as HTMLElement | null;
      nameInput?.focus();
    });
  }
}
