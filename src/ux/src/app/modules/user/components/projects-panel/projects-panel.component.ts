import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProjectService, ProjectSummary } from '../../../../services/project.service';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { ToastService } from '../../../../services/toast.service';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { resolveProjectGenerationRoute } from '../../../project-generation/utils/project-generation-route.utils';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { LoadingOverlayComponent } from '../../../../components/shared/loading-overlay/loading-overlay.component';
import { NoDataStateComponent } from '../../../../components/shared/no-data-state/no-data-state.component';

export interface Project extends ProjectSummary {
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-projects-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmationModalComponent, SearchSortComponent, ModalComponent, LoadingOverlayComponent, NoDataStateComponent],
  templateUrl: './projects-panel.component.html',
  styleUrls: ['./projects-panel.component.css']
})
export class ProjectsPanelComponent implements OnInit {
  projects: Project[] = [];
  filteredProjects: Project[] = [];
  isLoadingProjects = false;
  showDeleteConfirmation = false;
  showOpenConflictConfirmation = false;
  showImportProjectModal = false;
  isDeletingProject = false;
  isImportingProject = false;
  projectToDelete: Project | null = null;
  projectToOpen: Project | null = null;
  importProjectUrl = '';
  importProjectValidationMessage = '';
  private currentSearchSortEvent: SearchSortEvent = { searchTerm: '', sortOption: null };

  readonly searchConfig: SearchConfig = {
    placeholder: 'Search projects by name or description...',
    properties: ['name', 'description', 'id']
  };

  readonly sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' },
    { label: 'Created Date (Newest)', property: 'createdAt', direction: 'desc' },
    { label: 'Created Date (Oldest)', property: 'createdAt', direction: 'asc' },
    { label: 'Last Modified (Newest)', property: 'updatedAt', direction: 'desc' },
    { label: 'Last Modified (Oldest)', property: 'updatedAt', direction: 'asc' }
  ];

  readonly deleteModalConfig = {
    title: 'Are you sure to permanently delete this project ?',
    message: ['This will hard delete the project, project runs, and contributor records. This action cannot be undone.'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  readonly openConflictModalConfig = {
    title: 'Another collaborator is editing this project',
    message: [
      'Opening now may lead to version conflicts if you save changes based on an older draft.',
      'Do you want to continue and open the project anyway?'
    ],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Proceed', type: 'confirm' as const, action: 'confirm' as const }
    ]
  };

  constructor(
    private router: Router,
    private projectService: ProjectService,
    private localStorageService: LocalStorageService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.isLoadingProjects = true;
    this.projectService.getProjects().subscribe({
      next: (response) => {
        this.isLoadingProjects = false;
        this.projects = Array.isArray(response) ? response as Project[] : [];
        this.filteredProjects = [...this.projects];
      },
      error: () => {
        this.isLoadingProjects = false;
        this.toastService.error('Failed to load projects');
      }
    });
  }

  onSearchSortChange(event: SearchSortEvent): void {
    this.currentSearchSortEvent = event;
    this.applySearchSort();
  }

  openImportProjectModal(): void {
    this.showImportProjectModal = true;
    this.importProjectUrl = '';
    this.importProjectValidationMessage = '';
  }

  closeImportProjectModal(): void {
    if (this.isImportingProject) {
      return;
    }
    this.showImportProjectModal = false;
    this.importProjectUrl = '';
    this.importProjectValidationMessage = '';
  }

  importProject(): void {
    const normalizedUrl = this.importProjectUrl.trim();
    if (!this.isValidProjectImportUrl(normalizedUrl)) {
      this.importProjectValidationMessage = 'Enter a valid project collaboration URL.';
      return;
    }

    this.importProjectValidationMessage = '';
    this.isImportingProject = true;
    this.projectService.importProject(normalizedUrl).subscribe({
      next: (response) => {
        this.isImportingProject = false;
        const importedProject = response as Project;
        const importedProjectId = importedProject.projectId || importedProject.id;
        if (this.projects.some((project) => (project.projectId || project.id) === importedProjectId)) {
          this.toastService.error('Project already Exists');
          this.closeImportProjectModal();
          return;
        }
        this.projects = [importedProject, ...this.projects];
        this.applySearchSort();
        this.closeImportProjectModal();
        this.toastService.success('Project imported successfully');
      },
      error: (error) => {
        this.isImportingProject = false;
        this.importProjectValidationMessage = error?.error?.errorMsg || 'Failed to import project.';
      }
    });
  }

  private applySearchSort(): void {
    let filtered = [...this.projects];
    if (this.currentSearchSortEvent.searchTerm) {
      const searchLower = this.currentSearchSortEvent.searchTerm.toLowerCase();
      filtered = filtered.filter((project) =>
        this.searchConfig.properties.some((prop) => {
          const value = (project as any)[prop];
          return value && value.toString().toLowerCase().includes(searchLower);
        })
      );
    }
    if (this.currentSearchSortEvent.sortOption) {
      filtered.sort((a, b) => {
        const aValue = (a as any)[this.currentSearchSortEvent.sortOption!.property];
        const bValue = (b as any)[this.currentSearchSortEvent.sortOption!.property];
        let comparison = 0;
        if (aValue < bValue) comparison = -1;
        if (aValue > bValue) comparison = 1;
        return this.currentSearchSortEvent.sortOption!.direction === 'asc' ? comparison : -comparison;
      });
    }
    this.filteredProjects = filtered;
  }

  createNewProject(): void {
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');
    this.router.navigate(['/project-generation']);
  }

  openProject(project: Project): void {
    const projectId = project.projectId || project.id;
    if (!projectId) {
      this.toastService.error('Unable to open project');
      return;
    }
    this.navigateToProject(projectId, project.generator);
  }

  promptDeleteProject(project: Project): void {
    this.projectToDelete = project;
    this.showDeleteConfirmation = true;
  }

  confirmDeleteProject(): void {
    const projectId = this.projectToDelete?.projectId || this.projectToDelete?.id;
    if (!projectId) {
      this.showDeleteConfirmation = false;
      this.projectToDelete = null;
      this.toastService.error('Unable to delete project');
      return;
    }
    this.isDeletingProject = true;
    this.projectService.deleteProject(projectId).subscribe({
      next: () => {
        this.isDeletingProject = false;
        this.showDeleteConfirmation = false;
        this.projectToDelete = null;
        this.toastService.success('Project deleted successfully');
        this.loadProjects();
      },
      error: () => {
        this.isDeletingProject = false;
        this.showDeleteConfirmation = false;
        this.projectToDelete = null;
        this.toastService.error('Failed to delete project');
      }
    });
  }

  cancelDeleteProject(): void {
    this.showDeleteConfirmation = false;
    this.projectToDelete = null;
  }

  confirmOpenProjectDespiteConflict(): void {
    const projectId = this.projectToOpen?.projectId || this.projectToOpen?.id;
    this.showOpenConflictConfirmation = false;
    this.projectToOpen = null;
    if (!projectId) {
      this.toastService.error('Unable to open project');
      return;
    }
    this.navigateToProject(projectId, this.projectToOpen?.generator);
  }

  cancelOpenProjectDespiteConflict(): void {
    this.showOpenConflictConfirmation = false;
    this.projectToOpen = null;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(',', '');
  }

  getProjectLanguageLabel(generator: string | null | undefined): string {
    const normalizedGenerator = String(generator ?? '').trim().toLowerCase();
    switch (normalizedGenerator) {
      case 'python':
        return 'Python';
      case 'node':
        return 'Node';
      case 'java':
        return 'Java';
      default:
        return normalizedGenerator ? normalizedGenerator.toUpperCase() : 'Unknown';
    }
  }

  private navigateToProject(projectId: string, generator?: string): void {
    this.router.navigate([resolveProjectGenerationRoute(generator)], { queryParams: { projectId } });
  }

  private isValidProjectImportUrl(value: string): boolean {
    try {
      const parsedUrl = new URL(value);
      const routeText = `${parsedUrl.pathname}${parsedUrl.hash}`;
      return routeText.includes('project-collaboration/');
    } catch {
      return false;
    }
  }
}
