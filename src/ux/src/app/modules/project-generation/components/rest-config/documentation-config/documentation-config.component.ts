import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { HelpPopoverComponent } from '../../../../../components/help-popover/help-popover.component';
import { ModalComponent } from '../../../../../components/modal/modal.component';
import { EditDocumentationOperationComponent } from './edit-documentation-operation/edit-documentation-operation.component';

type EndpointKey = 'list' | 'get' | 'create' | 'update' | 'patch' | 'delete' | 'bulkInsert' | 'bulkUpdate' | 'bulkDelete';

@Component({
  selector: 'app-rest-config-documentation-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    HelpPopoverComponent,
    ModalComponent,
    EditDocumentationOperationComponent
  ],
  templateUrl: './documentation-config.component.html',
  styleUrls: ['./documentation-config.component.css']
})
export class DocumentationConfigComponent implements OnChanges {
  @Input({ required: true }) draft: any;
  @ViewChild(EditDocumentationOperationComponent) editOperationComponent?: EditDocumentationOperationComponent;

  readonly tagPreviewLimit = 2;

  showEditModal = false;
  activeEndpointKey: EndpointKey | null = null;
  activeEndpointLabel = '';
  modalDraft: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; } = {
    description: '',
    group: '',
    descriptionTags: [],
    deprecated: false
  };

  readonly endpointDefinitions: Array<{ key: EndpointKey; label: string; }> = [
    { key: 'create', label: 'Create' },
    { key: 'get', label: 'Get By Key' },
    { key: 'list', label: 'List' },
    { key: 'patch', label: 'Patch' },
    { key: 'delete', label: 'Delete' },
    { key: 'bulkInsert', label: 'Bulk Insert' },
    { key: 'bulkUpdate', label: 'Bulk Update' },
    { key: 'bulkDelete', label: 'Bulk Delete' }
  ];

  ngOnChanges(): void {
    this.ensureDocumentationDefaults();
  }

  get selectedEndpoints(): Array<{ key: EndpointKey; label: string; }> {
    return this.endpointDefinitions.filter((endpoint) => Boolean(this.draft?.methods?.[endpoint.key]));
  }

  getDescriptionTags(endpoint: EndpointKey): string[] {
    return this.getDocumentationEntry(endpoint).descriptionTags;
  }

  getVisibleTags(endpoint: EndpointKey): string[] {
    return this.getDescriptionTags(endpoint).slice(0, this.tagPreviewLimit);
  }

  hasOverflowTags(endpoint: EndpointKey): boolean {
    return this.getDescriptionTags(endpoint).length > this.tagPreviewLimit;
  }

  getOverflowCount(endpoint: EndpointKey): number {
    return Math.max(0, this.getDescriptionTags(endpoint).length - this.tagPreviewLimit);
  }

  getOverflowTags(endpoint: EndpointKey): string[] {
    return this.getDescriptionTags(endpoint).slice(this.tagPreviewLimit);
  }

  getDescription(endpoint: EndpointKey): string {
    return String(this.getDocumentationEntry(endpoint).description ?? '').trim();
  }

  getDeprecatedFlag(endpoint: EndpointKey): boolean {
    return this.getDocumentationEntry(endpoint).deprecated;
  }

  openEditModal(endpoint: EndpointKey): void {
    const def = this.endpointDefinitions.find((item) => item.key === endpoint);
    const entry = this.getDocumentationEntry(endpoint);

    this.activeEndpointKey = endpoint;
    this.activeEndpointLabel = def?.label ?? 'Endpoint';
    this.modalDraft = {
      description: String(entry.description ?? '').trim(),
      group: String(entry.group ?? '').trim(),
      descriptionTags: [...entry.descriptionTags],
      deprecated: Boolean(entry.deprecated)
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.activeEndpointKey = null;
    this.activeEndpointLabel = '';
    this.modalDraft = {
      description: '',
      group: '',
      descriptionTags: [],
      deprecated: false
    };
  }

  saveEditModal(): void {
    if (!this.editOperationComponent?.validate()) {
      return;
    }

    if (!this.activeEndpointKey) {
      return;
    }

    const entry = this.getDocumentationEntry(this.activeEndpointKey);
    entry.description = String(this.modalDraft.description ?? '').trim();
    entry.group = String(this.modalDraft.group ?? '').trim();
    entry.deprecated = Boolean(this.modalDraft.deprecated);
    entry.descriptionTags = Array.from(
      new Set(
        (Array.isArray(this.modalDraft.descriptionTags) ? this.modalDraft.descriptionTags : [])
          .map((item) => String(item ?? '').trim())
          .filter(Boolean)
      )
    );

    this.closeEditModal();
  }

  getRequestObjectLabel(endpoint: EndpointKey): string {
    if (endpoint === 'create') {
      return String(this.draft?.requestResponse?.request?.create?.dtoName ?? '').trim() || 'None';
    }
    if (endpoint === 'update') {
      return String(this.draft?.requestResponse?.request?.update?.dtoName ?? '').trim() || 'None';
    }
    if (endpoint === 'patch') {
      return this.draft?.requestResponse?.request?.patch?.mode === 'JSON_PATCH' ? 'JSON Patch' : 'JSON Merge';
    }
    if (endpoint === 'bulkInsert') {
      const createDto = String(this.draft?.requestResponse?.request?.create?.dtoName ?? '').trim();
      return createDto ? `List<${createDto}>` : 'None';
    }
    if (endpoint === 'bulkUpdate') {
      const updateDto = String(this.draft?.requestResponse?.request?.update?.dtoName ?? '').trim();
      const createDto = String(this.draft?.requestResponse?.request?.create?.dtoName ?? '').trim();
      if (updateDto) {
        return `List<${updateDto}>`;
      }
      return createDto ? `List<${createDto}>` : 'None';
    }
    if (endpoint === 'delete') {
      const deleteDto = String(this.draft?.requestResponse?.request?.delete?.dtoName ?? '').trim();
      const deleteIdType = String(this.draft?.requestResponse?.request?.deleteByIdType ?? '').trim();
      return deleteDto || deleteIdType || 'None';
    }
    if (endpoint === 'get') {
      const getIdType = String(this.draft?.requestResponse?.request?.getByIdType ?? '').trim();
      return getIdType || 'None';
    }
    return 'None';
  }

  getResponseObjectLabel(endpoint: EndpointKey): string {
    const responseType = this.draft?.requestResponse?.response?.responseType;
    if (responseType === 'DTO_DIRECT') {
      return 'DTO';
    }
    if (responseType === 'CUSTOM_WRAPPER') {
      return String(this.draft?.requestResponse?.response?.endpointDtos?.[endpoint] ?? '').trim()
        || String(this.draft?.requestResponse?.response?.dtoName ?? '').trim()
        || 'Custom Wrapper';
    }
    return 'ResponseEntity<T>';
  }

  private ensureDocumentationDefaults(): void {
    if (!this.draft) {
      return;
    }

    if (!this.draft.documentation) {
      this.draft.documentation = { endpoints: {} };
    }
    if (!this.draft.documentation.endpoints) {
      this.draft.documentation.endpoints = {};
    }

    this.endpointDefinitions.forEach((endpoint) => {
      const current = this.draft.documentation.endpoints[endpoint.key];
      const defaults = this.getDefaultEntry(endpoint.key, endpoint.label);
      if (!current || typeof current !== 'object') {
        this.draft.documentation.endpoints[endpoint.key] = defaults;
        return;
      }
      const tags = Array.isArray(current.descriptionTags)
        ? current.descriptionTags.map((item: unknown) => String(item ?? '').trim()).filter(Boolean)
        : [];
      this.draft.documentation.endpoints[endpoint.key] = {
        description: String(current.description ?? '').trim() || defaults.description,
        group: String(current.group ?? '').trim() || defaults.group,
        descriptionTags: Array.from(new Set(tags.length ? tags : defaults.descriptionTags)),
        deprecated: Boolean(current.deprecated)
      };
    });
  }

  private getDefaultEntry(endpoint: EndpointKey, endpointLabel: string): { description: string; group: string; descriptionTags: string[]; deprecated: boolean } {
    const resourceName = String(this.draft?.resourceName ?? this.draft?.mappedEntityName ?? '').trim() || 'API';
    return {
      description: `${endpointLabel} operation for ${resourceName}`,
      group: `${resourceName} Group`,
      descriptionTags: [endpoint],
      deprecated: false
    };
  }

  private getDocumentationEntry(endpoint: EndpointKey): { description: string; group: string; descriptionTags: string[]; deprecated: boolean; } {
    this.ensureDocumentationDefaults();
    return this.draft.documentation.endpoints[endpoint] as { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
  }
}
