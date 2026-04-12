import { Component, EventEmitter, Input, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../../../services/toast.service';
import { AuthService, CaptchaChallenge } from '../../../../services/auth.service';
import { Router } from '@angular/router';
import { ComponentThemeService } from '../../../../services/component-theme.service';

@Component({
  selector: 'app-otp-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './otp-modal.component.html',
  styleUrls: ['./otp-modal.component.css']
})
export class OTPModalComponent implements OnInit, OnDestroy {
  @Input() email: string = '';
  @Input() password: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() verified = new EventEmitter<void>();

  otpValue: string = '';
  otpError: string = '';
  isLoading = false;
  timeLeft: number = 180;
  canResend: boolean = false;
  isCaptchaLoading = false;
  captchaId = '';
  captchaText = '';
  captchaImageUrl = '';
  captchaError = '';
  private countdownInterval: any;

  constructor(
    private toastService: ToastService,
    private authService: AuthService,
    private router: Router,
    public themeService: ComponentThemeService
  ) {}

  ngOnInit(): void {
    this.startCountdown();
    this.loadCaptcha();
  }

  ngOnDestroy(): void {
    this.clearCountdown();
  }

  startCountdown(): void {
    this.timeLeft = 180;
    this.canResend = false;
    this.clearCountdown();

    this.countdownInterval = setInterval(() => {
      this.timeLeft--;
      if (this.timeLeft <= 0) {
        this.canResend = true;
        this.clearCountdown();
      }
    }, 1000);
  }

  clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  get formattedTime(): string {
    const minutes = Math.floor(this.timeLeft / 60);
    const seconds = this.timeLeft % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  get isOtpComplete(): boolean {
    return this.otpValue.length === 6 && !this.otpError;
  }

  onOtpInput(): void {
    this.otpValue = this.otpValue.replace(/\D/g, '');

    if (this.otpValue.length > 6) {
      this.otpValue = this.otpValue.substring(0, 6);
    }

    this.otpError = '';
  }

  onOtpBlur(): void {
    this.validateOtp();
  }

  validateOtp(): boolean {
    this.otpError = '';

    if (!this.otpValue || this.otpValue.trim() === '') {
      this.otpError = 'OTP is required';
      return false;
    }

    if (this.otpValue.length !== 6) {
      this.otpError = 'OTP must be exactly 6 digits';
      return false;
    }

    if (!/^\d{6}$/.test(this.otpValue)) {
      this.otpError = 'OTP must contain exactly 6 digits';
      return false;
    }

    return true;
  }

  verifyOTP(): void {
    if (!this.validateOtp()) {
      return;
    }

    this.isLoading = true;

    this.authService.verifyOTP({
      identifier: this.email,
      otp: this.otpValue
    }).subscribe({
      next: (response: any) => {
        this.toastService.success(response.message || 'OTP verified successfully!');
        this.verified.emit();
        this.performAutoLogin();
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Invalid OTP. Please try again.';
        this.otpError = errorMessage;
        this.toastService.error(errorMessage);
      }
    });
  }

  performAutoLogin(): void {
    if (!this.password) {
      this.isLoading = false;
      this.closeModal();
      this.toastService.info('Please login with your credentials.');
      return;
    }

    this.authService.login({
      identifier: this.email,
      password: this.password
    }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.closeModal();
        this.toastService.success('Login successful! Welcome back.');
        setTimeout(() => {
          this.router.navigate(['/user/dashboard']);
        }, 100);
      },
      error: (error) => {
        this.isLoading = false;
        this.closeModal();
        const errorMessage = error.message || 'Auto-login failed. Please login manually.';
        this.toastService.error(errorMessage);
      }
    });
  }

  resendOTP(): void {
    if (!this.canResend || this.isLoading) {
      return;
    }

    if (!this.ensureCaptchaReady()) {
      return;
    }

    this.isLoading = true;

    this.authService.sendOTP({
      identifier: this.email,
      captchaId: this.captchaId,
      captchaText: this.captchaText.trim()
    }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.toastService.success('OTP has been resent to your email!');
        this.startCountdown();
        this.otpValue = '';
        this.otpError = '';
        this.loadCaptcha();
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to resend OTP. Please try again.';
        this.toastService.error(errorMessage);
        this.loadCaptcha();
      }
    });
  }

  onCaptchaInput(): void {
    this.captchaError = '';
  }

  refreshCaptcha(): void {
    if (!this.isCaptchaLoading) {
      this.loadCaptcha();
    }
  }

  closeModal(): void {
    this.close.emit();
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
        this.captchaId = '';
        this.captchaImageUrl = '';
        this.captchaError = 'Failed to load captcha. Please refresh and try again.';
      }
    });
  }

  private ensureCaptchaReady(): boolean {
    if (this.isCaptchaLoading || !this.captchaId) {
      this.captchaError = 'Captcha is still loading. Please wait or refresh it.';
      return false;
    }

    if (!this.captchaText.trim()) {
      this.captchaError = 'Captcha is required to resend OTP.';
      return false;
    }

    return true;
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
