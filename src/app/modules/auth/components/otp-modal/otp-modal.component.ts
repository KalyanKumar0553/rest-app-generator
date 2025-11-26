import { Component, EventEmitter, Input, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../../../services/toast.service';
import { AuthService } from '../../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-otp-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './otp-modal.component.html',
  styleUrls: ['./otp-modal.component.css']
})
export class OTPModalComponent implements OnInit, OnDestroy {
  @Input() email: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() verified = new EventEmitter<void>();

  otpValue: string = '';
  otpError: string = '';
  isLoading = false;
  timeLeft: number = 180;
  canResend: boolean = false;
  private countdownInterval: any;

  constructor(
    private toastService: ToastService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.startCountdown();
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
    this.otpValue = this.otpValue.replace(/[^a-zA-Z0-9]/g, '');

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
      this.otpError = 'OTP must be exactly 6 characters';
      return false;
    }

    if (!/^[a-zA-Z0-9]{6}$/.test(this.otpValue)) {
      this.otpError = 'OTP must contain only alphanumeric characters';
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
      email: this.email,
      otp: this.otpValue
    }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'OTP verified successfully!');
        this.verified.emit();
        this.closeModal();
        this.router.navigate(['/user/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Invalid OTP. Please try again.';
        this.otpError = errorMessage;
        this.toastService.error(errorMessage);
      }
    });
  }

  resendOTP(): void {
    if (!this.canResend || this.isLoading) {
      return;
    }

    this.isLoading = true;

    this.authService.sendOTP({ email: this.email }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.toastService.success('OTP has been resent to your email!');
        this.startCountdown();
        this.otpValue = '';
        this.otpError = '';
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to resend OTP. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }

  closeModal(): void {
    this.close.emit();
  }
}
