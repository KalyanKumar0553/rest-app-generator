import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntitiesComponent } from '../entities/entities.component';
import { AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';

interface ProjectSettings {
  projectGroup: string;
  projectName: string;
  buildType: 'gradle' | 'maven';
  language: 'java' | 'kotlin';
  frontend: string;
}

interface DatabaseSettings {
  database: string;
  dbGeneration: string;
  pluralizeTableNames: boolean;
  addDateCreatedLastUpdated: boolean;
}

interface DeveloperPreferences {
  applFormat: 'yaml' | 'properties';
  packages: 'technical' | 'domain' | 'mixed';
  enableOpenAPI: boolean;
  useDockerCompose: boolean;
  javaVersion: string;
  deployment: string;
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
    EntitiesComponent
  ],
  templateUrl: './project-generation-dashboard.component.html',
  styleUrls: ['./project-generation-dashboard.component.css']
})
export class ProjectGenerationDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  isSidebarOpen = false;
  isLoading = false;
  isLoggedIn = false;
  projectId: number | null = null;
  hasUnsavedChanges = false;
  showInfoBanner = true;
  activeSection = 'general';

  entities: any[] = [];
  relations: any[] = [];

  showBackConfirmation = false;
  backConfirmationConfig = {
    title: 'Unsaved Changes',
    message: ['You have unsaved changes. All changes will be discarded if you leave this page.', 'Are you sure you want to continue?'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Discard Changes', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  projectSettings: ProjectSettings = {
    projectGroup: 'io.bootify',
    projectName: 'my-app',
    buildType: 'gradle',
    language: 'java',
    frontend: 'none'
  };

  databaseSettings: DatabaseSettings = {
    database: 'PostgreSQL',
    dbGeneration: 'Hibernate (update)',
    pluralizeTableNames: false,
    addDateCreatedLastUpdated: false
  };

  developerPreferences: DeveloperPreferences = {
    applFormat: 'yaml',
    packages: 'technical',
    enableOpenAPI: false,
    useDockerCompose: false,
    javaVersion: '21',
    deployment: 'None'
  };

  dependencies = '';
  dependencyInput = '';
  selectedDependencies: string[] = [];
  filteredDependencies: string[] = [];
  availableDependencies = [
    'spring-boot-starter-web',
    'spring-boot-starter-data-jpa',
    'spring-boot-starter-security',
    'spring-boot-starter-validation',
    'spring-boot-starter-test',
    'spring-boot-starter-actuator',
    'spring-boot-devtools',
    'lombok',
    'mapstruct',
    'commons-lang3',
    'commons-collections4',
    'guava',
    'jackson-databind',
    'hibernate-validator',
    'flyway-core',
    'liquibase-core',
    'postgresql',
    'mysql-connector-java',
    'h2',
    'mongodb-driver',
    'redis',
    'kafka',
    'rabbitmq',
    'jwt',
    'swagger-ui',
    'openapi'
  ];

  frontendOptions = ['None', 'React', 'Vue', 'Angular'];
  databaseOptions = ['PostgreSQL', 'MySQL', 'H2', 'MongoDB'];
  dbGenerationOptions = ['Hibernate (update)', 'Hibernate (create)', 'Liquibase', 'Flyway'];
  javaVersionOptions = ['17', '21'];
  deploymentOptions = ['None', 'Docker', 'Kubernetes', 'Cloud'];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();

    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        if (params['projectId']) {
          this.projectId = +params['projectId'];
          this.loadProject(this.projectId);
        }
      });

    this.trackChanges();
  }

  ngOnDestroy(): void {
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
      dependencies: this.dependencies
    };
  }

  async loadProject(projectId: number): Promise<void> {
    this.isLoading = true;
    try {
      this.entities = [];
      this.relations = [];
      this.toastService.success('Project loaded successfully');
    } catch (error) {
      this.toastService.error('Failed to load project');
      console.error('Error loading project:', error);
    } finally {
      this.isLoading = false;
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

  navigateToSection(section: string): void {
    this.activeSection = section;
    this.closeSidebar();
  }

  handleBack(): void {
    if (this.hasUnsavedChanges) {
      this.showBackConfirmation = true;
    } else {
      this.navigateBack();
    }
  }

  confirmBack(): void {
    this.showBackConfirmation = false;
    this.navigateBack();
  }

  cancelBack(): void {
    this.showBackConfirmation = false;
  }

  navigateBack(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/user/dashboard']);
    } else {
      this.router.navigate(['/']);
    }
  }

  closeInfoBanner(): void {
    this.showInfoBanner = false;
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

  async setupEntities(): Promise<void> {
    this.isLoading = true;
    try {
      this.toastService.success('Navigating to entity setup');
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async saveProject(): Promise<void> {
    this.isLoading = true;
    try {
      const projectData = this.getProjectData();

      this.hasUnsavedChanges = false;
      this.toastService.success('Project saved successfully');
    } catch (error) {
      this.toastService.error('Failed to save project');
      console.error('Error saving project:', error);
    } finally {
      this.isLoading = false;
    }
  }
}
