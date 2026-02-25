import { CommonModule } from '@angular/common';
import { Component, Input, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ModalComponent } from '../../../../../components/modal/modal.component';
import { AddDataObjectComponent } from '../../add-data-object/add-data-object.component';
import { FieldTypeSelectComponent } from '../../field-type-select/field-type-select.component';
import { ENTITY_FIELD_TYPE_OPTIONS } from '../../../constants/backend-field-types';

@Component({
  selector: 'app-rest-config-request-response-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatSelectModule,
    MatRadioModule,
    MatButtonModule,
    ModalComponent,
    AddDataObjectComponent,
    FieldTypeSelectComponent
  ],
  templateUrl: './request-response-config.component.html',
  styleUrls: ['./request-response-config.component.css']
})
export class RequestResponseConfigComponent {
  readonly responseOperationOrder: Array<'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete'> =
    ['create', 'get', 'list', 'delete', 'bulkInsert', 'bulkDelete'];
  @Input({ required: true }) draft: any;
  @Input() entityFieldTypeOptions: string[] = [];
  @Input() availableModels: Array<{ name?: string }> = [];
  @Input() dtoObjects: Array<{
    name?: string;
    dtoType?: 'request' | 'response';
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
    fields?: any[];
  }> = [];
  @Input() enumTypes: string[] = [];

  @ViewChild('dtoConfigComponent') dtoConfigComponent?: AddDataObjectComponent;

  showDtoConfigModal = false;
  dtoConfigTarget: 'list' | 'create' | 'delete' | 'response' = 'create';
  responseTargetOperation: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete' | null = null;
  dtoEditDataObject: {
    name: string;
    dtoType?: 'request' | 'response';
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
    fields: any[];
  } | null = null;
  private previousDtoName = '';

  get requestDtoOptions(): string[] {
    const fromConfiguredDtos = (this.dtoObjects ?? [])
      .filter((item) => {
        const dtoType = String(item?.dtoType ?? '').trim().toLowerCase();
        return !dtoType || dtoType === 'request';
      })
      .map((item) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return Array.from(new Set(fromConfiguredDtos));
  }

  get responseDtoOptions(): string[] {
    const fromConfiguredDtos = (this.dtoObjects ?? [])
      .filter((item) => String(item?.dtoType ?? '').trim().toLowerCase() === 'response')
      .map((item) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return Array.from(new Set(fromConfiguredDtos));
  }

  get allDtoOptions(): string[] {
    const fromConfiguredDtos = (this.dtoObjects ?? [])
      .map((item) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return Array.from(new Set(fromConfiguredDtos));
  }

  get listResponseOptions(): string[] {
    const typedList = this.idTypeOptions.map((item) => `List<${item}>`);
    const dtoList = this.responseDtoOptions.map((item) => `List<${item}>`);
    return Array.from(new Set([...typedList, ...dtoList]));
  }

  get booleanOptions(): string[] {
    return ['true', 'false'];
  }

  get selectedResponseOperations(): Array<'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete'> {
    return this.responseOperationOrder.filter((key) => {
      if (key === 'bulkInsert') {
        return Boolean(this.draft?.methods?.bulkInsert);
      }
      if (key === 'bulkDelete') {
        return Boolean(this.draft?.methods?.bulkDelete);
      }
      return Boolean(this.draft?.methods?.[key]);
    });
  }

  get idTypeOptions(): string[] {
    const options = Array.isArray(this.entityFieldTypeOptions) ? this.entityFieldTypeOptions : [];
    const cleaned = options.map((item) => String(item ?? '').trim()).filter(Boolean);
    return cleaned.length ? Array.from(new Set(cleaned)) : ENTITY_FIELD_TYPE_OPTIONS;
  }

  openDtoConfig(
    target: 'list' | 'create' | 'delete' | 'response',
    responseOperation?: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete'
  ): void {
    this.dtoConfigTarget = target;
    this.responseTargetOperation = target === 'response' ? (responseOperation ?? null) : null;
    const selectedName = target === 'response'
      ? this.getResponseEndpointDtoName(this.responseTargetOperation)
      : String(this.draft?.requestResponse?.request?.[target]?.dtoName ?? '').trim();
    this.previousDtoName = selectedName;
    const found = (this.dtoObjects ?? []).find((item) => String(item?.name ?? '').trim() === selectedName);
    if (found) {
      this.dtoEditDataObject = JSON.parse(JSON.stringify({
        name: String(found.name ?? '').trim(),
        dtoType: found.dtoType ?? (target === 'response' ? 'response' : 'request'),
        responseWrapper: found.responseWrapper ?? 'STANDARD_ENVELOPE',
        enableFieldProjection: Boolean(found.enableFieldProjection ?? true),
        includeHateoasLinks: Boolean(found.includeHateoasLinks ?? true),
        fields: Array.isArray(found.fields) ? found.fields : []
      }));
    } else {
      this.dtoEditDataObject = {
        name: '',
        dtoType: target === 'response' ? 'response' : 'request',
        responseWrapper: 'STANDARD_ENVELOPE',
        enableFieldProjection: true,
        includeHateoasLinks: true,
        fields: []
      };
    }
    this.showDtoConfigModal = true;
  }

  closeDtoConfig(): void {
    this.showDtoConfigModal = false;
    this.dtoEditDataObject = null;
    this.responseTargetOperation = null;
  }

  saveDtoConfig(): void {
    this.dtoConfigComponent?.onSave();
  }

  onDtoConfigSave(dto: {
    name: string;
    dtoType?: 'request' | 'response';
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
    fields: any[];
  }): void {
    const name = String(dto?.name ?? '').trim();
    if (!name) {
      return;
    }
    const list = Array.isArray(this.dtoObjects) ? this.dtoObjects : [];
    const existingIndex = list.findIndex((item) => String(item?.name ?? '').trim() === this.previousDtoName);
    if (existingIndex >= 0) {
      list[existingIndex] = { ...dto, name };
    } else {
      list.push({ ...dto, name });
    }
    if (this.dtoConfigTarget === 'response') {
      const responseKey = this.responseTargetOperation;
      if (responseKey) {
        this.ensureResponseEndpointDtos();
        this.draft.requestResponse.response.endpointDtos[responseKey] = name;
      }
      this.applyResponseSettingsFromDto(dto);
    } else {
      this.draft.requestResponse.request[this.dtoConfigTarget].dtoName = name;
      if (this.dtoConfigTarget === 'create' && !this.draft.requestResponse.request.bulkInsertType) {
        this.draft.requestResponse.request.bulkInsertType = `List<${name}>`;
      }
      if (this.dtoConfigTarget === 'create' && !this.draft.requestResponse.request.bulkUpdateType) {
        this.draft.requestResponse.request.bulkUpdateType = `List<${name}>`;
      }
    }
    this.closeDtoConfig();
  }

  onResponseDtoSelectionChange(selectedName: string): void {
    const name = String(selectedName ?? '').trim();
    const responseKey = this.responseTargetOperation;
    if (responseKey) {
      this.ensureResponseEndpointDtos();
      this.draft.requestResponse.response.endpointDtos[responseKey] = name;
    }
    const selected = (this.dtoObjects ?? []).find((item) => String(item?.name ?? '').trim() === name);
    if (selected) {
      this.applyResponseSettingsFromDto(selected);
    }
  }

  getResponseOperationLabel(operation: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete'): string {
    if (operation === 'get') {
      return 'Get By ID';
    }
    if (operation === 'delete') {
      return 'Delete';
    }
    if (operation === 'bulkInsert') {
      return 'Bulk Insert';
    }
    if (operation === 'bulkDelete') {
      return 'Bulk Delete';
    }
    if (operation === 'list') {
      return 'List';
    }
    return 'Create';
  }

  getResponseEndpointDtoName(operation: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete' | null): string {
    if (!operation) {
      return '';
    }
    this.ensureResponseEndpointDtos();
    return String(this.draft?.requestResponse?.response?.endpointDtos?.[operation] ?? '').trim();
  }

  setResponseEndpointDtoName(
    operation: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete',
    value: string
  ): void {
    this.ensureResponseEndpointDtos();
    this.draft.requestResponse.response.endpointDtos[operation] = String(value ?? '').trim();
    const selected = (this.dtoObjects ?? []).find((item) => String(item?.name ?? '').trim() === String(value ?? '').trim());
    if (selected) {
      this.applyResponseSettingsFromDto(selected);
    }
  }

  private ensureResponseEndpointDtos(): void {
    if (!this.draft?.requestResponse?.response) {
      return;
    }
    if (!this.draft.requestResponse.response.endpointDtos || typeof this.draft.requestResponse.response.endpointDtos !== 'object') {
      this.draft.requestResponse.response.endpointDtos = {};
    }
    this.responseOperationOrder.forEach((operation) => {
      if (typeof this.draft.requestResponse.response.endpointDtos[operation] !== 'string') {
        this.draft.requestResponse.response.endpointDtos[operation] = '';
      }
    });
  }

  getResponseOptions(operation: 'create' | 'get' | 'list' | 'delete' | 'bulkInsert' | 'bulkDelete'): string[] {
    if (operation === 'get') {
      return Array.from(new Set([...this.idTypeOptions, ...this.allDtoOptions]));
    }
    if (operation === 'list') {
      return this.listResponseOptions;
    }
    if (operation === 'create') {
      return this.idTypeOptions;
    }
    if (operation === 'delete' || operation === 'bulkInsert' || operation === 'bulkDelete') {
      return this.booleanOptions;
    }
    return this.responseDtoOptions;
  }

  private applyResponseSettingsFromDto(dto: {
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
  }): void {
    this.draft.requestResponse.response.responseWrapper = dto.responseWrapper ?? 'STANDARD_ENVELOPE';
    this.draft.requestResponse.response.enableFieldProjection = Boolean(dto.enableFieldProjection ?? true);
    this.draft.requestResponse.response.includeHateoasLinks = Boolean(dto.includeHateoasLinks ?? true);
  }
}
