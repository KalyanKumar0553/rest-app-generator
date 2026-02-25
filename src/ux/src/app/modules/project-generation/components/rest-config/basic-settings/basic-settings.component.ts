import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-rest-config-basic-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatRadioModule,
    MatSelectModule,
    MatExpansionModule
  ],
  templateUrl: './basic-settings.component.html',
  styleUrls: ['./basic-settings.component.css']
})
export class BasicSettingsComponent implements OnChanges, AfterViewInit {
  @Input({ required: true }) draft: any;
  @Input() softDeleteEnabled = false;
  @Input() availableModels: Array<any> = [];
  @Input() showResourceNameErrors = false;
  @Input() resourceNameRequired = false;
  @Input() resourceNameDuplicate = false;
  @Input() basePathRequired = false;
  @Input() mapToEntityRequired = false;
  @Input() lockEntityMapping = false;
  @Output() resourceNameChange = new EventEmitter<void>();
  @Output() basePathChange = new EventEmitter<void>();
  @Output() mappingChange = new EventEmitter<void>();
  @ViewChild('resourceNameModel') resourceNameModel?: NgModel;
  @ViewChild('mappedEntityModel') mappedEntityModel?: NgModel;

  get availableEntityNames(): string[] {
    const currentMappedEntity = String(this.draft?.mappedEntityName ?? '').trim();
    const currentSpecName = String(this.draft?.resourceName ?? '').trim();

    const names = (Array.isArray(this.availableModels) ? this.availableModels : [])
      .filter((item) => {
        const entityName = String(item?.name ?? '').trim();
        if (!entityName) {
          return false;
        }

        const mappedSpecName = this.resolveMappedSpecName(item);
        const isAlreadyMapped = Boolean(item?.addRestEndpoints) || Boolean(mappedSpecName);

        if (!isAlreadyMapped) {
          return true;
        }

        if (entityName === currentMappedEntity) {
          return true;
        }

        return Boolean(currentSpecName && mappedSpecName === currentSpecName);
      })
      .map((item) => String(item?.name ?? '').trim())
      .filter(Boolean);

    return Array.from(new Set(names));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['showResourceNameErrors']
      || changes['resourceNameRequired']
      || changes['resourceNameDuplicate']
      || changes['mapToEntityRequired']
    ) {
      this.syncResourceNameErrorState();
      this.syncEntityMappingErrorState();
    }
  }

  ngAfterViewInit(): void {
    this.syncResourceNameErrorState();
    this.syncEntityMappingErrorState();
  }

  onResourceNameInput(): void {
    this.resourceNameChange.emit();
  }

  onApiVersioningToggle(enabled: boolean): void {
    if (enabled) {
      this.draft.apiVersioning.strategy = 'header';
    }
  }

  onMapToEntityToggle(enabled: boolean): void {
    this.draft.mapToEntity = Boolean(enabled);
    if (!enabled) {
      this.draft.mappedEntityName = '';
    } else if (!String(this.draft?.basePath ?? '').trim()) {
      const fallbackEntity = String(this.draft?.mappedEntityName ?? '').trim();
      if (fallbackEntity) {
        this.draft.basePath = this.defaultBasePath(fallbackEntity);
      }
    }
    this.mappingChange.emit();
    this.syncEntityMappingErrorState();
  }

  onMappedEntityChange(): void {
    if (!String(this.draft?.basePath ?? '').trim()) {
      const mappedName = String(this.draft?.mappedEntityName ?? '').trim();
      if (mappedName) {
        this.draft.basePath = this.defaultBasePath(mappedName);
        this.basePathChange.emit();
      }
    }
    this.mappingChange.emit();
    this.syncEntityMappingErrorState();
  }

  onBasePathInput(): void {
    this.basePathChange.emit();
  }

  private syncResourceNameErrorState(): void {
    setTimeout(() => {
      const control = this.resourceNameModel?.control;
      if (!control) {
        return;
      }

      const nextErrors = { ...(control.errors || {}) } as Record<string, unknown>;
      delete nextErrors['duplicateName'];
      delete nextErrors['requiredName'];

      if (this.showResourceNameErrors && this.resourceNameRequired) {
        nextErrors['requiredName'] = true;
      }
      if (this.showResourceNameErrors && !this.resourceNameRequired && this.resourceNameDuplicate) {
        nextErrors['duplicateName'] = true;
      }

      control.setErrors(Object.keys(nextErrors).length ? nextErrors : null);
      if (this.showResourceNameErrors) {
        control.markAsTouched();
        control.markAsDirty();
      }
    });
  }

  private syncEntityMappingErrorState(): void {
    setTimeout(() => {
      const control = this.mappedEntityModel?.control;
      if (!control) {
        return;
      }

      const nextErrors = { ...(control.errors || {}) } as Record<string, unknown>;
      delete nextErrors['requiredMappedEntity'];

      if (this.draft?.mapToEntity && this.mapToEntityRequired) {
        nextErrors['requiredMappedEntity'] = true;
      }

      control.setErrors(Object.keys(nextErrors).length ? nextErrors : null);
      if (this.mapToEntityRequired) {
        control.markAsTouched();
        control.markAsDirty();
      }
    });
  }

  private resolveMappedSpecName(entity: any): string {
    return String(
      entity?.['rest-spec-name']
      ?? entity?.restSpecName
      ?? entity?.restSpec
      ?? entity?.restConfig?.resourceName
      ?? ''
    ).trim();
  }

  private defaultBasePath(entityName: string): string {
    return `/${String(entityName ?? '').trim().toLowerCase()}`;
  }
}
