import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, ViewChild, ElementRef, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { SearchSortComponent, SearchSortEvent, SearchConfig, SortOption } from '../../../../components/search-sort/search-sort.component';
import { FieldItemComponent, Field } from '../field-item/field-item.component';
import { ConfigureEntityComponent } from '../configure-entity/configure-entity.component';
import { GeneralSettings, LombokSettings } from './entity-settings.model';

export interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  fields: Field[];
  useLombok?: boolean;
  lombokSettings?: LombokSettings;
  generalSettings?: GeneralSettings;
}

@Component({
  selector: 'app-add-entity',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    ConfigureEntityComponent,
    ModalComponent,
    SearchSortComponent,
    FieldItemComponent
  ],
  templateUrl: './add-entity.component.html',
  styleUrls: ['./add-entity.component.css']
})
export class AddEntityComponent implements OnChanges {
  @Input() editEntity: Entity | null = null;
  @Input() isOpen = false;
  @Input() existingEntities: Entity[] = [];
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();
  @ViewChild('entityNameInput') entityNameInput?: ElementRef<HTMLInputElement>;
  @ViewChild('entityNameModel') entityNameModel?: NgModel;
  @ViewChild('entityForm') entityForm?: NgForm;
  @ViewChildren(FieldItemComponent) fieldItems?: QueryList<FieldItemComponent>;
  @ViewChildren('fieldCard') fieldCards?: QueryList<ElementRef<HTMLDivElement>>;

  entityName = '';
  mappedSuperclass = false;
  addRestEndpoints = false;
  nameError = '';
  submitted = false;
  fieldSearchConfig: SearchConfig = {
    placeholder: 'Search fields...',
    properties: ['name', 'type']
  };
  fieldSearchTerm = '';
  fieldSortOptions: SortOption[] = [
    { label: 'Field name', property: 'name', direction: 'asc' },
    { label: 'Field name', property: 'name', direction: 'desc' },
    { label: 'Field type', property: 'type', direction: 'asc' },
    { label: 'Field type', property: 'type', direction: 'desc' }
  ];
  fieldSortSelection: SortOption | null = null;
  showFieldConfigModal = false;
  fieldConfigIndex: number | null = null;
  filteredFieldList: Array<{ field: Field; index: number }> = [];
  showEntityConfigModal = false;
  useLombok = false;
  lombokSettings: LombokSettings = this.createDefaultLombokSettings();
  generalSettings: GeneralSettings = this.createDefaultGeneralSettings();

  fields: Field[] = [
    {
      type: 'Long',
      name: 'id',
      primaryKey: true,
      validations: [{ key: '', searchTerm: '' }]
    }
  ];

  private tempFields: Field[] = [];

  fieldTypes = [
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
    'Enum',
    'byte[]'
  ];

  private createDefaultLombokSettings(): LombokSettings {
    return {
      generateBuilder: false,
      generateToString: false,
      generateEqualsAndHashCode: false
    };
  }

  private createDefaultGeneralSettings(): GeneralSettings {
    return {
      softDelete: false,
      auditing: false,
      makeImmutable: false,
      naturalIdCache: false
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        if (this.editEntity) {
          this.loadEntityData(this.editEntity);
        } else {
          this.resetForm();
        }
      } else {
        this.tempFields = [];
      }
    }

    if (changes['editEntity'] && this.editEntity) {
      this.loadEntityData(this.editEntity);
    }
  }

  loadEntityData(entity: Entity): void {
    this.entityName = entity.name;
    this.mappedSuperclass = entity.mappedSuperclass;
    this.addRestEndpoints = entity.addRestEndpoints;
    this.fields = JSON.parse(JSON.stringify(entity.fields));
    this.useLombok = !!entity.useLombok;
    this.lombokSettings = {
      ...this.createDefaultLombokSettings(),
      ...(entity.lombokSettings || {})
    };
    this.generalSettings = {
      ...this.createDefaultGeneralSettings(),
      ...(entity.generalSettings || {})
    };
    this.nameError = '';
    this.submitted = false;
    this.setEntityNameError('');
    this.enforceSinglePrimary();
    this.updateFilteredFields();
  }

  resetForm(): void {
    this.entityName = '';
    this.mappedSuperclass = false;
    this.addRestEndpoints = false;
    this.useLombok = false;
    this.lombokSettings = this.createDefaultLombokSettings();
    this.generalSettings = this.createDefaultGeneralSettings();
    this.nameError = '';
    this.submitted = false;
    this.fields = [
      {
        type: 'Long',
        name: 'id',
        primaryKey: true,
        validations: [{ key: '', searchTerm: '' }]
      }
    ];
    this.tempFields = [];
    this.entityForm?.resetForm();
    this.enforceSinglePrimary();
    this.updateFilteredFields();
  }

  addField(): void {
    const newField: Field = {
      type: 'String',
      name: '',
      maxLength: 255,
      primaryKey: false,
      nameError: '',
      validations: [{ key: '', searchTerm: '' }]
    };
    this.fields.push(newField);
    const newIndex = this.fields.length - 1;
    this.openFieldConfig(newIndex);
  }

  onFieldNameChange(field: Field): void {
    if (field.nameError) {
      field.nameError = '';
      const index = this.fields.indexOf(field);
      if (index > -1) {
        this.fieldItems?.toArray()[index]?.setCustomError(null);
      }
    }
    this.updateFilteredFields();
    this.validateDuplicateFieldNames();
  }

  onEntityNameChange(): void {
    if (this.nameError) {
      this.setEntityNameError('');
    }
  }

  removeField(index: number): void {
    if (index > 0) {
      this.fields.splice(index, 1);
      this.updateFilteredFields();
    }
  }

  onFieldTypeChange(field: Field): void {
    if (field.type === 'String') {
      if (!field.maxLength) {
        field.maxLength = 255;
      }
    } else {
      delete field.maxLength;
    }
    this.updateFilteredFields();
  }

  validateEntityName(): boolean {
    const candidateName = (this.entityName || '').trim();
    if (!candidateName) {
      this.setEntityNameError('Entity name is required.');
      this.focusEntityName();
      return false;
    }

    const editingName = (this.editEntity?.name || '').trim().toLowerCase();
    const duplicateEntity = this.existingEntities.find(entity => {
      const name = (entity?.name || '').trim().toLowerCase();
      if (!name) {
        return false;
      }
      if (this.editEntity && name === editingName) {
        return false;
      }
      return name === candidateName.toLowerCase();
    });

    if (duplicateEntity) {
      this.setEntityNameError(`Entity "${this.entityName}" already exists.`);
      this.focusEntityName();
      return false;
    }

    this.setEntityNameError('');
    return true;
  }

  validateFieldName(field: Field, index: number): boolean {
    if (!field.name.trim()) {
      this.setFieldError(index, 'Field name is required.');
      return false;
    }

    const alphanumericPattern = /^[a-zA-Z0-9]+$/;
    if (!alphanumericPattern.test(field.name)) {
      this.setFieldError(index, 'Field name must be alphanumeric without spaces.');
      return false;
    }

    const duplicateField = this.fields.filter(
      f => f !== field && f.name.toLowerCase() === field.name.trim().toLowerCase()
    );

    if (duplicateField.length > 0) {
      this.setFieldError(index, `Field "${field.name}" already exists in this entity.`);
      return false;
    }

    this.setFieldError(index, '');
    return true;
  }

  validateAllFields(): boolean {
    let isValid = true;
    let firstInvalidIndex: number | null = null;
    for (let i = 0; i < this.fields.length; i++) {
      const field = this.fields[i];
      if (!this.validateFieldName(field, i)) {
        isValid = false;
        if (firstInvalidIndex === null) {
          firstInvalidIndex = i;
        }
      }
    }
    if (firstInvalidIndex !== null) {
      this.scrollToFieldCard(firstInvalidIndex);
      this.focusFieldName(firstInvalidIndex);
    }
    return isValid;
  }

  moveFieldUp(index: number): void {
    if (index > 1) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index - 1];
      this.fields[index - 1] = temp;
      this.updateFilteredFields();
    }
  }

  moveFieldDown(index: number): void {
    if (index < this.fields.length - 1 && index > 0) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index + 1];
      this.fields[index + 1] = temp;
      this.updateFilteredFields();
    }
  }

  onSave(): void {
    this.submitted = true;
    this.entityForm?.form.markAllAsTouched();
    this.entityNameModel?.control.markAsTouched();
    this.fieldItems?.forEach(item => item.markAsTouched());

    if (!this.validateEntityName()) {
      return;
    }

    if (!this.validateAllFields()) {
      return;
    }

    const entity = {
      name: this.entityName,
      mappedSuperclass: this.mappedSuperclass,
      addRestEndpoints: this.addRestEndpoints,
      fields: JSON.parse(JSON.stringify(this.fields)),
      useLombok: this.useLombok,
      lombokSettings: { ...this.lombokSettings },
      generalSettings: { ...this.generalSettings }
    };

    this.save.emit(entity);
    this.tempFields = [];
    this.resetForm();
  }

  onCancel(): void {
    this.tempFields = [];
    this.resetForm();
    this.cancel.emit();
  }

  onMappedSuperclassChange(): void {
    this.addRestEndpoints = false;
  }

  private focusEntityName(): void {
    setTimeout(() => {
      this.entityNameInput?.nativeElement.focus();
      this.entityNameInput?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }

  private focusFieldName(index: number): void {
    const fieldArray = this.fieldItems?.toArray() || [];
    const fieldItem = fieldArray[index];
    if (fieldItem) {
      setTimeout(() => fieldItem.focusNameInput());
    }
  }

  private setEntityNameError(message: string): void {
    this.nameError = message;
    if (this.entityNameModel?.control) {
      const currentErrors = { ...(this.entityNameModel.control.errors || {}) };
      if (message) {
        currentErrors['custom'] = true;
      } else {
        delete currentErrors['custom'];
      }
      const finalErrors = Object.keys(currentErrors).length ? currentErrors : null;
      this.entityNameModel.control.setErrors(finalErrors);
      this.entityNameModel.control.updateValueAndValidity();
    }
  }

  private setFieldError(index: number, message: string): void {
    const field = this.fields[index];
    if (!field) {
      return;
    }
    field.nameError = message;
    const item = this.fieldItems?.toArray()[index];
    item?.setCustomError(message || null);
  }

  onPrimaryChange(field: Field): void {
    const selectedIndex = this.fields.indexOf(field);
    if (selectedIndex === -1) {
      return;
    }
    if (field.primaryKey) {
      this.enforceSinglePrimary(selectedIndex);
    }
  }

  onFieldChange(field: Field): void {
    const index = this.fields.indexOf(field);
    if (index > -1 && field.nameError) {
      this.setFieldError(index, '');
    }
    this.updateFilteredFields();
    this.validateDuplicateFieldNames();
  }

  private enforceSinglePrimary(preferredIndex?: number): void {
    let primaryIndex = preferredIndex ?? this.fields.findIndex(f => f.primaryKey);
    if (primaryIndex === -1 && this.fields.length > 0) {
      return;
    }
    this.fields.forEach((f, idx) => {
      f.primaryKey = idx === primaryIndex;
    });
    this.updateFilteredFields();
  }

  get primarySelected(): boolean {
    return this.fields.some(f => f.primaryKey);
  }

  openEntityConfig(): void {
    this.showEntityConfigModal = true;
  }

  onEntityConfigSave(event: { useLombok: boolean; lombokSettings: LombokSettings; generalSettings: GeneralSettings }): void {
    this.useLombok = event.useLombok;
    this.lombokSettings = { ...event.lombokSettings };
    this.generalSettings = { ...event.generalSettings };
    this.showEntityConfigModal = false;
  }

  onEntityConfigCancel(): void {
    this.showEntityConfigModal = false;
  }

  openFieldConfig(index: number): void {
    this.fieldConfigIndex = index;
    this.showFieldConfigModal = true;
  }

  closeFieldConfig(): void {
    if (this.fieldConfigIndex !== null) {
      const field = this.fields[this.fieldConfigIndex];
      this.trimEmptyValidations(field);
      if (!this.isFieldValid(field)) {
        this.removeField(this.fieldConfigIndex);
      }
    }
    this.fieldConfigIndex = null;
    this.showFieldConfigModal = false;
  }

  onFieldSearch(event: SearchSortEvent): void {
    this.fieldSearchTerm = (event.searchTerm || '').toLowerCase();
    this.fieldSortSelection = event.sortOption;
    this.updateFilteredFields();
  }

  trackField(_: number, item: { field: Field; index: number }): string {
    return `${item.index}-${item.field.name}-${item.field.type}`;
  }

  private updateFilteredFields(): void {
    const term = this.fieldSearchTerm?.trim().toLowerCase();
    const list = this.fields.map((field, index) => ({ field, index }));
    if (!term) {
      this.filteredFieldList = this.sortFields(list);
      return;
    }
    const filtered = list.filter(({ field }) => {
      return (
        (field.name && field.name.toLowerCase().includes(term)) ||
        (field.type && field.type.toLowerCase().includes(term))
      );
    });
    this.filteredFieldList = this.sortFields(filtered);
  }

  private sortFields(list: Array<{ field: Field; index: number }>): Array<{ field: Field; index: number }> {
    if (!this.fieldSortSelection) {
      return list;
    }
    const { property, direction } = this.fieldSortSelection;
    return [...list].sort((a, b) => {
      const aVal = (a.field as any)[property] || '';
      const bVal = (b.field as any)[property] || '';
      const compare = String(aVal).localeCompare(String(bVal));
      return direction === 'asc' ? compare : -compare;
    });
  }

  private trimEmptyValidations(field: Field | undefined): void {
    if (!field || !field.validations) {
      return;
    }
    field.validations = field.validations.filter(v => !!(v.key && v.key.trim()));
  }

  private isFieldValid(field: Field | undefined): boolean {
    if (!field) return false;
    const hasName = !!(field.name && field.name.trim());
    const noError = !field.nameError;
    return hasName && noError;
  }

  private validateDuplicateFieldNames(): void {
    const nameMap = new Map<string, number[]>();
    this.fields.forEach((field, idx) => {
      const key = (field.name || '').trim().toLowerCase();
      if (!key) {
        return;
      }
      const list = nameMap.get(key) || [];
      list.push(idx);
      nameMap.set(key, list);
    });

    // Clear existing duplicate errors first
    this.fields.forEach((field, idx) => {
      if (field.nameError && field.nameError.includes('already exists in this entity.')) {
        this.setFieldError(idx, '');
      }
    });

    nameMap.forEach(indices => {
      if (indices.length > 1) {
        indices.forEach(i => {
          const f = this.fields[i];
          this.setFieldError(i, `Field "${f.name}" already exists in this entity.`);
        });
      }
    });
  }

  private scrollToFieldCard(fieldIndex: number): void {
    const card = this.fieldCards?.find(
      el => el.nativeElement.getAttribute('data-field-index') === String(fieldIndex)
    );
    if (card) {
      card.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }
}
