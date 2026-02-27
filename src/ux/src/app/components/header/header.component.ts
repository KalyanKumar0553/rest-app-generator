import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { LocalStorageService } from '../../services/local-storage.service';
import { AuthService } from '../../services/auth.service';
import { ModalService } from '../../services/modal.service';
import { APP_SETTINGS } from '../../settings/app-settings';
import { Theme, ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  isMenuOpen = false;
  isDashboardRoute = false;
  currentTheme: Theme = 'light';
  private themeSubscription?: Subscription;
  readonly appSettings = APP_SETTINGS;

  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private authService: AuthService,
    private modalService: ModalService,
    private themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.checkRoute(this.router.url);
    this.currentTheme = this.themeService.getCurrentTheme();
    this.themeSubscription = this.themeService.theme$.subscribe((theme) => {
      this.currentTheme = theme;
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.checkRoute(event.url);
    });
  }

  private checkRoute(url: string): void {
    this.isDashboardRoute = url.includes('/user/dashboard') || url.includes('/project-generation');
  }

  navigateToHome(): void {
    this.router.navigate(['/']);
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
    if (this.isMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  closeMenu(): void {
    this.isMenuOpen = false;
    document.body.style.overflow = '';
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  handleAccountClick(event: Event): void {
    event.preventDefault();
    this.closeMenu();

    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/user/dashboard']);
    } else {
      this.router.navigate(['/']);
      setTimeout(() => {
        this.modalService.openLoginModal();
      }, 100);
    }
  }

  handleStartProjectClick(event: Event): void {
    event.preventDefault();
    this.closeMenu();
    this.router.navigate(['/project-generation']);
  }

  handleLearnClick(event: Event): void {
    event.preventDefault();
    this.closeMenu();
    this.router.navigate(['/documentation']);
  }

  ngOnDestroy(): void {
    this.themeSubscription?.unsubscribe();
  }
}
