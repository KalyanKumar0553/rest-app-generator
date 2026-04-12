import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../services/auth.service';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { SidenavComponent, NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { APP_SETTINGS } from '../../../../settings/app-settings';
import { UserService } from '../../../../services/user.service';
import { AiLabsService } from '../../../../services/ai-labs.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatIconModule,
    ConfirmationModalComponent,
    SidenavComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  readonly appSettings = APP_SETTINGS;
  userEmail = '';
  isSidebarOpen = false;
  activeSection = 'projects';
  showLogoutConfirmation = false;
  isLoggingOut = false;
  userPermissions: string[] = [];
  isAiLabsEnabled = false;

  private readonly baseNavItems: NavItem[] = [
    { icon: 'folder', label: 'Projects', value: 'projects' },
    { icon: 'psychology', label: 'AI Labs', value: 'ai-labs' },
    { icon: 'person', label: 'Profile', value: 'profile' },
    { icon: 'settings', label: 'Settings', value: 'settings' }
  ];

  get navItems(): NavItem[] {
    const items = this.baseNavItems.filter((item) => item.value !== 'ai-labs' || this.isAiLabsEnabled);
    if (this.userPermissions.includes('artifact.app.read')) {
      items.push({ icon: 'inventory_2', label: 'Artifacts', value: 'artifacts' });
    }
    return items;
  }

  readonly logoutModalConfig = {
    title: 'Are you sure you want to logout ?',
    message: ['You will need to login again to access your dashboard.'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Logout', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService,
    private aiLabsService: AiLabsService
  ) {}

  @HostListener('window:pageshow', ['$event'])
  onPageShow(event: PageTransitionEvent): void {
    if (event.persisted || !this.authService.getAccessToken()) {
      this.checkAuthentication();
    }
  }

  @HostListener('window:popstate')
  onPopState(): void {
    this.checkAuthentication();
  }

  ngOnInit(): void {
    this.checkAuthentication();
    const userData = this.authService.getUserData();
    if (userData) {
      this.userEmail = userData.email;
    }
    this.aiLabsService.getAvailability().subscribe({
      next: (response) => {
        this.isAiLabsEnabled = !!response?.enabled;
        this.syncActiveSection(this.router.url);
      },
      error: () => {
        this.isAiLabsEnabled = false;
        this.syncActiveSection(this.router.url);
      }
    });
    this.userService.getUserRoles().subscribe({
      next: (response) => {
        this.userPermissions = response.permissions || [];
        this.syncActiveSection(this.router.url);
      },
      error: () => {
        this.userPermissions = [];
      }
    });
    this.syncActiveSection(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.syncActiveSection(event.urlAfterRedirects);
        this.closeSidebar();
      });
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  navigateToSection(section: string): void {
    this.router.navigate(['/user/dashboard', section]);
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

  private checkAuthentication(): void {
    const token = this.authService.getAccessToken();
    if (!token || !this.authService.currentUserValue) {
      this.router.navigate(['/'], { replaceUrl: true });
    }
  }

  private syncActiveSection(url: string): void {
    const section = url.split('/').filter(Boolean).pop();
    this.activeSection = this.navItems.some((item) => item.value === section) ? section as string : 'projects';
  }
}
