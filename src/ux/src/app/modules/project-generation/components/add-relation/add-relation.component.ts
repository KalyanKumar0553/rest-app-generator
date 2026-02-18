import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

type OnDeleteOption = 'NONE' | 'CASCADE' | 'SET_NULL';

export interface JoinColumnConfig {
  name?: string;
  nullable?: boolean;
  referencedColumnName?: string;
  index?: boolean;
  onDelete?: OnDeleteOption;
}

export interface JoinTableConfig {
  name?: string;
  joinColumns?: JoinColumnConfig[];
  inverseJoinColumns?: JoinColumnConfig[];
  uniquePair?: boolean;
  onDelete?: OnDeleteOption;
}

export interface Relation {
  sourceEntity: string;
  sourceFieldName: string;
  targetEntity: string;
  targetFieldName?: string;
  unidirectional?: boolean;
  relationType: string;
  required?: boolean;

  mappedBy?: string;
  cascade?: string[];
  orphanRemoval?: boolean;
  orderBy?: string;
  orderColumn?: { name?: string };

  optional?: boolean;
  joinColumn?: JoinColumnConfig;

  joinTable?: JoinTableConfig;
}

interface Entity {
  name: string;
  fields?: Array<{ name?: string }>;
}

@Component({
  selector: 'app-add-relation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  templateUrl: './add-relation.component.html',
  styleUrls: ['./add-relation.component.css']
})
export class AddRelationComponent implements OnChanges {
  @Input() editRelation: Relation | null = null;
  @Input() isOpen = false;
  @Input() entities: Entity[] = [];
  @Input() existingRelations: Relation[] = [];
  @Output() save = new EventEmitter<Relation>();
  @Output() cancel = new EventEmitter<void>();

  sourceEntity = '';
  sourceFieldName = '';
  targetEntity = '';
  targetFieldName = '';
  relationType = '';
  unidirectional = false;
  required = false;

  mappedBy = '';
  cascade: string[] = [];
  orphanRemoval = false;
  orderBy = '';
  orderColumnName = '';

  optional = true;
  joinColumnName = '';
  joinColumnNullable = true;
  joinColumnReferencedColumnName = '';
  joinColumnIndex = false;
  joinColumnOnDelete: OnDeleteOption | '' = '';

  joinTableName = '';
  joinColumnNameForJoinTable = '';
  joinColumnReferencedForJoinTable = '';
  inverseJoinColumnNameForJoinTable = '';
  inverseJoinColumnReferencedForJoinTable = '';
  joinTableUniquePair = false;
  joinTableOnDelete: OnDeleteOption | '' = '';

  sourceEntityError = '';
  sourceFieldNameError = '';
  targetEntityError = '';
  targetFieldNameError = '';
  relationTypeError = '';
  mappedByError = '';
  joinTableError = '';
  relationConfigExpanded = true;

  relationTypes = [
    'OneToOne',
    'OneToMany',
    'ManyToOne',
    'ManyToMany'
  ];

  cascadeOptions = ['ALL', 'PERSIST', 'MERGE', 'REMOVE', 'REFRESH', 'DETACH'];
  onDeleteOptions: OnDeleteOption[] = ['NONE', 'CASCADE', 'SET_NULL'];

  getCascadeOptionLabel(option: string): string {
    switch (option) {
      case 'ALL':
        return 'All operations';
      case 'PERSIST':
        return 'Persist';
      case 'MERGE':
        return 'Merge';
      case 'REMOVE':
        return 'Remove';
      case 'REFRESH':
        return 'Refresh';
      case 'DETACH':
        return 'Detach';
      default:
        return option;
    }
  }

  getOnDeleteOptionLabel(option: OnDeleteOption): string {
    switch (option) {
      case 'NONE':
        return 'No action';
      case 'CASCADE':
        return 'Cascade delete';
      case 'SET_NULL':
        return 'Set related value to null';
      default:
        return option;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        if (this.editRelation) {
          this.loadRelationData(this.editRelation);
        } else {
          this.resetForm();
        }
      }
    }

    if (changes['editRelation'] && this.editRelation) {
      this.loadRelationData(this.editRelation);
    }
  }

  loadRelationData(relation: Relation): void {
    this.sourceEntity = relation.sourceEntity;
    this.sourceFieldName = relation.sourceFieldName;
    this.targetEntity = relation.targetEntity;
    this.targetFieldName = relation.targetFieldName || '';
    this.relationType = relation.relationType;
    this.unidirectional = relation.unidirectional ?? !relation.targetFieldName;
    this.required = relation.required || false;

    this.mappedBy = relation.mappedBy || '';
    this.cascade = this.normalizeCascadeSelection([...(relation.cascade ?? [])]);
    this.orphanRemoval = Boolean(relation.orphanRemoval);
    this.orderBy = relation.orderBy || '';
    this.orderColumnName = relation.orderColumn?.name || '';

    this.optional = relation.optional ?? true;
    this.joinColumnName = relation.joinColumn?.name || '';
    this.joinColumnNullable = relation.joinColumn?.nullable ?? true;
    this.joinColumnReferencedColumnName = relation.joinColumn?.referencedColumnName || '';
    this.joinColumnIndex = Boolean(relation.joinColumn?.index);
    this.joinColumnOnDelete = relation.joinColumn?.onDelete || '';

    this.joinTableName = relation.joinTable?.name || '';
    this.joinColumnNameForJoinTable = relation.joinTable?.joinColumns?.[0]?.name || '';
    this.joinColumnReferencedForJoinTable = relation.joinTable?.joinColumns?.[0]?.referencedColumnName || '';
    this.inverseJoinColumnNameForJoinTable = relation.joinTable?.inverseJoinColumns?.[0]?.name || '';
    this.inverseJoinColumnReferencedForJoinTable = relation.joinTable?.inverseJoinColumns?.[0]?.referencedColumnName || '';
    this.joinTableUniquePair = Boolean(relation.joinTable?.uniquePair);
    this.joinTableOnDelete = relation.joinTable?.onDelete || '';

    this.clearErrors();
  }

  resetForm(): void {
    this.sourceEntity = '';
    this.sourceFieldName = '';
    this.targetEntity = '';
    this.targetFieldName = '';
    this.relationType = '';
    this.unidirectional = false;
    this.required = false;

    this.resetRelationConfig();
    this.clearErrors();
  }

  clearErrors(): void {
    this.sourceEntityError = '';
    this.sourceFieldNameError = '';
    this.targetEntityError = '';
    this.targetFieldNameError = '';
    this.relationTypeError = '';
    this.mappedByError = '';
    this.joinTableError = '';
  }

  onSourceEntityChange(): void {
    this.sourceEntityError = '';
    this.sourceFieldName = '';
    this.sourceFieldNameError = '';
  }

  onSourceFieldNameChange(): void {
    this.sourceFieldNameError = '';
  }

  onTargetEntityChange(): void {
    this.targetEntityError = '';
    this.targetFieldName = '';
    this.targetFieldNameError = '';
  }

  onTargetFieldNameChange(): void {
    this.targetFieldNameError = '';
  }

  onRelationTypeChange(): void {
    this.relationTypeError = '';
    this.mappedByError = '';
    this.joinTableError = '';
    this.resetRelationConfig();
    this.applyFieldVisibilityRules();
    this.relationConfigExpanded = true;
  }

  onUnidirectionalChange(): void {
    this.applyFieldVisibilityRules();
  }

  onCascadeChange(): void {
    this.cascade = this.normalizeCascadeSelection(this.cascade);
  }

  validateSourceEntity(): boolean {
    if (!this.sourceEntity) {
      this.sourceEntityError = 'Source entity is required.';
      return false;
    }
    this.sourceEntityError = '';
    return true;
  }

  validateSourceFieldName(): boolean {
    if (!this.shouldRequireSourceFieldName()) {
      this.sourceFieldNameError = '';
      return true;
    }
    if (!this.sourceFieldName.trim()) {
      this.sourceFieldNameError = 'Source field name is required.';
      return false;
    }
    this.sourceFieldNameError = '';
    return true;
  }

  validateTargetEntity(): boolean {
    if (!this.targetEntity) {
      this.targetEntityError = 'Target entity is required.';
      return false;
    }

    this.targetEntityError = '';
    return true;
  }

  validateTargetFieldName(): boolean {
    if (!this.shouldRequireTargetFieldName()) {
      this.targetFieldNameError = '';
      return true;
    }
    if (!this.targetFieldName.trim()) {
      this.targetFieldNameError = 'Target field name is required.';
      return false;
    }
    this.targetFieldNameError = '';
    return true;
  }

  validateRelationType(): boolean {
    if (!this.relationType) {
      this.relationTypeError = 'Relation type is required.';
      return false;
    }
    this.relationTypeError = '';
    return true;
  }

  validateRelationConfig(): boolean {
    this.mappedByError = '';
    this.joinTableError = '';

    if (this.relationType === 'ManyToMany') {
      if (!this.joinTableName.trim()) {
        this.joinTableError = 'Intermediate table name is required for ManyToMany.';
        return false;
      }
    }

    return true;
  }

  validateAll(): boolean {
    const isSourceEntityValid = this.validateSourceEntity();
    const isSourceFieldNameValid = this.validateSourceFieldName();
    const isTargetEntityValid = this.validateTargetEntity();
    const isTargetFieldNameValid = this.validateTargetFieldName();
    const isRelationTypeValid = this.validateRelationType();
    const isRelationConfigValid = this.validateRelationConfig();

    return isSourceEntityValid && isSourceFieldNameValid && isTargetEntityValid && isTargetFieldNameValid && isRelationTypeValid && isRelationConfigValid;
  }

  onSave(): void {
    if (!this.validateAll()) {
      return;
    }

    const resolvedSourceFieldName = this.sourceFieldName.trim() || this.targetFieldName.trim() || 'relation';
    const relation: Relation = {
      sourceEntity: this.sourceEntity,
      sourceFieldName: resolvedSourceFieldName,
      targetEntity: this.targetEntity,
      targetFieldName: this.shouldRequireTargetFieldName() ? (this.targetFieldName || undefined) : undefined,
      unidirectional: this.unidirectional,
      relationType: this.relationType,
      required: this.required
    };

    const normalizedCascade = this.normalizeCascadeSelection(this.cascade);
    if (normalizedCascade.length > 0) {
      relation.cascade = normalizedCascade;
    }

    if (this.relationType === 'OneToOne') {
      relation.optional = this.optional;
      relation.mappedBy = this.mappedBy.trim() || undefined;
      relation.orphanRemoval = this.orphanRemoval;
      if (!relation.mappedBy) {
        const joinColumn = this.buildJoinColumnConfig();
        if (joinColumn) {
          relation.joinColumn = joinColumn;
        }
      }
    }

    if (this.relationType === 'OneToMany') {
      relation.mappedBy = this.mappedBy.trim() || this.targetFieldName.trim() || undefined;
      relation.orphanRemoval = this.orphanRemoval;
      relation.orderBy = this.orderBy.trim() || undefined;
      relation.orderColumn = this.orderColumnName.trim() ? { name: this.orderColumnName.trim() } : undefined;
    }

    if (this.relationType === 'ManyToOne') {
      relation.optional = this.optional;
      const joinColumn = this.buildJoinColumnConfig();
      if (joinColumn) {
        relation.joinColumn = joinColumn;
      }
    }

    if (this.relationType === 'ManyToMany') {
      const joinTable = this.buildJoinTableConfig();
      if (joinTable) {
        relation.joinTable = joinTable;
      }
    }

    this.save.emit(relation);
    this.resetForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
  }

  isOneToMany(): boolean {
    return this.relationType === 'OneToMany';
  }

  isOneToOne(): boolean {
    return this.relationType === 'OneToOne';
  }

  isManyToOne(): boolean {
    return this.relationType === 'ManyToOne';
  }

  isManyToMany(): boolean {
    return this.relationType === 'ManyToMany';
  }

  shouldShowRequiredOption(): boolean {
    if (this.isManyToMany()) {
      return false;
    }
    return Boolean(
      this.sourceEntity &&
      this.targetEntity &&
      this.sourceEntity !== this.targetEntity
    );
  }

  showSourceFieldName(): boolean {
    if (this.isOneToMany()) {
      return !this.unidirectional;
    }
    return true;
  }

  showTargetFieldName(): boolean {
    if (this.isOneToMany()) {
      return true;
    }
    if (this.isOneToOne() || this.isManyToOne() || this.isManyToMany()) {
      return !this.unidirectional;
    }
    return true;
  }

  shouldRequireSourceFieldName(): boolean {
    return this.showSourceFieldName();
  }

  shouldRequireTargetFieldName(): boolean {
    return this.showTargetFieldName();
  }

  toggleRelationConfig(): void {
    this.relationConfigExpanded = !this.relationConfigExpanded;
  }

  private buildJoinColumnConfig(): JoinColumnConfig | undefined {
    const config: JoinColumnConfig = {};
    if (this.joinColumnName.trim()) {
      config.name = this.joinColumnName.trim();
    }
    config.nullable = this.joinColumnNullable;
    if (this.joinColumnReferencedColumnName.trim()) {
      config.referencedColumnName = this.joinColumnReferencedColumnName.trim();
    }
    if (this.joinColumnIndex) {
      config.index = true;
    }
    if (this.joinColumnOnDelete) {
      config.onDelete = this.joinColumnOnDelete;
    }

    return this.hasJoinColumnValues(config) ? config : undefined;
  }

  private buildJoinTableConfig(): JoinTableConfig | undefined {
    const config: JoinTableConfig = {};

    if (this.joinTableName.trim()) {
      config.name = this.joinTableName.trim();
    }

    const joinColumn: JoinColumnConfig = {};
    if (this.joinColumnNameForJoinTable.trim()) {
      joinColumn.name = this.joinColumnNameForJoinTable.trim();
    }
    if (this.joinColumnReferencedForJoinTable.trim()) {
      joinColumn.referencedColumnName = this.joinColumnReferencedForJoinTable.trim();
    }
    if (this.hasJoinColumnValues(joinColumn)) {
      config.joinColumns = [joinColumn];
    }

    const inverseJoinColumn: JoinColumnConfig = {};
    if (this.inverseJoinColumnNameForJoinTable.trim()) {
      inverseJoinColumn.name = this.inverseJoinColumnNameForJoinTable.trim();
    }
    if (this.inverseJoinColumnReferencedForJoinTable.trim()) {
      inverseJoinColumn.referencedColumnName = this.inverseJoinColumnReferencedForJoinTable.trim();
    }
    if (this.hasJoinColumnValues(inverseJoinColumn)) {
      config.inverseJoinColumns = [inverseJoinColumn];
    }

    if (this.joinTableUniquePair) {
      config.uniquePair = true;
    }

    if (this.joinTableOnDelete) {
      config.onDelete = this.joinTableOnDelete;
    }

    return this.hasJoinTableValues(config) ? config : undefined;
  }

  private hasJoinColumnValues(config: JoinColumnConfig | undefined): boolean {
    return Boolean(
      config && (
        config.name ||
        config.referencedColumnName ||
        config.index ||
        config.onDelete ||
        config.nullable !== undefined
      )
    );
  }

  private hasJoinTableValues(config: JoinTableConfig | undefined): boolean {
    return Boolean(
      config && (
        config.name ||
        (config.joinColumns && config.joinColumns.length > 0) ||
        (config.inverseJoinColumns && config.inverseJoinColumns.length > 0) ||
        config.uniquePair ||
        config.onDelete
      )
    );
  }

  private resetRelationConfig(): void {
    this.mappedBy = '';
    this.cascade = [];
    this.orphanRemoval = false;
    this.orderBy = '';
    this.orderColumnName = '';

    this.optional = true;
    this.joinColumnName = '';
    this.joinColumnNullable = true;
    this.joinColumnReferencedColumnName = '';
    this.joinColumnIndex = false;
    this.joinColumnOnDelete = '';

    this.joinTableName = '';
    this.joinColumnNameForJoinTable = '';
    this.joinColumnReferencedForJoinTable = '';
    this.inverseJoinColumnNameForJoinTable = '';
    this.inverseJoinColumnReferencedForJoinTable = '';
    this.joinTableUniquePair = false;
    this.joinTableOnDelete = '';
  }

  private applyFieldVisibilityRules(): void {
    if (!this.showSourceFieldName()) {
      this.sourceFieldNameError = '';
    }
    if (!this.showTargetFieldName()) {
      this.targetFieldNameError = '';
    }
  }

  private normalizeCascadeSelection(cascade: string[]): string[] {
    const unique = Array.from(new Set((cascade ?? []).filter(Boolean)));
    if (unique.includes('ALL')) {
      return ['ALL'];
    }
    return unique;
  }

}
