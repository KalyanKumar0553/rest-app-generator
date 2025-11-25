import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocalStorageService } from '../../services/local-storage.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  isMenuOpen = false;

  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private authService: AuthService
  ) {}

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

  handleAccountClick(event: Event): void {
    event.preventDefault();
    this.closeMenu();

    const userDetails = this.localStorageService.get('userDetails');

    if (!userDetails) {
      console.log('No user details found in localStorage');
      return;
    }

    const token = userDetails.token;

    if (!token) {
      console.log('No JWT token found in user details');
      return;
    }

    this.authService.validateToken(token).subscribe({
      next: (isValid) => {
        if (isValid) {
          this.router.navigate(['/dashboard']);
        } else {
          console.log('Token validation failed');
          this.localStorageService.clear();
        }
      },
      error: (error) => {
        console.error('Error validating token:', error);
        this.localStorageService.clear();
      }
    });
  }
}