import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../services/auth.service';
import { UserService, UserRoles } from '../../../../services/user.service';
import { ProjectService, ProjectSummary } from '../../../../services/project.service';
import { ToastService } from '../../../../services/toast.service';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { APP_SETTINGS } from '../../../../settings/app-settings';
import { LocalStorageService } from '../../../../services/local-storage.service';

export interface Project extends ProjectSummary {
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, ConfirmationModalComponent, SearchSortComponent, SidenavComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  readonly appSettings = APP_SETTINGS;
  userEmail: string = '';
  userName: string = '';
  userRoles: string[] = [];
  userPermissions: string[] = [];
  isLoadingRoles: boolean = false;
  showLogoutConfirmation: boolean = false;
  isLoggingOut: boolean = false;
  showDeleteConfirmation: boolean = false;
  isDeletingProject: boolean = false;
  isSidebarOpen: boolean = false;
  projects: Project[] = [];
  filteredProjects: Project[] = [];
  isLoadingProjects: boolean = false;
  activeSection: string = 'projects';
  projectToDelete: Project | null = null;

  navItems: NavItem[] = [
    { icon: 'folder', label: 'Projects', value: 'projects' },
    { icon: 'person', label: 'Profile', value: 'profile' },
    { icon: 'settings', label: 'Settings', value: 'settings' }
  ];

  searchConfig: SearchConfig = {
    placeholder: 'Search projects by name or description...',
    properties: ['name', 'description', 'id']
  };

  sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' },
    { label: 'Created Date (Newest)', property: 'createdAt', direction: 'desc' },
    { label: 'Created Date (Oldest)', property: 'createdAt', direction: 'asc' },
    { label: 'Last Modified (Newest)', property: 'updatedAt', direction: 'desc' },
    { label: 'Last Modified (Oldest)', property: 'updatedAt', direction: 'asc' }
  ];

  logoutModalConfig = {
    title: 'Logout Confirmation',
    message: [
      'Are you sure you want to logout?',
      'You will need to login again to access your dashboard.'
    ],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Logout', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  deleteModalConfig = {
    title: 'Delete Project',
    message: [
      'Are you sure you want to permanently delete this project?',
      'This will hard delete the project, project runs, and contributor records. This action cannot be undone.'
    ],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService,
    private projectService: ProjectService,
    private toastService: ToastService,
    private localStorageService: LocalStorageService,
  ) {}

  @HostListener('window:pageshow', ['$event'])
  onPageShow(event: PageTransitionEvent): void {
    if (event.persisted || !this.authService.getAccessToken()) {
      this.checkAuthentication();
    }
  }

  @HostListener('window:popstate', ['$event'])
  onPopState(event: PopStateEvent): void {
    this.checkAuthentication();
  }

  ngOnInit(): void {
    this.checkAuthentication();

    const userData = this.authService.getUserData();
    if (userData) {
      this.userEmail = userData.email;
      this.userName = userData.name || 'User';
    }

    this.loadUserRoles();
    this.loadProjects();
  }

  private checkAuthentication(): void {
    const token = this.authService.getAccessToken();
    if (!token || !this.authService.currentUserValue) {
      this.router.navigate(['/'], { replaceUrl: true });
    }
  }

  loadUserRoles(): void {
    this.isLoadingRoles = true;

    this.userService.getUserRoles().subscribe({
      next: (rolesData: UserRoles) => {
        this.isLoadingRoles = false;
        this.userRoles = rolesData.roles || [];
        this.userPermissions = rolesData.permissions || [];
      },
      error: () => {
        this.isLoadingRoles = false;
        this.toastService.error('Failed to load user roles');
      }
    });
  }

  hasRole(role: string): boolean {
    return this.userRoles.includes(role);
  }

  hasPermission(permission: string): boolean {
    return this.userPermissions.includes(permission);
  }

  logout(): void {
    this.showLogoutConfirmation = true;
  }

  confirmLogout(): void {
    this.isLoggingOut = true;

    this.authService.logout().subscribe({
      next: () => {
        this.isLoggingOut = false;
        this.showLogoutConfirmation = false;
        this.toastService.success('Logged out successfully');
      },
      error: () => {
        this.isLoggingOut = false;
        this.showLogoutConfirmation = false;
      }
    });
  }

  cancelLogout(): void {
    this.showLogoutConfirmation = false;
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

  navigateToAccount(): void {
  }

  navigateToPlan(): void {
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  navigateToSection(section: string): void {
    this.activeSection = section;
    this.closeSidebar();
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
      filtered = filtered.filter(project => {
        return this.searchConfig.properties.some(prop => {
          const value = (project as any)[prop];
          return value && value.toString().toLowerCase().includes(searchLower);
        });
      });
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

  openProject(project: Project): void {
    this.router.navigate(['/project-generation'], {
      queryParams: { projectId: project.projectId || project.id }
    });
  }

  createNewProject(): void {
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');
    this.router.navigate(['/project-generation']);
  }
}
