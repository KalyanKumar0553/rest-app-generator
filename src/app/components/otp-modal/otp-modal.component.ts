import { Component, EventEmitter, Input, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast.service';
import { AuthService } from '../../services/auth.service';
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

  otpDigits: string[] = ['', '', '', '', '', ''];
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

  onOtpInput(event: any, index: number): void {
    const input = event.target;
    const value = input.value;

    if (value.length > 1) {
      input.value = value.charAt(0);
      this.otpDigits[index] = value.charAt(0);
    } else {
      this.otpDigits[index] = value;
    }

    if (value && index < 5) {
      const nextInput = document.getElementById(`otp-${index + 1}`);
      if (nextInput) {
        (nextInput as HTMLInputElement).focus();
      }
    }
  }

  onOtpKeyDown(event: KeyboardEvent, index: number): void {
    if (event.key === 'Backspace' && !this.otpDigits[index] && index > 0) {
      const prevInput = document.getElementById(`otp-${index - 1}`);
      if (prevInput) {
        (prevInput as HTMLInputElement).focus();
      }
    }
  }

  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pastedData = event.clipboardData?.getData('text');

    if (pastedData && /^\d{6}$/.test(pastedData)) {
      this.otpDigits = pastedData.split('');
      const lastInput = document.getElementById('otp-5');
      if (lastInput) {
        (lastInput as HTMLInputElement).focus();
      }
    }
  }

  get otpValue(): string {
    return this.otpDigits.join('');
  }

  get isOtpComplete(): boolean {
    return this.otpDigits.every(digit => digit !== '');
  }

  verifyOTP(): void {
    if (!this.isOtpComplete) {
      this.toastService.error('Please enter the complete OTP');
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
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Invalid OTP. Please try again.';
        this.toastService.error(errorMessage);
        this.otpDigits = ['', '', '', '', '', ''];
        const firstInput = document.getElementById('otp-0');
        if (firstInput) {
          (firstInput as HTMLInputElement).focus();
        }
      }
    });
  }

  resendOTP(): void {
    if (!this.canResend) {
      return;
    }

    this.isLoading = true;

    this.authService.sendOTP({ email: this.email }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.toastService.success(response.message || 'OTP has been resent to your email');
        this.startCountdown();
        this.otpDigits = ['', '', '', '', '', ''];
        const firstInput = document.getElementById('otp-0');
        if (firstInput) {
          (firstInput as HTMLInputElement).focus();
        }
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.message || 'Failed to resend OTP. Please try again.';
        this.toastService.error(errorMessage);
      }
    });
  }

  closeModal(): void {
    this.clearCountdown();
    this.close.emit();
  }
}
