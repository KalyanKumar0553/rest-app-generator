import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { AuthService, UserData } from '../../../../services/auth.service';
import { UserService, UserRoles } from '../../../../services/user.service';
import { ToastService } from '../../../../services/toast.service';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ConfirmationModalComponent],
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

  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private authService: AuthService,
    private userService: UserService,
    private toastService: ToastService
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
}
