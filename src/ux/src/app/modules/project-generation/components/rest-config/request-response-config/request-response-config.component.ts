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
    AddDataObjectComponent
  ],
  templateUrl: './request-response-config.component.html',
  styleUrls: ['./request-response-config.component.css']
})
export class RequestResponseConfigComponent {
  @Input({ required: true }) draft: any;
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
  dtoConfigTarget: 'create' | 'update' | 'response' = 'create';
  dtoEditDataObject: {
    name: string;
    dtoType?: 'request' | 'response';
    responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
    enableFieldProjection?: boolean;
    includeHateoasLinks?: boolean;
    fields: any[];
  } | null = null;
  private previousDtoName = '';

  get dtoOptions(): string[] {
    const fromConfiguredDtos = (this.dtoObjects ?? [])
      .map((item) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return Array.from(new Set(fromConfiguredDtos));
  }

  get bulkDtoTypeOptions(): string[] {
    return this.dtoOptions.map((name) => `List<${name}>`);
  }

  get bulkDeleteTypeOptions(): string[] {
    return ['List<UUID>', 'List<Long>'];
  }

  openDtoConfig(target: 'create' | 'update' | 'response'): void {
    this.dtoConfigTarget = target;
    const selectedName = target === 'response'
      ? String(this.draft?.requestResponse?.response?.dtoName ?? '').trim()
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
      this.draft.requestResponse.response.dtoName = name;
      this.applyResponseSettingsFromDto(dto);
    } else {
      this.draft.requestResponse.request[this.dtoConfigTarget].dtoName = name;
      if (!this.draft.requestResponse.request.bulkInsertType) {
        this.draft.requestResponse.request.bulkInsertType = `List<${name}>`;
      }
      if (!this.draft.requestResponse.request.bulkUpdateType) {
        this.draft.requestResponse.request.bulkUpdateType = `List<${name}>`;
      }
    }
    this.closeDtoConfig();
  }

  onResponseDtoSelectionChange(selectedName: string): void {
    const name = String(selectedName ?? '').trim();
    this.draft.requestResponse.response.dtoName = name;
    const selected = (this.dtoObjects ?? []).find((item) => String(item?.name ?? '').trim() === name);
    if (selected) {
      this.applyResponseSettingsFromDto(selected);
    }
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
