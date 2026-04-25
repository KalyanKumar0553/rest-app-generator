import { ChangeDetectorRef, Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { stableArray, emptyCache, StableCache } from '../../../../utils/stable-reference';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, Subscription, firstValueFrom, takeUntil, debounceTime, distinctUntilChanged, switchMap, of, catchError } from 'rxjs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCheckbox, MatCheckboxChange } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTableModule } from '@angular/material/table';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ProjectSpecComponent } from '../project-spec/project-spec.component';
import { AddProfileComponent } from '../add-profile/add-profile.component';
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { AuthService } from '../../../../services/auth.service';
import {
  ProjectCollaborationAction,
  ProjectCollaborationRequest,
  ProjectCollaborationState,
  ProjectContributor,
  ProjectContributorPermissions,
  ProjectDraftPayload,
  ProjectDraftTabData,
  ProjectDraftTabPatchPayload,
  PublishedPluginModule,
  ProjectService,
  ProjectTabDefinition
} from '../../../../services/project.service';
import type { ProjectDetails } from '../../../../services/project.service';
import { ToastService } from '../../../../services/toast.service';
import { UserService, UserSearchResult } from '../../../../services/user.service';
import { SearchableSelectComponent, SelectOption } from '../../../../components/searchable-select/searchable-select.component';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG, API_ENDPOINTS, STORAGE_KEYS } from '../../../../constants/api.constants';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { finalize } from 'rxjs/operators';
import { ValidatorService } from '../../../../services/validator.service';
import { buildMavenNamingRules, isValidJavaProjectFolderName } from '../../validators/naming-validation';
import { APP_SETTINGS } from '../../../../settings/app-settings';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';
import {
  ACTUATOR_ENDPOINT_OPTIONS,
  DEFAULT_ACTUATOR_ENDPOINTS
} from '../actuator-config/actuator-config.component';
import { RestConfigComponent, RestEndpointConfig } from '../rest-config/rest-config.component';
import { ENTITY_FIELD_TYPE_OPTIONS } from '../../constants/backend-field-types';
import { VALIDATION_MESSAGES } from '../../constants/validation-messages';
import {
  PluginModuleCard,
  PluginModuleSelection,
  ShippableModuleCard
} from '../modules-selection/modules-selection.component';

import {
  ProjectSettings,
  DatabaseSettings,
  DatabaseOption,
  DeveloperPreferences,
  ProjectGenerationStageEvent,
  ProjectRunSummary,
  ControllerRestSpecRow
} from './project-generation-dashboard.models';
import {
  BASE_NAV_ITEMS,
  ACTUATOR_NAV_ITEM,
  CONTROLLERS_NAV_ITEM,
  MAPPERS_NAV_ITEM,
  FRONTEND_OPTIONS,
  DATABASE_OPTIONS,
  DB_TYPE_OPTIONS,
  DB_GENERATION_OPTIONS,
  JAVA_VERSION_OPTIONS,
  DEPLOYMENT_OPTIONS,
  DEFAULT_PROJECT_SETTINGS,
  DEFAULT_DATABASE_SETTINGS,
  DEFAULT_DEVELOPER_PREFERENCES,
  DEFAULT_CONTROLLERS_CONFIG,
  BACK_CONFIRMATION_CONFIG,
  ENTITIES_DELETE_CONFIRMATION_CONFIG,
  REST_SPEC_DELETE_CONFIRMATION_CONFIG,
  CONFIGURE_API_DISABLE_CONFIRMATION_CONFIG,
  CONTROLLERS_CONFIG_DISCARD_CONFIRMATION_CONFIG,
  GENERATION_CANCEL_CONFIRMATION_CONFIG,
  RECENT_PROJECT_PROMPT_CONFIG
} from './project-generation-dashboard.defaults';
import { ProjectSpecMapperService } from '../../services/project-spec-mapper.service';
import { toDatabaseCode, resolveDatabaseType, trimmed, toArtifactId, hasNumber, isValidProjectDescription } from '../../utils/project-generation.utils';
import { ProjectDraftState } from '../../models/project-draft.models';
import { loadProjectDraftFromStorage, saveProjectDraftToStorage } from '../../utils/project-draft-storage.utils';
import { resolveProjectGenerationRoute } from '../../utils/project-generation-route.utils';
import {
  ProjectLanguageOption,
  SupportedProjectLanguage,
  getMigrationTargetLanguageOptions,
  getProjectLanguageLabel,
  getProjectLanguageOption,
  normalizeProjectLanguage
} from '../../utils/project-language.utils';
import { ProjectGenerationActuatorSectionComponent } from './project-generation-actuator-section.component';
import { ProjectGenerationCollaborateSectionComponent } from './project-generation-collaborate-section.component';
import { ProjectGenerationControllersSectionComponent } from './project-generation-controllers-section.component';
import { ProjectGenerationDataObjectsSectionComponent } from './project-generation-data-objects-section.component';
import { ProjectGenerationEntitiesSectionComponent } from './project-generation-entities-section.component';
import { ProjectGenerationExploreSectionComponent } from './project-generation-explore-section.component';
import { ProjectGenerationGeneralSectionComponent } from './project-generation-general-section.component';
import { ProjectGenerationMappersSectionComponent } from './project-generation-mappers-section.component';
import { ProjectGenerationModuleTabSectionComponent } from './project-generation-module-tab-section.component';
import { ProjectGenerationModulesSectionComponent } from './project-generation-modules-section.component';

@Component({
  selector: 'app-project-generation-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatAutocompleteModule,
    MatTableModule,
    ConfirmationModalComponent,
    ModalComponent,
    AddProfileComponent,
    ProjectSpecComponent,
    SidenavComponent,
    SearchableSelectComponent,
    ProjectGenerationGeneralSectionComponent,
    ProjectGenerationActuatorSectionComponent,
    ProjectGenerationEntitiesSectionComponent,
    ProjectGenerationDataObjectsSectionComponent,
    ProjectGenerationMappersSectionComponent,
    ProjectGenerationControllersSectionComponent,
    ProjectGenerationModulesSectionComponent,
    ProjectGenerationModuleTabSectionComponent,
    ProjectGenerationCollaborateSectionComponent,
    ProjectGenerationExploreSectionComponent
  ],
  templateUrl: './project-generation-dashboard.component.html',
  styleUrls: ['./project-generation-dashboard.component.css']
})
export class ProjectGenerationDashboardComponent implements OnInit, OnDestroy {
  private static readonly projectStorageResetKey = 'project_storage_reset_v20260314';
  private static readonly shippableModuleKeys = ['rbac', 'auth', 'state-machine', 'subscription', 'swagger', 'cdn'];
  private static readonly shippableModuleCards: ShippableModuleCard[] = [
    {
      key: 'rbac',
      title: 'RBAC',
      description: 'Adds role-based access control primitives, permissions, and management APIs to the generated project.'
    },
    {
      key: 'auth',
      title: 'Authentication',
      description: 'Adds login, token, OAuth, and profile management capabilities as a shipped module in the generated project.'
    },
    {
      key: 'state-machine',
      title: 'State Management',
      description: 'Adds the workflow and state-machine execution layer so the generated project can run orchestrated transitions.'
    },
    {
      key: 'subscription',
      title: 'Subscription',
      description: 'Adds subscription, entitlement, pricing, and quota management support as a reusable generated module.'
    },
    {
      key: 'swagger',
      title: 'Swagger',
      description: 'Adds OpenAPI metadata and interactive API documentation support to generated runtimes.'
    },
    {
      key: 'cdn',
      title: 'Azure CDN Upload',
      description: 'Adds draft image storage, one-at-a-time Azure CDN upload processing, and admin APIs for queue control.'
    }
  ];
  private destroy$ = new Subject<void>();
  private readonly maxYamlSpecPayloadBytes = 2 * 1024 * 1024;
  readonly appSettings = APP_SETTINGS;

  isSidebarOpen = false;
  isLoading = false;
  isLoggedIn = false;
  projectId: string | null = null;
  hasUnsavedChanges = false;
  activeSection = 'general';

  baseNavItems: NavItem[] = [...BASE_NAV_ITEMS];
  moduleNavItems: NavItem[] = [];
  actuatorNavItem: NavItem = ACTUATOR_NAV_ITEM;
  controllersNavItem: NavItem = CONTROLLERS_NAV_ITEM;
  mappersNavItem: NavItem = MAPPERS_NAV_ITEM;

  entities: any[] = [];
  dataObjects: any[] = [];
  relations: any[] = [];
  enums: any[] = [];
  mappers: any[] = [];
  moduleConfigs: Record<string, any> = {};
  selectedPlugins: PluginModuleSelection[] = [];
  publishedPluginModules: PluginModuleCard[] = [];
  readonly shippableModuleCards = ProjectGenerationDashboardComponent.shippableModuleCards;
  dataObjectsDefaultTab: 'dataObjects' | 'enums' | 'mappers' = 'dataObjects';
  dataObjectsActiveTab: 'dataObjects' | 'enums' | 'mappers' = 'dataObjects';

  showBackConfirmation = false;
  showEntitiesDeleteConfirmation = false;
  showRestSpecDeleteConfirmation = false;
  showConfigureApiDisableConfirmation = false;
  showRecentProjectPrompt = false;
  showGenerationCancelConfirmation = false;
  private previousDatabaseSelection = 'POSTGRES';
  private previousDatabaseType: 'SQL' | 'NOSQL' | 'NONE' = 'SQL';
  private pendingDatabaseSelection: string | null = null;
  private databaseSelectionBeforeConfirmation: string | null = null;
  private hasCheckedRecentProjectPrompt = false;
  private recentProjectToResume: { id: string } | null = null;
  isExploreSyncing = false;
  isGeneratingFromDtoSave = false;
  backendProjectId: string | null = null;
  projectOwnerId: string | null = null;
  projectCanManageContributors = false;
  projectContributors: ProjectContributor[] = [];
  collaborationInviteToken: string | null = null;
  collaborationRequests: ProjectCollaborationRequest[] = [];
  collaborationRequestColumns: string[] = ['requester', 'requested', 'granted', 'status', 'actions'];
  contributorPermissionColumns: string[] = ['userId', 'edit', 'generate', 'manage', 'actions'];
  contributorUserId = '';
  isContributorSaving = false;
  showAddContributorModal = false;
  showContributorPermissionsModal = false;
  contributorSearchResults: SelectOption[] = [];
  isContributorSearching = false;
  private contributorSearchInput$ = new Subject<string>();
  selectedContributorItems: SelectOption[] = [];
  isAddingContributors = false;
  selectedContributorForMobileEdit: ProjectContributor | null = null;
  private projectEventsSource: WebSocket | null = null;
  private pendingGenerationYamlSpec: string | null = null;
  private generationGuestSubscription: Subscription | null = null;
  private collaborationHeartbeatId: number | null = null;
  private collaborationSessionId: string | null = null;
  private changeTrackingIntervalId: number | null = null;
  private savedProjectStateSnapshot = '';
  private requestedSectionFromRoute = 'general';
  private requestedExploreViewFromRoute: 'runs' | 'migrate' = 'runs';
  private dependenciesLoadPromise: Promise<void> | null = null;
  private publishedPluginModulesLoadPromise: Promise<void> | null = null;
  private publishedPluginModulesLoadedGenerator: string | null = null;
  private loadedDraftSections = new Set<string>();
  private draftSectionLoadPromises = new Map<string, Promise<void>>();
  private pendingCreateDraftPromise: Promise<string | null> | null = null;
  private tabDefinitionsLoadedFully = false;
  draftVersion = 1;
  tabDefinitions: ProjectTabDefinition[] = [];
  exploreZipBlob: Blob | null = null;
  exploreZipFileName = 'project.zip';
  exploreRuns: ProjectRunSummary[] = [];
  exploreStageEvents: ProjectGenerationStageEvent[] = [];
  isExplorePreviewOpen = false;
  activeExplorePreviewRunId = '';
  exploreView: 'runs' | 'migrate' = 'runs';
  selectedMigrationLanguage: SupportedProjectLanguage | null = null;
  isMigratingProject = false;
  collaborationState: ProjectCollaborationState = {
    activeEditors: 0,
    editors: [],
    recentActions: []
  };
  readonly exploreStageColumns: string[] = ['stage', 'status', 'progress', 'updatedAt', 'actions'];

  backConfirmationConfig = BACK_CONFIRMATION_CONFIG;
  entitiesDeleteConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = { ...ENTITIES_DELETE_CONFIRMATION_CONFIG };
  restSpecDeleteConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = { ...REST_SPEC_DELETE_CONFIRMATION_CONFIG };
  configureApiDisableConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = { ...CONFIGURE_API_DISABLE_CONFIRMATION_CONFIG };
  controllersConfigDiscardConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = { ...CONTROLLERS_CONFIG_DISCARD_CONFIRMATION_CONFIG };
  generationCancelConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = { ...GENERATION_CANCEL_CONFIRMATION_CONFIG };
  recentProjectPromptConfig: { title: string; message: string; buttons: ModalButton[] } = { ...RECENT_PROJECT_PROMPT_CONFIG };
  pendingRestSpecDeleteKey: string | null = null;

  projectSettings: ProjectSettings = { ...DEFAULT_PROJECT_SETTINGS };
  databaseSettings: DatabaseSettings = { ...DEFAULT_DATABASE_SETTINGS };
  developerPreferences: DeveloperPreferences = { ...DEFAULT_DEVELOPER_PREFERENCES, profiles: [] };

  controllersConfig: RestEndpointConfig = { ...DEFAULT_CONTROLLERS_CONFIG };
  controllersConfigEnabled = true;
  controllersCreatingNewConfig = false;
  controllersEditingSpecKey: string | null = null;
  controllersEditingSpecConfig: RestEndpointConfig | null = null;
  @ViewChild('controllersRestConfigComponent') controllersRestConfigComponent?: RestConfigComponent;
  showProfileModal = false;
  showYamlPreviewModal = false;
  yamlPreviewContent = '';
  showControllersConfigDiscardConfirmation = false;
  private configureApiToggleSource: MatCheckbox | null = null;
  selectedActuatorConfiguration = 'default';
  actuatorConfigurationOptions: string[] = ['default'];
  actuatorProfileEndpoints: Record<string, string[]> = {
    default: [...DEFAULT_ACTUATOR_ENDPOINTS]
  };
  readonly actuatorConfigurationOptions$ = this.projectGenerationState.actuatorConfigurationOptions$;
  readonly actuatorProfileEndpoints$ = this.projectGenerationState.actuatorEndpointsByConfiguration$;

  dependencies = '';
  dependencyInput = '';
  selectedDependencies: string[] = [];
  filteredDependencies: string[] = [];
  availableDependencies: string[] = [];
  projectGroupError = '';
  projectNameError = '';
  projectDescriptionError = '';
  @ViewChild('projectGroupInput') projectGroupInput?: ElementRef<HTMLInputElement>;
  @ViewChild('projectNameInput') projectNameInput?: ElementRef<HTMLInputElement>;

  frontendOptions = FRONTEND_OPTIONS;
  databaseOptions: DatabaseOption[] = DATABASE_OPTIONS;
  dbTypeOptions: Array<'SQL' | 'NOSQL' | 'NONE'> = DB_TYPE_OPTIONS;
  dbGenerationOptions = DB_GENERATION_OPTIONS;
  javaVersionOptions = JAVA_VERSION_OPTIONS;
  deploymentOptions = DEPLOYMENT_OPTIONS;
  private visibleNavItemsCache: NavItem[] = [];
  private visibleNavItemsCacheKey = '';
  private _existingContribIdsCache = emptyCache<string[]>();
  private _selectedShippableModulesCache = emptyCache<string[]>();
  private _controllerRestSpecRowsCache = emptyCache<ControllerRestSpecRow[]>();
  private _controllersEditModeExistingNamesCache = emptyCache<string[]>();
  private _filteredDbOptionsCache = emptyCache<DatabaseOption[]>();
  private _controllersEntityFieldsCache = emptyCache<Array<{ name: string; type?: string }>>();
  private _controllersFieldTypeOptionsCache = emptyCache<string[]>();
  private _controllersEnumTypesCache = emptyCache<string[]>();
  private _configuredEntityRestSpecNamesCache = emptyCache<string[]>();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private projectService: ProjectService,
    private toastService: ToastService,
    private userService: UserService,
    private http: HttpClient,
    private validatorService: ValidatorService,
    private localStorageService: LocalStorageService,
    private cdr: ChangeDetectorRef,
    private projectGenerationState: ProjectGenerationStateService,
    private specMapper: ProjectSpecMapperService
  ) {}

  get visibleNavItems(): NavItem[] {
    const cacheKey = this.buildVisibleNavItemsCacheKey();
    if (cacheKey === this.visibleNavItemsCacheKey) {
      return this.visibleNavItemsCache;
    }

    const shouldShowExplore = !this.isGeneratingFromDtoSave && this.canAccessExplore();
    const isNoneDatabase = toDatabaseCode(this.databaseSettings.database) === 'NONE';
    this.visibleNavItemsCache = [...this.tabDefinitions]
      .sort((left, right) => {
        if (left.order === right.order) {
          return left.key.localeCompare(right.key);
        }
        return left.order - right.order;
      })
      .filter((tab) => this.activeSection === tab.key || this.isLoggedIn || tab.key !== 'modules')
      .filter((tab) => this.activeSection === tab.key || shouldShowExplore || tab.key !== 'explore')
      .filter((tab) => this.activeSection === tab.key || (this.backendProjectId && this.canManageContributors()) || tab.key !== 'collaborate')
      .filter((tab) => this.activeSection === tab.key || this.developerPreferences.enableActuator || tab.key !== 'actuator')
      .filter((tab) => this.activeSection === tab.key || this.developerPreferences.configureApi || tab.key !== 'controllers')
      .filter((tab) => this.activeSection === tab.key || this.dataObjects.length > 0 || tab.key !== 'mappers')
      .filter((tab) => this.activeSection === tab.key || !isNoneDatabase || tab.key !== 'entities')
      .map((tab) => ({
        icon: tab.icon,
        label: tab.label,
        value: tab.key
      }));
    this.visibleNavItemsCacheKey = cacheKey;
    return this.visibleNavItemsCache;
  }

  private buildVisibleNavItemsCacheKey(): string {
    const tabSignature = this.tabDefinitions
      .map((tab) => `${tab.key}:${tab.label}:${tab.icon}:${tab.order}`)
      .join('|');
    return [
      tabSignature,
      this.isLoggedIn ? '1' : '0',
      this.isGeneratingFromDtoSave ? '1' : '0',
      this.isExploreSyncing ? '1' : '0',
      this.canAccessExplore() ? '1' : '0',
      trimmed(this.backendProjectId),
      this.projectCanManageContributors ? '1' : '0',
      this.developerPreferences.enableActuator ? '1' : '0',
      this.developerPreferences.configureApi ? '1' : '0',
      String(this.dataObjects.length),
      toDatabaseCode(this.databaseSettings.database)
    ].join('~');
  }

  isEntitiesTabVisible(): boolean {
    return toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  private loadTabDefinitions(generator: string, dependencies: string[] = this.selectedDependencies, tabKey?: string): void {
    this.projectService.getProjectTabDetails(generator, dependencies, tabKey)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tabDetails) => {
          this.tabDefinitions = Array.isArray(tabDetails) ? tabDetails : [];
          this.applyTabDefinitions(this.tabDefinitions);
          this.tabDefinitionsLoadedFully = !tabKey;
        },
        error: (error) => {
          console.error('Failed to load project tab definitions:', error);
        }
      });
  }

  private applyTabDefinitions(tabDetails: ProjectTabDefinition[]): void {
    if (!Array.isArray(tabDetails) || !tabDetails.length) {
      return;
    }
    this.tabDefinitions = [...tabDetails].sort((left, right) => {
      if (left.order === right.order) {
        return left.key.localeCompare(right.key);
      }
      return left.order - right.order;
    });
    if (!this.isSectionAvailable(this.activeSection)) {
      this.activeSection = 'general';
    }
  }

  ngOnInit(): void {
    this.clearLegacyProjectStorageIfRequired();
    this.isLoggedIn = this.authService.isLoggedIn();
    this.projectGenerationState.moduleConfigs$
      .pipe(takeUntil(this.destroy$))
      .subscribe((configs) => {
        this.moduleConfigs = configs;
      });
    this.projectGenerationState.setModuleConfigs(this.moduleConfigs);
    const initialProjectId = trimmed(this.route.snapshot.queryParamMap.get('projectId'));
    if (!initialProjectId) {
      this.loadTabDefinitions(this.projectSettings.language, this.selectedDependencies);
    }

    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.requestedSectionFromRoute = this.resolveRequestedSection(params['section']);
        this.requestedExploreViewFromRoute = this.resolveRequestedExploreView(params['exploreView']);
        this.exploreView = this.requestedExploreViewFromRoute;
        if (params['projectId']) {
          this.backendProjectId = String(params['projectId']).trim();
          this.loadProject(this.backendProjectId);
          return;
        }

        if (!this.hasCheckedRecentProjectPrompt) {
          this.hasCheckedRecentProjectPrompt = true;
          this.promptForRecentProjectIfAvailable();
        }
      });

    this.contributorSearchInput$
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((query) => {
          const normalizedQuery = query.trim();
          if (normalizedQuery.length < 2) {
            this.contributorSearchResults = [];
            this.isContributorSearching = false;
            return of([] as UserSearchResult[]);
          }
          this.isContributorSearching = true;
          return this.userService.searchUsers(normalizedQuery).pipe(
            catchError(() => {
              this.contributorSearchResults = [];
              this.isContributorSearching = false;
              return of([] as UserSearchResult[]);
            })
          );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe((results: UserSearchResult[]) => {
        this.contributorSearchResults = results.map((user) => ({
          id: user.userId,
          label: user.name || user.userId,
          subtitle: user.email,
          avatarUrl: user.avatarUrl
        }));
        this.isContributorSearching = false;
      });

    this.trackChanges();
    this.syncActuatorStateStore();
  }

  ngOnDestroy(): void {
    this.cancelGenerationRequests();
    this.leaveProjectCollaboration();
    this.closeProjectEventsSource();
    if (this.changeTrackingIntervalId !== null) {
      window.clearInterval(this.changeTrackingIntervalId);
      this.changeTrackingIntervalId = null;
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  trackChanges(): void {
    this.refreshSavedProjectStateSnapshot();
    if (this.changeTrackingIntervalId !== null) {
      window.clearInterval(this.changeTrackingIntervalId);
    }
    this.changeTrackingIntervalId = window.setInterval(() => {
      this.hasUnsavedChanges = this.savedProjectStateSnapshot !== this.serializeProjectState();
    }, 1000);
  }

  getProjectData(): ProjectDraftState<ProjectSettings, DatabaseSettings, DeveloperPreferences> {
    return {
      id: this.backendProjectId || this.projectId || undefined,
      settings: this.projectSettings,
      database: this.databaseSettings,
      preferences: this.developerPreferences,
      controllers: {
        enabled: this.controllersConfigEnabled,
        config: this.controllersConfig
      },
      actuator: {
        selectedConfiguration: this.selectedActuatorConfiguration,
        configurations: this.sanitizeActuatorConfigurations(this.actuatorProfileEndpoints)
      },
      dependencies: this.dependencies,
      selectedDependencies: this.selectedDependencies,
      entities: this.entities,
      dataObjects: this.dataObjects,
      relations: this.relations,
      enums: this.enums,
      mappers: this.mappers,
      moduleConfigs: this.moduleConfigs
      ,
      selectedPlugins: this.selectedPlugins
    };
  }

  private buildDraftPayload(): ProjectDraftPayload {
    return {
      draftData: this.getProjectData(),
      draftVersion: this.draftVersion
    };
  }

  private buildDraftTabPatchPayload(tabKey: string): ProjectDraftTabPatchPayload {
    if (!this.isDraftBackedSection(tabKey)) {
      throw new Error(`Tab ${tabKey} does not support draft patching.`);
    }
    return {
      tabKey,
      tabData: this.getDraftTabData(tabKey),
      draftVersion: this.draftVersion
    };
  }

  private getDraftTabData(tabKey: string): Record<string, any> {
    switch (tabKey) {
      case 'general':
        return {
          settings: this.projectSettings,
          database: this.databaseSettings,
          preferences: this.developerPreferences,
          dependencies: this.dependencies,
          selectedDependencies: this.selectedDependencies
        };
      case 'actuator':
        return {
          actuator: {
            selectedConfiguration: this.selectedActuatorConfiguration,
            configurations: this.sanitizeActuatorConfigurations(this.actuatorProfileEndpoints)
          }
        };
      case 'entities':
        return {
          entities: this.entities,
          relations: this.relations
        };
      case 'data-objects':
        return {
          dataObjects: this.dataObjects,
          enums: this.enums
        };
      case 'mappers':
        return {
          mappers: this.mappers
        };
      case 'modules':
        return {
          dependencies: this.dependencies,
          selectedDependencies: this.selectedDependencies,
          moduleConfigs: this.moduleConfigs,
          selectedPlugins: this.selectedPlugins
        };
      case 'controllers':
        return {
          controllers: {
            enabled: this.controllersConfigEnabled,
            config: this.controllersConfig
          }
        };
      case 'rbac':
      case 'auth':
      case 'state-machine':
      case 'subscription':
      case 'swagger':
        return {
          moduleConfigs: {
            [tabKey]: this.moduleConfigs[tabKey] || {}
          }
        };
      default:
        return {};
    }
  }

  async loadProject(projectId: string): Promise<void> {
    this.isLoading = true;
    try {
      if (!this.isLoggedIn) {
        this.toastService.error(`User doesn't have access.`);
        this.navigateHome();
        return;
      }

      const [projectDetails] = await Promise.all([
        firstValueFrom(this.projectService.getProject(projectId))
      ]);
      this.resetLazyLoadedDraftSections();
      this.resetDraftBackedState();
      this.backendProjectId = projectDetails.projectId || projectId;
      this.projectOwnerId = projectDetails.ownerId || null;
      this.projectCanManageContributors = Boolean(projectDetails.canManageContributors);
      this.projectContributors = projectDetails.contributors || [];
      this.collaborationInviteToken = projectDetails.collaborationInviteToken || null;
      this.collaborationRequests = projectDetails.collaborationRequests || [];
      this.draftVersion = Number(projectDetails.draftVersion || 1);
      this.tabDefinitions = Array.isArray(projectDetails.tabDetails) ? projectDetails.tabDetails : this.tabDefinitions;
      this.tabDefinitionsLoadedFully = Array.isArray(projectDetails.tabDetails) && projectDetails.tabDetails.length > 1;
      this.projectSettings.language = normalizeProjectLanguage(projectDetails.generator || this.projectSettings.language);
      this.applyTabDefinitions(this.tabDefinitions);
      this.selectDefaultMigrationLanguage();
      this.hydrateExploreStateFromProjectDetails(projectDetails);
      this.refreshExploreRunHistory();
      this.connectProjectEvents(this.backendProjectId);
      this.initializeProjectCollaboration(this.backendProjectId);
      this.projectSettings.projectName = projectDetails.name || this.projectSettings.projectName;
      this.projectSettings.projectDescription = projectDetails.description || this.projectSettings.projectDescription;
      const requestedSection = this.requestedSectionFromRoute;
      const initialSection = this.isSectionAvailable(requestedSection)
        ? requestedSection
        : (this.isSectionAvailable(this.activeSection) ? this.activeSection : 'general');
      this.activeSection = initialSection;
      await this.ensureSectionDataLoaded(initialSection);
      await this.ensureSectionAuxiliaryDataLoaded(initialSection);
      this.toastService.success('Project loaded successfully');
      this.refreshSavedProjectStateSnapshot();
    } catch (error) {
        this.toastService.error(`User doesn't have access.`);
        this.navigateHome();
      console.error('Error loading project:', error);
    } finally {
      this.isLoading = false;
    }
  }

  loadProjectFromStorage(projectId: string): any {
    return loadProjectDraftFromStorage(projectId);
  }

  saveProject(): void {
    if (this.isLoggedIn) {
      void this.saveProjectDraftToBackend(true);
      return;
    }
    this.saveProjectLocally();
  }

  private saveProjectLocally(): void {
    const projectData = this.getProjectData();
    this.projectId = saveProjectDraftToStorage(projectData, this.backendProjectId, this.projectId);
    this.toastService.success('Project saved successfully');
    this.refreshSavedProjectStateSnapshot();
  }

  private async saveProjectDraftToBackend(showToast: boolean): Promise<string | null> {
    return this.persistProjectDraftToBackend(showToast, false);
  }

  private async saveEntireProjectDraftToBackend(showToast: boolean): Promise<string | null> {
    return this.persistProjectDraftToBackend(showToast, true);
  }

  private async persistProjectDraftToBackend(showToast: boolean, forceFullSave: boolean): Promise<string | null> {
    if (!this.isLoggedIn) {
      this.saveProjectLocally();
      return this.projectId;
    }

    const payload = this.buildDraftPayload();
    const existingProjectId = trimmed(this.backendProjectId);
    try {
      if (existingProjectId) {
        if (!forceFullSave && !this.isDraftBackedSection(this.activeSection)) {
          if (showToast) {
            this.toastService.success('Project saved successfully');
          }
          return existingProjectId;
        }
        const response = forceFullSave
          ? await firstValueFrom(this.projectService.updateProjectDraft(existingProjectId, payload))
          : await firstValueFrom(this.projectService.patchProjectDraftTab(existingProjectId, this.buildDraftTabPatchPayload(this.activeSection)));
        this.draftVersion = Number(response?.draftVersion || (this.draftVersion + 1));
        this.recordCollaborationAction(existingProjectId, this.activeSection, 'DRAFT_SAVED', 'Saved latest project spec changes.');
        if (showToast) {
          this.toastService.success('Project saved successfully');
        }
        this.refreshSavedProjectStateSnapshot();
        return existingProjectId;
      }

      if (this.pendingCreateDraftPromise) {
        return await this.pendingCreateDraftPromise;
      }

      if (!(await this.validateUniqueProjectNameForCurrentUser())) {
        if (showToast) {
          this.toastService.error('Project name already exists. Choose a different project name');
        }
        return null;
      }

      this.pendingCreateDraftPromise = (async () => {
        const response = await firstValueFrom(this.projectService.createProjectDraft(payload));
        const projectId = trimmed(response?.projectId);
        if (!projectId) {
          throw new Error('Project id is missing in response.');
        }
        this.backendProjectId = projectId;
        this.draftVersion = Number(response?.draftVersion || 1);
        this.connectProjectEvents(projectId);
        this.initializeProjectCollaboration(projectId);
        this.recordCollaborationAction(projectId, this.activeSection, 'DRAFT_CREATED', 'Created a saved draft for collaborative editing.');
        if (showToast) {
          this.toastService.success('Project saved successfully');
        }
        this.refreshSavedProjectStateSnapshot();
        return projectId;
      })();

      try {
        return await this.pendingCreateDraftPromise;
      } finally {
        this.pendingCreateDraftPromise = null;
      }
    } catch (error) {
      this.applyProjectSaveErrorState(error);
      if (showToast) {
        this.toastService.error(this.getProjectSaveErrorMessage(error));
      }
      console.error('Error saving project draft:', error);
      return null;
    }
  }

  saveProjectAndInvokeApi(): void {
    if (!this.validateProjectNaming()) {
      this.activeSection = 'general';
      return;
    }
    if (this.isLoggedIn) {
      this.generateAndDownloadProjectFromBackend();
      return;
    }
    this.saveProject();
    this.generateAndDownloadProjectForGuest();
  }

  private clearLegacyProjectStorageIfRequired(): void {
    const resetApplied = this.localStorageService.getItem(ProjectGenerationDashboardComponent.projectStorageResetKey);
    if (resetApplied === 'true') {
      return;
    }
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');
    this.localStorageService.setItem(ProjectGenerationDashboardComponent.projectStorageResetKey, 'true');
  }

  canManageContributors(): boolean {
    return Boolean(this.backendProjectId && this.projectCanManageContributors);
  }

  canDetachFromProject(): boolean {
    return Boolean(this.backendProjectId && this.isCurrentUserContributor());
  }

  isCurrentUserContributor(contributor?: ProjectContributor): boolean {
    const currentUserId = String(this.authService.currentUserValue?.id ?? '').trim();
    const currentUserEmail = String(this.authService.currentUserValue?.email ?? '').trim().toLowerCase();
    const contributorUserId = String(contributor?.userId ?? '').trim();
    if (contributor) {
      return contributorUserId === currentUserId
        || (!!currentUserEmail && contributorUserId.toLowerCase() === currentUserEmail);
    }
    return this.projectContributors.some((item) => this.isCurrentUserContributor(item));
  }

  get collaborationInviteUrl(): string {
    const token = trimmed(this.collaborationInviteToken);
    return token ? `${window.location.origin}/project-collaboration/${encodeURIComponent(token)}` : '';
  }

  async addContributor(): Promise<void> {
    const projectId = this.backendProjectId?.trim();
    const userId = this.contributorUserId.trim();
    if (!projectId) {
      this.toastService.error('Save project first before managing contributors.');
      return;
    }
    if (!userId) {
      this.toastService.error('Enter a contributor user id.');
      return;
    }
    this.isContributorSaving = true;
    try {
      this.projectContributors = await firstValueFrom(this.projectService.addProjectContributor(projectId, userId));
      this.contributorUserId = '';
      this.toastService.success('Contributor added successfully.');
    } catch (error) {
      this.toastService.error('Failed to add contributor.');
      console.error('Error adding contributor:', error);
    } finally {
      this.isContributorSaving = false;
    }
  }

  async removeContributor(userId: string): Promise<void> {
    const projectId = this.backendProjectId?.trim();
    if (!projectId) {
      this.toastService.error('Save project first before managing contributors.');
      return;
    }
    this.isContributorSaving = true;
    try {
      await firstValueFrom(this.projectService.removeProjectContributor(projectId, userId));
      this.projectContributors = this.projectContributors.filter((contributor) => contributor.userId !== userId);
      this.toastService.success('Contributor removed successfully.');
    } catch (error) {
      this.toastService.error('Failed to remove contributor.');
      console.error('Error removing contributor:', error);
    } finally {
      this.isContributorSaving = false;
    }
  }

  openAddContributorModal(): void {
    this.showAddContributorModal = true;
    this.contributorSearchResults = [];
    this.selectedContributorItems = [];
    this.isContributorSearching = false;
  }

  closeAddContributorModal(): void {
    this.showAddContributorModal = false;
    this.contributorSearchResults = [];
    this.selectedContributorItems = [];
  }

  onContributorSearch(query: string): void {
    if (!query || query.length < 2) {
      this.contributorSearchResults = [];
      this.isContributorSearching = false;
      this.contributorSearchInput$.next('');
      return;
    }
    this.contributorSearchInput$.next(query);
  }

  get existingContributorIds(): string[] {
    return stableArray(this.projectContributors.map((c) => c.userId), this._existingContribIdsCache);
  }

  async submitAddContributors(): Promise<void> {
    const projectId = this.backendProjectId?.trim();
    if (!projectId) {
      this.toastService.error('Save project first before managing contributors.');
      return;
    }
    if (this.selectedContributorItems.length === 0) {
      this.toastService.error('Select at least one user to add.');
      return;
    }
    this.isAddingContributors = true;
    try {
      for (const item of this.selectedContributorItems) {
        this.projectContributors = await firstValueFrom(this.projectService.addProjectContributor(projectId, item.id));
      }
      this.toastService.success('Contributors added successfully.');
      this.closeAddContributorModal();
    } catch (error) {
      this.toastService.error('Failed to add contributors.');
      console.error('Error adding contributors:', error);
    } finally {
      this.isAddingContributors = false;
    }
  }

  async detachFromProject(): Promise<void> {
    const projectId = this.backendProjectId?.trim();
    if (!projectId || !this.canDetachFromProject()) {
      return;
    }
    this.isContributorSaving = true;
    try {
      await firstValueFrom(this.projectService.detachProjectContributor(projectId));
      this.toastService.success('Project archived from your collaborations.');
      this.router.navigate(['/user/dashboard/projects']);
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to archive this collaboration.');
    } finally {
      this.isContributorSaving = false;
    }
  }

  copyCollaborationInviteUrl(): void {
    const inviteUrl = this.collaborationInviteUrl;
    if (!inviteUrl) {
      this.toastService.error('Save project first before sharing collaboration access.');
      return;
    }
    navigator.clipboard.writeText(inviteUrl)
      .then(() => this.toastService.success('Collaboration invite URL copied.'))
      .catch(() => this.toastService.error('Failed to copy collaboration invite URL.'));
  }

  async refreshCollaborationRequests(): Promise<void> {
    const projectId = trimmed(this.backendProjectId);
    if (!projectId || !this.canManageContributors()) {
      return;
    }
    try {
      this.collaborationRequests = await firstValueFrom(this.projectService.getProjectCollaborationRequests(projectId));
    } catch (error) {
      console.error('Failed to refresh collaboration requests:', error);
    }
  }

  async approveCollaborationRequest(request: ProjectCollaborationRequest): Promise<void> {
    const projectId = trimmed(this.backendProjectId);
    const requestId = trimmed(request?.id);
    if (!projectId || !requestId) {
      return;
    }
    try {
      await firstValueFrom(this.projectService.reviewProjectCollaborationRequest(projectId, requestId, {
        status: 'ACCEPTED',
        ...request.grantedPermissions
      }));
      await this.refreshCollaborationRequests();
      this.projectContributors = await firstValueFrom(this.projectService.getProjectContributors(projectId));
      this.toastService.success('Collaboration request approved.');
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to approve collaboration request.');
    }
  }

  async rejectCollaborationRequest(request: ProjectCollaborationRequest): Promise<void> {
    const projectId = trimmed(this.backendProjectId);
    const requestId = trimmed(request?.id);
    if (!projectId || !requestId) {
      return;
    }
    try {
      await firstValueFrom(this.projectService.reviewProjectCollaborationRequest(projectId, requestId, {
        status: 'REJECTED',
        canEditDraft: false,
        canGenerate: false,
        canManageCollaboration: false
      }));
      await this.refreshCollaborationRequests();
      this.toastService.success('Collaboration request rejected.');
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to reject collaboration request.');
    }
  }

  async updateContributorPermissions(contributor: ProjectContributor): Promise<void> {
    const projectId = trimmed(this.backendProjectId);
    const contributorId = trimmed(contributor?.id);
    if (!projectId || !contributorId) {
      return;
    }
    const permissions: ProjectContributorPermissions = {
      canEditDraft: Boolean(contributor.canEditDraft),
      canGenerate: Boolean(contributor.canGenerate),
      canManageCollaboration: Boolean(contributor.canManageCollaboration)
    };
    try {
      this.projectContributors = await firstValueFrom(this.projectService.updateProjectContributorPermissions(projectId, contributorId, permissions));
      this.syncSelectedContributorForMobileEdit(contributorId);
      this.toastService.success('Contributor permissions updated.');
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to update contributor permissions.');
    }
  }

  get isMobileViewport(): boolean {
    return typeof window !== 'undefined' && window.innerWidth <= 768;
  }

  openContributorPermissionsModal(contributor: ProjectContributor): void {
    this.selectedContributorForMobileEdit = { ...contributor };
    this.showContributorPermissionsModal = true;
  }

  closeContributorPermissionsModal(): void {
    this.showContributorPermissionsModal = false;
    this.selectedContributorForMobileEdit = null;
  }

  async saveContributorPermissionsFromModal(): Promise<void> {
    if (!this.selectedContributorForMobileEdit) {
      return;
    }
    await this.updateContributorPermissions(this.selectedContributorForMobileEdit);
    this.closeContributorPermissionsModal();
  }

  formatPermissionsLabel(permissions?: ProjectContributorPermissions): string {
    if (!permissions) {
      return 'No actions';
    }
    const actions = [
      permissions.canEditDraft ? 'Edit spec' : '',
      permissions.canGenerate ? 'Generate' : '',
      permissions.canManageCollaboration ? 'Manage collaboration' : ''
    ].filter(Boolean);
    return actions.length ? actions.join(', ') : 'No actions';
  }

  private syncSelectedContributorForMobileEdit(contributorId: string): void {
    if (!this.selectedContributorForMobileEdit || !contributorId) {
      return;
    }
    if (trimmed(this.selectedContributorForMobileEdit.id) !== contributorId) {
      return;
    }
    const updatedContributor = this.projectContributors.find((item) => trimmed(item.id) === contributorId);
    if (updatedContributor) {
      this.selectedContributorForMobileEdit = { ...updatedContributor };
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    if (this.isSidebarOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
    document.body.style.overflow = '';
  }

  async navigateToSection(section: string): Promise<boolean> {
    if (section !== 'controllers') {
      this.cancelControllerRestSpecEdit();
      this.showControllersConfigDiscardConfirmation = false;
    }
    if (section === 'entities' && toDatabaseCode(this.databaseSettings.database) === 'NONE') {
      this.activeSection = 'general';
      this.closeSidebar();
      return false;
    }
    if (section === 'actuator' && !this.developerPreferences.enableActuator) {
      this.activeSection = 'general';
      this.closeSidebar();
      return false;
    }
    if (section === 'controllers' && !this.developerPreferences.configureApi) {
      this.activeSection = 'general';
      this.closeSidebar();
      return false;
    }
    if (section === 'mappers') {
      this.dataObjectsDefaultTab = 'mappers';
    } else if (section === 'data-objects') {
      this.dataObjectsDefaultTab = 'dataObjects';
    }
    if (section === 'explore') {
      const previousSection = this.activeSection;
      await this.handleExploreTab(previousSection);
      this.closeSidebar();
      return true;
    }
    if (section === 'collaborate') {
      this.activeSection = section;
      await this.refreshCollaborationRequests();
      this.closeSidebar();
      return true;
    }
    if (!this.tabDefinitionsLoadedFully && section !== 'general') {
      this.loadTabDefinitions(this.projectSettings.language, this.selectedDependencies);
    }
    if (this.isLoggedIn && section !== this.activeSection && this.canPersistDraftSilently()) {
      const savedProjectId = await this.saveProjectDraftToBackend(false);
      if (!savedProjectId) {
        this.closeSidebar();
        return false;
      }
    }
    try {
      await this.ensureSectionDataLoaded(section);
      await this.ensureSectionAuxiliaryDataLoaded(section);
    } catch (error) {
      this.toastService.error('Failed to load project section.');
      console.error('Error loading project section:', error);
      this.closeSidebar();
      return false;
    }
    this.activeSection = section;
    this.closeSidebar();
    return true;
  }

  async handleExploreTab(previousSection: string): Promise<void> {
    if (this.isExploreSyncing) {
      return;
    }
    this.exploreView = 'runs';
    this.selectDefaultMigrationLanguage();

    if (!this.validateProjectNaming()) {
      this.activeSection = 'general';
      return;
    }

    if (this.isLoggedIn && this.backendProjectId) {
      try {
        await this.ensureAllDraftSectionsLoaded();
      } catch (error) {
        this.toastService.error('Failed to load project data for explore.');
        this.activeSection = previousSection;
        console.error('Error preparing project data for explore:', error);
        return;
      }
    }

    const yamlSpec = this.buildCurrentProjectYaml();
    if (this.useCachedZipIfAvailable(yamlSpec, true)) {
      if (this.isLoggedIn) {
        this.refreshExploreRunHistory();
      }
      return;
    }

    if (!this.isLoggedIn) {
      this.generateExploreZipWithoutProjectId(previousSection);
      return;
    }

    const projectId = this.backendProjectId?.trim();
    if (!projectId) {
      this.toastService.error('Please save project first to generate and explore zip.');
      this.activeSection = previousSection;
      return;
    }

    this.activeSection = 'explore';
    this.isExploreSyncing = true;
    const runsUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.LIST_BY_PROJECT(projectId)}`;

    this.http.get<ProjectRunSummary[]>(runsUrl)
      .pipe(finalize(() => {
        this.isExploreSyncing = false;
      }))
      .subscribe({
        next: async (runs) => {
          this.exploreRuns = this.sortExploreRuns(runs);
          this.exploreStageEvents = [];
          this.exploreZipBlob = null;
          this.exploreZipFileName = `${toArtifactId(this.projectSettings.projectName || projectId)}.zip`;

          const downloadableRun = this.getLatestDownloadableRun(runs);
          if (downloadableRun) {
            const runId = this.getRunIdentifier(downloadableRun);
            if (!runId) {
              return;
            }
            try {
              await this.downloadAndPrepareExploreZip(runId, projectId, yamlSpec);
              return;
            } catch {
              this.toastService.error('Failed to load the saved project zip.');
            }
          }

          const latestRun = this.getLatestRun(this.exploreRuns);
          const latestStatus = String(latestRun?.status ?? '').toUpperCase();
          if (latestStatus === 'QUEUED' || latestStatus === 'INPROGRESS') {
            this.connectProjectEvents(projectId);
          }
        },
        error: () => {
          this.toastService.error('Failed to check generation status.');
          this.activeSection = previousSection;
        }
      });
  }

  private generateAndDownloadProjectFromBackend(): void {
    if (this.isGeneratingFromDtoSave) {
      return;
    }

    this.isGeneratingFromDtoSave = true;
    this.pendingGenerationYamlSpec = null;

    void (async () => {
      try {
        await this.ensureAllDraftSectionsLoaded();
        const yamlSpec = this.buildCurrentProjectYaml();
        if (this.useCachedZipIfAvailable(yamlSpec, true)) {
          return;
        }
        if (!this.validateYamlSpecPayloadSize(yamlSpec)) {
          return;
        }
        this.clearLocalZipDataBeforeGeneration();
        this.pendingGenerationYamlSpec = yamlSpec;
        const projectId = await this.saveProjectDraftToBackend(false);
        if (!projectId) {
          this.pendingGenerationYamlSpec = null;
          return;
        }

        await firstValueFrom(this.projectService.generateProject(projectId));
        this.toastService.success('Project generation started. Waiting for zip...');
        this.connectProjectEvents(projectId);
      } catch (error) {
        this.pendingGenerationYamlSpec = null;
        this.toastService.error(this.getProjectSaveErrorMessage(error));
      } finally {
        this.isGeneratingFromDtoSave = false;
      }
    })();
  }

  private generateAndDownloadProjectForGuest(): void {
    if (this.isGeneratingFromDtoSave) {
      return;
    }

    const yamlSpec = this.buildCurrentProjectYaml();
    if (this.useCachedZipIfAvailable(yamlSpec, true)) {
      return;
    }
    if (!this.validateYamlSpecPayloadSize(yamlSpec)) {
      return;
    }
    this.clearLocalZipDataBeforeGeneration();

    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT_VIEW.GENERATE_ZIP}`;
    this.isGeneratingFromDtoSave = true;

    this.generationGuestSubscription = this.http.post(url, yamlSpec, {
      headers: { 'Content-Type': 'text/plain' },
      responseType: 'arraybuffer'
    })
      .pipe(finalize(() => {
        this.isGeneratingFromDtoSave = false;
      }))
      .subscribe({
        next: (zipData) => {
          if (!zipData || zipData.byteLength === 0) {
            this.toastService.error('Generated zip is empty.');
            return;
          }

          this.exploreZipBlob = new Blob([zipData], { type: 'application/zip' });
          this.exploreZipFileName = `${toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
          this.cacheZipFromArrayBuffer(yamlSpec, zipData, this.exploreZipFileName);
          this.isExplorePreviewOpen = false;
          this.activeExplorePreviewRunId = '';
          this.activeSection = 'explore';
          this.toastService.success('Project generated successfully.');
          this.generationGuestSubscription = null;
        },
        error: () => {
          this.toastService.error('Failed to generate project zip.');
          this.generationGuestSubscription = null;
        }
      });
  }

  onCancelGenerationRequested(): void {
    if (!this.isGeneratingFromDtoSave) {
      return;
    }
    this.showGenerationCancelConfirmation = true;
  }

  confirmCancelGenerationRequest(): void {
    this.showGenerationCancelConfirmation = false;
    this.cancelGenerationRequests();
    this.closeProjectEventsSource();
    this.isGeneratingFromDtoSave = false;
    this.toastService.success('Project generation canceled.');
  }

  cancelCancelGenerationRequest(): void {
    this.showGenerationCancelConfirmation = false;
  }

  private buildCurrentProjectYaml(): string {
    return this.specMapper.buildYaml(this.getProjectData());
  }

  private async ensureAllDraftSectionsLoaded(): Promise<void> {
    if (!this.isLoggedIn || !this.backendProjectId) {
      return;
    }
    const draftSections = this.visibleNavItems
      .map((item) => item.value)
      .filter((section, index, values) => values.indexOf(section) === index)
      .filter((section) => this.isDraftBackedSection(section));
    await Promise.all(draftSections.map((section) => this.ensureSectionDataLoaded(section)));
  }

  private async ensureSectionDataLoaded(section: string): Promise<void> {
    const normalizedSection = trimmed(section);
    if (!this.isLoggedIn || !this.backendProjectId || !normalizedSection || !this.isDraftBackedSection(normalizedSection)) {
      return;
    }
    if (this.loadedDraftSections.has(normalizedSection)) {
      return;
    }
    const existingPromise = this.draftSectionLoadPromises.get(normalizedSection);
    if (existingPromise) {
      return existingPromise;
    }
    const loadPromise = (async () => {
      const response = await firstValueFrom(this.projectService.getProjectDraftTab(this.backendProjectId!, normalizedSection));
      this.applyDraftTabData(normalizedSection, response);
      await this.ensureSectionAuxiliaryDataLoaded(normalizedSection);
      this.loadedDraftSections.add(normalizedSection);
      this.refreshSavedProjectStateSnapshot();
    })();
    this.draftSectionLoadPromises.set(normalizedSection, loadPromise);
    try {
      await loadPromise;
    } finally {
      this.draftSectionLoadPromises.delete(normalizedSection);
    }
  }

  private applyDraftTabData(section: string, response: ProjectDraftTabData): void {
    const tabData = response?.tabData || {};
    switch (section) {
      case 'general': {
        const settings = tabData['settings'] || {};
        const database = tabData['database'] || {};
        const preferences = tabData['preferences'] || {};
        this.projectSettings = {
          ...this.projectSettings,
          ...settings
        };
        this.projectSettings.projectName = this.projectSettings.projectName || this.projectSettings.projectName;
        this.databaseSettings = {
          ...this.databaseSettings,
          ...database
        };
        this.databaseSettings.dbType = resolveDatabaseType(this.databaseSettings.dbType, this.databaseSettings.database);
        this.databaseSettings.database = toDatabaseCode(this.databaseSettings.database);
        this.ensureDatabaseSelectionForType();
        this.previousDatabaseSelection = this.databaseSettings.database;
        this.previousDatabaseType = this.databaseSettings.dbType;
        this.developerPreferences = {
          ...this.developerPreferences,
          ...preferences,
          enableActuator: Boolean((preferences as Record<string, unknown>)['enableActuator']),
          configureApi: (preferences as Record<string, unknown>)['configureApi'] === undefined
            ? this.developerPreferences.configureApi
            : Boolean((preferences as Record<string, unknown>)['configureApi']),
          enableLombok: (preferences as Record<string, unknown>)['enableLombok'] === undefined
            ? Boolean((preferences as Record<string, unknown>)['optionalLombok'])
            : Boolean((preferences as Record<string, unknown>)['enableLombok'])
        };
        this.applyDependencyDraftData(tabData);
        this.syncActuatorConfigurationsWithProfiles();
        this.syncActuatorStateStore();
        return;
      }
      case 'modules':
        this.applyDependencyDraftData(tabData);
        if (tabData['moduleConfigs'] && typeof tabData['moduleConfigs'] === 'object') {
          this.moduleConfigs = {
            ...this.moduleConfigs,
            ...(tabData['moduleConfigs'] as Record<string, any>)
          };
          this.projectGenerationState.setModuleConfigs(this.moduleConfigs);
        }
        this.applySelectedPluginsDraftData(tabData);
        return;
      case 'actuator': {
        const actuator = (tabData['actuator'] as Record<string, any> | undefined) || {};
        const configurationOptions = this.specMapper.getActuatorConfigurationOptions(this.developerPreferences.profiles);
        this.actuatorConfigurationOptions = configurationOptions;
        this.actuatorProfileEndpoints = this.sanitizeActuatorConfigurations(
          actuator['configurations'] ?? actuator['endpoints'],
          configurationOptions
        );
        const selectedConfig = this.normalizeActuatorConfigurationName(actuator['selectedConfiguration']) ?? 'default';
        this.selectedActuatorConfiguration = configurationOptions.includes(selectedConfig) ? selectedConfig : 'default';
        this.syncActuatorStateStore();
        return;
      }
      case 'entities':
        this.entities = Array.isArray(tabData['entities']) ? tabData['entities'] as any[] : [];
        this.relations = Array.isArray(tabData['relations']) ? tabData['relations'] as any[] : [];
        return;
      case 'data-objects':
        this.dataObjects = Array.isArray(tabData['dataObjects']) ? tabData['dataObjects'] as any[] : [];
        this.enums = Array.isArray(tabData['enums']) ? tabData['enums'] as any[] : [];
        return;
      case 'mappers':
        this.mappers = Array.isArray(tabData['mappers']) ? tabData['mappers'] as any[] : [];
        return;
      case 'controllers': {
        const controllers = (tabData['controllers'] as Record<string, any> | undefined) || {};
        this.controllersConfigEnabled = controllers['enabled'] === undefined
          ? true
          : Boolean(controllers['enabled']);
        this.controllersConfig = this.specMapper.parseControllersConfig(controllers['config']);
        return;
      }
      case 'rbac':
      case 'auth':
      case 'state-machine':
      case 'subscription':
      case 'swagger':
        if (tabData['moduleConfigs'] && typeof tabData['moduleConfigs'] === 'object') {
          this.moduleConfigs = {
            ...this.moduleConfigs,
            ...(tabData['moduleConfigs'] as Record<string, any>)
          };
          this.projectGenerationState.setModuleConfigs(this.moduleConfigs);
        }
        return;
      default:
        return;
    }
  }

  private applyDependencyDraftData(tabData: Record<string, any>): void {
    const selectedDependencies = tabData['selectedDependencies'];
    const dependencies = tabData['dependencies'];
    if (Array.isArray(selectedDependencies)) {
      this.selectedDependencies = this.normalizeSelectedDependencies(selectedDependencies
        .map((dependency: unknown) => String(dependency ?? '').trim())
        .filter((dependency: string) => dependency.length > 0));
    } else if (typeof dependencies === 'string' && dependencies.trim()) {
      this.selectedDependencies = this.normalizeSelectedDependencies(dependencies
        .split(',')
        .map((dependency: string) => dependency.trim())
        .filter((dependency: string) => dependency.length > 0));
    }
    this.dependencies = typeof dependencies === 'string'
      ? this.selectedDependencies.join(', ')
      : this.selectedDependencies.join(', ');
  }

  private applySelectedPluginsDraftData(tabData: Record<string, any>): void {
    const selectedPlugins = tabData['selectedPlugins'];
    this.selectedPlugins = Array.isArray(selectedPlugins)
      ? selectedPlugins
          .filter((plugin): plugin is Record<string, any> => !!plugin && typeof plugin === 'object')
          .map((plugin) => ({
            pluginId: String(plugin['pluginId'] ?? ''),
            versionId: String(plugin['versionId'] ?? ''),
            code: String(plugin['code'] ?? ''),
            name: String(plugin['name'] ?? ''),
            versionCode: String(plugin['versionCode'] ?? '')
          }))
          .filter((plugin) => plugin.pluginId.length > 0 && plugin.versionId.length > 0)
      : [];
  }

  onSelectedPluginsChange(selectedPlugins: PluginModuleSelection[]): void {
    this.selectedPlugins = Array.isArray(selectedPlugins) ? [...selectedPlugins] : [];
  }

  private async loadPublishedPluginModules(generator?: string): Promise<void> {
    if (!this.isLoggedIn) {
      this.publishedPluginModules = [];
      return;
    }
    const normalizedGenerator = trimmed(generator) || trimmed(this.projectSettings.language);
    if (
      this.publishedPluginModulesLoadedGenerator === normalizedGenerator
      && this.publishedPluginModules.length > 0
    ) {
      return;
    }
    if (this.publishedPluginModulesLoadPromise) {
      return this.publishedPluginModulesLoadPromise;
    }
    try {
      this.publishedPluginModulesLoadPromise = firstValueFrom(this.projectService.getPublishedPluginModules(normalizedGenerator))
        .then((modules) => {
          this.publishedPluginModules = (Array.isArray(modules) ? modules : []).map((module: PublishedPluginModule) => ({
            id: module.id,
            code: module.code,
            name: module.name,
            description: module.description,
            category: module.category,
            enabled: module.enabled,
            currentPublishedVersionId: module.currentPublishedVersionId,
            versions: (module.versions || []).map((version) => ({
              id: version.id,
              versionCode: version.versionCode,
              published: version.published
            }))
          }));
          this.publishedPluginModulesLoadedGenerator = normalizedGenerator;
        });
      await this.publishedPluginModulesLoadPromise;
    } catch {
      this.publishedPluginModules = [];
      this.publishedPluginModulesLoadedGenerator = null;
    } finally {
      this.publishedPluginModulesLoadPromise = null;
    }
  }

  private async ensureSectionAuxiliaryDataLoaded(section: string): Promise<void> {
    if (!this.isLoggedIn) {
      return;
    }
    if (section === 'modules') {
      await this.loadPublishedPluginModules(this.projectSettings.language);
    }
  }

  private resetLazyLoadedDraftSections(): void {
    this.loadedDraftSections.clear();
    this.draftSectionLoadPromises.clear();
  }

  private resetDraftBackedState(): void {
    this.entities = [];
    this.dataObjects = [];
    this.relations = [];
    this.enums = [];
    this.mappers = [];
    this.moduleConfigs = {};
    this.projectGenerationState.setModuleConfigs(this.moduleConfigs);
    this.selectedPlugins = [];
    this.dependencies = '';
    this.selectedDependencies = [];
    this.controllersConfig = { ...DEFAULT_CONTROLLERS_CONFIG };
    this.controllersConfigEnabled = true;
    this.projectSettings = { ...DEFAULT_PROJECT_SETTINGS };
    this.databaseSettings = { ...DEFAULT_DATABASE_SETTINGS };
    this.developerPreferences = { ...DEFAULT_DEVELOPER_PREFERENCES, profiles: [] };
    this.syncActuatorConfigurationsWithProfiles();
    this.syncActuatorStateStore();
  }

  private getProjectSaveErrorMessage(error: any): string {
    const message = this.extractProjectSaveErrorMessage(error);
    if (message.toLowerCase().includes('project name already exists')) {
      return 'Project name already exists. Choose a different project name';
    }
    return message || 'Failed to save and start project generation.';
  }

  private connectProjectEvents(projectId: string): void {
    if (!projectId) {
      return;
    }
    this.closeProjectEventsSource();

    const url = this.buildProjectRealtimeSocketUrl(projectId);
    const source = new WebSocket(url);
    this.projectEventsSource = source;

    source.onopen = () => {
      this.initializeProjectCollaboration(projectId);
    };

    source.onmessage = (event: MessageEvent) => {
      const envelope = this.parseJsonPayload(event.data);
      if (!envelope) {
        return;
      }
      const eventName = String(envelope.event ?? '').trim();
      const payload = envelope.payload;
      switch (eventName) {
        case 'stage':
          this.upsertExploreStageEvent(payload);
          break;
        case 'presence':
          this.applyCollaborationState(payload);
          break;
        case 'presence.registered':
          this.collaborationSessionId = String(payload?.sessionId ?? '').trim() || this.collaborationSessionId;
          this.applyCollaborationState(payload);
          if (this.collaborationSessionId) {
            this.startCollaborationHeartbeat(projectId, this.collaborationSessionId);
          }
          break;
        case 'collaboration-action':
          this.prependCollaborationAction(payload);
          break;
        case 'generation': {
          const status = String(payload?.status ?? '').toUpperCase();
          if (status === 'SUCCESS' && Boolean(payload?.hasZip)) {
            void this.handleGeneratedZipEvent(projectId, payload);
          } else if (status === 'ERROR') {
            const message = typeof payload?.message === 'string' && payload.message.trim()
              ? payload.message.trim()
              : 'Project generation failed.';
            this.refreshExploreRunHistory();
            this.toastService.error(message);
          }
          break;
        }
        default:
          break;
      }
    };

    source.onerror = () => {
      this.stopCollaborationHeartbeat();
      this.closeProjectEventsSource();
    };

    source.onclose = () => {
      this.stopCollaborationHeartbeat();
      this.projectEventsSource = null;
    };
  }

  private buildProjectRealtimeSocketUrl(projectId: string): string {
    const origin = new URL(API_CONFIG.BASE_URL);
    const protocol = origin.protocol === 'https:' ? 'wss:' : 'ws:';
    const baseUrl = `${protocol}//${origin.host}/ws/projects/${projectId}`;
    const token = this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (!token) {
      return baseUrl;
    }
    return `${baseUrl}?access_token=${encodeURIComponent(token)}`;
  }

  private parseJsonPayload(raw: any): any | null {
    if (typeof raw !== 'string' || !raw.trim()) {
      return null;
    }

    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  private upsertExploreStageEvent(payload: any): void {
    const stage = String(payload?.stage ?? '').trim();
    if (!stage) {
      return;
    }

    const nextEvent: ProjectGenerationStageEvent = {
      stage,
      stepName: typeof payload?.stepName === 'string' ? payload.stepName.trim() : '',
      stepOrder: Number(payload?.stepOrder || 0) || undefined,
      totalSteps: Number(payload?.totalSteps || 0) || undefined,
      attempt: Number(payload?.attempt || 0) || undefined,
      retryEnabled: Boolean(payload?.retryEnabled),
      runId: typeof payload?.runId === 'string' ? payload.runId : undefined,
      status: String(payload?.status ?? 'UNKNOWN').trim() || 'UNKNOWN',
      message: typeof payload?.message === 'string' ? payload.message.trim() : '',
      timestamp: typeof payload?.timestamp === 'string' ? payload.timestamp : undefined
    };

    const existingIndex = this.exploreStageEvents.findIndex((event) => event.stage === stage);
    if (existingIndex >= 0) {
      this.exploreStageEvents[existingIndex] = nextEvent;
      this.exploreStageEvents = [...this.exploreStageEvents];
      return;
    }

    this.exploreStageEvents = [...this.exploreStageEvents, nextEvent];
  }

  private async handleGeneratedZipEvent(projectId: string, payload: any): Promise<void> {
    const yamlForCache = this.pendingGenerationYamlSpec || this.buildCurrentProjectYaml();
    const runId = String(payload?.runId ?? '').trim();

    try {
      if (runId) {
        await this.downloadAndPrepareExploreZip(runId, projectId, yamlForCache);
      } else {
        await this.refreshExploreRunHistoryAndLoadZip(projectId, yamlForCache);
      }
      this.refreshExploreRunHistory();
      this.activeSection = 'explore';
      this.toastService.success('Project zip is ready.');
    } catch {
      this.toastService.error('Failed to load the generated project zip.');
    }
  }

  private downloadZipFile(base64Payload: string, fileName: string, yamlSpec: string): void {
    try {
      const bytes = this.base64ToUint8Array(base64Payload);
      this.exploreZipBlob = new Blob([bytes.buffer as ArrayBuffer], { type: 'application/zip' });
      this.exploreZipFileName = fileName || `${toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
      this.cacheZipFromBase64(yamlSpec, base64Payload, this.exploreZipFileName);
      this.activeExplorePreviewRunId = '';
      this.isExplorePreviewOpen = false;
      this.activeSection = 'explore';
    } catch {
      this.toastService.error('Failed to download generated zip file.');
    }
  }

  private closeProjectEventsSource(): void {
    if (this.projectEventsSource) {
      this.projectEventsSource.close();
      this.projectEventsSource = null;
    }
    this.pendingGenerationYamlSpec = null;
  }

  private initializeProjectCollaboration(projectId: string | null): void {
    const normalizedProjectId = trimmed(projectId);
    if (!normalizedProjectId || !this.isLoggedIn) {
      return;
    }
    this.stopCollaborationHeartbeat();
    this.sendProjectRealtimeMessage('presence.register', {
      sessionId: this.collaborationSessionId ?? undefined
    });
  }

  private startCollaborationHeartbeat(projectId: string, sessionId: string): void {
    this.stopCollaborationHeartbeat();
    this.collaborationHeartbeatId = window.setInterval(() => {
      this.sendProjectRealtimeMessage('presence.heartbeat', { sessionId });
    }, 15000);
  }

  private stopCollaborationHeartbeat(): void {
    if (this.collaborationHeartbeatId !== null) {
      window.clearInterval(this.collaborationHeartbeatId);
      this.collaborationHeartbeatId = null;
    }
  }

  private leaveProjectCollaboration(): void {
    const sessionId = trimmed(this.collaborationSessionId);
    this.stopCollaborationHeartbeat();
    if (!sessionId || !this.isLoggedIn) {
      return;
    }
    this.sendProjectRealtimeMessage('presence.leave', { sessionId });
  }

  private recordCollaborationAction(projectId: string, tabKey: string, actionType: string, message: string): void {
    const sessionId = trimmed(this.collaborationSessionId);
    if (!projectId || !sessionId || !this.isLoggedIn) {
      return;
    }
    this.sendProjectRealtimeMessage('collaboration.action', {
      sessionId,
      tabKey,
      actionType,
      draftVersion: this.draftVersion,
      message
    });
  }

  private sendProjectRealtimeMessage(type: string, payload: Record<string, unknown>): void {
    if (!this.projectEventsSource || this.projectEventsSource.readyState !== WebSocket.OPEN) {
      return;
    }
    try {
      this.projectEventsSource.send(JSON.stringify({ type, payload }));
    } catch {
      console.error('Failed to send project realtime message.');
    }
  }

  private applyCollaborationState(state: Partial<ProjectCollaborationState> | null | undefined): void {
    this.collaborationState = {
      activeEditors: Number(state?.activeEditors || 0),
      editors: Array.isArray(state?.editors) ? state?.editors : [],
      recentActions: Array.isArray(state?.recentActions) ? state?.recentActions : this.collaborationState.recentActions
    };
  }

  private prependCollaborationAction(action: Partial<ProjectCollaborationAction>): void {
    if (!action?.actionId) {
      return;
    }
    const recentActions = this.collaborationState.recentActions.filter((item) => item.actionId !== action.actionId);
    this.collaborationState = {
      ...this.collaborationState,
      recentActions: [action as ProjectCollaborationAction, ...recentActions].slice(0, 25)
    };
  }

  private cancelGenerationRequests(): void {
    if (this.generationGuestSubscription) {
      this.generationGuestSubscription.unsubscribe();
      this.generationGuestSubscription = null;
    }
  }

  private getLatestRun(runs: ProjectRunSummary[] | null | undefined): ProjectRunSummary | null {
    const sortedRuns = this.sortExploreRuns(runs);
    if (sortedRuns.length === 0) {
      return null;
    }
    return sortedRuns[0] ?? null;
  }

  private async downloadAndPrepareExploreZip(runId: string, projectId: string, yamlSpec?: string): Promise<void> {
    const downloadUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.DOWNLOAD(runId)}`;
    const zipData = await firstValueFrom(this.http.get(downloadUrl, { responseType: 'arraybuffer' }));
    if (!zipData) {
      throw new Error('Zip payload is empty');
    }

    this.exploreZipBlob = new Blob([zipData], { type: 'application/zip' });
    this.exploreZipFileName = `${toArtifactId(this.projectSettings.projectName || projectId)}.zip`;
    this.activeExplorePreviewRunId = '';
    this.isExplorePreviewOpen = false;
    if (yamlSpec) {
      this.cacheZipFromArrayBuffer(yamlSpec, zipData, this.exploreZipFileName);
    }
  }

  private async refreshExploreRunHistoryAndLoadZip(projectId: string, yamlSpec: string): Promise<void> {
    const runsUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.LIST_BY_PROJECT(projectId)}`;
    const runs = await firstValueFrom(this.http.get<ProjectRunSummary[]>(runsUrl));
    this.exploreRuns = this.sortExploreRuns(runs);
    const downloadableRun = this.getLatestDownloadableRun(runs);
    const runId = this.getRunIdentifier(downloadableRun);
    if (!runId) {
      throw new Error('Generated zip is not available yet.');
    }
    await this.downloadAndPrepareExploreZip(runId, projectId, yamlSpec);
  }

  private generateExploreZipWithoutProjectId(previousSection: string): void {
    const yamlSpec = this.buildCurrentProjectYaml();
    if (this.useCachedZipIfAvailable(yamlSpec, true)) {
      return;
    }
    if (!this.validateYamlSpecPayloadSize(yamlSpec)) {
      this.activeSection = previousSection;
      return;
    }

    this.isExploreSyncing = true;
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT_VIEW.GENERATE_ZIP}`;

    this.http.post(url, yamlSpec, {
      headers: { 'Content-Type': 'text/plain' },
      responseType: 'arraybuffer'
    })
      .pipe(finalize(() => {
        this.isExploreSyncing = false;
      }))
      .subscribe({
        next: (zipData) => {
          if (!zipData || zipData.byteLength === 0) {
            this.toastService.error('Generated zip is empty.');
            this.activeSection = previousSection;
            return;
          }

          this.exploreZipBlob = new Blob([zipData], { type: 'application/zip' });
          this.exploreZipFileName = `${toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
          this.cacheZipFromArrayBuffer(yamlSpec, zipData, this.exploreZipFileName);
          this.activeSection = 'explore';
        },
        error: () => {
          this.toastService.error('Failed to generate project preview zip.');
          this.activeSection = previousSection;
        }
      });
  }

  reloadExplore(): void {
    this.closeExplorePreview();
    this.handleExploreTab(this.activeSection);
  }

  canOpenExploreRunPreview(run: ProjectRunSummary | null | undefined): boolean {
    return Boolean(run?.hasZip && this.getRunIdentifier(run));
  }

  async openExploreRunPreview(run?: ProjectRunSummary | null): Promise<void> {
    const runId = this.getRunIdentifier(run);
    if (!runId) {
      if (this.exploreZipBlob) {
        this.activeExplorePreviewRunId = '';
        this.isExplorePreviewOpen = true;
      }
      return;
    }

    if (this.isExploreSyncing) {
      return;
    }

    if (this.activeExplorePreviewRunId === runId && this.exploreZipBlob) {
      this.isExplorePreviewOpen = true;
      return;
    }

    const projectId = trimmed(run?.projectId || this.backendProjectId);
    if (!projectId) {
      this.toastService.error('Failed to load project preview for the selected run.');
      return;
    }

    this.isExploreSyncing = true;
    try {
      await this.downloadAndPrepareExploreZip(runId, projectId);
      this.activeExplorePreviewRunId = runId;
      this.isExplorePreviewOpen = true;
    } catch {
      this.toastService.error('Failed to load project preview for the selected run.');
    } finally {
      this.isExploreSyncing = false;
    }
  }

  closeExplorePreview(): void {
    this.isExplorePreviewOpen = false;
  }

  isExploreRunsView(): boolean {
    return this.exploreView === 'runs';
  }

  isExploreMigrationView(): boolean {
    return this.exploreView === 'migrate';
  }

  showExploreRunsView(): void {
    this.exploreView = 'runs';
  }

  showExploreMigrationView(): void {
    this.closeExplorePreview();
    this.selectDefaultMigrationLanguage();
    this.exploreView = 'migrate';
  }

  getCurrentProjectLanguageLabel(): string {
    return getProjectLanguageLabel(this.projectSettings.language);
  }

  getCurrentProjectLanguageRuntime(): string {
    return getProjectLanguageOption(this.projectSettings.language).runtime;
  }

  getMigrationTargetLanguageOptions(): ProjectLanguageOption[] {
    return getMigrationTargetLanguageOptions(this.projectSettings.language);
  }

  selectMigrationLanguage(language: SupportedProjectLanguage): void {
    this.selectedMigrationLanguage = language;
  }

  canRunProjectMigration(): boolean {
    return this.isLoggedIn;
  }

  canSubmitProjectMigration(): boolean {
    const selectedLanguage = this.selectedMigrationLanguage;
    return Boolean(
      this.canRunProjectMigration()
      && selectedLanguage
      && selectedLanguage !== normalizeProjectLanguage(this.projectSettings.language)
      && !this.isMigratingProject
    );
  }

  async migrateProjectLanguage(): Promise<void> {
    if (this.isMigratingProject) {
      return;
    }

    if (!this.isLoggedIn) {
      this.toastService.error('Sign in and save the project before running a language migration.');
      return;
    }

    if (!this.validateProjectNaming()) {
      this.activeSection = 'general';
      return;
    }

    const targetLanguage = this.selectedMigrationLanguage;
    if (!targetLanguage) {
      this.toastService.error('Select a target language to migrate the project.');
      return;
    }

    const currentLanguage = normalizeProjectLanguage(this.projectSettings.language);
    if (targetLanguage === currentLanguage) {
      this.toastService.error('Choose a different target language to start migration.');
      return;
    }

    const previousLanguage = currentLanguage;
    this.isMigratingProject = true;
    try {
      await this.ensureAllDraftSectionsLoaded();
      this.projectSettings.language = targetLanguage;
      this.loadTabDefinitions(targetLanguage, this.selectedDependencies);
      await this.loadPublishedPluginModules(targetLanguage);
      this.clearLocalZipDataBeforeGeneration();
      const projectId = await this.saveEntireProjectDraftToBackend(false);
      if (!projectId) {
        throw new Error('Project id is missing after saving the migrated draft.');
      }

      await firstValueFrom(this.projectService.generateProject(projectId));
      this.connectProjectEvents(projectId);
      this.refreshExploreRunHistory();
      this.activeSection = 'explore';
      this.exploreView = 'runs';
      this.toastService.success(`Queued a fresh ${getProjectLanguageLabel(targetLanguage)} migration run.`);
      await this.router.navigate([resolveProjectGenerationRoute(targetLanguage)], {
        queryParams: {
          projectId,
          section: 'explore',
          exploreView: 'runs'
        }
      });
    } catch (error) {
      this.projectSettings.language = previousLanguage;
      this.loadTabDefinitions(previousLanguage, this.selectedDependencies);
      await this.loadPublishedPluginModules(previousLanguage);
      this.selectDefaultMigrationLanguage();
      this.toastService.error(this.getProjectSaveErrorMessage(error));
      console.error('Error migrating project language:', error);
    } finally {
      this.isMigratingProject = false;
    }
  }

  getExploreEmptyMessage(): string {
    const latestStatus = String(this.exploreRuns[0]?.status ?? '').toUpperCase();
    if (latestStatus === 'QUEUED' || latestStatus === 'INPROGRESS') {
      return 'Project generation in progress. The generated zip will appear here once the current run completes.';
    }
    return 'No generated zip is saved for this project yet. Recent generation activity will appear here once runs start.';
  }

  get activeEditorsBadgeLabel(): string {
    return `${this.collaborationState.activeEditors} editing`;
  }

  shouldShowActiveEditorsBadge(): boolean {
    return this.collaborationState.activeEditors > 1;
  }

  isGenerationInProgress(): boolean {
    return this.exploreStageEvents.some((event) => ['INPROGRESS', 'RETRYING'].includes(String(event.status || '').toUpperCase()))
      || ['QUEUED', 'INPROGRESS'].includes(String(this.exploreRuns[0]?.status ?? '').toUpperCase());
  }

  getExploreStageRows(): ProjectGenerationStageEvent[] {
    return [...this.exploreStageEvents].sort((left, right) => {
      const leftOrder = Number(left.stepOrder || 0);
      const rightOrder = Number(right.stepOrder || 0);
      if (leftOrder !== rightOrder) {
        return leftOrder - rightOrder;
      }
      return this.formatExploreStageLabel(left.stage).localeCompare(this.formatExploreStageLabel(right.stage));
    });
  }

  getExploreStageProgress(event: ProjectGenerationStageEvent): string {
    const current = Number(event.stepOrder || 0);
    const total = Number(event.totalSteps || 0);
    if (!current || !total) {
      return 'Waiting';
    }
    return `${current} / ${total}`;
  }

  canRetryExploreStage(event: ProjectGenerationStageEvent): boolean {
    return Boolean(trimmed(this.backendProjectId))
      && String(event.status || '').toUpperCase() === 'ERROR'
      && !this.isGeneratingFromDtoSave;
  }

  retryExploreStage(event: ProjectGenerationStageEvent): void {
    const projectId = trimmed(this.backendProjectId);
    if (!projectId) {
      return;
    }

    this.projectService.retryProjectStage(projectId, event.stage, event.runId).subscribe({
      next: () => {
        this.toastService.success(`Queued a fresh generation run after ${this.formatExploreStageLabel(event.stage)} failed.`);
        this.refreshExploreRunHistory();
      },
      error: () => {
        this.toastService.error('Failed to queue retry for the selected generation stage.');
      }
    });
  }

  private useCachedZipIfAvailable(yamlSpec: string, switchToExplore: boolean): boolean {
    const cacheKey = this.getZipCacheKey();
    const cachedEntry = this.localStorageService.getProjectZipCache(cacheKey);
    if (!cachedEntry || cachedEntry.yamlSpec !== yamlSpec || !cachedEntry.zipBase64) {
      return false;
    }

    try {
      const bytes = this.base64ToUint8Array(cachedEntry.zipBase64);
      this.exploreZipBlob = new Blob([bytes.buffer as ArrayBuffer], { type: 'application/zip' });
      this.exploreZipFileName = cachedEntry.fileName || `${toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
      this.activeExplorePreviewRunId = '';
      this.isExplorePreviewOpen = false;
      if (switchToExplore) {
        this.activeSection = 'explore';
      }
      return true;
    } catch {
      this.localStorageService.clearProjectZipCache(cacheKey);
      return false;
    }
  }

  private validateYamlSpecPayloadSize(yamlSpec: string): boolean {
    const payloadBytes = new Blob([yamlSpec ?? ''], { type: 'text/plain' }).size;
    if (payloadBytes <= this.maxYamlSpecPayloadBytes) {
      return true;
    }

    const payloadSizeMb = (payloadBytes / (1024 * 1024)).toFixed(2);
    this.toastService.error(`Spec YAML size (${payloadSizeMb} MB) exceeds the 2 MB limit. Reduce payload and try again.`);
    return false;
  }

  private cacheZipFromArrayBuffer(yamlSpec: string, zipData: ArrayBuffer, fileName: string): void {
    if (!zipData || zipData.byteLength === 0) {
      return;
    }

    this.localStorageService.setProjectZipCache(this.getZipCacheKey(), {
      yamlSpec,
      zipBase64: this.arrayBufferToBase64(zipData),
      fileName: fileName || 'project.zip',
      updatedAt: Date.now()
    });
  }

  private cacheZipFromBase64(yamlSpec: string, zipBase64: string, fileName: string): void {
    if (!yamlSpec || !zipBase64) {
      return;
    }

    this.localStorageService.setProjectZipCache(this.getZipCacheKey(), {
      yamlSpec,
      zipBase64,
      fileName: fileName || 'project.zip',
      updatedAt: Date.now()
    });
  }

  private hydrateExploreStateFromProjectDetails(projectDetails: ProjectDetails): void {
    const projectId = trimmed(projectDetails.projectId || String(projectDetails.id || ''));
    const projectYaml = typeof projectDetails.yaml === 'string' ? projectDetails.yaml : '';
    this.exploreStageEvents = [];
    this.exploreRuns = [];

    if (projectDetails.latestRunStatus) {
      this.exploreRuns = [{
        runId: projectDetails.latestRunId,
        id: projectDetails.latestRunId,
        projectId: projectId || this.backendProjectId || '',
        status: projectDetails.latestRunStatus,
        hasZip: Boolean(projectDetails.latestRunHasZip),
        runNumber: projectDetails.latestRunNumber
      }];
    }

    if (!projectDetails.latestRunHasZip || !projectDetails.latestRunZipBase64) {
      this.exploreZipBlob = null;
      this.exploreZipFileName = projectDetails.latestRunZipFileName || 'project.zip';
      this.activeExplorePreviewRunId = '';
      this.isExplorePreviewOpen = false;
      return;
    }

    this.downloadZipFile(
      projectDetails.latestRunZipBase64,
      projectDetails.latestRunZipFileName || 'project.zip',
      projectYaml
    );
  }

  private getZipCacheKey(): string {
    const backendId = trimmed(this.backendProjectId);
    const localProjectId = this.projectId ? String(this.projectId) : '';
    const artifactId = toArtifactId(this.projectSettings.projectName || 'project');
    const scope = this.isLoggedIn ? 'auth' : 'guest';
    return [scope, backendId || localProjectId || artifactId].join(':');
  }

  private hasCachedZipForCurrentProject(): boolean {
    const cacheEntry = this.localStorageService.getProjectZipCache(this.getZipCacheKey());
    return Boolean(cacheEntry?.zipBase64);
  }

  private canAccessExplore(): boolean {
    return this.hasCachedZipForCurrentProject() || (this.isLoggedIn && Boolean(trimmed(this.backendProjectId)));
  }

  private clearLocalZipDataBeforeGeneration(): void {
    this.localStorageService.clearProjectZipCache(this.getZipCacheKey());
    this.exploreZipBlob = null;
    this.exploreZipFileName = 'project.zip';
    this.exploreRuns = [];
    this.exploreStageEvents = [];
    this.activeExplorePreviewRunId = '';
    this.isExplorePreviewOpen = false;
    if (this.activeSection === 'explore') {
      this.activeSection = 'general';
    }
  }

  private sortExploreRuns(runs: ProjectRunSummary[] | null | undefined): ProjectRunSummary[] {
    if (!Array.isArray(runs)) {
      return [];
    }
    return [...runs].sort((left, right) => {
      const leftDate = new Date(left.updatedAt ?? left.createdAt ?? '').getTime();
      const rightDate = new Date(right.updatedAt ?? right.createdAt ?? '').getTime();
      if (leftDate === rightDate) {
        return (Number(right.runNumber) || 0) - (Number(left.runNumber) || 0);
      }
      return rightDate - leftDate;
    });
  }

  private getRunIdentifier(run: ProjectRunSummary | null | undefined): string {
    return String(run?.runId ?? run?.id ?? '').trim();
  }

  private getLatestDownloadableRun(runs: ProjectRunSummary[] | null | undefined): ProjectRunSummary | null {
    return this.sortExploreRuns(runs).find((run) => Boolean(run?.hasZip && this.getRunIdentifier(run))) ?? null;
  }

  private refreshExploreRunHistory(): void {
    const projectId = trimmed(this.backendProjectId);
    if (!projectId || !this.isLoggedIn) {
      return;
    }

    const runsUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.LIST_BY_PROJECT(projectId)}`;
    this.http.get<ProjectRunSummary[]>(runsUrl).subscribe({
      next: (runs) => {
        this.exploreRuns = this.sortExploreRuns(runs);
      },
      error: () => {
        console.error('Failed to refresh project run history.');
      }
    });
  }

  getExploreRunStatusClass(status: string | null | undefined): string {
    switch (String(status ?? '').toUpperCase()) {
      case 'SUCCESS':
      case 'DONE':
        return 'explore-status-badge-success';
      case 'ERROR':
      case 'FAILED':
      case 'CANCELLED':
        return 'explore-status-badge-error';
      case 'INPROGRESS':
      case 'QUEUED':
        return 'explore-status-badge-progress';
      default:
        return 'explore-status-badge-neutral';
    }
  }

  getExploreEventCount(): number {
    return this.exploreStageEvents.length || this.exploreRuns.length;
  }

  formatExploreStageLabel(stage: string | null | undefined): string {
    const normalized = String(stage ?? '').trim();
    if (!normalized) {
      return 'Unknown stage';
    }
    return normalized
      .toLowerCase()
      .split('_')
      .filter(Boolean)
      .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
      .join(' ');
  }

  trackExploreRun = (_index: number, run: ProjectRunSummary): string => {
    return this.getRunIdentifier(run) || `${run.runNumber ?? _index}`;
  };

  trackExploreStageEvent = (_index: number, event: ProjectGenerationStageEvent): string => {
    return event.stage;
  };

  private base64ToUint8Array(base64Payload: string): Uint8Array {
    const binary = window.atob(base64Payload);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i += 1) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    const chunkSize = 0x8000;
    let binary = '';

    for (let i = 0; i < bytes.length; i += chunkSize) {
      const chunk = bytes.subarray(i, i + chunkSize);
      binary += String.fromCharCode(...chunk);
    }

    return window.btoa(binary);
  }

  handleHome(): void {
    if (this.hasUnsavedChanges) {
      this.showBackConfirmation = true;
    } else {
      this.navigateHome();
    }
  }

  openDocumentation(
    event: Event,
    section: 'general' | 'actuator' | 'entities' | 'dataObjects' | 'controllers' | 'mappers' | 'explore'
  ): void {
    event.preventDefault();
    this.router.navigate(['/documentation'], { queryParams: { section } });
  }

  confirmBack(): void {
    this.showBackConfirmation = false;
    this.navigateHome();
  }

  cancelBack(): void {
    this.showBackConfirmation = false;
  }

  confirmRecentProjectResume(): void {
    const recentProjectId = this.recentProjectToResume?.id;
    this.showRecentProjectPrompt = false;
    this.recentProjectToResume = null;

    if (!recentProjectId) {
      return;
    }

    const savedProject = this.loadProjectFromStorage(String(recentProjectId));
    if (!savedProject) {
      this.toastService.error('Saved project draft was not found.');
      return;
    }

    this.resumeProjectFromLocalDraft(savedProject, String(recentProjectId));
  }

  cancelRecentProjectResume(): void {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.clear();
    }
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');
    this.showRecentProjectPrompt = false;
    this.recentProjectToResume = null;
  }

  navigateHome(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/user/dashboard']);
    } else {
      this.router.navigate(['/']);
    }
  }

  loadDependencies(forceReload = false): Promise<void> {
    if (!forceReload && this.availableDependencies.length > 0) {
      return Promise.resolve();
    }
    if (!forceReload && this.dependenciesLoadPromise) {
      return this.dependenciesLoadPromise;
    }

    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.DEPENDENCIES.LIST}`;
    this.dependenciesLoadPromise = firstValueFrom(this.http.get<string[]>(url))
      .then((dependencies) => {
        this.availableDependencies = Array.isArray(dependencies) ? dependencies : [];
      })
      .catch(() => {
        this.toastService.error('Failed to load dependencies');
        this.availableDependencies = [];
      })
      .finally(() => {
        this.dependenciesLoadPromise = null;
      });

    return this.dependenciesLoadPromise;
  }

  filterDependencies(value: string): void {
    if (!value || value.trim() === '') {
      this.filteredDependencies = [];
      return;
    }

    if (!this.availableDependencies.length) {
      void this.loadDependencies().then(() => this.filterDependencies(value));
      return;
    }

    const filterValue = value.toLowerCase();
    this.filteredDependencies = this.availableDependencies
      .filter(dep =>
        dep.toLowerCase().includes(filterValue) &&
        !ProjectGenerationDashboardComponent.shippableModuleKeys.includes(dep) &&
        !this.selectedDependencies.includes(dep)
      )
      .slice(0, 10);
  }

  addDependency(event: any): void {
    const value = event.option.value;
    if (value && !this.selectedDependencies.includes(value)) {
      this.selectedDependencies.push(value);
      this.dependencies = this.selectedDependencies.join(', ');
      this.refreshModuleTabDefinitions();
    }
    this.dependencyInput = '';
    this.filteredDependencies = [];
  }

  removeDependency(dep: string): void {
    const index = this.selectedDependencies.indexOf(dep);
    if (index >= 0) {
      this.selectedDependencies.splice(index, 1);
      this.dependencies = this.selectedDependencies.join(', ');
      if (ProjectGenerationDashboardComponent.shippableModuleKeys.includes(dep)) {
        delete this.moduleConfigs[dep];
        this.projectGenerationState.removeModuleConfig(dep);
      }
      this.refreshModuleTabDefinitions();
    }
  }

  get selectedShippableModules(): string[] {
    return stableArray(
      this.selectedDependencies.filter((dependency) =>
        ProjectGenerationDashboardComponent.shippableModuleKeys.includes(dependency)
      ),
      this._selectedShippableModulesCache
    );
  }

  onSelectedShippableModulesChange(moduleKeys: string[]): void {
    const customDependencies = this.selectedDependencies.filter((dependency) =>
      !ProjectGenerationDashboardComponent.shippableModuleKeys.includes(dependency)
    );
    this.selectedDependencies = this.normalizeSelectedDependencies([...customDependencies, ...moduleKeys]);
    this.dependencies = this.selectedDependencies.join(', ');
    this.refreshModuleTabDefinitions();
  }

  getModulesPrimaryActionLabel(): string {
    return this.developerPreferences.configureApi ? 'Proceed To Controller' : 'Save Project';
  }

  async handleModulesPrimaryAction(): Promise<void> {
    if (this.developerPreferences.configureApi) {
      await this.proceedFromModules();
      return;
    }
    await this.saveProjectAndInvokeApi();
  }

  isModuleSectionActive(moduleKey: string): boolean {
    return this.isLoggedIn && this.activeSection === moduleKey;
  }

  private refreshModuleTabDefinitions(): void {
    if (!this.isLoggedIn) {
      return;
    }
    this.loadTabDefinitions(this.projectSettings.language, this.selectedDependencies);
  }

  onProjectGroupChange(): void {
    if (this.projectGroupError) {
      this.projectGroupError = '';
    }
  }

  onProjectNameChange(): void {
    if (this.projectNameError) {
      this.projectNameError = '';
    }
  }

  onProjectDescriptionChange(): void {
    if (this.projectDescriptionError) {
      this.projectDescriptionError = '';
    }
  }

  onEntitiesChange(entities: any[]): void {
    this.entities = entities;
  }

  onEntitiesFromDataObjectsChange(entities: any[]): void {
    this.entities = entities;
  }

  onDataObjectsChange(dataObjects: any[]): void {
    this.dataObjects = dataObjects;
    if (this.dataObjects.length === 0 && this.activeSection === 'mappers') {
      this.activeSection = 'data-objects';
      this.dataObjectsDefaultTab = 'dataObjects';
    }
  }

  onEnumsChange(enums: any[]): void {
    this.enums = enums;
  }

  onMappersChange(mappers: any[]): void {
    this.mappers = mappers;
  }

  onDataObjectsActiveTabChange(tab: 'dataObjects' | 'enums' | 'mappers'): void {
    this.dataObjectsActiveTab = tab;
  }

  async setupEntities(): Promise<void> {
    this.isLoading = true;
    try {
      if (await this.navigateToSection('entities')) {
        this.toastService.success('Entity setup loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async setupDataObjects(): Promise<void> {
    this.isLoading = true;
    try {
      this.dataObjectsDefaultTab = 'dataObjects';
      if (await this.navigateToSection('data-objects')) {
        this.toastService.success('Data objects setup loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async setupControllers(): Promise<void> {
    this.isLoading = true;
    try {
      if (await this.navigateToSection('controllers')) {
        this.toastService.success('Controller setup loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async setupModules(): Promise<void> {
    this.isLoading = true;
    try {
      if (await this.navigateToSection('modules')) {
        this.toastService.success('Module selection loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  private async proceedFromModules(): Promise<void> {
    this.isLoading = true;
    try {
      if (!(await this.navigateToSection('modules'))) {
        return;
      }
      await this.reloadTabDefinitionsForCurrentSelection();
      const nextSection = this.getNextModuleConfigurationSection()
        ?? (this.developerPreferences.configureApi ? 'controllers' : null);
      if (!nextSection) {
        await this.saveProjectAndInvokeApi();
        return;
      }
      if (await this.navigateToSection(nextSection)) {
        this.toastService.success(this.getSectionLoadedMessage(nextSection));
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async setupMappers(): Promise<void> {
    this.isLoading = true;
    try {
      this.dataObjectsDefaultTab = 'mappers';
      if (await this.navigateToSection('mappers')) {
        this.toastService.success('Mapper setup loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async setupActuator(): Promise<void> {
    this.isLoading = true;
    try {
      if (await this.navigateToSection('actuator')) {
        this.toastService.success('Actuator setup loaded');
      }
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  getGeneralPrimaryActionLabel(): string {
    if (this.activeSection === 'general' && this.developerPreferences.enableActuator) {
      return 'Setup Actuator';
    }
    return this.isEntitiesTabVisible() ? 'Setup Entities' : 'Setup Data Object';
  }

  async handleGeneralPrimaryAction(): Promise<void> {
    if (this.activeSection === 'general' && this.developerPreferences.enableActuator) {
      await this.setupActuator();
      return;
    }
    if (this.isEntitiesTabVisible()) {
      await this.setupEntities();
      return;
    }
    await this.setupDataObjects();
  }

  getDataObjectsPrimaryActionLabel(): string {
    if (this.dataObjects.length === 0) {
      if (!this.isLoggedIn) {
        return this.developerPreferences.configureApi ? 'Setup Controller' : 'Save Project';
      }
      return 'Setup Modules';
    }
    return 'Setup Mappers';
  }

  async handleDataObjectsPrimaryAction(): Promise<void> {
    if (this.dataObjects.length === 0) {
      if (!this.isLoggedIn) {
        if (this.developerPreferences.configureApi) {
          await this.setupControllers();
          return;
        }
        await this.saveProjectAndInvokeApi();
        return;
      }
      await this.setupModules();
      return;
    }
    await this.setupMappers();
  }

  getMappersPrimaryActionLabel(): string {
    if (!this.isLoggedIn) {
      return this.developerPreferences.configureApi ? 'Setup Controller' : 'Save Project';
    }
    return 'Setup Modules';
  }

  async handleMappersPrimaryAction(): Promise<void> {
    if (!this.isLoggedIn) {
      if (this.developerPreferences.configureApi) {
        await this.setupControllers();
        return;
      }
      await this.saveProjectAndInvokeApi();
      return;
    }
    await this.setupModules();
  }

  onAddProfileClick(): void {
    this.showProfileModal = true;
  }

  onEnableActuatorChange(enabled: boolean): void {
    this.developerPreferences.enableActuator = Boolean(enabled);
    if (!this.developerPreferences.enableActuator && this.activeSection === 'actuator') {
      this.activeSection = 'general';
      return;
    }
    if (this.developerPreferences.enableActuator) {
      this.syncActuatorConfigurationsWithProfiles();
      this.syncActuatorStateStore();
    }
  }

  onEnableConfigureApiChange(event: MatCheckboxChange): void {
    const nextEnabled = Boolean(event?.checked);
    this.configureApiToggleSource = event?.source ?? null;
    if (nextEnabled) {
      this.developerPreferences.configureApi = true;
      if (this.configureApiToggleSource) {
        this.configureApiToggleSource.checked = true;
      }
      this.cdr.detectChanges();
      return;
    }

    this.developerPreferences.configureApi = true;
    if (this.configureApiToggleSource) {
      this.configureApiToggleSource.checked = true;
    }
    this.showConfigureApiDisableConfirmation = true;
    this.cdr.detectChanges();
  }

  confirmDisableConfigureApi(): void {
    this.developerPreferences.configureApi = false;
    if (this.configureApiToggleSource) {
      this.configureApiToggleSource.checked = false;
    }
    this.clearAllApiConfigurations();
    if (this.activeSection === 'controllers') {
      this.activeSection = 'general';
    }
    this.showConfigureApiDisableConfirmation = false;
    this.configureApiToggleSource = null;
    this.cdr.detectChanges();
  }

  cancelDisableConfigureApi(): void {
    this.developerPreferences.configureApi = true;
    if (this.configureApiToggleSource) {
      this.configureApiToggleSource.checked = true;
    }
    this.showConfigureApiDisableConfirmation = false;
    this.configureApiToggleSource = null;
    this.cdr.detectChanges();
  }

  onControllersConfigSave(config: RestEndpointConfig, persistAsGlobal = true): void {
    const sanitizedConfig = this.specMapper.parseControllersConfig(config);
    if (persistAsGlobal) {
      this.controllersConfig = sanitizedConfig;
      this.controllersConfigEnabled = true;
    }
    this.applyControllersEntityMapping(sanitizedConfig, this.controllersEditingSpecKey);
    this.toastService.success('Controller configuration updated');
  }

  onControllersConfigCancel(): void {
    // Keep existing in-page configuration unchanged on cancel.
  }

  get controllerRestSpecRows(): ControllerRestSpecRow[] {
    const rowsByName = new Map<string, ControllerRestSpecRow>();
    const entityList = Array.isArray(this.entities) ? this.entities : [];

    entityList.forEach((entity: any, index: number) => {
      if (!Boolean(entity?.addRestEndpoints) || !entity?.restConfig) {
        return;
      }
      const normalizedConfig = this.specMapper.parseControllersConfig(entity.restConfig);
      const name = trimmed(normalizedConfig.resourceName);
      const entityName = trimmed(entity?.name);
      if (!name || !entityName) {
        return;
      }
      const mappedToEntity = Boolean(normalizedConfig.mapToEntity);

      const existing = rowsByName.get(name);
      if (!existing) {
        rowsByName.set(name, {
          key: name,
          name,
          totalEndpoints: this.countEnabledEndpoints(normalizedConfig),
          mappedEntities: mappedToEntity ? [entityName] : [],
          entityIndexes: [index],
          hasControllersConfig: false
        });
        return;
      }

      existing.totalEndpoints = this.countEnabledEndpoints(normalizedConfig);
      if (mappedToEntity && !existing.mappedEntities.includes(entityName)) {
        existing.mappedEntities.push(entityName);
      }
      if (!existing.entityIndexes.includes(index)) {
        existing.entityIndexes.push(index);
      }
    });

    const controllerConfigName = trimmed(this.controllersConfig?.resourceName);
    if (this.controllersConfigEnabled && controllerConfigName) {
      const normalizedControllerConfig = this.specMapper.parseControllersConfig(this.controllersConfig);
      const existingControllerRow = rowsByName.get(controllerConfigName);
      if (!existingControllerRow) {
        const mappedEntityName = normalizedControllerConfig.mapToEntity
          ? trimmed(normalizedControllerConfig.mappedEntityName)
          : '';
        rowsByName.set(controllerConfigName, {
          key: controllerConfigName,
          name: controllerConfigName,
          totalEndpoints: this.countEnabledEndpoints(normalizedControllerConfig),
          mappedEntities: mappedEntityName ? [mappedEntityName] : [],
          entityIndexes: mappedEntityName
            ? entityList
              .map((entity: any, index: number) => ({ name: trimmed(entity?.name), index }))
              .filter((item) => item.name === mappedEntityName)
              .map((item) => item.index)
            : [],
          hasControllersConfig: true
        });
      } else {
        existingControllerRow.totalEndpoints = this.countEnabledEndpoints(normalizedControllerConfig);
        existingControllerRow.hasControllersConfig = true;
        if (normalizedControllerConfig.mapToEntity) {
          const mappedEntityName = trimmed(normalizedControllerConfig.mappedEntityName);
          if (mappedEntityName && !existingControllerRow.mappedEntities.includes(mappedEntityName)) {
            existingControllerRow.mappedEntities.push(mappedEntityName);
          }
          const mappedIndex = entityList.findIndex((entity: any) => trimmed(entity?.name) === mappedEntityName);
          if (mappedIndex >= 0 && !existingControllerRow.entityIndexes.includes(mappedIndex)) {
            existingControllerRow.entityIndexes.push(mappedIndex);
          }
        }
      }
    }

    // Ensure entity-to-spec mapping works even when entity carries only rest-spec name
    // and not full inline restConfig.
    entityList.forEach((entity: any, index: number) => {
      if (!Boolean(entity?.addRestEndpoints)) {
        return;
      }
      // If inline restConfig exists, mapping is already handled above using mapToEntity flag.
      if (entity?.restConfig) {
        return;
      }
      const entityName = trimmed(entity?.name);
      const specName = this.resolveEntityRestSpecName(entity);
      if (!entityName || !specName) {
        return;
      }

      const row = rowsByName.get(specName);
      if (!row) {
        rowsByName.set(specName, {
          key: specName,
          name: specName,
          totalEndpoints: 0,
          mappedEntities: [entityName],
          entityIndexes: [index],
          hasControllersConfig: false
        });
        return;
      }

      if (!row.mappedEntities.includes(entityName)) {
        row.mappedEntities.push(entityName);
      }
      if (!row.entityIndexes.includes(index)) {
        row.entityIndexes.push(index);
      }
    });

    return stableArray(
      Array.from(rowsByName.values()).sort((a, b) => a.name.localeCompare(b.name)),
      this._controllerRestSpecRowsCache
    );
  }

  get isControllersSpecEditMode(): boolean {
    return Boolean(this.controllersEditingSpecKey && this.controllersEditingSpecConfig);
  }

  get isControllersSpecCreateMode(): boolean {
    return this.controllersCreatingNewConfig;
  }

  get showControllersConfigEditor(): boolean {
    return this.isControllersSpecEditMode || this.isControllersSpecCreateMode;
  }

  get controllersEditModeExistingNames(): string[] {
    const rows = this.controllerRestSpecRows;
    if (!this.controllersEditingSpecKey) {
      return stableArray(rows.map((row) => row.name), this._controllersEditModeExistingNamesCache);
    }
    return stableArray(
      rows.filter((row) => row.key !== this.controllersEditingSpecKey).map((row) => row.name),
      this._controllersEditModeExistingNamesCache
    );
  }

  editControllerRestSpec(row: ControllerRestSpecRow): void {
    this.controllersCreatingNewConfig = false;
    this.controllersEditingSpecKey = row.key;
    if (row.hasControllersConfig) {
      this.controllersEditingSpecConfig = this.specMapper.parseControllersConfig(this.controllersConfig);
      return;
    }
    const firstEntityIndex = row.entityIndexes[0];
    const entity = firstEntityIndex >= 0 ? this.entities[firstEntityIndex] : null;
    this.controllersEditingSpecConfig = entity?.restConfig
      ? this.specMapper.parseControllersConfig(entity.restConfig)
      : null;
    if (this.controllersEditingSpecConfig) {
      const currentMapToEntity = Boolean((this.controllersEditingSpecConfig as any).mapToEntity);
      const currentMappedEntity = trimmed((this.controllersEditingSpecConfig as any).mappedEntityName);
      if (!currentMapToEntity || !currentMappedEntity) {
        const firstMapped = trimmed(row.mappedEntities?.[0]);
        (this.controllersEditingSpecConfig as any).mapToEntity = Boolean(firstMapped);
        (this.controllersEditingSpecConfig as any).mappedEntityName = firstMapped;
      }
    }
    if (!this.controllersEditingSpecConfig) {
      this.controllersEditingSpecKey = null;
    }
  }

  cancelControllerRestSpecEdit(): void {
    this.controllersCreatingNewConfig = false;
    this.controllersEditingSpecKey = null;
    this.controllersEditingSpecConfig = null;
  }

  startNewControllerRestSpec(): void {
    this.controllersCreatingNewConfig = true;
    this.controllersEditingSpecKey = null;
    this.controllersEditingSpecConfig = null;
  }

  onControllersEditorSave(config: RestEndpointConfig): void {
    if (!this.isControllersSpecEditMode || !this.controllersEditingSpecKey) {
      this.onControllersConfigSave(config, true);
      this.controllersCreatingNewConfig = false;
      this.cancelControllerRestSpecEdit();
      return;
    }

    const editingKey = trimmed(this.controllersEditingSpecKey);
    const globalKey = trimmed(this.controllersConfig?.resourceName);
    const editingGlobalConfig = Boolean(this.controllersConfigEnabled && editingKey && editingKey === globalKey);

    this.onControllersConfigSave(config, editingGlobalConfig);
    this.cancelControllerRestSpecEdit();
  }

  onControllersEditorCancel(): void {
    if (this.isControllersSpecCreateMode) {
      this.cancelControllerRestSpecEdit();
      return;
    }
    if (this.isControllersSpecEditMode) {
      this.cancelControllerRestSpecEdit();
      return;
    }
    this.onControllersConfigCancel();
  }

  saveControllersConfiguration(): void {
    this.controllersRestConfigComponent?.saveConfig();
  }

  requestCancelControllersConfiguration(): void {
    this.showControllersConfigDiscardConfirmation = true;
  }

  confirmCancelControllersConfiguration(): void {
    this.showControllersConfigDiscardConfirmation = false;
    this.onControllersEditorCancel();
  }

  cancelCancelControllersConfiguration(): void {
    this.showControllersConfigDiscardConfirmation = false;
  }

  requestDeleteControllerRestSpec(row: ControllerRestSpecRow): void {
    this.pendingRestSpecDeleteKey = row.key;
    this.restSpecDeleteConfirmationConfig = {
      ...this.restSpecDeleteConfirmationConfig,
      message: `Delete REST configuration "${row.name}" and remove mapped model references?`
    };
    this.showRestSpecDeleteConfirmation = true;
  }

  confirmDeleteControllerRestSpec(): void {
    const key = this.pendingRestSpecDeleteKey;
    if (!key) {
      this.cancelDeleteControllerRestSpec();
      return;
    }

    const row = this.controllerRestSpecRows.find((item) => item.key === key);
    if (!row) {
      this.cancelDeleteControllerRestSpec();
      return;
    }

    row.entityIndexes.forEach((entityIndex) => {
      const entity = this.entities[entityIndex];
      if (!entity) {
        return;
      }
      entity.addRestEndpoints = false;
      entity.restConfig = undefined;
    });
    this.entities = [...this.entities];

    if (row.hasControllersConfig && trimmed(this.controllersConfig?.resourceName) === key) {
      this.controllersConfigEnabled = false;
      this.controllersConfig = { ...DEFAULT_CONTROLLERS_CONFIG };
    }

    if (this.controllersEditingSpecKey === key) {
      this.cancelControllerRestSpecEdit();
    }

    this.cancelDeleteControllerRestSpec();
    this.toastService.success('REST configuration deleted');
  }

  cancelDeleteControllerRestSpec(): void {
    this.showRestSpecDeleteConfirmation = false;
    this.pendingRestSpecDeleteKey = null;
  }

  onActuatorConfigurationChange(configuration: string): void {
    const normalized = this.normalizeActuatorConfigurationName(configuration) ?? 'default';
    if (!this.actuatorConfigurationOptions.includes(normalized)) {
      this.selectedActuatorConfiguration = 'default';
      return;
    }
    this.selectedActuatorConfiguration = normalized;
    if (!this.actuatorProfileEndpoints[normalized]?.length) {
      this.actuatorProfileEndpoints[normalized] = [...DEFAULT_ACTUATOR_ENDPOINTS];
    }
  }

  onActuatorEndpointsChange(endpoints: string[]): void {
    const key = this.selectedActuatorConfiguration || 'default';
    this.actuatorProfileEndpoints = {
      ...this.actuatorProfileEndpoints,
      [key]: this.sanitizeActuatorEndpoints(endpoints)
    };
  }

  onActuatorConfigurationsChange(configurations: Record<string, string[]>): void {
    this.projectGenerationState.setActuatorConfigurations(configurations);
    const snapshot = this.projectGenerationState.getActuatorStateSnapshot();
    this.actuatorConfigurationOptions = snapshot.configurationOptions;
    this.actuatorProfileEndpoints = snapshot.endpointsByConfiguration;
  }

  closeProfileModal(): void {
    this.showProfileModal = false;
  }

  async openYamlPreviewModal(): Promise<void> {
    if (this.isLoggedIn && this.backendProjectId) {
      try {
        await this.ensureAllDraftSectionsLoaded();
      } catch (error) {
        this.toastService.error('Failed to load project data for preview.');
        console.error('Error preparing YAML preview:', error);
        return;
      }
    }
    this.yamlPreviewContent = this.buildCurrentProjectYaml();
    this.showYamlPreviewModal = true;
  }

  closeYamlPreviewModal(): void {
    this.showYamlPreviewModal = false;
  }

  onProfilesSave(profiles: string[]): void {
    this.developerPreferences.profiles = [...profiles];
    this.syncActuatorConfigurationsWithProfiles();
    this.syncActuatorStateStore();
    this.closeProfileModal();
  }

  removeProfile(profile: string): void {
    this.developerPreferences.profiles = this.developerPreferences.profiles.filter(item => item !== profile);
    this.syncActuatorConfigurationsWithProfiles();
    this.syncActuatorStateStore();
  }

  private sanitizeActuatorEndpoints(rawEndpoints: unknown): string[] {
    const allowed = new Set(ACTUATOR_ENDPOINT_OPTIONS.map((option) => option.value));
    if (!Array.isArray(rawEndpoints)) {
      return [...DEFAULT_ACTUATOR_ENDPOINTS];
    }

    const cleaned = rawEndpoints
      .map((item) => String(item || '').trim().toLowerCase())
      .filter((item) => item && allowed.has(item));

    const unique = Array.from(new Set(cleaned));
    return unique.length ? unique : [...DEFAULT_ACTUATOR_ENDPOINTS];
  }

  get selectedActuatorEndpoints(): string[] {
    const selected = this.actuatorProfileEndpoints[this.selectedActuatorConfiguration];
    return this.sanitizeActuatorEndpoints(selected);
  }

  private normalizeActuatorConfigurationName(value: unknown): string | null {
    const normalized = this.normalizeActuatorProfileName(value);
    if (!normalized) {
      return null;
    }
    return normalized === 'default' ? 'default' : normalized;
  }

  private normalizeActuatorProfileName(value: unknown): string | null {
    if (value === null || value === undefined) {
      return null;
    }
    const t = String(value).trim();
    if (!t) {
      return null;
    }
    return t.toLowerCase();
  }

  private sanitizeActuatorConfigurations(
    rawConfigurations: unknown,
    availableConfigurations: string[] = this.specMapper.getActuatorConfigurationOptions(this.developerPreferences.profiles)
  ): Record<string, string[]> {
    const allowedConfigurations = new Set(availableConfigurations);
    const sanitized: Record<string, string[]> = {
      default: [...DEFAULT_ACTUATOR_ENDPOINTS]
    };

    if (Array.isArray(rawConfigurations)) {
      sanitized['default'] = this.sanitizeActuatorEndpoints(rawConfigurations);
      return sanitized;
    }

    if (!(rawConfigurations instanceof Object)) {
      return sanitized;
    }

    Object.entries(rawConfigurations as Record<string, unknown>).forEach(([rawKey, value]) => {
      const key = this.normalizeActuatorConfigurationName(rawKey);
      if (!key || !allowedConfigurations.has(key)) {
        return;
      }

      const include = value instanceof Object && !Array.isArray(value)
        ? (value as any)?.endpoints?.include ?? (value as any)?.include
        : value;

      sanitized[key] = this.sanitizeActuatorEndpoints(include);
    });

    if (!sanitized['default']?.length) {
      sanitized['default'] = [...DEFAULT_ACTUATOR_ENDPOINTS];
    }

    return sanitized;
  }

  private syncActuatorConfigurationsWithProfiles(): void {
    const options = this.specMapper.getActuatorConfigurationOptions(this.developerPreferences.profiles);
    this.actuatorConfigurationOptions = options;
    this.actuatorProfileEndpoints = this.sanitizeActuatorConfigurations(this.actuatorProfileEndpoints, options);
    if (!options.includes(this.selectedActuatorConfiguration)) {
      this.selectedActuatorConfiguration = 'default';
    }
  }

  private syncActuatorStateStore(): void {
    this.projectGenerationState.setProfiles(this.developerPreferences?.profiles ?? []);
    this.projectGenerationState.setActuatorConfigurations(this.actuatorProfileEndpoints);
    const snapshot = this.projectGenerationState.getActuatorStateSnapshot();
    this.actuatorConfigurationOptions = snapshot.configurationOptions;
    this.actuatorProfileEndpoints = snapshot.endpointsByConfiguration;
  }

  onHelpIconInteraction(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }

  onDatabaseSelectionChange(value: string): void {
    const selectedCode = toDatabaseCode(value);
    if (selectedCode === 'NONE' && this.hasConfiguredEntities()) {
      this.databaseSelectionBeforeConfirmation = this.previousDatabaseSelection;
      this.pendingDatabaseSelection = selectedCode;
      this.restoreDatabaseSelection(this.databaseSelectionBeforeConfirmation || this.previousDatabaseSelection);
      this.showEntitiesDeleteConfirmation = true;
      return;
    }

    this.applyDatabaseSelection(selectedCode);
  }

  onDatabaseTypeChange(type: 'SQL' | 'NOSQL' | 'NONE'): void {
    if (type === 'NONE' && this.hasConfiguredEntities()) {
      this.databaseSelectionBeforeConfirmation = this.previousDatabaseSelection;
      this.pendingDatabaseSelection = 'NONE';
      this.databaseSettings.dbType = this.previousDatabaseType;
      this.restoreDatabaseSelection(this.previousDatabaseSelection);
      this.showEntitiesDeleteConfirmation = true;
      return;
    }
    this.databaseSettings.dbType = type;
    this.ensureDatabaseSelectionForType();
  }

  confirmEntitiesDelete(): void {
    if (this.pendingDatabaseSelection !== 'NONE') {
      this.showEntitiesDeleteConfirmation = false;
      return;
    }

    this.entities = [];
    this.relations = [];
    this.applyDatabaseSelection('NONE');
    this.showEntitiesDeleteConfirmation = false;
    this.pendingDatabaseSelection = null;
    this.databaseSelectionBeforeConfirmation = null;
  }

  cancelEntitiesDelete(): void {
    this.showEntitiesDeleteConfirmation = false;
    this.pendingDatabaseSelection = null;
    this.restoreDatabaseSelection(this.databaseSelectionBeforeConfirmation || this.previousDatabaseSelection);
    this.databaseSelectionBeforeConfirmation = null;
  }

  private applyDatabaseSelection(code: string): void {
    this.databaseSettings.database = code;
    this.databaseSettings.dbType = resolveDatabaseType(this.databaseSettings.dbType, code);
    this.previousDatabaseSelection = code;
    this.previousDatabaseType = this.databaseSettings.dbType;
    if (code === 'NONE' && this.activeSection === 'entities') {
      this.activeSection = 'general';
    }
  }

  get filteredDatabaseOptions(): DatabaseOption[] {
    const currentType = this.databaseSettings.dbType;
    return stableArray(this.databaseOptions.filter(option => option.type === currentType), this._filteredDbOptionsCache);
  }

  private hasConfiguredEntities(): boolean {
    return this.entities.length > 0 || this.relations.length > 0;
  }

  shouldShowDbGeneration(): boolean {
    return this.databaseSettings.dbType !== 'NONE' && toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  shouldShowPluralizeTableNames(): boolean {
    return this.databaseSettings.dbType !== 'NONE' && toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  private restoreDatabaseSelection(value: string): void {
    const selection = toDatabaseCode(value);
    setTimeout(() => {
      this.databaseSettings.database = selection;
      this.databaseSettings.dbType = resolveDatabaseType(this.databaseSettings.dbType, selection);
      this.cdr.detectChanges();
    }, 0);
  }

  private ensureDatabaseSelectionForType(): void {
    if (this.databaseSettings.dbType === 'NONE') {
      this.databaseSettings.database = 'NONE';
      return;
    }
    const selectedDatabase = toDatabaseCode(this.databaseSettings.database);
    const allowed = this.filteredDatabaseOptions.some(option => option.value === selectedDatabase);
    if (allowed) {
      return;
    }
    const firstMatching = this.filteredDatabaseOptions.find(option => option.type === this.databaseSettings.dbType)?.value;
    this.databaseSettings.database = firstMatching || 'POSTGRES';
  }

  private countEnabledEndpoints(config: RestEndpointConfig): number {
    const methods = config?.methods;
    if (!methods) {
      return 0;
    }
    return [
      methods.list, methods.get, methods.create, methods.patch,
      methods.delete, methods.bulkInsert, methods.bulkUpdate, methods.bulkDelete
    ].filter(Boolean).length;
  }

  private resolveEntityRestSpecName(entity: any): string {
    const inlineName = trimmed(entity?.restConfig?.resourceName);
    if (inlineName) {
      return inlineName;
    }
    return trimmed(
      entity?.['rest-spec-name']
      ?? entity?.restSpecName
      ?? entity?.restSpec
    );
  }

  private applyControllersEntityMapping(config: RestEndpointConfig, previousSpecKey: string | null): void {
    const nextSpecName = trimmed(config?.resourceName);
    const previousName = trimmed(previousSpecKey);
    const selectedEntityName = config.mapToEntity ? trimmed(config?.mappedEntityName) : '';
    const normalizedConfig = this.specMapper.parseControllersConfig(config);

    this.entities.forEach((entity: any) => {
      const entityName = trimmed(entity?.name);
      const mappedSpecName = this.resolveEntityRestSpecName(entity);
      const inlineSpecName = trimmed(entity?.restConfig?.resourceName);
      const matchesPrevious = Boolean(previousName) && (mappedSpecName === previousName || inlineSpecName === previousName);
      const matchesNext = Boolean(nextSpecName) && (mappedSpecName === nextSpecName || inlineSpecName === nextSpecName);
      const shouldClear = matchesPrevious || matchesNext;

      if (!shouldClear) {
        return;
      }

      if (!selectedEntityName) {
        entity.addRestEndpoints = true;
        entity.restConfig = this.specMapper.parseControllersConfig({
          ...normalizedConfig,
          mapToEntity: false,
          mappedEntityName: ''
        });
        delete entity['rest-spec-name'];
        return;
      }

      if (entityName !== selectedEntityName) {
        entity.addRestEndpoints = false;
        entity.restConfig = undefined;
        delete entity['rest-spec-name'];
      }
    });

    if (!selectedEntityName) {
      this.entities = [...this.entities];
      return;
    }

    const mappedEntity = this.entities.find((entity: any) => trimmed(entity?.name) === selectedEntityName);
    if (mappedEntity) {
      mappedEntity.addRestEndpoints = true;
      mappedEntity.restConfig = this.specMapper.parseControllersConfig(config);
      mappedEntity['rest-spec-name'] = nextSpecName || selectedEntityName;
    }
    this.entities = [...this.entities];
  }

  private clearAllApiConfigurations(): void {
    this.controllersConfigEnabled = false;
    this.controllersConfig = { ...DEFAULT_CONTROLLERS_CONFIG };
    this.controllersCreatingNewConfig = false;
    this.controllersEditingSpecKey = null;
    this.controllersEditingSpecConfig = null;

    this.entities = (Array.isArray(this.entities) ? this.entities : []).map((entity: any) => {
      const updated = { ...entity };
      updated.addRestEndpoints = false;
      delete updated.restConfig;
      delete updated['rest-spec-name'];
      delete updated.restSpecName;
      delete updated.restSpec;
      return updated;
    });
  }

  get controllersEntityFields(): Array<{ name: string; type?: string }> {
    const fields = (Array.isArray(this.entities) ? this.entities : [])
      .flatMap((entity: any) => Array.isArray(entity?.fields) ? entity.fields : []);
    const uniqueByName = new Map<string, { name: string; type?: string }>();
    fields.forEach((field: any) => {
      const name = String(field?.name ?? '').trim();
      if (!name || uniqueByName.has(name)) {
        return;
      }
      const type = String(field?.type ?? '').trim();
      uniqueByName.set(name, { name, type: type || undefined });
    });
    return stableArray(Array.from(uniqueByName.values()), this._controllersEntityFieldsCache);
  }

  get controllersFieldTypeOptions(): string[] {
    const types = (Array.isArray(this.entities) ? this.entities : [])
      .flatMap((entity: any) => Array.isArray(entity?.fields) ? entity.fields : [])
      .map((field: any) => String(field?.type ?? '').trim())
      .filter(Boolean);
    const enums = (Array.isArray(this.enums) ? this.enums : [])
      .map((item: any) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return stableArray(Array.from(new Set([...ENTITY_FIELD_TYPE_OPTIONS, ...types, ...enums])), this._controllersFieldTypeOptionsCache);
  }

  get controllersDtoObjects(): Array<{ name?: string; dtoType?: 'request' | 'response'; fields?: unknown[] }> {
    return Array.isArray(this.dataObjects) ? this.dataObjects : [];
  }

  get controllersEnumTypes(): string[] {
    return stableArray(
      (Array.isArray(this.enums) ? this.enums : [])
        .map((item: any) => String(item?.name ?? '').trim())
        .filter(Boolean),
      this._controllersEnumTypesCache
    );
  }

  get configuredEntityRestSpecNames(): string[] {
    return stableArray(
      (Array.isArray(this.entities) ? this.entities : [])
        .filter((entity: any) => Boolean(entity?.addRestEndpoints))
        .map((entity: any) => String(entity?.restConfig?.resourceName ?? '').trim())
        .filter(Boolean),
      this._configuredEntityRestSpecNamesCache
    );
  }

  private validateProjectNaming(): boolean {
    const projectName = trimmed(this.projectSettings.projectName);
    const projectDescription = trimmed(this.projectSettings.projectDescription);
    this.projectGroupError = '';
    this.projectNameError = '';
    this.projectDescriptionError = '';

    if (!projectName) {
      this.projectNameError = VALIDATION_MESSAGES.projectNameRequired;
      this.focusProjectNamingErrorField();
      return false;
    }

    if (!isValidJavaProjectFolderName(projectName)) {
      this.projectNameError = VALIDATION_MESSAGES.projectNameInvalidFolder;
      this.focusProjectNamingErrorField();
      return false;
    }

    const validationTarget = {
      projectGroup: this.projectSettings.projectGroup,
      artifactId: toArtifactId(projectName)
    };

    const valid = this.validatorService.validate(
      validationTarget,
      buildMavenNamingRules({
        groupField: 'projectGroup',
        artifactField: 'artifactId',
        setGroupError: (message) => {
          this.projectGroupError = message;
        },
        setArtifactError: (message) => {
          this.projectNameError = `${VALIDATION_MESSAGES.artifactIdInvalid} ${message}`;
        }
      }),
      { silent: true }
    );

    if (!valid) {
      this.focusProjectNamingErrorField();
      return false;
    }

    if (projectDescription && !isValidProjectDescription(projectDescription)) {
      this.projectDescriptionError = VALIDATION_MESSAGES.projectDescriptionInvalid;
      this.focusProjectNamingErrorField();
      return false;
    }

    return true;
  }

  private async validateUniqueProjectNameForCurrentUser(): Promise<boolean> {
    if (!this.isLoggedIn || trimmed(this.backendProjectId)) {
      return true;
    }

    const projectName = trimmed(this.projectSettings.projectName);
    if (!projectName) {
      return true;
    }

    try {
      const projects = await firstValueFrom(this.projectService.getProjects());
      const duplicate = (Array.isArray(projects) ? projects : []).some((project) =>
        trimmed(project?.name).toLowerCase() === projectName.toLowerCase()
      );
      if (!duplicate) {
        return true;
      }
      this.projectNameError = 'Project name already exists. Choose a different project name';
      this.activeSection = 'general';
      this.focusProjectNamingErrorField();
      return false;
    } catch (error) {
      console.error('Failed to validate project name uniqueness:', error);
      return true;
    }
  }

  private extractProjectSaveErrorMessage(error: any): string {
    return trimmed(error?.error?.errorMsg || error?.error?.message || error?.error?.error || error?.message);
  }

  private applyProjectSaveErrorState(error: any): void {
    const message = this.extractProjectSaveErrorMessage(error).toLowerCase();
    if (message.includes('project name already exists')) {
      this.projectNameError = 'Project name already exists. Choose a different project name';
      this.activeSection = 'general';
      this.focusProjectNamingErrorField();
    }
  }

  private getNextModuleConfigurationSection(): string | null {
    const moduleTabs = this.selectedShippableModules.filter((moduleKey) => this.isSectionAvailable(moduleKey));
    return moduleTabs.length ? moduleTabs[0] : null;
  }

  private async reloadTabDefinitionsForCurrentSelection(): Promise<void> {
    if (!this.isLoggedIn) {
      return;
    }
    try {
      const tabDetails = await firstValueFrom(
        this.projectService.getProjectTabDetails(this.projectSettings.language, this.selectedDependencies)
      );
      this.tabDefinitions = Array.isArray(tabDetails) ? tabDetails : [];
      this.applyTabDefinitions(this.tabDefinitions);
      this.tabDefinitionsLoadedFully = true;
    } catch (error) {
      console.error('Failed to refresh project tab definitions:', error);
    }
  }

  private getSectionLoadedMessage(section: string): string {
    switch (section) {
      case 'rbac':
        return 'RBAC configuration loaded';
      case 'auth':
        return 'Authentication configuration loaded';
      case 'state-machine':
        return 'State-machine configuration loaded';
      case 'subscription':
        return 'Subscription configuration loaded';
      case 'swagger':
        return 'Swagger configuration loaded';
      case 'controllers':
        return 'Controller setup loaded';
      default:
        return 'Section loaded';
    }
  }

  private canPersistDraftSilently(): boolean {
    const projectName = trimmed(this.projectSettings.projectName);
    const projectDescription = trimmed(this.projectSettings.projectDescription);
    if (!projectName || !isValidJavaProjectFolderName(projectName)) {
      return false;
    }

    const validationTarget = {
      projectGroup: this.projectSettings.projectGroup,
      artifactId: toArtifactId(projectName)
    };

    const valid = this.validatorService.validate(
      validationTarget,
      buildMavenNamingRules({
        groupField: 'projectGroup',
        artifactField: 'artifactId',
        setGroupError: () => {},
        setArtifactError: () => {}
      }),
      { silent: true }
    );

    return valid && (!projectDescription || isValidProjectDescription(projectDescription)) && this.isDraftBackedSection(this.activeSection);
  }

  private resolveRequestedSection(section: string | null | undefined): string {
    const normalized = trimmed(section).toLowerCase();
    return normalized || 'general';
  }

  private resolveRequestedExploreView(view: string | null | undefined): 'runs' | 'migrate' {
    return trimmed(view).toLowerCase() === 'migrate' ? 'migrate' : 'runs';
  }

  private selectDefaultMigrationLanguage(): void {
    const availableTargets = this.getMigrationTargetLanguageOptions();
    if (!availableTargets.length) {
      this.selectedMigrationLanguage = null;
      return;
    }
    if (this.selectedMigrationLanguage && availableTargets.some((option) => option.value === this.selectedMigrationLanguage)) {
      return;
    }
    this.selectedMigrationLanguage = availableTargets[0].value;
  }

  private isDraftBackedSection(section: string): boolean {
    return section !== 'explore' && section !== 'collaborate';
  }

  private isSectionAvailable(section: string): boolean {
    return this.visibleNavItems.some((item) => item.value === section);
  }

  private focusProjectNamingErrorField(): void {
    setTimeout(() => {
      if (this.projectGroupError) {
        this.projectGroupInput?.nativeElement?.focus();
        return;
      }
      if (this.projectNameError) {
        this.projectNameInput?.nativeElement?.focus();
      }
    });
  }

  private promptForRecentProjectIfAvailable(): void {
    const projects = this.getSavedProjects();
    if (!projects.length) {
      return;
    }

    const latestProject = projects
      .filter(project => hasNumber(project?.id))
      .sort((left, right) => Number(right.id) - Number(left.id))[0];

    if (!latestProject) {
      return;
    }

    this.recentProjectToResume = { id: String(latestProject.id) };
    this.showRecentProjectPrompt = true;
  }

  private resumeProjectFromLocalDraft(savedProject: any, localProjectId: string): void {
    this.resetLazyLoadedDraftSections();
    this.resetDraftBackedState();
    this.closeProjectEventsSource();
    this.backendProjectId = null;
    this.projectOwnerId = null;
    this.projectCanManageContributors = false;
    this.projectContributors = [];
    this.collaborationInviteToken = null;
    this.collaborationRequests = [];
    this.projectId = localProjectId;
    this.draftVersion = 1;

    this.projectSettings = {
      ...this.projectSettings,
      ...(savedProject?.settings || {})
    };
    this.databaseSettings = {
      ...this.databaseSettings,
      ...(savedProject?.database || {})
    };
    this.databaseSettings.dbType = resolveDatabaseType(this.databaseSettings.dbType, this.databaseSettings.database);
    this.databaseSettings.database = toDatabaseCode(this.databaseSettings.database);
    this.ensureDatabaseSelectionForType();
    this.previousDatabaseSelection = this.databaseSettings.database;
    this.previousDatabaseType = this.databaseSettings.dbType;
    this.developerPreferences = {
      ...this.developerPreferences,
      ...(savedProject?.preferences || {}),
      profiles: Array.isArray(savedProject?.preferences?.profiles) ? savedProject.preferences.profiles : []
    };
    this.dependencies = typeof savedProject?.dependencies === 'string' ? savedProject.dependencies : '';
    this.selectedDependencies = this.normalizeSelectedDependencies(
      Array.isArray(savedProject?.selectedDependencies) ? savedProject.selectedDependencies : []
    );
    this.entities = Array.isArray(savedProject?.entities) ? savedProject.entities : [];
    this.dataObjects = Array.isArray(savedProject?.dataObjects) ? savedProject.dataObjects : [];
    this.relations = Array.isArray(savedProject?.relations) ? savedProject.relations : [];
    this.enums = Array.isArray(savedProject?.enums) ? savedProject.enums : [];
    this.mappers = Array.isArray(savedProject?.mappers) ? savedProject.mappers : [];
    this.moduleConfigs = savedProject?.moduleConfigs && typeof savedProject.moduleConfigs === 'object'
      ? savedProject.moduleConfigs
      : {};
    this.projectGenerationState.setModuleConfigs(this.moduleConfigs);
    this.controllersConfigEnabled = savedProject?.controllers?.enabled === undefined
      ? true
      : Boolean(savedProject.controllers.enabled);
    this.controllersConfig = this.specMapper.parseControllersConfig(savedProject?.controllers?.config);
    this.syncActuatorConfigurationsWithProfiles();
    this.syncActuatorStateStore();
    this.loadTabDefinitions(this.projectSettings.language, this.selectedDependencies);
    this.activeSection = 'general';
    this.refreshSavedProjectStateSnapshot();
    this.toastService.success('Project loaded successfully');
  }

  private getSavedProjects(): any[] {
    try {
      const raw = localStorage.getItem('projects');
      const parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private refreshSavedProjectStateSnapshot(): void {
    this.savedProjectStateSnapshot = this.serializeProjectState();
    this.hasUnsavedChanges = false;
  }

  private serializeProjectState(): string {
    return JSON.stringify(this.getProjectData());
  }

  private normalizeSelectedDependencies(dependencies: unknown[]): string[] {
    const normalizedDependencies: string[] = [];
    const seen = new Set<string>();
    for (const dependency of dependencies) {
      const normalized = this.normalizeDependencyKey(String(dependency ?? '').trim());
      if (!normalized || seen.has(normalized)) {
        continue;
      }
      seen.add(normalized);
      normalizedDependencies.push(normalized);
    }
    return normalizedDependencies;
  }

  private normalizeDependencyKey(dependency: string): string {
    return dependency === 'azure-cdn-upload' ? 'cdn' : dependency;
  }
}
