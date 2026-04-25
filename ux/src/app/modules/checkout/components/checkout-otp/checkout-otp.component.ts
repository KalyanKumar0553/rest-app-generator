import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TurnstileComponent } from '../../../../components/turnstile/turnstile.component';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutStateService } from '../../services/checkout-state.service';
import { ToastService } from '../../../../services/toast.service';
import { getApiUserMessage } from '../../../../utils/api-error.utils';

@Component({
  selector: 'app-checkout-otp',
  standalone: true,
  imports: [CommonModule, FormsModule, TurnstileComponent],
  templateUrl: './checkout-otp.component.html',
  styleUrls: ['./checkout-otp.component.css']
})
export class CheckoutOtpComponent implements OnInit, OnDestroy {
  @ViewChild(TurnstileComponent) turnstileWidget?: TurnstileComponent;

  otpValue = '';
  otpError = '';
  isLoading = false;

  timeLeft = 180;
  canResend = false;
  private countdownInterval: any = null;

  resendTurnstileToken = '';
  showResendTurnstile = false;

  get maskedMobile(): string {
    return this.checkoutState.maskedMobile();
  }

  get formattedTime(): string {
    const m = Math.floor(this.timeLeft / 60);
    const s = this.timeLeft % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  get isOtpComplete(): boolean {
    return /^\d{6}$/.test(this.otpValue);
  }

  get isVerifyDisabled(): boolean {
    return !this.isOtpComplete || this.isLoading;
  }

  constructor(
    private router: Router,
    private checkoutService: CheckoutService,
    public checkoutState: CheckoutStateService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.startCountdown();
  }

  ngOnDestroy(): void {
    this.clearCountdown();
  }

  onOtpInput(): void {
    this.otpValue = this.otpValue.replace(/\D/g, '').slice(0, 6);
    this.otpError = '';
  }

  onResendTurnstileToken(token: string): void {
    this.resendTurnstileToken = token;
  }

  onResendTurnstileExpired(): void {
    this.resendTurnstileToken = '';
  }

  requestResend(): void {
    if (!this.canResend || this.isLoading) return;
    // Show Turnstile widget for resend if not yet shown
    if (!this.showResendTurnstile) {
      this.showResendTurnstile = true;
      return;
    }
    if (!this.resendTurnstileToken) {
      this.toastService.error('Please complete the security check to resend OTP.');
      return;
    }
    this.doResend();
  }

  confirmResend(): void {
    if (!this.resendTurnstileToken) {
      this.toastService.error('Please complete the security check.');
      return;
    }
    this.doResend();
  }

  verifyOtp(): void {
    if (!this.isOtpComplete) {
      this.otpError = 'Please enter the complete 6-digit OTP.';
      return;
    }

    this.isLoading = true;

    this.checkoutService.verifyOtp({
      mobile: this.checkoutState.snapshot.mobile,
      otp: this.otpValue
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.checkoutState.setGuestToken(res.guestToken);
        this.toastService.success(res.message || 'OTP verified successfully!');
        this.router.navigate(['/checkout/payment']);
      },
      error: (err) => {
        this.isLoading = false;
        this.otpError = getApiUserMessage(err, 'Invalid OTP. Please check and try again.');
        this.toastService.error(this.otpError);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/checkout']);
  }

  private doResend(): void {
    this.isLoading = true;

    this.checkoutService.resendOtp({
      mobile: this.checkoutState.snapshot.mobile,
      turnstileToken: this.resendTurnstileToken
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success('OTP resent to your mobile number.');
        this.otpValue = '';
        this.otpError = '';
        this.resendTurnstileToken = '';
        this.showResendTurnstile = false;
        this.turnstileWidget?.reset();
        this.startCountdown();
      },
      error: (err) => {
        this.isLoading = false;
        this.resendTurnstileToken = '';
        this.turnstileWidget?.reset();
        const msg = getApiUserMessage(err, 'Failed to resend OTP. Please try again.');
        this.toastService.error(msg);
      }
    });
  }

  private startCountdown(): void {
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

  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }
}
