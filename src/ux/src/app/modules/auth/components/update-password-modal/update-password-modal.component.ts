import { Component, EventEmitter, Input, Output } from '@angular/core';
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
export class UpdatePasswordModalComponent {
  @Input() email: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() passwordReset = new EventEmitter<void>();

  otp: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  validationErrors: ValidationErrors = {};
  isLoading = false;

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

  onOtpInput(): void {
    this.otp = this.otp.replace(/\D/g, '');
    if (this.otp.length > 6) {
      this.otp = this.otp.slice(0, 6);
    }
    this.validationErrors = FormValidator.clearErrors(this.validationErrors, 'otp');
  }

  onOtpBlur(): void {
    this.validateField('otp');
  }

  onPasswordBlur(): void {
    this.validateField('newPassword');
    if (this.confirmPassword) {
      this.validateField('confirmPassword');
    }
  }

  onConfirmPasswordBlur(): void {
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

  onSubmit(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;

    this.authService.resetPasswordWithOTP({
      email: this.email,
      otp: this.otp,
      newPassword: this.newPassword
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'Password updated successfully.');
        this.passwordReset.emit();
        this.resetForm();
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to reset password. Please try again.';
        this.toastService.error(errorMessage);
        this.validationErrors = { ...this.validationErrors, otp: errorMessage };
      }
    });
  }

  onClose(): void {
    this.resetForm();
    this.close.emit();
  }

  private resetForm(): void {
    this.otp = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.validationErrors = {};
  }
}
