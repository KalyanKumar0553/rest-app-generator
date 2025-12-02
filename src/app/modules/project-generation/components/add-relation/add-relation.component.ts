import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

export interface Relation {
  sourceEntity: string;
  sourceFieldName: string;
  targetEntity: string;
  targetFieldName?: string;
  relationType: string;
  required?: boolean;
}

interface Entity {
  name: string;
  fields?: any[];
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
    MatButtonModule
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
  required = false;

  sourceEntityError = '';
  sourceFieldNameError = '';
  targetEntityError = '';
  relationTypeError = '';

  relationTypes = [
    'OneToOne',
    'OneToMany',
    'ManyToOne',
    'ManyToMany'
  ];

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
    this.required = relation.required || false;
    this.clearErrors();
  }

  resetForm(): void {
    this.sourceEntity = '';
    this.sourceFieldName = '';
    this.targetEntity = '';
    this.targetFieldName = '';
    this.relationType = '';
    this.required = false;
    this.clearErrors();
  }

  clearErrors(): void {
    this.sourceEntityError = '';
    this.sourceFieldNameError = '';
    this.targetEntityError = '';
    this.relationTypeError = '';
  }

  onSourceEntityChange(): void {
    this.sourceEntityError = '';
  }

  onSourceFieldNameChange(): void {
    this.sourceFieldNameError = '';
  }

  onTargetEntityChange(): void {
    this.targetEntityError = '';
  }

  onRelationTypeChange(): void {
    this.relationTypeError = '';
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
    if (!this.sourceFieldName.trim()) {
      this.sourceFieldNameError = 'Source field name is required.';
      return false;
    }

    const alphanumericPattern = /^[a-zA-Z0-9]+$/;
    if (!alphanumericPattern.test(this.sourceFieldName)) {
      this.sourceFieldNameError = 'Field name must be alphanumeric without spaces.';
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

    if (this.targetEntity === this.sourceEntity) {
      this.targetEntityError = 'Target entity must be different from source entity.';
      return false;
    }

    this.targetEntityError = '';
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

  validateAll(): boolean {
    const isSourceEntityValid = this.validateSourceEntity();
    const isSourceFieldNameValid = this.validateSourceFieldName();
    const isTargetEntityValid = this.validateTargetEntity();
    const isRelationTypeValid = this.validateRelationType();

    return isSourceEntityValid && isSourceFieldNameValid && isTargetEntityValid && isRelationTypeValid;
  }

  onSave(): void {
    if (!this.validateAll()) {
      return;
    }

    const relation: Relation = {
      sourceEntity: this.sourceEntity,
      sourceFieldName: this.sourceFieldName,
      targetEntity: this.targetEntity,
      targetFieldName: this.targetFieldName || undefined,
      relationType: this.relationType,
      required: this.required
    };

    this.save.emit(relation);
    this.resetForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
  }
}
