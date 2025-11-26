import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ModalService } from '../../services/modal.service';
import { ToastService } from '../../services/toast.service';
import { AuthService, SignupRequest, LoginRequest } from '../../services/auth.service';
import { FormValidator, ValidationErrors, CommonValidationRules } from '../../validators/form-validator';
import { OTPModalComponent } from '../otp-modal/otp-modal.component';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, OTPModalComponent],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.css']
})
export class LoginModalComponent {
  @Output() close = new EventEmitter<void>();

  isSignupMode = true;
  isForgotPasswordMode = false;
  showOtpModal = false;
  email = '';
  password = '';
  acceptTerms = false;
  receiveUpdates = false;
  validationErrors: ValidationErrors = {};
  isLoading = false;

  constructor(
    private router: Router,
    private modalService: ModalService,
    private toastService: ToastService,
    private authService: AuthService
  ) {}

  get emailError(): string {
    return this.validationErrors['email'] || '';
  }

  get passwordError(): string {
    return this.validationErrors['password'] || '';
  }

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
    this.validationErrors = {};
    this.isLoading = false;
  }

  validateForm(): boolean {
    const formData = {
      email: this.email,
      password: this.password
    };

    const rules: any = {};

    if (this.isForgotPasswordMode) {
      rules.email = CommonValidationRules.email;
    } else {
      rules.email = CommonValidationRules.email;
      rules.password = CommonValidationRules.password;
    }

    this.validationErrors = FormValidator.validate(formData, rules);
    return !FormValidator.hasErrors(this.validationErrors);
  }

  onEmailBlur(): void {
    if (this.email) {
      const error = FormValidator.validateSingleField(
        this.email,
        CommonValidationRules.email,
        'email'
      );
      if (error) {
        this.validationErrors['email'] = error;
      } else {
        this.validationErrors = FormValidator.clearErrors(this.validationErrors, 'email');
      }
    }
  }

  onPasswordBlur(): void {
    if (this.password && !this.isForgotPasswordMode) {
      const error = FormValidator.validateSingleField(
        this.password,
        CommonValidationRules.password,
        'password'
      );
      if (error) {
        this.validationErrors['password'] = error;
      } else {
        this.validationErrors = FormValidator.clearErrors(this.validationErrors, 'password');
      }
    }
  }

  isFormValid(): boolean {
    if (this.isForgotPasswordMode) {
      return this.email && !this.emailError;
    }
    if (!this.email || !this.password) {
      return false;
    }
    if (this.isSignupMode && !this.acceptTerms) {
      return false;
    }
    return !this.emailError && !this.passwordError;
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
    if (!this.validateForm()) {
      return;
    }

    if (this.isForgotPasswordMode) {
      this.handleForgotPassword();
      return;
    }

    if (this.isSignupMode) {
      this.handleSignup();
    } else {
      this.handleLogin();
    }
  }

  handleSignup(): void {
    this.isLoading = true;

    const signupData: SignupRequest = {
      email: this.email,
      password: this.password,
      acceptTerms: this.acceptTerms,
      receiveUpdates: this.receiveUpdates
    };

    this.authService.signup(signupData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success('Account created successfully! Please verify your email with the OTP sent to your inbox.');
        this.showOtpModal = true;
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to create account. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }

  onOtpVerified(): void {
    this.showOtpModal = false;
    this.closeModal();
  }

  onOtpModalClose(): void {
    this.showOtpModal = false;
  }

  handleLogin(): void {
    this.isLoading = true;

    const loginData: LoginRequest = {
      email: this.email,
      password: this.password
    };

    this.authService.login(loginData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'Login successful!');
        this.closeModal();
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Invalid email or password.';
        this.toastService.error(errorMessage);
      }
    });
  }

  handleForgotPassword(): void {
    this.isLoading = true;

    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'Password reset link has been sent to your email.');
        setTimeout(() => {
          this.backToLogin();
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to send password reset link. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }
}
