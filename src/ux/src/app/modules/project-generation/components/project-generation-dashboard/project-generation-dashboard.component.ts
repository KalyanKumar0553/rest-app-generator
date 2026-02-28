import { ChangeDetectorRef, Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, Subscription, firstValueFrom, takeUntil } from 'rxjs';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { EntitiesComponent } from '../entities/entities.component';
import { DataObjectsComponent } from '../data-objects/data-objects.component';
import { ProjectViewComponent } from '../project-view/project-view.component';
import { ProjectSpecComponent } from '../project-spec/project-spec.component';
import { ControllersSpecTableComponent } from '../controllers-spec-table/controllers-spec-table.component';
import { AddProfileComponent } from '../add-profile/add-profile.component';
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG, API_ENDPOINTS } from '../../../../constants/api.constants';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { LoadingOverlayComponent } from '../../../../components/shared/loading-overlay/loading-overlay.component';
import { finalize } from 'rxjs/operators';
import { ValidatorService } from '../../../../services/validator.service';
import { buildMavenNamingRules, isValidJavaProjectFolderName } from '../../validators/naming-validation';
import { APP_SETTINGS } from '../../../../settings/app-settings';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';
import {
  ActuatorConfigComponent,
  ACTUATOR_ENDPOINT_OPTIONS,
  DEFAULT_ACTUATOR_ENDPOINTS
} from '../actuator-config/actuator-config.component';
import { RestConfigComponent, RestEndpointConfig } from '../rest-config/rest-config.component';
import { ENTITY_FIELD_TYPE_OPTIONS } from '../../constants/backend-field-types';
import { VALIDATION_MESSAGES } from '../../constants/validation-messages';

import {
  ProjectSettings,
  DatabaseSettings,
  DatabaseOption,
  DeveloperPreferences,
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
import { toDatabaseCode, resolveDatabaseType, trimmed, toArtifactId, hasNumber } from '../../utils/project-generation.utils';

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
    MatProgressSpinnerModule,
    MatAutocompleteModule,
    ConfirmationModalComponent,
    ModalComponent,
    EntitiesComponent,
    DataObjectsComponent,
    ActuatorConfigComponent,
    AddProfileComponent,
    ProjectViewComponent,
    ProjectSpecComponent,
    ControllersSpecTableComponent,
    RestConfigComponent,
    SidenavComponent,
    InfoBannerComponent,
    LoadingOverlayComponent
  ],
  templateUrl: './project-generation-dashboard.component.html',
  styleUrls: ['./project-generation-dashboard.component.css']
})
export class ProjectGenerationDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private readonly maxYamlSpecPayloadBytes = 2 * 1024 * 1024;
  readonly appSettings = APP_SETTINGS;

  isSidebarOpen = false;
  isLoading = false;
  isLoggedIn = false;
  projectId: number | null = null;
  hasUnsavedChanges = false;
  activeSection = 'general';

  baseNavItems: NavItem[] = [...BASE_NAV_ITEMS];
  actuatorNavItem: NavItem = ACTUATOR_NAV_ITEM;
  controllersNavItem: NavItem = CONTROLLERS_NAV_ITEM;
  mappersNavItem: NavItem = MAPPERS_NAV_ITEM;

  entities: any[] = [];
  dataObjects: any[] = [];
  relations: any[] = [];
  enums: any[] = [];
  mappers: any[] = [];
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
  private recentProjectToResume: { id: number } | null = null;
  isExploreSyncing = false;
  isGeneratingFromDtoSave = false;
  backendProjectId: string | null = null;
  private projectEventsSource: EventSource | null = null;
  private pendingGenerationYamlSpec: string | null = null;
  private generationCreateSubscription: Subscription | null = null;
  private generationGuestSubscription: Subscription | null = null;
  exploreZipBlob: Blob | null = null;
  exploreZipFileName = 'project.zip';

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
  @ViewChild('projectGroupInput') projectGroupInput?: ElementRef<HTMLInputElement>;
  @ViewChild('projectNameInput') projectNameInput?: ElementRef<HTMLInputElement>;

  frontendOptions = FRONTEND_OPTIONS;
  databaseOptions: DatabaseOption[] = DATABASE_OPTIONS;
  dbTypeOptions: Array<'SQL' | 'NOSQL' | 'NONE'> = DB_TYPE_OPTIONS;
  dbGenerationOptions = DB_GENERATION_OPTIONS;
  javaVersionOptions = JAVA_VERSION_OPTIONS;
  deploymentOptions = DEPLOYMENT_OPTIONS;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService,
    private http: HttpClient,
    private validatorService: ValidatorService,
    private localStorageService: LocalStorageService,
    private cdr: ChangeDetectorRef,
    private projectGenerationState: ProjectGenerationStateService,
    private specMapper: ProjectSpecMapperService
  ) {}

  get visibleNavItems(): NavItem[] {
    const navItems = [...this.baseNavItems];
    if (this.developerPreferences.enableActuator) {
      navItems.splice(1, 0, this.actuatorNavItem);
    }
    if (this.dataObjects.length > 0) {
      const dataObjectsIndex = navItems.findIndex((item) => item.value === 'data-objects');
      const mapperInsertIndex = dataObjectsIndex >= 0 ? dataObjectsIndex + 1 : navItems.length - 1;
      navItems.splice(mapperInsertIndex, 0, this.mappersNavItem);
    }

    if (this.developerPreferences.configureApi) {
      const mappersIndex = navItems.findIndex((item) => item.value === 'mappers');
      const dataObjectsIndex = navItems.findIndex((item) => item.value === 'data-objects');
      const insertIndex = mappersIndex >= 0 ? mappersIndex + 1 : (dataObjectsIndex >= 0 ? dataObjectsIndex + 1 : navItems.length - 1);
      navItems.splice(insertIndex, 0, this.controllersNavItem);
    }

    const shouldShowExplore = !this.isGeneratingFromDtoSave && !this.isExploreSyncing && this.hasCachedZipForCurrentProject();
    if (!shouldShowExplore) {
      const exploreIndex = navItems.findIndex((item) => item.value === 'explore');
      if (exploreIndex >= 0) {
        navItems.splice(exploreIndex, 1);
      }
    }

    const isNoneDatabase = toDatabaseCode(this.databaseSettings.database) === 'NONE';
    if (!isNoneDatabase) {
      return navItems;
    }
    return navItems.filter(item => item.value !== 'entities');
  }

  isEntitiesTabVisible(): boolean {
    return toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();

    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        if (params['projectId']) {
          this.projectId = +params['projectId'];
          this.loadProject(this.projectId);
          return;
        }

        if (!this.hasCheckedRecentProjectPrompt) {
          this.hasCheckedRecentProjectPrompt = true;
          this.promptForRecentProjectIfAvailable();
        }
      });

    this.trackChanges();
    this.loadDependencies();
    this.syncActuatorStateStore();
  }

  ngOnDestroy(): void {
    this.cancelGenerationRequests();
    this.closeProjectEventsSource();
    this.destroy$.next();
    this.destroy$.complete();
  }

  trackChanges(): void {
    const initialState = JSON.stringify(this.getProjectData());

    setInterval(() => {
      const currentState = JSON.stringify(this.getProjectData());
      this.hasUnsavedChanges = initialState !== currentState;
    }, 1000);
  }

  getProjectData() {
    return {
      id: this.projectId || undefined,
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
      mappers: this.mappers
    };
  }

  async loadProject(projectId: number): Promise<void> {
    this.isLoading = true;
    try {
      const projectData = this.loadProjectFromStorage(projectId);

      if (projectData) {
        this.entities = projectData.entities || [];
        this.dataObjects = projectData.dataObjects || [];
        this.relations = projectData.relations || [];
        this.enums = Array.isArray(projectData.enums) ? projectData.enums : [];
        this.mappers = Array.isArray(projectData.mappers) ? projectData.mappers : [];
        this.projectSettings = projectData.settings || this.projectSettings;
        this.databaseSettings = projectData.database || this.databaseSettings;
        this.databaseSettings.dbType = resolveDatabaseType(this.databaseSettings.dbType, this.databaseSettings.database);
        this.databaseSettings.database = toDatabaseCode(this.databaseSettings.database);
        this.ensureDatabaseSelectionForType();
        this.previousDatabaseSelection = this.databaseSettings.database;
        this.previousDatabaseType = this.databaseSettings.dbType;
        this.developerPreferences = {
          ...this.developerPreferences,
          ...(projectData.preferences || {}),
          enableActuator: Boolean(projectData?.preferences?.enableActuator),
          configureApi: projectData?.preferences?.configureApi === undefined
            ? this.developerPreferences.configureApi
            : Boolean(projectData?.preferences?.configureApi),
          enableLombok: projectData?.preferences?.enableLombok === undefined
            ? Boolean(projectData?.preferences?.optionalLombok)
            : Boolean(projectData?.preferences?.enableLombok)
        };
        this.controllersConfigEnabled = projectData?.controllers?.enabled === undefined
          ? true
          : Boolean(projectData?.controllers?.enabled);
        this.controllersConfig = this.specMapper.parseControllersConfig(projectData?.controllers?.config);
        const configurationOptions = this.specMapper.getActuatorConfigurationOptions(projectData?.preferences?.profiles);
        this.actuatorConfigurationOptions = configurationOptions;
        this.actuatorProfileEndpoints = this.sanitizeActuatorConfigurations(
          projectData?.actuator?.configurations ?? projectData?.actuator?.endpoints,
          configurationOptions
        );
        const selectedConfig = this.normalizeActuatorConfigurationName(projectData?.actuator?.selectedConfiguration) ?? 'default';
        this.selectedActuatorConfiguration = configurationOptions.includes(selectedConfig) ? selectedConfig : 'default';
        this.syncActuatorStateStore();
        this.dependencies = projectData.dependencies || '';
        this.toastService.success('Project loaded successfully');
      } else {
        this.entities = [];
        this.dataObjects = [];
        this.relations = [];
        this.enums = [];
        this.mappers = [];
        this.controllersConfig = { ...DEFAULT_CONTROLLERS_CONFIG };
        this.controllersConfigEnabled = true;
        this.syncActuatorConfigurationsWithProfiles();
        this.syncActuatorStateStore();
      }
    } catch (error) {
      this.toastService.error('Failed to load project');
      console.error('Error loading project:', error);
    } finally {
      this.isLoading = false;
    }
  }

  loadProjectFromStorage(projectId: number): any {
    const savedProjects = localStorage.getItem('projects');
    if (savedProjects) {
      const projects = JSON.parse(savedProjects);
      return projects.find((p: any) => p.id === projectId);
    }
    return null;
  }

  saveProject(): void {
    const projectData = this.getProjectData();
    const savedProjects = localStorage.getItem('projects');
    let projects = savedProjects ? JSON.parse(savedProjects) : [];

    if (this.projectId) {
      const index = projects.findIndex((p: any) => p.id === this.projectId);
      if (index !== -1) {
        projects[index] = projectData;
      } else {
        projects.push(projectData);
      }
    } else {
      projectData.id = Date.now();
      this.projectId = projectData.id;
      projects.push(projectData);
    }

    localStorage.setItem('projects', JSON.stringify(projects));
    this.toastService.success('Project saved successfully');
    this.hasUnsavedChanges = false;
  }

  saveProjectAndInvokeApi(): void {
    if (!this.validateProjectNaming()) {
      this.activeSection = 'general';
      return;
    }
    this.saveProject();
    if (this.isLoggedIn) {
      this.generateAndDownloadProjectFromBackend();
      return;
    }
    this.generateAndDownloadProjectForGuest();
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

  navigateToSection(section: string): void {
    if (section !== 'controllers') {
      this.cancelControllerRestSpecEdit();
      this.showControllersConfigDiscardConfirmation = false;
    }
    if (section === 'entities' && toDatabaseCode(this.databaseSettings.database) === 'NONE') {
      this.activeSection = 'general';
      this.closeSidebar();
      return;
    }
    if (section === 'actuator' && !this.developerPreferences.enableActuator) {
      this.activeSection = 'general';
      this.closeSidebar();
      return;
    }
    if (section === 'controllers' && !this.developerPreferences.configureApi) {
      this.activeSection = 'general';
      this.closeSidebar();
      return;
    }
    if (section === 'mappers') {
      this.dataObjectsDefaultTab = 'mappers';
    } else if (section === 'data-objects') {
      this.dataObjectsDefaultTab = 'dataObjects';
    }
    if (section === 'explore') {
      const previousSection = this.activeSection;
      this.handleExploreTab(previousSection);
      this.closeSidebar();
      return;
    }
    this.activeSection = section;
    this.closeSidebar();
  }

  handleExploreTab(previousSection: string): void {
    if (this.isExploreSyncing) {
      return;
    }

    if (!this.validateProjectNaming()) {
      this.activeSection = 'general';
      return;
    }

    if (!this.isLoggedIn) {
      this.generateExploreZipWithoutProjectId(previousSection);
      return;
    }

    const yamlSpec = this.buildCurrentProjectYaml();
    if (this.useCachedZipIfAvailable(yamlSpec, true)) {
      return;
    }

    const projectId = this.backendProjectId?.trim();
    if (!projectId) {
      this.toastService.error('Please save project first to generate and explore zip.');
      this.activeSection = previousSection;
      return;
    }

    this.isExploreSyncing = true;
    const runsUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.LIST_BY_PROJECT(projectId)}`;

    this.http.get<ProjectRunSummary[]>(runsUrl)
      .pipe(finalize(() => {
        this.isExploreSyncing = false;
      }))
      .subscribe({
        next: async (runs) => {
          const latestRun = this.getLatestRun(runs);
          if (!latestRun) {
            this.toastService.error('No generation run found for this project.');
            this.activeSection = previousSection;
            return;
          }

          const status = String(latestRun.status ?? '').toUpperCase();
          if (status === 'QUEUED' || status === 'INPROGRESS') {
            this.toastService.error('Request in progress. Please wait.');
            this.activeSection = previousSection;
            return;
          }

          if (status !== 'SUCCESS' && status !== 'DONE') {
            this.toastService.error(`Latest generation status is ${status || 'unknown'}.`);
            this.activeSection = previousSection;
            return;
          }

          try {
            await this.downloadAndPrepareExploreZip(latestRun.id, projectId, yamlSpec);
            this.activeSection = 'explore';
          } catch {
            this.toastService.error('Failed to download generated zip.');
            this.activeSection = previousSection;
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

    const yamlSpec = this.buildCurrentProjectYaml();
    if (this.useCachedZipIfAvailable(yamlSpec, true)) {
      return;
    }
    if (!this.validateYamlSpecPayloadSize(yamlSpec)) {
      return;
    }
    this.clearLocalZipDataBeforeGeneration();

    this.isGeneratingFromDtoSave = true;
    this.pendingGenerationYamlSpec = yamlSpec;

    this.generationCreateSubscription = this.createProjectOnBackend(yamlSpec)
      .pipe(finalize(() => {
        this.isGeneratingFromDtoSave = false;
      }))
      .subscribe({
        next: (response) => {
          const projectId = response?.projectId?.trim();
          if (!projectId) {
            this.toastService.error('Project saved but project id is missing in response.');
            this.pendingGenerationYamlSpec = null;
            return;
          }

          this.backendProjectId = projectId;
          this.toastService.success('Project generation started. Waiting for zip...');
          this.connectProjectEvents(projectId);
        },
        error: () => {
          this.pendingGenerationYamlSpec = null;
          this.toastService.error('Failed to save and start project generation.');
          this.generationCreateSubscription = null;
        }
      });
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

  private createProjectOnBackend(yamlSpec: string) {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.CREATE}`;
    return this.http.post<{ projectId?: string; status?: string }>(url, yamlSpec, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  private connectProjectEvents(projectId: string): void {
    this.closeProjectEventsSource();

    const url = `${API_CONFIG.BASE_URL}/api/projects/${projectId}/events`;
    const source = new EventSource(url, { withCredentials: true });
    this.projectEventsSource = source;

    source.addEventListener('generation', (event: MessageEvent) => {
      const payload = this.parseJsonPayload(event.data);
      if (!payload) {
        return;
      }

      const status = String(payload.status ?? '').toUpperCase();
      if (status === 'SUCCESS' && payload.zipBase64) {
        const fileName = typeof payload.fileName === 'string' && payload.fileName.trim()
          ? payload.fileName.trim()
          : 'project.zip';
        const yamlForCache = this.pendingGenerationYamlSpec || this.buildCurrentProjectYaml();
        this.downloadZipFile(payload.zipBase64, fileName, yamlForCache);
        this.toastService.success('Project zip is ready.');
        this.closeProjectEventsSource();
      } else if (status === 'ERROR') {
        const message = typeof payload.message === 'string' && payload.message.trim()
          ? payload.message.trim()
          : 'Project generation failed.';
        this.toastService.error(message);
        this.closeProjectEventsSource();
      }
    });

    source.onerror = () => {
      this.closeProjectEventsSource();
    };
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

  private downloadZipFile(base64Payload: string, fileName: string, yamlSpec: string): void {
    try {
      const bytes = this.base64ToUint8Array(base64Payload);
      this.exploreZipBlob = new Blob([bytes.buffer as ArrayBuffer], { type: 'application/zip' });
      this.exploreZipFileName = fileName || `${toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
      this.cacheZipFromBase64(yamlSpec, base64Payload, this.exploreZipFileName);
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

  private cancelGenerationRequests(): void {
    if (this.generationCreateSubscription) {
      this.generationCreateSubscription.unsubscribe();
      this.generationCreateSubscription = null;
    }
    if (this.generationGuestSubscription) {
      this.generationGuestSubscription.unsubscribe();
      this.generationGuestSubscription = null;
    }
  }

  private getLatestRun(runs: ProjectRunSummary[] | null | undefined): ProjectRunSummary | null {
    if (!Array.isArray(runs) || runs.length === 0) {
      return null;
    }

    const sorted = [...runs].sort((left, right) => {
      const leftDate = new Date(left.createdAt ?? '').getTime();
      const rightDate = new Date(right.createdAt ?? '').getTime();
      if (leftDate === rightDate) {
        return (Number(right.runNumber) || 0) - (Number(left.runNumber) || 0);
      }
      return rightDate - leftDate;
    });

    return sorted[0] ?? null;
  }

  private async downloadAndPrepareExploreZip(runId: string, projectId: string, yamlSpec: string): Promise<void> {
    const downloadUrl = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.RUN.DOWNLOAD(runId)}`;
    const zipData = await firstValueFrom(this.http.get(downloadUrl, { responseType: 'arraybuffer' }));
    if (!zipData) {
      throw new Error('Zip payload is empty');
    }

    this.exploreZipBlob = new Blob([zipData], { type: 'application/zip' });
    this.exploreZipFileName = `${toArtifactId(this.projectSettings.projectName || projectId)}.zip`;
    this.cacheZipFromArrayBuffer(yamlSpec, zipData, this.exploreZipFileName);
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
    this.handleExploreTab(this.activeSection);
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

  private clearLocalZipDataBeforeGeneration(): void {
    this.localStorageService.clearProjectZipCache(this.getZipCacheKey());
    this.exploreZipBlob = null;
    this.exploreZipFileName = 'project.zip';
    if (this.activeSection === 'explore') {
      this.activeSection = 'general';
    }
  }

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

    this.projectId = recentProjectId;
    this.loadProject(recentProjectId);
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

  loadDependencies(): void {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.DEPENDENCIES.LIST}`;
    this.http.get<string[]>(url).subscribe({
      next: (dependencies) => {
        this.availableDependencies = Array.isArray(dependencies) ? dependencies : [];
      },
      error: () => {
        this.toastService.error('Failed to load dependencies');
        this.availableDependencies = [];
      }
    });
  }

  filterDependencies(value: string): void {
    if (!value || value.trim() === '') {
      this.filteredDependencies = [];
      return;
    }

    const filterValue = value.toLowerCase();
    this.filteredDependencies = this.availableDependencies
      .filter(dep =>
        dep.toLowerCase().includes(filterValue) &&
        !this.selectedDependencies.includes(dep)
      )
      .slice(0, 10);
  }

  addDependency(event: any): void {
    const value = event.option.value;
    if (value && !this.selectedDependencies.includes(value)) {
      this.selectedDependencies.push(value);
      this.dependencies = this.selectedDependencies.join(', ');
    }
    this.dependencyInput = '';
    this.filteredDependencies = [];
  }

  removeDependency(dep: string): void {
    const index = this.selectedDependencies.indexOf(dep);
    if (index >= 0) {
      this.selectedDependencies.splice(index, 1);
      this.dependencies = this.selectedDependencies.join(', ');
    }
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
      this.navigateToSection('entities');
      this.toastService.success('Entity setup loaded');
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
      this.navigateToSection('data-objects');
      this.toastService.success('Data objects setup loaded');
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
      this.navigateToSection('controllers');
      this.toastService.success('Controller setup loaded');
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
      this.navigateToSection('mappers');
      this.toastService.success('Mapper setup loaded');
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
      this.navigateToSection('actuator');
      this.toastService.success('Actuator setup loaded');
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
      return this.developerPreferences.configureApi ? 'Setup Controller' : 'Save Project';
    }
    return 'Setup Mappers';
  }

  async handleDataObjectsPrimaryAction(): Promise<void> {
    if (this.dataObjects.length === 0) {
      if (this.developerPreferences.configureApi) {
        await this.setupControllers();
        return;
      }
      await this.saveProjectAndInvokeApi();
      return;
    }
    await this.setupMappers();
  }

  getMappersPrimaryActionLabel(): string {
    return this.developerPreferences.configureApi ? 'Setup Controller' : 'Save Project';
  }

  async handleMappersPrimaryAction(): Promise<void> {
    if (this.developerPreferences.configureApi) {
      await this.setupControllers();
      return;
    }
    this.saveProjectAndInvokeApi();
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

    return Array.from(rowsByName.values()).sort((a, b) => a.name.localeCompare(b.name));
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
    if (!this.controllersEditingSpecKey) {
      return this.controllerRestSpecRows.map((row) => row.name);
    }
    return this.controllerRestSpecRows
      .filter((row) => row.key !== this.controllersEditingSpecKey)
      .map((row) => row.name);
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

  openYamlPreviewModal(): void {
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
    return this.databaseOptions.filter(option => option.type === currentType);
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
    return Array.from(uniqueByName.values());
  }

  get controllersFieldTypeOptions(): string[] {
    const types = (Array.isArray(this.entities) ? this.entities : [])
      .flatMap((entity: any) => Array.isArray(entity?.fields) ? entity.fields : [])
      .map((field: any) => String(field?.type ?? '').trim())
      .filter(Boolean);
    const enums = (Array.isArray(this.enums) ? this.enums : [])
      .map((item: any) => String(item?.name ?? '').trim())
      .filter(Boolean);
    return Array.from(new Set([...ENTITY_FIELD_TYPE_OPTIONS, ...types, ...enums]));
  }

  get controllersDtoObjects(): Array<{ name?: string; dtoType?: 'request' | 'response'; fields?: unknown[] }> {
    return Array.isArray(this.dataObjects) ? this.dataObjects : [];
  }

  get controllersEnumTypes(): string[] {
    return (Array.isArray(this.enums) ? this.enums : [])
      .map((item: any) => String(item?.name ?? '').trim())
      .filter(Boolean);
  }

  get configuredEntityRestSpecNames(): string[] {
    return (Array.isArray(this.entities) ? this.entities : [])
      .filter((entity: any) => Boolean(entity?.addRestEndpoints))
      .map((entity: any) => String(entity?.restConfig?.resourceName ?? '').trim())
      .filter(Boolean);
  }

  private validateProjectNaming(): boolean {
    const projectName = trimmed(this.projectSettings.projectName);
    this.projectGroupError = '';
    this.projectNameError = '';

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
    }

    return valid;
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

    this.recentProjectToResume = { id: Number(latestProject.id) };
    this.showRecentProjectPrompt = true;
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
}
