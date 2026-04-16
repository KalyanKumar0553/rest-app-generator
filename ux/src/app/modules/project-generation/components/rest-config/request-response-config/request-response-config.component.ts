import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { stableArray, emptyCache } from '../../../../../utils/stable-reference';
import { FormsModule } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
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
export class RequestResponseConfigComponent implements OnChanges {
  readonly requiredFieldErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: (control) => Boolean(this.shouldValidateRequiredFields && control?.invalid)
  };

  readonly responseOperationOrder: Array<'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'> =
    ['create', 'get', 'list', 'patch', 'delete', 'bulkInsert', 'bulkUpdate', 'bulkDelete'];
  @Input({ required: true }) draft: any;
  @Input() showRequiredErrors = false;
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
  @Input() javaVersion = '21';
  @Input() enumTypes: string[] = [];

  @ViewChild('dtoConfigComponent') dtoConfigComponent?: AddDataObjectComponent;

  showDtoConfigModal = false;
  dtoConfigTarget: 'list' | 'create' | 'patch' | 'delete' | 'response' = 'create';
  responseTargetOperation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete' | null = null;
  dtoEditDataObject: {
    name: string;
    dtoType?: 'request' | 'response';
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
    fields: any[];
  } | null = null;
  private previousDtoName = '';

  get mappedEntityName(): string {
    return String(this.draft?.mappedEntityName ?? '').trim();
  }

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

  private _createRequestOptionsCache = emptyCache<string[]>();
  get createRequestOptions(): string[] {
    const mapped = this.mappedEntityName;
    return stableArray(Array.from(new Set([mapped, ...this.requestDtoOptions].filter(Boolean))), this._createRequestOptionsCache);
  }

  get listRequestOptions(): string[] {
    return this.createRequestOptions;
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
    const mappedEntityLists = this.createRequestOptions.map((item) => `List<${item}>`);
    const typedList = this.idTypeOptions.map((item) => `List<${item}>`);
    const dtoList = this.responseDtoOptions.map((item) => `List<${item}>`);
    return Array.from(new Set([...mappedEntityLists, ...typedList, ...dtoList]));
  }

  get booleanOptions(): string[] {
    return ['true', 'false'];
  }

  private _selectedResponseOpsCache = emptyCache<Array<'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'>>();
  get selectedResponseOperations(): Array<'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'> {
    return stableArray(
      this.responseOperationOrder.filter((key) => {
        if (key === 'bulkInsert') {
          return Boolean(this.draft?.methods?.bulkInsert);
        }
        if (key === 'bulkUpdate') {
          return Boolean(this.draft?.methods?.bulkUpdate);
        }
        if (key === 'bulkDelete') {
          return Boolean(this.draft?.methods?.bulkDelete);
        }
        return Boolean(this.draft?.methods?.[key]);
      }),
      this._selectedResponseOpsCache
    );
  }

  private _idTypeOptionsCache = emptyCache<string[]>();
  get idTypeOptions(): string[] {
    const options = Array.isArray(this.entityFieldTypeOptions) ? this.entityFieldTypeOptions : [];
    const cleaned = options.map((item) => String(item ?? '').trim()).filter(Boolean);
    return stableArray(cleaned.length ? Array.from(new Set(cleaned)) : ENTITY_FIELD_TYPE_OPTIONS, this._idTypeOptionsCache);
  }

  get shouldValidateRequiredFields(): boolean {
    return this.showRequiredErrors && !Boolean(this.draft?.mapToEntity);
  }

  ngOnChanges(_changes: SimpleChanges): void {
    this.ensureRequestDefaults();
    this.ensureResponseDefaults();
  }

  onResponseTypeChange(value: string): void {
    if (String(value ?? '') === 'CUSTOM_WRAPPER') {
      this.ensureResponseDefaults();
    }
  }

  openDtoConfig(
    target: 'list' | 'create' | 'patch' | 'delete' | 'response',
    responseOperation?: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'
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

  getResponseOperationLabel(operation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'): string {
    if (operation === 'get') {
      return 'Get By Key';
    }
    if (operation === 'delete') {
      return 'Delete';
    }
    if (operation === 'bulkInsert') {
      return 'Bulk Insert';
    }
    if (operation === 'bulkUpdate') {
      return 'Bulk Update';
    }
    if (operation === 'bulkDelete') {
      return 'Bulk Delete';
    }
    if (operation === 'patch') {
      return 'Patch';
    }
    if (operation === 'list') {
      return 'List';
    }
    return 'Create';
  }

  getResponseEndpointDtoName(operation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete' | null): string {
    if (!operation) {
      return '';
    }
    this.ensureResponseEndpointDtos();
    return String(this.draft?.requestResponse?.response?.endpointDtos?.[operation] ?? '').trim();
  }

  setResponseEndpointDtoName(
    operation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete',
    value: string
  ): void {
    this.ensureResponseEndpointDtos();
    this.draft.requestResponse.response.endpointDtos[operation] = String(value ?? '').trim();
    const selected = (this.dtoObjects ?? []).find((item) => String(item?.name ?? '').trim() === String(value ?? '').trim());
    if (selected) {
      this.applyResponseSettingsFromDto(selected);
    }
  }

  isRequestFieldInvalid(field: 'create' | 'list' | 'patch' | 'delete' | 'getByIdType' | 'deleteByIdType' | 'bulkInsertType' | 'bulkUpdateType' | 'bulkDeleteType'): boolean {
    if (!this.shouldValidateRequiredFields) {
      return false;
    }
    const methods = this.draft?.methods ?? {};
    if (field === 'create' && !methods.create) {
      return false;
    }
    if (field === 'list' && !methods.list) {
      return false;
    }
    if (field === 'patch' && !methods.patch) {
      return false;
    }
    if ((field === 'delete' || field === 'deleteByIdType') && !methods.delete) {
      return false;
    }
    if (field === 'getByIdType' && !methods.get) {
      return false;
    }
    if (field === 'bulkInsertType' && !methods.bulkInsert) {
      return false;
    }
    if (field === 'bulkUpdateType' && !methods.bulkUpdate) {
      return false;
    }
    if (field === 'bulkDeleteType' && !methods.bulkDelete) {
      return false;
    }

    if (field === 'create' || field === 'list' || field === 'patch' || field === 'delete') {
      return !this.hasValue(this.draft?.requestResponse?.request?.[field]?.dtoName);
    }
    return !this.hasValue(this.draft?.requestResponse?.request?.[field]);
  }

  isResponseEndpointInvalid(operation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'): boolean {
    if (!this.shouldValidateRequiredFields) {
      return false;
    }
    if (this.draft?.requestResponse?.response?.responseType !== 'CUSTOM_WRAPPER') {
      return false;
    }
    if (!this.draft?.methods?.[operation]) {
      return false;
    }
    return !this.hasValue(this.getResponseEndpointDtoName(operation));
  }

  private hasValue(value: unknown): boolean {
    return String(value ?? '').trim().length > 0;
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

  getResponseOptions(operation: 'create' | 'get' | 'list' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete'): string[] {
    if (operation === 'get') {
      return Array.from(new Set([...this.createRequestOptions, ...this.allDtoOptions]));
    }
    if (operation === 'list') {
      return this.listResponseOptions;
    }
    if (operation === 'patch') {
      return Array.from(new Set([...this.createRequestOptions, ...this.responseDtoOptions]));
    }
    if (operation === 'create') {
      return this.idTypeOptions;
    }
    if (operation === 'delete') {
      return this.idTypeOptions;
    }
    if (operation === 'bulkInsert' || operation === 'bulkDelete') {
      return this.idTypeOptions.map((item) => `List<${item}>`);
    }
    if (operation === 'bulkUpdate') {
      return this.listResponseOptions;
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

  private ensureRequestDefaults(): void {
    const request = this.draft?.requestResponse?.request;
    if (!request) {
      return;
    }

    const mapped = this.mappedEntityName;
    const shouldReplaceLegacyValue = (value: unknown): boolean => {
      const normalized = String(value ?? '').trim().toLowerCase();
      return !normalized || normalized === 'request' || normalized === 'createrequest' || normalized === 'updaterequest' || normalized === 'patchrequest';
    };

    if (this.draft?.methods?.create && shouldReplaceLegacyValue(request?.create?.dtoName) && mapped) {
      request.create.dtoName = mapped;
    }

    if (this.draft?.methods?.list && shouldReplaceLegacyValue(request?.list?.dtoName) && mapped) {
      request.list.dtoName = mapped;
    }

    if (this.draft?.methods?.patch && shouldReplaceLegacyValue(request?.patch?.dtoName) && mapped) {
      request.patch.dtoName = mapped;
    }
  }

  private ensureResponseDefaults(): void {
    if (this.draft?.requestResponse?.response?.responseType !== 'CUSTOM_WRAPPER') {
      return;
    }
    this.ensureResponseEndpointDtos();
    const keyType = this.resolveKeyType();
    const entity = this.mappedEntityName;
    if (!entity) {
      return;
    }

    const defaults: Record<string, string> = {
      create: keyType,
      get: entity,
      list: `List<${entity}>`,
      patch: entity,
      delete: keyType,
      bulkInsert: `List<${keyType}>`,
      bulkUpdate: `List<${entity}>`,
      bulkDelete: `List<${keyType}>`
    };

    this.responseOperationOrder.forEach((operation) => {
      if (!this.draft?.methods?.[operation]) {
        return;
      }
      const current = String(this.draft.requestResponse.response.endpointDtos?.[operation] ?? '').trim();
      if (!current) {
        this.draft.requestResponse.response.endpointDtos[operation] = defaults[operation] ?? '';
      }
    });
  }

  private resolveKeyType(): string {
    const raw = String(this.draft?.pathVariableType ?? '').trim().toUpperCase();
    if (raw === 'LONG') {
      return 'Long';
    }
    if (raw === 'STRING') {
      return 'String';
    }
    return 'UUID';
  }
}
