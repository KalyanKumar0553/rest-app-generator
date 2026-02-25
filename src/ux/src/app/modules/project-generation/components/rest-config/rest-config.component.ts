import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnDestroy, Output, QueryList, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { Subscription } from 'rxjs';
import { BasicSettingsComponent } from './basic-settings/basic-settings.component';
import { EndpointConfigComponent } from './endpoint-config/endpoint-config.component';
import { RequestResponseConfigComponent } from './request-response-config/request-response-config.component';
import { DocumentationConfigComponent } from './documentation-config/documentation-config.component';
import { RestConfigOverflowSheetComponent } from './rest-config-overflow-sheet/rest-config-overflow-sheet.component';
import { ENTITY_FIELD_TYPE_OPTIONS } from '../../constants/backend-field-types';

export type ApiVersioningStrategy = 'header' | 'path';
export type PathVariableType = 'UUID' | 'LONG' | 'STRING';
export type DeletionMode = 'SOFT' | 'HARD';
export type PaginationMode = 'OFFSET' | 'CURSOR';
export type SortDirection = 'ASC' | 'DESC';
export type BulkUpdateMode = 'PUT' | 'PATCH';
export type OptimisticLockHandling = 'FAIL_ON_CONFLICT' | 'SKIP_CONFLICTS';
export type ValidationStrategy = 'VALIDATE_ALL_FIRST' | 'SKIP_DUPLICATES';
export type BatchFailureStrategy = 'STOP_ON_FIRST_ERROR' | 'CONTINUE_AND_REPORT_FAILURES';
export type RequestDtoMode = 'GENERATE_DTO' | 'NONE';
export type PatchRequestMode = 'JSON_MERGE_PATCH' | 'JSON_PATCH';
export type ResponseType = 'RESPONSE_ENTITY' | 'DTO_DIRECT' | 'CUSTOM_WRAPPER';
export type ResponseWrapper = 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';

export interface RestEndpointConfig {
  resourceName: string;
  basePath: string;
  mapToEntity: boolean;
  mappedEntityName: string;
  methods: {
    list: boolean;
    get: boolean;
    create: boolean;
    update: boolean;
    patch: boolean;
    delete: boolean;
    bulkInsert: boolean;
    bulkUpdate: boolean;
    bulkDelete: boolean;
  };
  apiVersioning: {
    enabled: boolean;
    strategy: ApiVersioningStrategy;
    headerName: string;
    defaultVersion: string;
  };
  pathVariableType: PathVariableType;
  deletion: {
    mode: DeletionMode;
    restoreEndpoint: boolean;
    includeDeletedParam: boolean;
  };
  hateoas: {
    enabled: boolean;
    selfLink: boolean;
    updateLink: boolean;
    deleteLink: boolean;
  };
  pagination: {
    enabled: boolean;
    mode: PaginationMode;
    sortField: string;
    sortDirection: SortDirection;
  };
  searchFiltering: {
    keywordSearch: boolean;
    jpaSpecification: boolean;
    searchableFields: string[];
  };
  batchOperations: {
    insert: {
      batchSize: number;
      enableAsyncMode: boolean;
    };
    update: {
      batchSize: number;
      updateMode: BulkUpdateMode;
      optimisticLockHandling: OptimisticLockHandling;
      validationStrategy: ValidationStrategy;
      enableAsyncMode: boolean;
      asyncProcessing: boolean;
    };
    bulkDelete: {
      deletionStrategy: DeletionMode;
      batchSize: number;
      failureStrategy: BatchFailureStrategy;
      enableAsyncMode: boolean;
      allowIncludeDeletedParam: boolean;
    };
  };
  requestResponse: {
    request: {
      list: { mode: RequestDtoMode; dtoName: string; };
      create: { mode: RequestDtoMode; dtoName: string; };
      delete: { mode: RequestDtoMode; dtoName: string; };
      update: { mode: RequestDtoMode; dtoName: string; };
      patch: { mode: PatchRequestMode; };
      getByIdType: string;
      deleteByIdType: string;
      bulkInsertType: string;
      bulkUpdateType: string;
      bulkDeleteType: string;
    };
    response: {
      responseType: ResponseType;
      dtoName: string;
      endpointDtos: {
        list: string;
        get: string;
        create: string;
        update: string;
        patch: string;
        delete: string;
        bulkInsert: string;
        bulkUpdate: string;
        bulkDelete: string;
      };
      responseWrapper: ResponseWrapper;
      enableFieldProjection: boolean;
      includeHateoasLinks: boolean;
    };
  };
  documentation: {
    endpoints: {
      list: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      get: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      create: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      update: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      patch: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      delete: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      bulkInsert: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      bulkUpdate: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
      bulkDelete: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };
    };
  };
}

const DEFAULT_REST_ENDPOINT_CONFIG: RestEndpointConfig = {
  resourceName: '',
  basePath: '',
  mapToEntity: false,
  mappedEntityName: '',
  methods: {
    list: true,
    get: true,
    create: true,
    update: false,
    patch: true,
    delete: true,
    bulkInsert: true,
    bulkUpdate: true,
    bulkDelete: true
  },
  apiVersioning: {
    enabled: false,
    strategy: 'header',
    headerName: 'X-API-VERSION',
    defaultVersion: '1'
  },
  pathVariableType: 'UUID',
  deletion: {
    mode: 'SOFT',
    restoreEndpoint: true,
    includeDeletedParam: true
  },
  hateoas: {
    enabled: true,
    selfLink: true,
    updateLink: true,
    deleteLink: true
  },
  pagination: {
    enabled: true,
    mode: 'OFFSET',
    sortField: 'createdAt',
    sortDirection: 'DESC'
  },
  searchFiltering: {
    keywordSearch: true,
    jpaSpecification: true,
    searchableFields: []
  },
  batchOperations: {
    insert: {
      batchSize: 500,
      enableAsyncMode: false
    },
    update: {
      batchSize: 500,
      updateMode: 'PUT',
      optimisticLockHandling: 'FAIL_ON_CONFLICT',
      validationStrategy: 'VALIDATE_ALL_FIRST',
      enableAsyncMode: false,
      asyncProcessing: true
    },
    bulkDelete: {
      deletionStrategy: 'SOFT',
      batchSize: 1000,
      failureStrategy: 'STOP_ON_FIRST_ERROR',
      enableAsyncMode: false,
      allowIncludeDeletedParam: true
    }
  },
  requestResponse: {
    request: {
      list: {
        mode: 'GENERATE_DTO',
        dtoName: ''
      },
      create: {
        mode: 'GENERATE_DTO',
        dtoName: 'Request'
      },
      delete: {
        mode: 'GENERATE_DTO',
        dtoName: ''
      },
      update: {
        mode: 'GENERATE_DTO',
        dtoName: 'UpdateRequest'
      },
      patch: {
        mode: 'JSON_MERGE_PATCH'
      },
      getByIdType: 'UUID',
      deleteByIdType: 'UUID',
      bulkInsertType: '',
      bulkUpdateType: '',
      bulkDeleteType: 'List<UUID>'
    },
    response: {
      responseType: 'RESPONSE_ENTITY',
      dtoName: '',
      endpointDtos: {
        list: '',
        get: '',
        create: '',
        update: '',
        patch: '',
        delete: '',
        bulkInsert: '',
        bulkUpdate: '',
        bulkDelete: ''
      },
      responseWrapper: 'STANDARD_ENVELOPE',
      enableFieldProjection: true,
      includeHateoasLinks: true
    }
  },
  documentation: {
    endpoints: {
      list: { description: 'List operation for API', group: 'API Group', descriptionTags: ['list'], deprecated: false },
      get: { description: 'Get By Key operation for API', group: 'API Group', descriptionTags: ['get'], deprecated: false },
      create: { description: 'Create operation for API', group: 'API Group', descriptionTags: ['create'], deprecated: false },
      update: { description: 'Update operation for API', group: 'API Group', descriptionTags: ['update'], deprecated: false },
      patch: { description: 'Patch operation for API', group: 'API Group', descriptionTags: ['patch'], deprecated: false },
      delete: { description: 'Delete operation for API', group: 'API Group', descriptionTags: ['delete'], deprecated: false },
      bulkInsert: { description: 'Bulk Insert operation for API', group: 'API Group', descriptionTags: ['bulkInsert'], deprecated: false },
      bulkUpdate: { description: 'Bulk Update operation for API', group: 'API Group', descriptionTags: ['bulkUpdate'], deprecated: false },
      bulkDelete: { description: 'Bulk Delete operation for API', group: 'API Group', descriptionTags: ['bulkDelete'], deprecated: false }
    }
  }
};

@Component({
  selector: 'app-rest-config',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatBottomSheetModule,
    BasicSettingsComponent,
    EndpointConfigComponent,
    RequestResponseConfigComponent,
    DocumentationConfigComponent
  ],
  templateUrl: './rest-config.component.html',
  styleUrls: ['./rest-config.component.css'],
  animations: [
    trigger('tabContent', [
      transition('* <=> *', [
        style({ opacity: 0, transform: 'translateY(4px)' }),
        animate('180ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ])
  ]
})
export class RestConfigComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() config: RestEndpointConfig | null = null;
  @Input() entityFields: Array<{ name?: string; type?: string } | string> = [];
  @Input() fieldTypeOptions: string[] = [];
  @Input() availableModels: Array<{ name?: string }> = [];
  @Input() dtoObjects: Array<{ name?: string; dtoType?: 'request' | 'response'; fields?: unknown[] }> = [];
  @Input() enumTypes: string[] = [];
  @Input() softDeleteEnabled = false;
  @Input() lockEntityMapping = false;
  @Input() existingRestConfigNames: string[] = [];
  @Output() save = new EventEmitter<RestEndpointConfig>();
  @Output() cancel = new EventEmitter<void>();

  draft: RestEndpointConfig = this.cloneConfig(DEFAULT_REST_ENDPOINT_CONFIG);
  showResourceNameErrors = false;
  resourceNameRequired = false;
  resourceNameDuplicate = false;
  basePathRequired = false;
  mapToEntityRequired = false;
  activeTab: 'basic' | 'endpoints' | 'request' | 'error' | 'docs' = 'basic';
  overflowTabs: Array<{ id: 'basic' | 'endpoints' | 'request' | 'error' | 'docs'; label: string; icon: string }> = [];
  readonly tabs: Array<{ id: 'basic' | 'endpoints' | 'request' | 'error' | 'docs'; label: string; icon: string }> = [
    { id: 'basic', label: 'Basic Settings', icon: 'settings' },
    { id: 'endpoints', label: 'Endpoints', icon: 'list_alt' },
    { id: 'request', label: 'Request & Response', icon: 'description' },
    // { id: 'error', label: 'Error Handling', icon: 'sms_failed' }, // Temporarily disabled; keep for future enablement.
    { id: 'docs', label: 'Documentation', icon: 'menu_book' }
  ];

  @ViewChild('tabRow', { static: false }) tabRow?: ElementRef<HTMLElement>;
  @ViewChildren('tabBtn') tabButtons!: QueryList<ElementRef<HTMLElement>>;
  private tabButtonChangesSub?: Subscription;

  constructor(private readonly bottomSheet: MatBottomSheet) {}

  get entityFieldTypeOptions(): string[] {
    const configuredOptions = (Array.isArray(this.fieldTypeOptions) ? this.fieldTypeOptions : [])
      .map((item) => String(item ?? '').trim())
      .filter(Boolean);
    if (configuredOptions.length) {
      return Array.from(new Set(configuredOptions));
    }
    const mapped = (Array.isArray(this.entityFields) ? this.entityFields : [])
      .map((item) => typeof item === 'string' ? '' : String(item?.type ?? '').trim())
      .filter(Boolean);
    const unique = Array.from(new Set(mapped));
    return unique.length ? unique : ENTITY_FIELD_TYPE_OPTIONS;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config']) {
      this.draft = this.sanitizeConfig(this.config ?? DEFAULT_REST_ENDPOINT_CONFIG);
      this.showResourceNameErrors = false;
      this.resourceNameRequired = false;
      this.resourceNameDuplicate = false;
      this.basePathRequired = false;
      this.mapToEntityRequired = false;
    }
    if ((changes['config'] || changes['existingRestConfigNames']) && this.showResourceNameErrors) {
      this.validateResourceNameUnique();
    }
    this.scheduleOverflowCheck();
  }

  ngAfterViewInit(): void {
    this.tabButtonChangesSub = this.tabButtons.changes.subscribe(() => this.scheduleOverflowCheck());
    this.scheduleOverflowCheck();
  }

  ngOnDestroy(): void {
    this.tabButtonChangesSub?.unsubscribe();
  }

  setActiveTab(tab: 'basic' | 'endpoints' | 'request' | 'error' | 'docs'): void {
    this.activeTab = tab;
    this.scrollToActiveTab();
    this.scheduleOverflowCheck();
  }

  openOverflowTabs(): void {
    if (!this.overflowTabs.length) {
      return;
    }
    const ref = this.bottomSheet.open(RestConfigOverflowSheetComponent, {
      data: { tabs: this.overflowTabs, activeTab: this.activeTab }
    });
    ref.afterDismissed().subscribe((selectedTab: 'basic' | 'endpoints' | 'request' | 'error' | 'docs' | undefined) => {
      if (selectedTab) {
        this.setActiveTab(selectedTab);
      }
    });
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    this.scheduleOverflowCheck();
  }

  saveConfig(): void {
    this.showResourceNameErrors = true;
    if (!this.validateResourceNameUnique() || !this.validateBasePath() || !this.validateEntityMapping()) {
      this.activeTab = 'basic';
      this.scrollToActiveTab();
      return;
    }
    this.save.emit(this.sanitizeConfig(this.draft));
  }

  cancelConfig(): void {
    this.cancel.emit();
  }

  private scheduleOverflowCheck(): void {
    setTimeout(() => this.updateOverflowTabs());
  }

  private scrollToActiveTab(): void {
    setTimeout(() => {
      const tabIndex = this.tabs.findIndex((tab) => tab.id === this.activeTab);
      const tabEl = this.tabButtons?.toArray()?.[tabIndex]?.nativeElement;
      if (!tabEl) {
        return;
      }
      tabEl.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
    });
  }

  private updateOverflowTabs(): void {
    const rowEl = this.tabRow?.nativeElement;
    const buttonEls = this.tabButtons?.toArray() ?? [];
    if (!rowEl || !buttonEls.length) {
      this.overflowTabs = [];
      return;
    }

    const rowRect = rowEl.getBoundingClientRect();
    const overflowBoundary = rowRect.right - 46;
    const hiddenTabIds = new Set<string>();

    buttonEls.forEach((btn, index) => {
      const rect = btn.nativeElement.getBoundingClientRect();
      const isHidden = rect.right > overflowBoundary || rect.left < rowRect.left;
      if (isHidden) {
        hiddenTabIds.add(this.tabs[index].id);
      }
    });

    this.overflowTabs = this.tabs.filter((tab) => hiddenTabIds.has(tab.id));
  }

  private sanitizeConfig(config: RestEndpointConfig): RestEndpointConfig {
    const docResourceName = String(config?.resourceName ?? config?.mappedEntityName ?? '').trim();
    return {
      resourceName: String(config?.resourceName ?? '').trim(),
      basePath: String(config?.basePath ?? '').trim(),
      mapToEntity: Boolean(config?.mapToEntity),
      mappedEntityName: String(config?.mappedEntityName ?? '').trim(),
      methods: {
        list: Boolean(config?.methods?.list ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.list),
        get: Boolean(config?.methods?.get ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.get),
        create: Boolean(config?.methods?.create ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.create),
        update: false,
        patch: Boolean(config?.methods?.patch ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.patch),
        delete: Boolean(config?.methods?.delete ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.delete),
        bulkInsert: Boolean(config?.methods?.bulkInsert ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.bulkInsert),
        bulkUpdate: Boolean(config?.methods?.bulkUpdate ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.bulkUpdate),
        bulkDelete: Boolean(config?.methods?.bulkDelete ?? DEFAULT_REST_ENDPOINT_CONFIG.methods.bulkDelete)
      },
      apiVersioning: {
        enabled: Boolean(config?.apiVersioning?.enabled),
        strategy: config?.apiVersioning?.strategy === 'path' ? 'path' : 'header',
        headerName: String(config?.apiVersioning?.headerName ?? DEFAULT_REST_ENDPOINT_CONFIG.apiVersioning.headerName).trim(),
        defaultVersion: String(config?.apiVersioning?.defaultVersion ?? DEFAULT_REST_ENDPOINT_CONFIG.apiVersioning.defaultVersion).trim()
      },
      pathVariableType: this.normalizePathVariableType(config?.pathVariableType),
      deletion: {
        mode: config?.deletion?.mode === 'HARD' ? 'HARD' : 'SOFT',
        restoreEndpoint: Boolean(config?.deletion?.restoreEndpoint),
        includeDeletedParam: Boolean(config?.deletion?.includeDeletedParam)
      },
      hateoas: {
        enabled: Boolean(config?.hateoas?.enabled),
        selfLink: Boolean(config?.hateoas?.selfLink),
        updateLink: Boolean(config?.hateoas?.updateLink),
        deleteLink: Boolean(config?.hateoas?.deleteLink)
      },
      pagination: {
        enabled: Boolean(config?.pagination?.enabled),
        mode: config?.pagination?.mode === 'CURSOR' ? 'CURSOR' : 'OFFSET',
        sortField: String(config?.pagination?.sortField ?? DEFAULT_REST_ENDPOINT_CONFIG.pagination.sortField).trim(),
        sortDirection: config?.pagination?.sortDirection === 'ASC' ? 'ASC' : 'DESC'
      },
      searchFiltering: {
        keywordSearch: Boolean(config?.searchFiltering?.keywordSearch),
        jpaSpecification: Boolean(config?.searchFiltering?.jpaSpecification),
        searchableFields: Array.isArray(config?.searchFiltering?.searchableFields)
          ? config.searchFiltering.searchableFields.map(item => String(item ?? '').trim()).filter(Boolean)
          : []
      },
      batchOperations: {
        insert: {
          batchSize: Math.max(1, Number(config?.batchOperations?.insert?.batchSize ?? DEFAULT_REST_ENDPOINT_CONFIG.batchOperations.insert.batchSize)),
          enableAsyncMode: Boolean(config?.batchOperations?.insert?.enableAsyncMode)
        },
        update: {
          batchSize: Math.max(1, Number(config?.batchOperations?.update?.batchSize ?? DEFAULT_REST_ENDPOINT_CONFIG.batchOperations.update.batchSize)),
          updateMode: config?.batchOperations?.update?.updateMode === 'PATCH' ? 'PATCH' : 'PUT',
          optimisticLockHandling: config?.batchOperations?.update?.optimisticLockHandling === 'SKIP_CONFLICTS' ? 'SKIP_CONFLICTS' : 'FAIL_ON_CONFLICT',
          validationStrategy: config?.batchOperations?.update?.validationStrategy === 'SKIP_DUPLICATES' ? 'SKIP_DUPLICATES' : 'VALIDATE_ALL_FIRST',
          enableAsyncMode: Boolean(config?.batchOperations?.update?.enableAsyncMode),
          asyncProcessing: Boolean(config?.batchOperations?.update?.asyncProcessing)
        },
        bulkDelete: {
          deletionStrategy: config?.batchOperations?.bulkDelete?.deletionStrategy === 'HARD' ? 'HARD' : 'SOFT',
          batchSize: Math.max(1, Number(config?.batchOperations?.bulkDelete?.batchSize ?? DEFAULT_REST_ENDPOINT_CONFIG.batchOperations.bulkDelete.batchSize)),
          failureStrategy: config?.batchOperations?.bulkDelete?.failureStrategy === 'CONTINUE_AND_REPORT_FAILURES'
            ? 'CONTINUE_AND_REPORT_FAILURES'
            : 'STOP_ON_FIRST_ERROR',
          enableAsyncMode: Boolean(config?.batchOperations?.bulkDelete?.enableAsyncMode),
          allowIncludeDeletedParam: Boolean(config?.batchOperations?.bulkDelete?.allowIncludeDeletedParam)
        }
      },
      requestResponse: {
        request: {
          list: {
            mode: config?.requestResponse?.request?.list?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String(config?.requestResponse?.request?.list?.dtoName ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.list.dtoName).trim()
          },
          create: {
            mode: config?.requestResponse?.request?.create?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String(config?.requestResponse?.request?.create?.dtoName ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.create.dtoName).trim()
          },
          delete: {
            mode: config?.requestResponse?.request?.delete?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String(config?.requestResponse?.request?.delete?.dtoName ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.delete.dtoName).trim()
          },
          update: {
            mode: config?.requestResponse?.request?.update?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String(config?.requestResponse?.request?.update?.dtoName ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.update.dtoName).trim()
          },
          patch: {
            mode: config?.requestResponse?.request?.patch?.mode === 'JSON_PATCH' ? 'JSON_PATCH' : 'JSON_MERGE_PATCH'
          },
          getByIdType: String(config?.requestResponse?.request?.getByIdType ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.getByIdType).trim(),
          deleteByIdType: String(config?.requestResponse?.request?.deleteByIdType ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.deleteByIdType).trim(),
          bulkInsertType: String(config?.requestResponse?.request?.bulkInsertType ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.bulkInsertType).trim(),
          bulkUpdateType: String(config?.requestResponse?.request?.bulkUpdateType ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.bulkUpdateType).trim(),
          bulkDeleteType: String(config?.requestResponse?.request?.bulkDeleteType ?? DEFAULT_REST_ENDPOINT_CONFIG.requestResponse.request.bulkDeleteType).trim()
        },
        response: {
          responseType: config?.requestResponse?.response?.responseType === 'DTO_DIRECT'
            ? 'DTO_DIRECT'
            : config?.requestResponse?.response?.responseType === 'CUSTOM_WRAPPER'
              ? 'CUSTOM_WRAPPER'
              : 'RESPONSE_ENTITY',
          dtoName: String(config?.requestResponse?.response?.dtoName ?? '').trim(),
          endpointDtos: {
            list: String(config?.requestResponse?.response?.endpointDtos?.list ?? '').trim(),
            get: String(config?.requestResponse?.response?.endpointDtos?.get ?? '').trim(),
            create: String(config?.requestResponse?.response?.endpointDtos?.create ?? '').trim(),
            update: String(config?.requestResponse?.response?.endpointDtos?.update ?? '').trim(),
            patch: String(config?.requestResponse?.response?.endpointDtos?.patch ?? '').trim(),
            delete: String(config?.requestResponse?.response?.endpointDtos?.delete ?? '').trim(),
            bulkInsert: String(config?.requestResponse?.response?.endpointDtos?.bulkInsert ?? '').trim(),
            bulkUpdate: String(config?.requestResponse?.response?.endpointDtos?.bulkUpdate ?? '').trim(),
            bulkDelete: String(config?.requestResponse?.response?.endpointDtos?.bulkDelete ?? '').trim()
          },
          responseWrapper: config?.requestResponse?.response?.responseWrapper === 'NONE'
            ? 'NONE'
            : config?.requestResponse?.response?.responseWrapper === 'UPSERT'
              ? 'UPSERT'
              : 'STANDARD_ENVELOPE',
          enableFieldProjection: Boolean(config?.requestResponse?.response?.enableFieldProjection),
          includeHateoasLinks: Boolean(config?.requestResponse?.response?.includeHateoasLinks)
        }
      },
      documentation: {
        endpoints: {
          list: this.normalizeDocumentationEntry('list', config?.documentation?.endpoints?.list, docResourceName),
          get: this.normalizeDocumentationEntry('get', config?.documentation?.endpoints?.get, docResourceName),
          create: this.normalizeDocumentationEntry('create', config?.documentation?.endpoints?.create, docResourceName),
          update: this.normalizeDocumentationEntry('update', config?.documentation?.endpoints?.update, docResourceName),
          patch: this.normalizeDocumentationEntry('patch', config?.documentation?.endpoints?.patch, docResourceName),
          delete: this.normalizeDocumentationEntry('delete', config?.documentation?.endpoints?.delete, docResourceName),
          bulkInsert: this.normalizeDocumentationEntry('bulkInsert', config?.documentation?.endpoints?.bulkInsert, docResourceName),
          bulkUpdate: this.normalizeDocumentationEntry('bulkUpdate', config?.documentation?.endpoints?.bulkUpdate, docResourceName),
          bulkDelete: this.normalizeDocumentationEntry('bulkDelete', config?.documentation?.endpoints?.bulkDelete, docResourceName)
        }
      }
    };
  }

  private normalizeDocumentationEntry(
    endpointKey: keyof RestEndpointConfig['documentation']['endpoints'],
    rawEntry: any,
    resourceName: string
  ): { description: string; group: string; descriptionTags: string[]; deprecated: boolean } {
    const fallback = this.getDefaultDocumentationEntry(endpointKey, resourceName);
    const tags = Array.isArray(rawEntry?.descriptionTags)
      ? rawEntry.descriptionTags.map((item: unknown) => String(item ?? '').trim()).filter(Boolean)
      : [];
    return {
      description: String(rawEntry?.description ?? '').trim() || fallback.description,
      group: String(rawEntry?.group ?? '').trim() || fallback.group,
      descriptionTags: Array.from(new Set(tags.length ? tags : fallback.descriptionTags)),
      deprecated: Boolean(rawEntry?.deprecated)
    };
  }

  private getDefaultDocumentationEntry(
    endpointKey: keyof RestEndpointConfig['documentation']['endpoints'],
    resourceName: string
  ): { description: string; group: string; descriptionTags: string[]; deprecated: boolean } {
    const endpointLabel: Record<string, string> = {
      list: 'List',
      get: 'Get By Key',
      create: 'Create',
      update: 'Update',
      patch: 'Patch',
      delete: 'Delete',
      bulkInsert: 'Bulk Insert',
      bulkUpdate: 'Bulk Update',
      bulkDelete: 'Bulk Delete'
    };
    const target = String(resourceName ?? '').trim() || 'API';
    return {
      description: `${endpointLabel[String(endpointKey)] || String(endpointKey)} operation for ${target}`,
      group: `${target} Group`,
      descriptionTags: [String(endpointKey)],
      deprecated: false
    };
  }

  private normalizePathVariableType(raw: unknown): PathVariableType {
    if (raw === 'LONG') {
      return 'LONG';
    }
    if (raw === 'STRING') {
      return 'STRING';
    }
    return 'UUID';
  }

  private cloneConfig(config: RestEndpointConfig): RestEndpointConfig {
    return this.sanitizeConfig(config);
  }

  onResourceNameChanged(): void {
    if (!this.showResourceNameErrors) {
      this.resourceNameRequired = false;
      this.resourceNameDuplicate = false;
      this.basePathRequired = false;
      return;
    }
    this.resourceNameRequired = false;
    this.validateResourceNameUnique();
  }

  onBasePathChanged(): void {
    if (!this.showResourceNameErrors) {
      this.basePathRequired = false;
      return;
    }
    this.validateBasePath();
  }

  onEntityMappingChanged(): void {
    if (!this.showResourceNameErrors) {
      this.mapToEntityRequired = false;
      return;
    }
    this.validateEntityMapping();
  }

  private validateResourceNameUnique(): boolean {
    const name = String(this.draft?.resourceName ?? '').trim().toLowerCase();
    if (!name) {
      this.resourceNameRequired = true;
      this.resourceNameDuplicate = false;
      return false;
    }
    this.resourceNameRequired = false;
    const duplicate = (Array.isArray(this.existingRestConfigNames) ? this.existingRestConfigNames : [])
      .map((item) => String(item ?? '').trim().toLowerCase())
      .filter(Boolean)
      .includes(name);
    this.resourceNameDuplicate = duplicate;
    return !duplicate;
  }

  private validateEntityMapping(): boolean {
    const mapToEntity = Boolean(this.draft?.mapToEntity);
    const mappedEntityName = String(this.draft?.mappedEntityName ?? '').trim();
    this.mapToEntityRequired = mapToEntity && !mappedEntityName;
    return !this.mapToEntityRequired;
  }

  private validateBasePath(): boolean {
    const basePath = String(this.draft?.basePath ?? '').trim();
    this.basePathRequired = !basePath;
    return !this.basePathRequired;
  }
}
