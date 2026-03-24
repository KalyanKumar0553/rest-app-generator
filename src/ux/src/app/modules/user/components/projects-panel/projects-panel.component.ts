import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProjectService, ProjectSummary } from '../../../../services/project.service';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { ToastService } from '../../../../services/toast.service';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { resolveProjectGenerationRoute } from '../../../project-generation/utils/project-generation-route.utils';

export interface Project extends ProjectSummary {
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-projects-panel',
  standalone: true,
  imports: [CommonModule, ConfirmationModalComponent, SearchSortComponent],
  templateUrl: './projects-panel.component.html',
  styleUrls: ['./projects-panel.component.css']
})
export class ProjectsPanelComponent implements OnInit {
  projects: Project[] = [];
  filteredProjects: Project[] = [];
  isLoadingProjects = false;
  showDeleteConfirmation = false;
  showOpenConflictConfirmation = false;
  isDeletingProject = false;
  projectToDelete: Project | null = null;
  projectToOpen: Project | null = null;

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
    let filtered = [...this.projects];
    if (event.searchTerm) {
      const searchLower = event.searchTerm.toLowerCase();
      filtered = filtered.filter((project) =>
        this.searchConfig.properties.some((prop) => {
          const value = (project as any)[prop];
          return value && value.toString().toLowerCase().includes(searchLower);
        })
      );
    }
    if (event.sortOption) {
      filtered.sort((a, b) => {
        const aValue = (a as any)[event.sortOption!.property];
        const bValue = (b as any)[event.sortOption!.property];
        let comparison = 0;
        if (aValue < bValue) comparison = -1;
        if (aValue > bValue) comparison = 1;
        return event.sortOption!.direction === 'asc' ? comparison : -comparison;
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
    this.projectService.getProjectCollaboration(projectId).subscribe({
      next: (state) => {
        if ((state?.activeEditors ?? 0) > 0) {
          this.projectToOpen = project;
          this.showOpenConflictConfirmation = true;
          return;
        }
        this.navigateToProject(projectId, project.generator);
      },
      error: () => {
        this.navigateToProject(projectId, project.generator);
      }
    });
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

  private navigateToProject(projectId: string, generator?: string): void {
    this.router.navigate([resolveProjectGenerationRoute(generator)], { queryParams: { projectId } });
  }
}
