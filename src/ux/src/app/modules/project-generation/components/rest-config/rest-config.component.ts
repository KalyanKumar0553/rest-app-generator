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
      list: { description: string; descriptionTags: string[]; deprecated: boolean; };
      get: { description: string; descriptionTags: string[]; deprecated: boolean; };
      create: { description: string; descriptionTags: string[]; deprecated: boolean; };
      update: { description: string; descriptionTags: string[]; deprecated: boolean; };
      patch: { description: string; descriptionTags: string[]; deprecated: boolean; };
      delete: { description: string; descriptionTags: string[]; deprecated: boolean; };
      bulkInsert: { description: string; descriptionTags: string[]; deprecated: boolean; };
      bulkUpdate: { description: string; descriptionTags: string[]; deprecated: boolean; };
      bulkDelete: { description: string; descriptionTags: string[]; deprecated: boolean; };
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
      list: { description: '', descriptionTags: [], deprecated: false },
      get: { description: '', descriptionTags: [], deprecated: false },
      create: { description: '', descriptionTags: [], deprecated: false },
      update: { description: '', descriptionTags: [], deprecated: false },
      patch: { description: '', descriptionTags: [], deprecated: false },
      delete: { description: '', descriptionTags: [], deprecated: false },
      bulkInsert: { description: '', descriptionTags: [], deprecated: false },
      bulkUpdate: { description: '', descriptionTags: [], deprecated: false },
      bulkDelete: { description: '', descriptionTags: [], deprecated: false }
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
  @Input() existingRestConfigNames: string[] = [];
  @Output() save = new EventEmitter<RestEndpointConfig>();
  @Output() cancel = new EventEmitter<void>();

  draft: RestEndpointConfig = this.cloneConfig(DEFAULT_REST_ENDPOINT_CONFIG);
  showResourceNameErrors = false;
  resourceNameRequired = false;
  resourceNameDuplicate = false;
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
    if (!this.validateResourceNameUnique() || !this.validateEntityMapping()) {
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
          list: {
            description: String(config?.documentation?.endpoints?.list?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.list?.descriptionTags)
              ? config.documentation.endpoints.list.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.list?.deprecated)
          },
          get: {
            description: String(config?.documentation?.endpoints?.get?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.get?.descriptionTags)
              ? config.documentation.endpoints.get.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.get?.deprecated)
          },
          create: {
            description: String(config?.documentation?.endpoints?.create?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.create?.descriptionTags)
              ? config.documentation.endpoints.create.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.create?.deprecated)
          },
          update: {
            description: String(config?.documentation?.endpoints?.update?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.update?.descriptionTags)
              ? config.documentation.endpoints.update.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.update?.deprecated)
          },
          patch: {
            description: String(config?.documentation?.endpoints?.patch?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.patch?.descriptionTags)
              ? config.documentation.endpoints.patch.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.patch?.deprecated)
          },
          delete: {
            description: String(config?.documentation?.endpoints?.delete?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.delete?.descriptionTags)
              ? config.documentation.endpoints.delete.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.delete?.deprecated)
          },
          bulkInsert: {
            description: String(config?.documentation?.endpoints?.bulkInsert?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.bulkInsert?.descriptionTags)
              ? config.documentation.endpoints.bulkInsert.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.bulkInsert?.deprecated)
          },
          bulkUpdate: {
            description: String(config?.documentation?.endpoints?.bulkUpdate?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.bulkUpdate?.descriptionTags)
              ? config.documentation.endpoints.bulkUpdate.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.bulkUpdate?.deprecated)
          },
          bulkDelete: {
            description: String(config?.documentation?.endpoints?.bulkDelete?.description ?? '').trim(),
            descriptionTags: Array.isArray(config?.documentation?.endpoints?.bulkDelete?.descriptionTags)
              ? config.documentation.endpoints.bulkDelete.descriptionTags.map(item => String(item ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean(config?.documentation?.endpoints?.bulkDelete?.deprecated)
          }
        }
      }
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
      return;
    }
    this.resourceNameRequired = false;
    this.validateResourceNameUnique();
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
}
