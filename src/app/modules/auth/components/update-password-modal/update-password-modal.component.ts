import { Component, EventEmitter, Input, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';
import { ComponentThemeService } from '../../../../services/component-theme.service';
import { FormValidator, ValidationErrors, CommonValidationRules } from '../../../../validators/form-validator';

@Component({
  selector: 'app-update-password-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './update-password-modal.component.html',
  styleUrls: ['./update-password-modal.component.css']
})
export class UpdatePasswordModalComponent implements OnInit, OnDestroy {
  private readonly RESEND_COOLDOWN_SECONDS = 180;

  @Input() email: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() passwordReset = new EventEmitter<void>();

  otp: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  validationErrors: ValidationErrors = {};
  isLoading = false;
  isResending = false;
  timeLeft: number = 180;
  canResend: boolean = false;
  private countdownInterval: any;

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    public themeService: ComponentThemeService
  ) {}

  get otpError(): string {
    return this.validationErrors['otp'] || '';
  }

  get passwordError(): string {
    return this.validationErrors['newPassword'] || '';
  }

  get confirmPasswordError(): string {
    return this.validationErrors['confirmPassword'] || '';
  }

  ngOnInit(): void {
    this.restartCountdown();
  }

  ngOnDestroy(): void {
    this.resetForm();
  }

  onOtpInput(): void {
    this.otp = this.otp.replace(/\D/g, '');
    if (this.otp.length > 6) {
      this.otp = this.otp.slice(0, 6);
    }
    this.validateField('otp');
  }

  onPasswordChange(value: string): void {
    this.newPassword = value;
    this.validateField('newPassword');
    if (this.confirmPassword) {
      this.validateField('confirmPassword');
    }
  }

  onConfirmPasswordChange(value: string): void {
    this.confirmPassword = value;
    this.validateField('confirmPassword');
  }

  validateField(fieldName: string): void {
    const rules = this.getValidationRules()[fieldName];
    const value = (this as any)[fieldName];
    const error = FormValidator.validateSingleField(value, rules, fieldName);

    if (error) {
      this.validationErrors = { ...this.validationErrors, [fieldName]: error };
    } else {
      this.validationErrors = FormValidator.clearErrors(this.validationErrors, fieldName);
    }
  }

  getValidationRules(): any {
    return {
      otp: CommonValidationRules.otp,
      newPassword: CommonValidationRules.password,
      confirmPassword: CommonValidationRules.confirmPassword(this.newPassword)
    };
  }

  validateForm(): boolean {
    const formData = {
      otp: this.otp,
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    };

    this.validationErrors = FormValidator.validate(formData, this.getValidationRules());
    return !FormValidator.hasErrors(this.validationErrors);
  }

  isFormValid(): boolean {
    return this.otp.length === 6 &&
      !!this.newPassword &&
      !!this.confirmPassword &&
      this.newPassword === this.confirmPassword &&
      !this.otpError &&
      !this.passwordError &&
      !this.confirmPasswordError;
  }

  get formattedTime(): string {
    const minutes = Math.floor(this.timeLeft / 60);
    const seconds = this.timeLeft % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  private restartCountdown(): void {
    this.timeLeft = this.RESEND_COOLDOWN_SECONDS;
    this.canResend = false;
    this.clearCountdown();

    this.countdownInterval = setInterval(() => {
      this.timeLeft = Math.max(0, this.timeLeft - 1);
      if (this.timeLeft === 0) {
        this.canResend = true;
        this.clearCountdown();
      }
    }, 1000);
  }

  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }

  onSubmit(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;

    this.authService.resetPasswordWithOTP({
      email: this.email,
      otp: this.otp,
      password: this.newPassword,
      retypePassword: this.confirmPassword
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'Password updated successfully.');
        this.passwordReset.emit();
        this.resetForm();
      },
      error: (error) => {
        this.isLoading = false;
    const errorMessage = error?.error?.errorMsg || 'Failed to reset password. Please try again.';
        this.toastService.error(errorMessage);
        this.validationErrors = { ...this.validationErrors, otp: errorMessage };
      }
    });
  }

  onClose(): void {
    this.resetForm();
    this.close.emit();
  }

  resendOTP(): void {
    if (!this.canResend || this.isLoading || this.isResending) {
      return;
    }

    this.isResending = true;

    this.authService.sendOTP({ email: this.email }).subscribe({
      next: (response: any) => {
        this.isResending = false;
        this.toastService.success(response.message || 'OTP has been resent to your email.');
        this.restartCountdown();
        this.otp = '';
        this.validationErrors = FormValidator.clearErrors(this.validationErrors, 'otp');
      },
      error: (error) => {
        this.isResending = false;
        const errorMessage = error?.error?.errorMsg || 'Failed to resend OTP. Please try again.';
      this.toastService.error(errorMessage);
    }
  });
}

  private resetForm(): void {
    this.otp = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.validationErrors = {};
    this.clearCountdown();
  }
}
