import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ModalService } from '../../../../services/modal.service';
import { ToastService } from '../../../../services/toast.service';
import { AuthService, SignupRequest, LoginRequest, CaptchaChallenge, AuthProvidersResponse } from '../../../../services/auth.service';
import { ComponentThemeService } from '../../../../services/component-theme.service';
import { FormValidator, ValidationErrors, CommonValidationRules } from '../../../../validators/form-validator';
import { OTPModalComponent } from '../otp-modal/otp-modal.component';
import { UpdatePasswordModalComponent } from '../update-password-modal/update-password-modal.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { OauthProgressService } from '../../../../services/oauth-progress.service';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, OTPModalComponent, UpdatePasswordModalComponent, ModalComponent],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.css']
})
export class LoginModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();

  isSignupMode = false;
  isForgotPasswordMode = false;
  showOtpModal = false;
  showUpdatePasswordModal = false;
  email = '';
  password = '';
  acceptTerms = false;
  receiveUpdates = false;
  validationErrors: ValidationErrors = {};
  isLoading = false;
  isCaptchaLoading = false;
  captchaId = '';
  captchaText = '';
  captchaImageUrl = '';
  captchaError = '';
  authProviders: AuthProvidersResponse = {
    googleEnabled: true,
    keycloakEnabled: false
  };

  constructor(
    private router: Router,
    private modalService: ModalService,
    private toastService: ToastService,
    private authService: AuthService,
    private oauthProgressService: OauthProgressService,
    public themeService: ComponentThemeService
  ) {}

  ngOnInit(): void {
    this.authService.getAuthProviders().subscribe({
      next: (providers) => {
        this.authProviders = {
          googleEnabled: Boolean(providers?.googleEnabled),
          keycloakEnabled: Boolean(providers?.keycloakEnabled)
        };
      },
      error: () => {
        this.authProviders = {
          googleEnabled: true,
          keycloakEnabled: false
        };
      }
    });
  }

  get modalTitle(): string {
    if (this.isForgotPasswordMode) {
      return 'Reset Password';
    }
    return this.isSignupMode ? 'Create new account' : 'Log in';
  }

  get emailError(): string {
    return this.validationErrors['email'] || '';
  }

  get passwordError(): string {
    return this.validationErrors['password'] || '';
  }

  get requiresCaptcha(): boolean {
    return this.isSignupMode || this.isForgotPasswordMode;
  }

  toggleMode(): void {
    this.isSignupMode = !this.isSignupMode;
    this.isForgotPasswordMode = false;
    this.resetForm();
    if (this.requiresCaptcha) {
      this.loadCaptcha();
    }
  }

  showForgotPassword(): void {
    this.isForgotPasswordMode = true;
    this.isSignupMode = false;
    this.resetForm();
    this.loadCaptcha();
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
    this.clearCaptchaState();
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

  onCaptchaInput(): void {
    this.captchaError = '';
  }

  isFormValid(): boolean {
    if (this.isForgotPasswordMode) {
      return !!this.email && !this.emailError && this.isCaptchaValid();
    }
    if (!this.email || !this.password) {
      return false;
    }
    if (this.isSignupMode && !this.acceptTerms) {
      return false;
    }
    return !this.emailError && !this.passwordError && (!this.requiresCaptcha || this.isCaptchaValid());
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
    if (!this.ensureCaptchaReady()) {
      return;
    }

    this.isLoading = true;

    const signupData: SignupRequest = {
      identifier: this.email,
      password: this.password,
      captchaId: this.captchaId,
      captchaText: this.captchaText.trim(),
      acceptTerms: this.acceptTerms,
      receiveUpdates: this.receiveUpdates
    };

    this.authService.signup(signupData).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success('Account created successfully! Please verify your email with the OTP sent to your inbox.');
        this.showOtpModal = true;
      },
      error: (error) => {
        this.isLoading = false;
        this.refreshCaptchaAfterFailure();
        const errorMessage = error?.error?.errorMsg ?? 'Failed to create account. Please try again.';

        if (errorMessage.toLowerCase().includes('already exists') || errorMessage.toLowerCase().includes('already registered')) {
          this.toastService.error(errorMessage + ' Please login instead.');
          setTimeout(() => {
            this.isSignupMode = false;
            this.isForgotPasswordMode = false;
            this.resetForm();
          }, 2000);
        } else {
          this.toastService.error(errorMessage);
        }
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

  onUpdatePasswordClose(): void {
    this.showUpdatePasswordModal = false;
    this.isSignupMode = false;
    this.isForgotPasswordMode = false;
  }

  onPasswordResetSuccess(): void {
    this.showUpdatePasswordModal = false;
    this.isSignupMode = false;
    this.isForgotPasswordMode = false;
    const cachedEmail = this.email;
    this.resetForm();
    this.email = cachedEmail;
    this.modalService.openLoginModal();
  }

  handleLogin(): void {
    this.isLoading = true;

    const loginData: LoginRequest = {
      identifier: this.email,
      password: this.password
    };

    this.authService.login(loginData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'Login successful!');
        this.closeModal();
        setTimeout(() => {
          this.router.navigate(['/user/dashboard']);
        }, 100);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Invalid email or password.';
        this.toastService.error(errorMessage);
      }
    });
  }

  handleForgotPassword(): void {
    if (!this.ensureCaptchaReady()) {
      return;
    }

    this.isLoading = true;

    this.authService.forgotPassword({
      identifier: this.email,
      captchaId: this.captchaId,
      captchaText: this.captchaText.trim()
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'OTP has been sent to your email.');
        this.showUpdatePasswordModal = true;
        this.isForgotPasswordMode = false;
        this.isSignupMode = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.refreshCaptchaAfterFailure();
        const errorMessage = error.message || 'Failed to send OTP. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }

  startGoogleLogin(): void {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.oauthProgressService.show('Redirecting to Google', '');
    this.close.emit();

    setTimeout(() => {
      this.authService.startGoogleLogin();
    }, 50);
  }

  startKeycloakLogin(): void {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.oauthProgressService.show('Redirecting to Keycloak', '');
    this.close.emit();

    setTimeout(() => {
      this.authService.startKeycloakLogin();
    }, 50);
  }

  refreshCaptcha(): void {
    if (!this.isCaptchaLoading) {
      this.loadCaptcha();
    }
  }

  private loadCaptcha(): void {
    this.isCaptchaLoading = true;
    this.captchaError = '';
    this.captchaText = '';

    this.authService.getCaptcha().subscribe({
      next: (captcha: CaptchaChallenge) => {
        this.isCaptchaLoading = false;
        this.captchaId = captcha.captchaId;
        this.captchaImageUrl = this.toCaptchaImageUrl(captcha.imageBase64);
      },
      error: () => {
        this.isCaptchaLoading = false;
        this.clearCaptchaState();
        this.captchaError = 'Failed to load captcha. Please refresh and try again.';
      }
    });
  }

  private isCaptchaValid(): boolean {
    return !this.isCaptchaLoading && !!this.captchaId && !!this.captchaText.trim();
  }

  private ensureCaptchaReady(): boolean {
    if (!this.requiresCaptcha) {
      return true;
    }

    if (this.isCaptchaLoading || !this.captchaId) {
      this.captchaError = 'Captcha is still loading. Please wait or refresh it.';
      return false;
    }

    if (!this.captchaText.trim()) {
      this.captchaError = 'Captcha is required.';
      return false;
    }

    return true;
  }

  private refreshCaptchaAfterFailure(): void {
    if (this.requiresCaptcha) {
      this.loadCaptcha();
    }
  }

  private clearCaptchaState(): void {
    this.captchaId = '';
    this.captchaText = '';
    this.captchaImageUrl = '';
    this.captchaError = '';
    this.isCaptchaLoading = false;
  }

  private toCaptchaImageUrl(imageBase64: string): string {
    if (imageBase64.startsWith('data:')) {
      return imageBase64;
    }

    return this.isSvgCaptcha(imageBase64)
      ? `data:image/svg+xml;base64,${imageBase64}`
      : `data:image/png;base64,${imageBase64}`;
  }

  private isSvgCaptcha(imageBase64: string): boolean {
    try {
      const decodedImage = atob(imageBase64).trim().toLowerCase();
      return decodedImage.startsWith('<?xml') || decodedImage.startsWith('<svg');
    } catch {
      return false;
    }
  }
}
