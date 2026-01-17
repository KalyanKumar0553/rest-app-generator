import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { AuthService, UserData } from '../../../../services/auth.service';
import { UserService, UserRoles } from '../../../../services/user.service';
import { ToastService } from '../../../../services/toast.service';
import { MockApiService } from '../../../../services/mock-api.service';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';

export interface Project {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  status: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, ConfirmationModalComponent, SearchSortComponent, SidenavComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  userEmail: string = '';
  userName: string = '';
  userRoles: string[] = [];
  userPermissions: string[] = [];
  isLoadingRoles: boolean = false;
  showLogoutConfirmation: boolean = false;
  isLoggingOut: boolean = false;
  isSidebarOpen: boolean = false;
  projects: Project[] = [];
  filteredProjects: Project[] = [];
  isLoadingProjects: boolean = false;
  activeSection: string = 'projects';

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

  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private authService: AuthService,
    private userService: UserService,
    private toastService: ToastService,
    private mockApiService: MockApiService
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

    this.mockApiService.get<any>('', 'assets/mock/projects-response.json').subscribe({
      next: (response) => {
        this.isLoadingProjects = false;
        this.projects = response.data.projects || [];
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
      queryParams: { projectId: project.id }
    });
  }

  createNewProject(): void {
    this.router.navigate(['/project-generation']);
  }
}
