import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ModalService } from '../../services/modal.service';
import { ToastService } from '../../services/toast.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.css']
})
export class LoginModalComponent {
  @Output() close = new EventEmitter<void>();

  isSignupMode = true;
  isForgotPasswordMode = false;
  email = '';
  password = '';
  acceptTerms = false;
  receiveUpdates = false;
  emailError = '';
  isLoading = false;

  constructor(
    private router: Router,
    private modalService: ModalService,
    private toastService: ToastService,
    private http: HttpClient
  ) {}

  toggleMode(): void {
    this.isSignupMode = !this.isSignupMode;
    this.isForgotPasswordMode = false;
    this.resetForm();
  }

  showForgotPassword(): void {
    this.isForgotPasswordMode = true;
    this.isSignupMode = false;
    this.resetForm();
  }

  backToLogin(): void {
    this.isForgotPasswordMode = false;
    this.isSignupMode = false;
    this.resetForm();
  }

  resetForm(): void {
    this.email = '';
    this.password = '';
    this.acceptTerms = false;
    this.receiveUpdates = false;
    this.emailError = '';
    this.isLoading = false;
  }

  validateEmail(): boolean {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!this.email) {
      this.emailError = 'Email is required';
      return false;
    }
    if (!emailPattern.test(this.email)) {
      this.emailError = 'Please enter a valid email address';
      return false;
    }
    this.emailError = '';
    return true;
  }

  onEmailBlur(): void {
    if (this.email) {
      this.validateEmail();
    }
  }

  isFormValid(): boolean {
    if (this.isForgotPasswordMode) {
      return this.email && this.emailError === '';
    }
    if (!this.email || !this.password) {
      return false;
    }
    if (this.isSignupMode && !this.acceptTerms) {
      return false;
    }
    return this.emailError === '';
  }

  navigateToTerms(event: Event): void {
    event.preventDefault();
    this.modalService.closeLoginModal();
    this.router.navigate(['/terms']);
  }

  navigateToPrivacy(event: Event): void {
    event.preventDefault();
    this.modalService.closeLoginModal();
    this.router.navigate(['/privacy']);
  }

  closeModal(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (!this.validateEmail()) {
      return;
    }

    if (this.isForgotPasswordMode) {
      this.handleForgotPassword();
      return;
    }

    if (this.isSignupMode) {
      console.log('Signup:', { email: this.email, password: this.password, acceptTerms: this.acceptTerms, receiveUpdates: this.receiveUpdates });
    } else {
      console.log('Login:', { email: this.email, password: this.password });
    }
  }

  handleForgotPassword(): void {
    this.isLoading = true;

    this.http.post('/api/auth/forgot-password', { email: this.email }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.toastService.success('Password reset link has been sent to your email.');
        setTimeout(() => {
          this.backToLogin();
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Failed to send password reset link. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }
}
