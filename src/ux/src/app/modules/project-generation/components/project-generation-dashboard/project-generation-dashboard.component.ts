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

interface ProjectSettings {
  projectGroup: string;
  projectName: string;
  buildType: 'gradle' | 'maven';
  language: 'java' | 'kotlin';
  frontend: string;
}

interface DatabaseSettings {
  dbType: 'SQL' | 'NOSQL' | 'NONE';
  database: string;
  dbGeneration: string;
  pluralizeTableNames: boolean;
}

interface DatabaseOption {
  value: string;
  label: string;
  type: 'SQL' | 'NOSQL';
}

interface DeveloperPreferences {
  applFormat: 'yaml' | 'properties';
  packages: 'technical' | 'domain' | 'mixed';
  enableOpenAPI: boolean;
  enableActuator: boolean;
  configureApi: boolean;
  enableLombok: boolean;
  useDockerCompose: boolean;
  profiles: string[];
  javaVersion: string;
  deployment: string;
}

interface ProjectRunSummary {
  id: string;
  projectId: string;
  status: string;
  createdAt?: string;
  runNumber?: number;
}

interface ControllerRestSpecRow {
  key: string;
  name: string;
  totalEndpoints: number;
  mappedEntities: string[];
  entityIndexes: number[];
  hasControllersConfig: boolean;
}

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

  baseNavItems: NavItem[] = [
    { icon: 'public', label: 'General', value: 'general' },
    { icon: 'storage', label: 'Entities', value: 'entities' },
    { icon: 'category', label: 'Data Objects', value: 'data-objects' },
    { icon: 'search', label: 'Explore', value: 'explore' },
  ];
  actuatorNavItem: NavItem = { icon: 'device_hub', label: 'Actuator', value: 'actuator' };
  controllersNavItem: NavItem = { icon: 'tune', label: 'Controllers', value: 'controllers' };
  mappersNavItem: NavItem = { icon: 'shuffle', label: 'Mappers', value: 'mappers' };

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
  backConfirmationConfig = {
    title: 'Unsaved Changes',
    message: ['You have unsaved changes. All changes will be discarded if you leave this page.', 'Are you sure you want to continue?'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Discard Changes', type: 'danger' as const, action: 'confirm' as const }
    ]
  };
  entitiesDeleteConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Confirmation',
    message: 'All Configured Entities will be deleted. Want to Continue ?',
    buttons: [
      { text: 'Continue', type: 'danger', action: 'confirm' },
      { text: 'Cancel', type: 'cancel', action: 'cancel' }
    ]
  };
  restSpecDeleteConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Delete REST Configuration',
    message: 'This will remove REST endpoint configuration and related model mapping.',
    buttons: [
      { text: 'Delete', type: 'danger', action: 'confirm' },
      { text: 'Cancel', type: 'cancel', action: 'cancel' }
    ]
  };
  configureApiDisableConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Confirmation',
    message: 'All API configuration will be lost. Want to proceed ?',
    buttons: [
      { text: 'Continue', type: 'danger', action: 'confirm' },
      { text: 'Cancel', type: 'cancel', action: 'cancel' }
    ]
  };
  controllersConfigDiscardConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Confirmation',
    message: 'All Change will be discarded from the configuration. Want to proceed ?',
    buttons: [
      { text: 'Confirm', type: 'danger', action: 'confirm' },
      { text: 'Cancel', type: 'cancel', action: 'cancel' }
    ]
  };
  generationCancelConfirmationConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Cancel Generation',
    message: 'Project generation is in progress. Do you want to cancel?',
    buttons: [
      { text: 'Confirm', type: 'danger', action: 'confirm' },
      { text: 'Cancel', type: 'confirm', action: 'cancel' }
    ]
  };
  pendingRestSpecDeleteKey: string | null = null;

  recentProjectPromptConfig: { title: string; message: string; buttons: ModalButton[] } = {
    title: 'Load Recent project',
    message: "You've worked on an another project before. Would you like to continue there?",
    buttons: [
      { text: 'Resume Last Project', type: 'confirm', action: 'confirm' },
      { text: 'Create New project', type: 'cancel', action: 'cancel' }
    ]
  };

  projectSettings: ProjectSettings = {
    projectGroup: APP_SETTINGS.defaultProjectGroup,
    projectName: 'my-app',
    buildType: 'gradle',
    language: 'java',
    frontend: 'none'
  };

  databaseSettings: DatabaseSettings = {
    dbType: 'SQL',
    database: 'POSTGRES',
    dbGeneration: 'Hibernate (update)',
    pluralizeTableNames: false
  };

  developerPreferences: DeveloperPreferences = {
    applFormat: 'yaml',
    packages: 'technical',
    enableOpenAPI: false,
    enableActuator: false,
    configureApi: true,
    enableLombok: false,
    useDockerCompose: false,
    profiles: [],
    javaVersion: '21',
    deployment: 'None'
  };
  controllersConfig: RestEndpointConfig = this.getDefaultControllersConfig();
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

  frontendOptions = ['None', 'React', 'Vue', 'Angular'];
  databaseOptions: DatabaseOption[] = [
    { value: 'MSSQL', label: 'MSSQL Server', type: 'SQL' },
    { value: 'MYSQL', label: 'MySQL', type: 'SQL' },
    { value: 'MARIADB', label: 'MariaDB', type: 'SQL' },
    { value: 'ORACLE', label: 'Oracle', type: 'SQL' },
    { value: 'POSTGRES', label: 'PostgreSQL', type: 'SQL' },
    { value: 'DERBY', label: 'Apache Derby', type: 'SQL' },
    { value: 'H2', label: 'H2 Database', type: 'SQL' },
    { value: 'HSQL', label: 'HyperSQL', type: 'SQL' },
    { value: 'MONGODB', label: 'MongoDB', type: 'NOSQL' }
  ];
  dbTypeOptions: Array<'SQL' | 'NOSQL' | 'NONE'> = ['SQL', 'NOSQL', 'NONE'];
  dbGenerationOptions = ['Hibernate (update)', 'Hibernate (create)'];
  javaVersionOptions = ['17', '21'];
  deploymentOptions = ['None', 'Docker', 'Kubernetes', 'Cloud'];
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService,
    private http: HttpClient,
    private validatorService: ValidatorService,
    private localStorageService: LocalStorageService,
    private cdr: ChangeDetectorRef,
    private projectGenerationState: ProjectGenerationStateService
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

    const isNoneDatabase = this.toDatabaseCode(this.databaseSettings.database) === 'NONE';
    if (!isNoneDatabase) {
      return navItems;
    }
    return navItems.filter(item => item.value !== 'entities');
  }

  isEntitiesTabVisible(): boolean {
    return this.toDatabaseCode(this.databaseSettings.database) !== 'NONE';
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
        this.databaseSettings.dbType = this.resolveDatabaseType(this.databaseSettings.dbType, this.databaseSettings.database);
        this.databaseSettings.database = this.toDatabaseCode(this.databaseSettings.database);
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
        this.controllersConfig = this.parseControllersConfig(projectData?.controllers?.config);
        const configurationOptions = this.getActuatorConfigurationOptions(projectData?.preferences?.profiles);
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
        this.controllersConfig = this.getDefaultControllersConfig();
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
    if (section === 'entities' && this.toDatabaseCode(this.databaseSettings.database) === 'NONE') {
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
          this.exploreZipFileName = `${this.toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
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
    const generatorSpec = this.mapProjectToGeneratorSpec(this.getProjectData());
    return this.convertObjectToYaml(generatorSpec);
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
      this.exploreZipFileName = fileName || `${this.toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
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
    this.exploreZipFileName = `${this.toArtifactId(this.projectSettings.projectName || projectId)}.zip`;
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
          this.exploreZipFileName = `${this.toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
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
      this.exploreZipFileName = cachedEntry.fileName || `${this.toArtifactId(this.projectSettings.projectName || 'project')}.zip`;
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
    const backendId = this.trimmed(this.backendProjectId);
    const localProjectId = this.projectId ? String(this.projectId) : '';
    const artifactId = this.toArtifactId(this.projectSettings.projectName || 'project');
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

  private convertObjectToYaml(value: any): string {
    return this.toYaml(value, 0).trim() + '\n';
  }

  private mapProjectToGeneratorSpec(project: any): any {
    const projectGroup = this.trimmed(project?.settings?.projectGroup) || 'com.example';
    const projectName = this.trimmed(project?.settings?.projectName) || 'demo-app';
    const databaseCode = this.toDatabaseCode(project?.database?.database);

    const app = {
      name: projectName,
      groupId: projectGroup,
      artifactId: this.toArtifactId(projectName),
      description: 'Generated by Rest App Generator',
      version: '0.0.1-SNAPSHOT',
      jdkVersion: this.trimmed(project?.preferences?.javaVersion) || '17',
      buildTool: this.trimmed(project?.settings?.buildType) || 'gradle',
      generator: this.trimmed(project?.settings?.language) || 'java'
    };

    const includeControllersSpec =
      Boolean(project?.preferences?.configureApi)
      && project?.controllers?.enabled !== false
      && this.hasNamedControllersConfig(project?.controllers?.config);
    const restSpecSection = this.buildRestSpecSection(project?.entities, project?.controllers?.config, includeControllersSpec);
    const spec: any = {
      app,
      database: databaseCode,
      dbType: this.resolveDatabaseType(project?.database?.dbType, databaseCode),
      applFormat: this.trimmed(project?.preferences?.applFormat) || 'yaml',
      enableOpenapi: Boolean(project?.preferences?.enableOpenAPI),
      enableActuator: Boolean(project?.preferences?.enableActuator),
      enableLombok: Boolean(
        project?.preferences?.enableLombok === undefined
          ? project?.preferences?.optionalLombok
          : project?.preferences?.enableLombok
      ),
      useDockerCompose: Boolean(project?.preferences?.useDockerCompose),
      packages: this.trimmed(project?.preferences?.packages) || 'technical',
      profiles: this.mapProfiles(project?.preferences?.profiles),
      dependencies: this.extractDependencies(project),
      basePackage: projectGroup,
      models: this.mapModels(project?.entities, project?.relations, restSpecSection.entityToSpecName),
      dtos: this.mapDtos(project?.dataObjects),
      enums: this.mapEnums(project?.enums)
    };
    const mapperSpecs = this.mapMappers(project?.mappers);
    if (mapperSpecs.length) {
      spec.mappers = mapperSpecs;
    }
    if (databaseCode !== 'NONE') {
      spec.dbGeneration = this.trimmed(project?.database?.dbGeneration) || 'Hibernate (update)';
      spec.pluralizeTableNames = Boolean(project?.database?.pluralizeTableNames);
    }
    if (Boolean(project?.preferences?.enableActuator)) {
      const actuatorConfigurations = this.sanitizeActuatorConfigurations(
        project?.actuator?.configurations ?? project?.actuator?.endpoints,
        this.getActuatorConfigurationOptions(project?.preferences?.profiles)
      );
      spec.actuator = {
        endpoints: {
          include: this.sanitizeActuatorEndpoints(actuatorConfigurations['default'])
        },
        profiles: Object.entries(actuatorConfigurations)
          .filter(([profile]) => profile !== 'default')
          .reduce((acc, [profile, endpoints]) => {
            acc[profile] = {
              endpoints: {
                include: this.sanitizeActuatorEndpoints(endpoints)
              }
            };
            return acc;
          }, {} as Record<string, unknown>)
      };
    }

    if (
      Boolean(project?.preferences?.configureApi)
      && project?.controllers?.enabled !== false
      && this.hasNamedControllersConfig(project?.controllers?.config)
    ) {
      const controllersConfig = this.mapRestConfig(project?.controllers?.config);
      if (controllersConfig) {
        spec.controllers = controllersConfig;
      }
    }

    if (restSpecSection.specs.length) {
      spec['rest-spec'] = restSpecSection.specs;
    }

    if (!spec.dependencies.length) {
      delete spec.dependencies;
    }
    if (!spec.models.length) {
      delete spec.models;
    }
    if (!spec.dtos.length) {
      delete spec.dtos;
    }
    if (!spec.enums.length) {
      delete spec.enums;
    }

    return spec;
  }

  private toDatabaseCode(value: unknown): string {
    const raw = this.trimmed(value) || '';
    const normalized = raw.toLowerCase();
    if (['NONE', 'OTHER', 'MSSQL', 'MYSQL', 'MARIADB', 'ORACLE', 'POSTGRES', 'MONGODB', 'DERBY', 'H2', 'HSQL'].includes(raw.toUpperCase())) {
      return raw.toUpperCase();
    }
    switch (normalized) {
      case 'none':
        return 'NONE';
      case 'other':
        return 'OTHER';
      case 'mssql':
      case 'mssql server':
      case 'sql server':
        return 'MSSQL';
      case 'mysql':
        return 'MYSQL';
      case 'mariadb':
        return 'MARIADB';
      case 'oracle':
        return 'ORACLE';
      case 'postgres':
      case 'postgresql':
        return 'POSTGRES';
      case 'mongodb':
      case 'mongo':
        return 'MONGODB';
      case 'derby':
      case 'apache derby':
        return 'DERBY';
      case 'h2':
      case 'h2 database':
        return 'H2';
      case 'hsql':
      case 'hypersql':
      case 'hsql database':
        return 'HSQL';
      default:
        return 'POSTGRES';
    }
  }

  private extractDependencies(project: any): string[] {
    const fromString = String(project?.dependencies ?? '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
    const fromSelected = Array.isArray(this.selectedDependencies)
      ? this.selectedDependencies.map((item) => String(item).trim()).filter(Boolean)
      : [];

    return Array.from(new Set([...fromSelected, ...fromString]));
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
    const sanitizedConfig = this.parseControllersConfig(config);
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
      const normalizedConfig = this.parseControllersConfig(entity.restConfig);
      const name = this.trimmed(normalizedConfig.resourceName);
      const entityName = this.trimmed(entity?.name);
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

    const controllerConfigName = this.trimmed(this.controllersConfig?.resourceName);
    if (this.controllersConfigEnabled && controllerConfigName) {
      const normalizedControllerConfig = this.parseControllersConfig(this.controllersConfig);
      const existingControllerRow = rowsByName.get(controllerConfigName);
      if (!existingControllerRow) {
        const mappedEntityName = normalizedControllerConfig.mapToEntity
          ? this.trimmed(normalizedControllerConfig.mappedEntityName)
          : '';
        rowsByName.set(controllerConfigName, {
          key: controllerConfigName,
          name: controllerConfigName,
          totalEndpoints: this.countEnabledEndpoints(normalizedControllerConfig),
          mappedEntities: mappedEntityName ? [mappedEntityName] : [],
          entityIndexes: mappedEntityName
            ? entityList
              .map((entity: any, index: number) => ({ name: this.trimmed(entity?.name), index }))
              .filter((item) => item.name === mappedEntityName)
              .map((item) => item.index)
            : [],
          hasControllersConfig: true
        });
      } else {
        existingControllerRow.totalEndpoints = this.countEnabledEndpoints(normalizedControllerConfig);
        existingControllerRow.hasControllersConfig = true;
        if (normalizedControllerConfig.mapToEntity) {
          const mappedEntityName = this.trimmed(normalizedControllerConfig.mappedEntityName);
          if (mappedEntityName && !existingControllerRow.mappedEntities.includes(mappedEntityName)) {
            existingControllerRow.mappedEntities.push(mappedEntityName);
          }
          const mappedIndex = entityList.findIndex((entity: any) => this.trimmed(entity?.name) === mappedEntityName);
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
      const entityName = this.trimmed(entity?.name);
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
      this.controllersEditingSpecConfig = this.parseControllersConfig(this.controllersConfig);
      return;
    }
    const firstEntityIndex = row.entityIndexes[0];
    const entity = firstEntityIndex >= 0 ? this.entities[firstEntityIndex] : null;
    this.controllersEditingSpecConfig = entity?.restConfig
      ? this.parseControllersConfig(entity.restConfig)
      : null;
    if (this.controllersEditingSpecConfig) {
      const currentMapToEntity = Boolean((this.controllersEditingSpecConfig as any).mapToEntity);
      const currentMappedEntity = this.trimmed((this.controllersEditingSpecConfig as any).mappedEntityName);
      if (!currentMapToEntity || !currentMappedEntity) {
        const firstMapped = this.trimmed(row.mappedEntities?.[0]);
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

    const editingKey = this.trimmed(this.controllersEditingSpecKey);
    const globalKey = this.trimmed(this.controllersConfig?.resourceName);
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

    if (row.hasControllersConfig && this.trimmed(this.controllersConfig?.resourceName) === key) {
      this.controllersConfigEnabled = false;
      this.controllersConfig = this.getDefaultControllersConfig();
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

  private getActuatorConfigurationOptions(profileSource: unknown = this.developerPreferences.profiles): string[] {
    const normalizedProfiles = this.mapProfiles(profileSource).filter(profile => profile !== 'default');
    return ['default', ...normalizedProfiles];
  }

  private normalizeActuatorConfigurationName(value: unknown): string | null {
    const normalized = this.normalizeProfileName(value);
    if (!normalized) {
      return null;
    }
    return normalized === 'default' ? 'default' : normalized;
  }

  private sanitizeActuatorConfigurations(
    rawConfigurations: unknown,
    availableConfigurations: string[] = this.getActuatorConfigurationOptions()
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
    const options = this.getActuatorConfigurationOptions();
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
    const selectedCode = this.toDatabaseCode(value);
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
    this.databaseSettings.dbType = this.resolveDatabaseType(this.databaseSettings.dbType, code);
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
    return this.databaseSettings.dbType !== 'NONE' && this.toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  shouldShowPluralizeTableNames(): boolean {
    return this.databaseSettings.dbType !== 'NONE' && this.toDatabaseCode(this.databaseSettings.database) !== 'NONE';
  }

  private restoreDatabaseSelection(value: string): void {
    const selection = this.toDatabaseCode(value);
    setTimeout(() => {
      this.databaseSettings.database = selection;
      this.databaseSettings.dbType = this.resolveDatabaseType(this.databaseSettings.dbType, selection);
      this.cdr.detectChanges();
    }, 0);
  }

  private ensureDatabaseSelectionForType(): void {
    if (this.databaseSettings.dbType === 'NONE') {
      this.databaseSettings.database = 'NONE';
      return;
    }
    const selectedDatabase = this.toDatabaseCode(this.databaseSettings.database);
    const allowed = this.filteredDatabaseOptions.some(option => option.value === selectedDatabase);
    if (allowed) {
      return;
    }
    const firstMatching = this.filteredDatabaseOptions.find(option => option.type === this.databaseSettings.dbType)?.value;
    this.databaseSettings.database = firstMatching || 'POSTGRES';
  }

  private resolveDatabaseType(type: unknown, databaseCode: unknown): 'SQL' | 'NOSQL' | 'NONE' {
    const normalizedType = this.trimmed(type)?.toUpperCase();
    if (normalizedType === 'NONE') {
      return 'NONE';
    }
    if (normalizedType === 'NOSQL') {
      return 'NOSQL';
    }
    if (normalizedType === 'SQL') {
      return 'SQL';
    }

    const normalizedDb = this.toDatabaseCode(databaseCode);
    if (normalizedDb === 'NONE') {
      return 'NONE';
    }
    if (normalizedDb === 'MONGODB') {
      return 'NOSQL';
    }
    return 'SQL';
  }

  private mapProfiles(profiles: unknown): string[] {
    if (!Array.isArray(profiles)) {
      return [];
    }
    const normalized = profiles
      .map(profile => this.normalizeProfileName(profile))
      .filter((profile): profile is string => Boolean(profile))
      .filter(profile => this.isValidProfileName(profile));
    return Array.from(new Set(normalized));
  }

  private mapModels(entities: any, relations: any, entityToSpecName: Record<string, string> = {}): any[] {
    const entityList = Array.isArray(entities) ? entities : [];
    const relationList = Array.isArray(relations) ? relations : [];

    return entityList.map((entity) => {
      const fields = Array.isArray(entity?.fields) ? entity.fields : [];
      const idField = fields.find((field: any) => Boolean(field?.primaryKey));
      const nonIdFields = fields.filter((field: any) => !field?.primaryKey);
      const modelRelations = relationList
        .filter((relation: any) => this.trimmed(relation?.sourceEntity) === this.trimmed(entity?.name))
        .map((relation: any) => this.mapRelation(relation));

      const model: any = {
        name: this.trimmed(entity?.name) || 'Entity',
        tableName: this.toSnakeCase(this.trimmed(entity?.name) || 'entity'),
        addRestEndpoints: Boolean(entity?.addRestEndpoints),
        addCrudOperations: Boolean(entity?.addCrudOperations),
        classMethods: this.mapClassMethods(entity?.classMethods),
        options: {
          entity: !Boolean(entity?.mappedSuperclass),
          immutable: Boolean(entity?.immutable),
          auditing: Boolean(entity?.auditable),
          softDelete: Boolean(entity?.softDelete),
          naturalIdCache: Boolean(entity?.naturalIdCache)
        },
        id: this.mapId(idField),
        fields: nonIdFields.map((field: any) => this.mapModelField(field))
      };

      const entityName = this.trimmed(entity?.name);
      const mappedSpecName = entityToSpecName[entityName];
      if (mappedSpecName) {
        model['rest-spec-name'] = mappedSpecName;
      }

      const restConfig = this.mapRestConfig(entity?.restConfig);
      if (restConfig) {
        model.rest = restConfig;
      }

      if (modelRelations.length) {
        model.relations = modelRelations;
      }

      return model;
    });
  }

  private buildRestSpecSection(
    entities: any,
    controllersConfig: any,
    includeControllers: boolean
  ): { specs: any[]; entityToSpecName: Record<string, string> } {
    const specs: any[] = [];
    const entityToSpecName: Record<string, string> = {};
    const specIndexByName = new Map<string, number>();
    const entityList = Array.isArray(entities) ? entities : [];

    const addOrUpdateSpec = (name: string, config: any): string => {
      const specName = this.trimmed(name) || 'RestSpec';
      const existingIndex = specIndexByName.get(specName);
      if (existingIndex === undefined) {
        specIndexByName.set(specName, specs.length);
        specs.push({ name: specName, ...config });
      } else {
        specs[existingIndex] = { name: specName, ...config };
      }
      return specName;
    };

    entityList.forEach((entity: any) => {
      if (!Boolean(entity?.addRestEndpoints)) {
        return;
      }
      const mapped = this.mapRestConfig(entity?.restConfig);
      if (!mapped) {
        return;
      }
      const entityName = this.trimmed(entity?.name);
      const specName = addOrUpdateSpec(this.trimmed(mapped.resourceName) || entityName || 'EntityRest', mapped);
      if (entityName) {
        entityToSpecName[entityName] = specName;
      }
    });

    if (includeControllers) {
      const mappedControllers = this.mapRestConfig(controllersConfig);
      if (mappedControllers) {
        addOrUpdateSpec(this.trimmed(mappedControllers.resourceName) || 'ControllersRest', mappedControllers);
      }
    }

    return { specs, entityToSpecName };
  }

  private hasNamedControllersConfig(config: any): boolean {
    return Boolean(this.trimmed(config?.resourceName));
  }

  private mapRestConfig(restConfig: any): any | null {
    if (!restConfig || typeof restConfig !== 'object') {
      return null;
    }
    const operations: string[] = [
      'list',
      'get',
      'create',
      'patch',
      'delete',
      'bulkInsert',
      'bulkUpdate',
      'bulkDelete'
    ];

    const methods: Record<string, any> = {};
    operations.forEach((key) => {
      if (!Boolean(restConfig?.methods?.[key])) {
        return;
      }
      const documentation = this.buildOperationDocumentationConfig(restConfig, key);
      methods[key] = {
        request: this.buildOperationRequestConfig(key, restConfig),
        response: this.buildOperationResponseConfig(restConfig, key),
        ...(documentation ? { documentation } : {})
      };
    });

    return {
      resourceName: this.trimmed(restConfig.resourceName),
      basePath: this.trimmed(restConfig.basePath),
      apiVersioning: {
        enabled: Boolean(restConfig?.apiVersioning?.enabled),
        strategy: restConfig?.apiVersioning?.strategy === 'path' ? 'path' : 'header',
        headerName: this.trimmed(restConfig?.apiVersioning?.headerName),
        defaultVersion: this.trimmed(restConfig?.apiVersioning?.defaultVersion)
      },
      pathVariableType: ['UUID', 'LONG', 'STRING'].includes(String(restConfig?.pathVariableType))
        ? restConfig.pathVariableType
        : 'UUID',
      deletion: {
        mode: restConfig?.deletion?.mode === 'HARD' ? 'HARD' : 'SOFT',
        restoreEndpoint: Boolean(restConfig?.deletion?.restoreEndpoint),
        includeDeletedParam: Boolean(restConfig?.deletion?.includeDeletedParam)
      },
      hateoas: {
        enabled: Boolean(restConfig?.hateoas?.enabled),
        selfLink: Boolean(restConfig?.hateoas?.selfLink),
        updateLink: Boolean(restConfig?.hateoas?.updateLink),
        deleteLink: Boolean(restConfig?.hateoas?.deleteLink)
      },
      documentation: {
        includeDefaultDocumentation: restConfig?.documentation?.includeDefaultDocumentation !== false
      },
      methods
    };
  }

  private buildOperationResponseConfig(restConfig: any, operationKey: string): any {
    const responseType = restConfig?.requestResponse?.response?.responseType === 'DTO_DIRECT'
      ? 'DTO_DIRECT'
      : restConfig?.requestResponse?.response?.responseType === 'CUSTOM_WRAPPER'
        ? 'CUSTOM_WRAPPER'
        : 'RESPONSE_ENTITY';
    const perOperationDto = this.trimmed(restConfig?.requestResponse?.response?.endpointDtos?.[operationKey]);
    return {
      responseType,
      dtoName: responseType === 'CUSTOM_WRAPPER'
        ? (perOperationDto || this.trimmed(restConfig?.requestResponse?.response?.dtoName))
        : this.trimmed(restConfig?.requestResponse?.response?.dtoName),
      responseWrapper: restConfig?.requestResponse?.response?.responseWrapper === 'NONE'
        ? 'NONE'
        : restConfig?.requestResponse?.response?.responseWrapper === 'UPSERT'
          ? 'UPSERT'
          : 'STANDARD_ENVELOPE',
      enableFieldProjection: Boolean(restConfig?.requestResponse?.response?.enableFieldProjection),
      includeHateoasLinks: Boolean(restConfig?.requestResponse?.response?.includeHateoasLinks)
    };
  }

  private buildOperationDocumentationConfig(restConfig: any, docKey: string): any | null {
    const includeDefaults = restConfig?.documentation?.includeDefaultDocumentation !== false;
    const defaultDocumentation = this.getDefaultDocumentationForOperation(docKey, this.trimmed(restConfig?.resourceName));
    const description = this.trimmed(restConfig?.documentation?.endpoints?.[docKey]?.description)
      || (includeDefaults ? defaultDocumentation.description : '');
    const group = this.trimmed(restConfig?.documentation?.endpoints?.[docKey]?.group)
      || (includeDefaults ? defaultDocumentation.group : '');
    const descriptionTags = Array.isArray(restConfig?.documentation?.endpoints?.[docKey]?.descriptionTags)
      ? restConfig.documentation.endpoints[docKey].descriptionTags.map((item: any) => this.trimmed(item)).filter(Boolean)
      : [];
    const resolvedDescriptionTags = descriptionTags.length
      ? descriptionTags
      : (includeDefaults ? defaultDocumentation.descriptionTags : []);
    const deprecated = Boolean(restConfig?.documentation?.endpoints?.[docKey]?.deprecated);

    const documentation: Record<string, any> = {};
    if (description) {
      documentation['description'] = description;
    }
    if (group) {
      documentation['group'] = group;
    }
    if (resolvedDescriptionTags.length) {
      documentation['descriptionTags'] = resolvedDescriptionTags;
    }
    if (deprecated) {
      documentation['deprecated'] = true;
    }

    return Object.keys(documentation).length ? documentation : null;
  }

  private getDefaultDocumentationForOperation(
    operationKey: string,
    resourceName: string
  ): { description: string; group: string; descriptionTags: string[] } {
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
    const target = this.trimmed(resourceName) || 'API';
    return {
      description: `${endpointLabel[String(operationKey)] || String(operationKey)} operation for ${target}`,
      group: `${target} Group`,
      descriptionTags: [String(operationKey)]
    };
  }

  private buildOperationRequestConfig(operationKey: string, restConfig: any): any {
    switch (operationKey) {
      case 'create':
        return {
          mode: restConfig?.requestResponse?.request?.create?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
          dtoName: this.trimmed(restConfig?.requestResponse?.request?.create?.dtoName)
        };
      case 'update':
        return {
          mode: restConfig?.requestResponse?.request?.update?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
          dtoName: this.trimmed(restConfig?.requestResponse?.request?.update?.dtoName)
        };
      case 'patch':
        return {
          mode: restConfig?.requestResponse?.request?.patch?.mode === 'JSON_PATCH' ? 'JSON_PATCH' : 'JSON_MERGE_PATCH'
        };
      case 'list':
        return {
          mode: restConfig?.requestResponse?.request?.list?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
          dtoName: this.trimmed(restConfig?.requestResponse?.request?.list?.dtoName),
          pagination: {
            enabled: Boolean(restConfig?.pagination?.enabled ?? restConfig?.pagination),
            mode: restConfig?.pagination?.mode === 'CURSOR' ? 'CURSOR' : 'OFFSET',
            sortField: this.trimmed(restConfig?.pagination?.sortField),
            sortDirection: restConfig?.pagination?.sortDirection === 'ASC' ? 'ASC' : 'DESC'
          },
          searchFiltering: {
            keywordSearch: Boolean(restConfig?.searchFiltering?.keywordSearch),
            jpaSpecification: Boolean(restConfig?.searchFiltering?.jpaSpecification),
            searchableFields: Array.isArray(restConfig?.searchFiltering?.searchableFields)
              ? restConfig.searchFiltering.searchableFields.map((item: any) => this.trimmed(item)).filter(Boolean)
              : []
          }
        };
      case 'get':
        return {
          idType: this.trimmed(restConfig?.requestResponse?.request?.getByIdType)
        };
      case 'delete':
        return {
          mode: restConfig?.requestResponse?.request?.delete?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
          dtoName: this.trimmed(restConfig?.requestResponse?.request?.delete?.dtoName),
          idType: this.trimmed(restConfig?.requestResponse?.request?.deleteByIdType)
        };
      case 'bulkInsert':
        {
          const createDto = this.trimmed(restConfig?.requestResponse?.request?.create?.dtoName);
          const derivedType = createDto ? `List<${createDto}>` : '';
        return {
          type: derivedType,
          batch: {
            batchSize: Number(restConfig?.batchOperations?.insert?.batchSize) || 1,
            enableAsyncMode: Boolean(restConfig?.batchOperations?.insert?.enableAsyncMode)
          }
        };
        }
      case 'bulkUpdate':
        {
          const updateDto = this.trimmed(restConfig?.requestResponse?.request?.update?.dtoName);
          const createDto = this.trimmed(restConfig?.requestResponse?.request?.create?.dtoName);
          const derivedType = updateDto
            ? `List<${updateDto}>`
            : createDto
              ? `List<${createDto}>`
              : '';
        return {
          type: derivedType,
          batch: {
            batchSize: Number(restConfig?.batchOperations?.update?.batchSize) || 1,
            updateMode: restConfig?.batchOperations?.update?.updateMode === 'PATCH' ? 'PATCH' : 'PUT',
            optimisticLockHandling: restConfig?.batchOperations?.update?.optimisticLockHandling === 'SKIP_CONFLICTS'
              ? 'SKIP_CONFLICTS'
              : 'FAIL_ON_CONFLICT',
            validationStrategy: restConfig?.batchOperations?.update?.validationStrategy === 'SKIP_DUPLICATES'
              ? 'SKIP_DUPLICATES'
              : 'VALIDATE_ALL_FIRST',
            enableAsyncMode: Boolean(restConfig?.batchOperations?.update?.enableAsyncMode),
            asyncProcessing: Boolean(restConfig?.batchOperations?.update?.asyncProcessing)
          }
        };
        }
      case 'bulkDelete':
        {
          const deleteDto = this.trimmed(restConfig?.requestResponse?.request?.delete?.dtoName);
          const deleteIdType = this.trimmed(restConfig?.requestResponse?.request?.deleteByIdType);
          const derivedType = deleteDto
            ? `List<${deleteDto}>`
            : deleteIdType
              ? `List<${deleteIdType}>`
              : '';
        return {
          type: derivedType,
          batch: {
            deletionStrategy: restConfig?.batchOperations?.bulkDelete?.deletionStrategy === 'HARD' ? 'HARD' : 'SOFT',
            batchSize: Number(restConfig?.batchOperations?.bulkDelete?.batchSize) || 1,
            failureStrategy: restConfig?.batchOperations?.bulkDelete?.failureStrategy === 'CONTINUE_AND_REPORT_FAILURES'
              ? 'CONTINUE_AND_REPORT_FAILURES'
              : 'STOP_ON_FIRST_ERROR',
            enableAsyncMode: Boolean(restConfig?.batchOperations?.bulkDelete?.enableAsyncMode),
            allowIncludeDeletedParam: Boolean(restConfig?.batchOperations?.bulkDelete?.allowIncludeDeletedParam)
          }
        };
        }
      default:
        return {};
    }
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

  private getDefaultControllersConfig(): RestEndpointConfig {
    return {
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
          list: { mode: 'GENERATE_DTO', dtoName: '' },
          create: { mode: 'GENERATE_DTO', dtoName: '' },
          delete: { mode: 'GENERATE_DTO', dtoName: '' },
          update: { mode: 'GENERATE_DTO', dtoName: '' },
          patch: { mode: 'JSON_MERGE_PATCH' },
          getByIdType: 'UUID',
          deleteByIdType: 'UUID',
          bulkInsertType: '',
          bulkUpdateType: '',
          bulkDeleteType: ''
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
        includeDefaultDocumentation: true,
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
  }

  private countEnabledEndpoints(config: RestEndpointConfig): number {
    const methods = config?.methods;
    if (!methods) {
      return 0;
    }
    return [
      methods.list,
      methods.get,
      methods.create,
      methods.patch,
      methods.delete,
      methods.bulkInsert,
      methods.bulkUpdate,
      methods.bulkDelete
    ].filter(Boolean).length;
  }

  private resolveEntityRestSpecName(entity: any): string {
    const inlineName = this.trimmed(entity?.restConfig?.resourceName);
    if (inlineName) {
      return inlineName;
    }
    return this.trimmed(
      entity?.['rest-spec-name']
      ?? entity?.restSpecName
      ?? entity?.restSpec
    );
  }

  private parseControllersConfig(rawConfig: RestEndpointConfig | null | undefined): RestEndpointConfig {
    const fallback = this.getDefaultControllersConfig();
    if (!rawConfig || typeof rawConfig !== 'object') {
      return fallback;
    }
    const cloned = JSON.parse(JSON.stringify(rawConfig)) as RestEndpointConfig;
    const merged: RestEndpointConfig = {
      ...fallback,
      ...cloned,
      mapToEntity: Boolean((cloned as any).mapToEntity ?? fallback.mapToEntity),
      mappedEntityName: this.trimmed((cloned as any).mappedEntityName),
      methods: { ...fallback.methods, ...(cloned as any).methods },
      apiVersioning: { ...fallback.apiVersioning, ...(cloned as any).apiVersioning },
      deletion: { ...fallback.deletion, ...(cloned as any).deletion },
      hateoas: { ...fallback.hateoas, ...(cloned as any).hateoas },
      pagination: { ...fallback.pagination, ...(cloned as any).pagination },
      searchFiltering: { ...fallback.searchFiltering, ...(cloned as any).searchFiltering },
      batchOperations: {
        insert: { ...fallback.batchOperations.insert, ...(cloned as any).batchOperations?.insert },
        update: { ...fallback.batchOperations.update, ...(cloned as any).batchOperations?.update },
        bulkDelete: { ...fallback.batchOperations.bulkDelete, ...(cloned as any).batchOperations?.bulkDelete }
      },
      requestResponse: {
        request: {
          ...fallback.requestResponse.request,
          ...(cloned as any).requestResponse?.request,
          list: { ...fallback.requestResponse.request.list, ...(cloned as any).requestResponse?.request?.list },
          create: { ...fallback.requestResponse.request.create, ...(cloned as any).requestResponse?.request?.create },
          delete: { ...fallback.requestResponse.request.delete, ...(cloned as any).requestResponse?.request?.delete },
          update: { ...fallback.requestResponse.request.update, ...(cloned as any).requestResponse?.request?.update },
          patch: { ...fallback.requestResponse.request.patch, ...(cloned as any).requestResponse?.request?.patch }
        },
        response: {
          ...fallback.requestResponse.response,
          ...(cloned as any).requestResponse?.response,
          endpointDtos: {
            ...fallback.requestResponse.response.endpointDtos,
            ...(cloned as any).requestResponse?.response?.endpointDtos
          }
        }
      },
      documentation: {
        includeDefaultDocumentation: (cloned as any).documentation?.includeDefaultDocumentation !== false,
        endpoints: {
          ...fallback.documentation.endpoints,
          ...(cloned as any).documentation?.endpoints
        }
      }
    };
    return merged;
  }

  private applyControllersEntityMapping(config: RestEndpointConfig, previousSpecKey: string | null): void {
    const nextSpecName = this.trimmed(config?.resourceName);
    const previousName = this.trimmed(previousSpecKey);
    const selectedEntityName = config.mapToEntity ? this.trimmed(config?.mappedEntityName) : '';
    const normalizedConfig = this.parseControllersConfig(config);

    this.entities.forEach((entity: any) => {
      const entityName = this.trimmed(entity?.name);
      const mappedSpecName = this.resolveEntityRestSpecName(entity);
      const inlineSpecName = this.trimmed(entity?.restConfig?.resourceName);
      const matchesPrevious = Boolean(previousName) && (mappedSpecName === previousName || inlineSpecName === previousName);
      const matchesNext = Boolean(nextSpecName) && (mappedSpecName === nextSpecName || inlineSpecName === nextSpecName);
      const shouldClear = matchesPrevious || matchesNext;

      if (!shouldClear) {
        return;
      }

      if (!selectedEntityName) {
        entity.addRestEndpoints = true;
        entity.restConfig = this.parseControllersConfig({
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

    const mappedEntity = this.entities.find((entity: any) => this.trimmed(entity?.name) === selectedEntityName);
    if (mappedEntity) {
      mappedEntity.addRestEndpoints = true;
      mappedEntity.restConfig = this.parseControllersConfig(config);
      mappedEntity['rest-spec-name'] = nextSpecName || selectedEntityName;
    }
    this.entities = [...this.entities];
  }

  private clearAllApiConfigurations(): void {
    this.controllersConfigEnabled = false;
    this.controllersConfig = this.getDefaultControllersConfig();
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

  private normalizeProfileName(value: unknown): string | null {
    if (value === null || value === undefined) {
      return null;
    }
    const trimmed = String(value).trim();
    if (!trimmed) {
      return null;
    }
    return trimmed.toLowerCase();
  }

  private isValidProfileName(profile: string): boolean {
    return /^[a-z0-9._-]+$/.test(profile);
  }

  private mapId(primaryKeyField: any): any {
    if (!primaryKeyField) {
      return {
        field: 'id',
        type: 'Long',
        generation: {
          strategy: 'IDENTITY'
        }
      };
    }

    const type = this.normalizeType(primaryKeyField?.type);
    const generation = type === 'UUID'
      ? { strategy: 'UUID', generatorName: 'uuid', hibernateUuidStrategy: 'uuid2' }
      : { strategy: 'IDENTITY' };

    return {
      field: this.trimmed(primaryKeyField?.name) || 'id',
      type,
      generation
    };
  }

  private mapModelField(field: any): any {
    const mapped: any = {
      name: this.trimmed(field?.name) || 'field',
      type: this.normalizeType(field?.type)
    };

    const constraints = this.mapConstraints(field?.constraints);
    if (constraints.length) {
      mapped.constraints = constraints;
    }

    const column: any = {};
    if (this.trimmed(field?.name)) {
      column.name = this.toSnakeCase(field.name);
    }
    if (typeof field?.required === 'boolean') {
      column.nullable = !field.required;
    } else if (constraints.some((constraint: any) => this.isNotNullConstraint(constraint))) {
      column.nullable = false;
    }
    if (field?.unique === true) {
      column.unique = true;
    }
    if (this.normalizeType(field?.type) === 'String' && this.hasNumber(field?.maxLength)) {
      column.length = Number(field.maxLength);
    }

    if (Object.keys(column).length) {
      mapped.column = column;
    }

    return mapped;
  }

  private mapDtos(dataObjects: any): any[] {
    const items = Array.isArray(dataObjects) ? dataObjects : [];
    return items.map((dataObject: any) => {
      const dto: any = {
        name: this.trimmed(dataObject?.name) || 'DataObject',
        type: this.trimmed(dataObject?.dtoType) || 'request',
        classMethods: this.mapClassMethods(dataObject?.classMethods),
        fields: (Array.isArray(dataObject?.fields) ? dataObject.fields : []).map((field: any) => this.mapDtoField(field))
      };

      if (dataObject?.mapperEnabled && Array.isArray(dataObject?.mapperModels) && dataObject.mapperModels.length) {
        dto.mapper = {
          enabled: true,
          models: dataObject.mapperModels
            .map((name: any) => this.trimmed(name))
            .filter(Boolean)
        };
      }

      return dto;
    });
  }

  private mapEnums(enums: any): any[] {
    const items = Array.isArray(enums) ? enums : [];
    return items
      .map((enumItem: any) => ({
        name: this.trimmed(enumItem?.name),
        storage: this.trimmed(enumItem?.storage) || 'STRING',
        constants: Array.isArray(enumItem?.constants)
          ? enumItem.constants.map((item: any) => this.trimmed(item)).filter(Boolean)
          : []
      }))
      .filter((enumItem: any) => enumItem.name && enumItem.constants.length > 0);
  }

  private mapMappers(mappers: any): any[] {
    const items = Array.isArray(mappers) ? mappers : [];
    return items
      .map((mapper: any) => ({
        name: this.trimmed(mapper?.name),
        fromModel: this.trimmed(mapper?.fromModel),
        toModel: this.trimmed(mapper?.toModel),
        mappings: Array.isArray(mapper?.mappings)
          ? mapper.mappings
            .map((item: any) => ({
              sourceField: this.trimmed(item?.sourceField),
              targetField: this.trimmed(item?.targetField)
            }))
            .filter((item: any) => item.sourceField && item.targetField)
          : []
      }))
      .filter((mapper: any) => mapper.name && mapper.fromModel && mapper.toModel && mapper.mappings.length > 0);
  }

  private mapDtoField(field: any): any {
    const dtoField: any = {
      name: this.trimmed(field?.name) || 'field',
      type: this.normalizeType(field?.type)
    };

    const jsonProperty = this.trimmed(field?.jsonProperty);
    if (jsonProperty) {
      dtoField.jsonProperty = jsonProperty;
    }

    const constraints = this.mapConstraints(field?.constraints);
    if (constraints.length) {
      dtoField.constraints = constraints;
    }

    return dtoField;
  }

  private mapClassMethods(classMethods: any): any {
    return {
      toString: Boolean(classMethods?.toString ?? true),
      hashCode: Boolean(classMethods?.hashCode ?? true),
      equals: Boolean(classMethods?.equals ?? true),
      noArgsConstructor: Boolean(classMethods?.noArgsConstructor ?? true),
      allArgsConstructor: Boolean(classMethods?.allArgsConstructor ?? true),
      builder: Boolean(classMethods?.builder ?? false)
    };
  }

  private mapRelation(relation: any): any {
    const type = this.trimmed(relation?.relationType) || 'ManyToOne';
    const mapped: any = {
      name: this.trimmed(relation?.sourceFieldName) || 'relation',
      type,
      target: this.trimmed(relation?.targetEntity) || 'Target'
    };

    const cascade = Array.isArray(relation?.cascade)
      ? relation.cascade.map((item: any) => this.trimmed(item)).filter(Boolean)
      : [];
    if (cascade.length) {
      mapped.cascade = Array.from(new Set(cascade));
    }

    const mappedBy = this.trimmed(relation?.mappedBy);
    if (mappedBy) {
      mapped.mappedBy = mappedBy;
    }
    if (relation?.orphanRemoval === true) {
      mapped.orphanRemoval = true;
    }
    const orderBy = this.trimmed(relation?.orderBy);
    if (orderBy) {
      mapped.orderBy = orderBy;
    }
    const orderColumnName = this.trimmed(relation?.orderColumn?.name);
    if (orderColumnName) {
      mapped.orderColumn = { name: orderColumnName };
    }

    if (type === 'ManyToOne' || type === 'OneToOne') {
      if (typeof relation?.optional === 'boolean') {
        mapped.optional = relation.optional;
      } else if (typeof relation?.required === 'boolean') {
        mapped.optional = !relation.required;
      }
      const joinColumn = this.mapJoinColumn(relation?.joinColumn);
      if (joinColumn) {
        mapped.joinColumn = joinColumn;
      }
    }

    if (type === 'ManyToMany') {
      const joinTable = this.mapJoinTable(relation?.joinTable);
      if (joinTable) {
        mapped.joinTable = joinTable;
      }
    }

    return mapped;
  }

  private mapJoinColumn(joinColumn: any): any | null {
    if (!joinColumn || typeof joinColumn !== 'object') {
      return null;
    }
    const mapped: any = {};
    const name = this.trimmed(joinColumn?.name);
    const referenced = this.trimmed(joinColumn?.referencedColumnName);
    if (name) {
      mapped.name = name;
    }
    if (referenced) {
      mapped.referencedColumnName = referenced;
    }
    if (typeof joinColumn?.nullable === 'boolean') {
      mapped.nullable = joinColumn.nullable;
    }
    if (joinColumn?.index === true) {
      mapped.index = true;
    }
    const onDelete = this.trimmed(joinColumn?.onDelete);
    if (onDelete) {
      mapped.onDelete = onDelete;
    }
    return Object.keys(mapped).length ? mapped : null;
  }

  private mapJoinTable(joinTable: any): any | null {
    if (!joinTable || typeof joinTable !== 'object') {
      return null;
    }

    const mapped: any = {};
    const name = this.trimmed(joinTable?.name);
    if (name) {
      mapped.name = name;
    }

    const joinColumns = this.mapJoinColumns(joinTable?.joinColumns);
    if (joinColumns.length) {
      mapped.joinColumns = joinColumns;
    }

    const inverseJoinColumns = this.mapJoinColumns(joinTable?.inverseJoinColumns);
    if (inverseJoinColumns.length) {
      mapped.inverseJoinColumns = inverseJoinColumns;
    }

    if (joinTable?.uniquePair === true) {
      mapped.uniquePair = true;
    }
    const onDelete = this.trimmed(joinTable?.onDelete);
    if (onDelete) {
      mapped.onDelete = onDelete;
    }

    return Object.keys(mapped).length ? mapped : null;
  }

  private mapJoinColumns(columns: any): any[] {
    const items = Array.isArray(columns) ? columns : [];
    return items
      .map((column) => this.mapJoinColumn(column))
      .filter(Boolean);
  }

  private mapConstraints(constraints: any): any[] {
    const items = Array.isArray(constraints) ? constraints : [];
    return items
      .map((constraint: any) => this.mapConstraint(constraint))
      .filter(Boolean);
  }

  private mapConstraint(constraint: any): any | null {
    const name = this.trimmed(constraint?.name);
    if (!name) {
      return null;
    }

    const value = this.trimmed(constraint?.value);
    const value2 = this.trimmed(constraint?.value2);
    const params: any = {};

    if (name === 'Size') {
      if (value) {
        params.min = this.toNumberIfPossible(value);
      }
      if (value2) {
        params.max = this.toNumberIfPossible(value2);
      }
    } else if (name === 'Min' || name === 'Max') {
      if (value) {
        params.value = this.toNumberIfPossible(value);
      }
    } else if (name === 'DecimalMin' || name === 'DecimalMax') {
      if (value) {
        params.value = value;
      }
      if (value2) {
        params.inclusive = this.toBooleanIfPossible(value2);
      }
    } else if (name === 'Digits') {
      if (value) {
        params.integer = this.toNumberIfPossible(value);
      }
      if (value2) {
        params.fraction = this.toNumberIfPossible(value2);
      }
    } else if (name === 'Pattern') {
      if (value) {
        params.regex = value;
      }
    } else {
      if (value) {
        params.value = this.toNumberIfPossible(value);
      }
      if (value2) {
        params.value2 = this.toNumberIfPossible(value2);
      }
    }

    if (Object.keys(params).length === 0) {
      return name;
    }

    return { [name]: params };
  }

  private toYaml(value: any, indent: number): string {
    const indentation = '  '.repeat(indent);

    if (value === null || value === undefined) {
      return 'null';
    }

    if (Array.isArray(value)) {
      if (value.length === 0) {
        return '[]';
      }
      return value.map(item => {
        if (this.isScalar(item)) {
          return `${indentation}- ${this.formatScalar(item)}`;
        }
        const nested = this.toYaml(item, indent + 1);
        return `${indentation}-\n${nested}`;
      }).join('\n');
    }

    if (typeof value === 'object') {
      const entries = Object.entries(value).filter(([, v]) => v !== undefined);
      if (entries.length === 0) {
        return '{}';
      }
      return entries.map(([key, val]) => {
        if (this.isScalar(val)) {
          return `${indentation}${key}: ${this.formatScalar(val)}`;
        }
        if (Array.isArray(val) && val.length === 0) {
          return `${indentation}${key}: []`;
        }
        if (typeof val === 'object' && val !== null && !Array.isArray(val) && Object.keys(val).length === 0) {
          return `${indentation}${key}: {}`;
        }
        return `${indentation}${key}:\n${this.toYaml(val, indent + 1)}`;
      }).join('\n');
    }

    return this.formatScalar(value);
  }

  private isScalar(value: any): boolean {
    return value === null || ['string', 'number', 'boolean'].includes(typeof value);
  }

  private formatScalar(value: any): string {
    if (value === null || value === undefined) {
      return 'null';
    }
    if (typeof value === 'number' || typeof value === 'boolean') {
      return String(value);
    }
    const stringValue = String(value);
    if (stringValue === '') {
      return '""';
    }
    if (/^[a-zA-Z0-9_\-./]+$/.test(stringValue)) {
      return stringValue;
    }
    return `"${stringValue.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
  }

  private trimmed(value: unknown): string {
    return String(value ?? '').trim();
  }

  private hasNumber(value: unknown): boolean {
    if (value === null || value === undefined) {
      return false;
    }
    return !Number.isNaN(Number(value));
  }

  private toNumberIfPossible(value: string): string | number {
    const numeric = Number(value);
    return Number.isNaN(numeric) ? value : numeric;
  }

  private toBooleanIfPossible(value: string): string | boolean {
    const normalized = value.toLowerCase();
    if (normalized === 'true') {
      return true;
    }
    if (normalized === 'false') {
      return false;
    }
    return value;
  }

  private normalizeType(type: unknown): string {
    const value = this.trimmed(type);
    const typeMap: Record<string, string> = {
      Int: 'Integer',
      Decimal: 'BigDecimal',
      Date: 'LocalDate',
      Time: 'LocalTime',
      DateTime: 'OffsetDateTime',
      Json: 'String'
    };
    return typeMap[value] || value || 'String';
  }

  private toSnakeCase(value: string): string {
    return value
      .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
      .replace(/[\s\-]+/g, '_')
      .replace(/__+/g, '_')
      .toLowerCase();
  }

  private toArtifactId(value: string): string {
    return value
      .trim()
      .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
      .replace(/[\s_]+/g, '-')
      .replace(/-+/g, '-')
      .toLowerCase();
  }

  private validateProjectNaming(): boolean {
    const projectName = this.trimmed(this.projectSettings.projectName);
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
      artifactId: this.toArtifactId(projectName)
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

  private isNotNullConstraint(constraint: any): boolean {
    if (typeof constraint === 'string') {
      return constraint === 'NotNull';
    }
    if (!constraint || typeof constraint !== 'object') {
      return false;
    }
    const [firstKey] = Object.keys(constraint);
    return firstKey === 'NotNull';
  }

  private promptForRecentProjectIfAvailable(): void {
    const projects = this.getSavedProjects();
    if (!projects.length) {
      return;
    }

    const latestProject = projects
      .filter(project => this.hasNumber(project?.id))
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
