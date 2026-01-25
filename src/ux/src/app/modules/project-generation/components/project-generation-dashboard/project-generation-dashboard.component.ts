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
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG, API_ENDPOINTS } from '../../../../constants/api.constants';

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
    EntitiesComponent,
    SidenavComponent
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

  navItems: NavItem[] = [
    { icon: 'public', label: 'General', value: 'general' },
    { icon: 'storage', label: 'Entities', value: 'entities' },
    { icon: 'category', label: 'Data Objects', value: 'data-objects' },
    { icon: 'search', label: 'Explore', value: 'explore' },
    { icon: 'download', label: 'Download', value: 'download' }
  ];

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
  availableDependencies: string[] = [];

  frontendOptions = ['None', 'React', 'Vue', 'Angular'];
  databaseOptions = ['PostgreSQL', 'MySQL', 'H2', 'MongoDB'];
  dbGenerationOptions = ['Hibernate (update)', 'Hibernate (create)', 'Liquibase', 'Flyway'];
  javaVersionOptions = ['17', '21'];
  deploymentOptions = ['None', 'Docker', 'Kubernetes', 'Cloud'];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService,
    private http: HttpClient
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
    this.loadDependencies();
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
      dependencies: this.dependencies,
      entities: this.entities,
      relations: this.relations
    };
  }

  async loadProject(projectId: number): Promise<void> {
    this.isLoading = true;
    try {
      const projectData = this.loadProjectFromStorage(projectId);

      if (projectData) {
        this.entities = projectData.entities || [];
        this.relations = projectData.relations || [];
        this.projectSettings = projectData.settings || this.projectSettings;
        this.databaseSettings = projectData.database || this.databaseSettings;
        this.developerPreferences = projectData.preferences || this.developerPreferences;
        this.dependencies = projectData.dependencies || '';
        this.toastService.success('Project loaded successfully');
      } else {
        this.entities = [];
        this.relations = [];
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

  handleHome(): void {
    if (this.hasUnsavedChanges) {
      this.showBackConfirmation = true;
    } else {
      this.navigateHome();
    }
  }

  confirmBack(): void {
    this.showBackConfirmation = false;
    this.navigateHome();
  }

  cancelBack(): void {
    this.showBackConfirmation = false;
  }

  navigateHome(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/user/dashboard']);
    } else {
      this.router.navigate(['/']);
    }
  }

  closeInfoBanner(): void {
    this.showInfoBanner = false;
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
      this.navigateToSection('data-objects');
      this.toastService.success('Data objects setup loaded');
    } catch (error) {
      this.toastService.error('Failed to proceed');
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }

}
