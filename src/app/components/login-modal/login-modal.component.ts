import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ModalService } from '../../services/modal.service';

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
  email = '';
  password = '';
  acceptTerms = false;
  receiveUpdates = false;
  emailError = '';

  constructor(
    private router: Router,
    private modalService: ModalService
  ) {}

  toggleMode(): void {
    this.isSignupMode = !this.isSignupMode;
    this.resetForm();
  }

  resetForm(): void {
    this.email = '';
    this.password = '';
    this.acceptTerms = false;
    this.receiveUpdates = false;
    this.emailError = '';
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

    if (this.isSignupMode) {
      console.log('Signup:', { email: this.email, password: this.password, acceptTerms: this.acceptTerms, receiveUpdates: this.receiveUpdates });
    } else {
      console.log('Login:', { email: this.email, password: this.password });
    }
  }
}
