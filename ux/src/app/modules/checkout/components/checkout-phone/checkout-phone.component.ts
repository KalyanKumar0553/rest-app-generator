import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TurnstileComponent } from '../../../../components/turnstile/turnstile.component';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutStateService } from '../../services/checkout-state.service';
import { ToastService } from '../../../../services/toast.service';
import { getApiUserMessage } from '../../../../utils/api-error.utils';

interface CountryCode {
  name: string;
  code: string;
  flag: string;
}

@Component({
  selector: 'app-checkout-phone',
  standalone: true,
  imports: [CommonModule, FormsModule, TurnstileComponent],
  templateUrl: './checkout-phone.component.html',
  styleUrls: ['./checkout-phone.component.css']
})
export class CheckoutPhoneComponent implements OnInit {
  @ViewChild(TurnstileComponent) turnstileWidget?: TurnstileComponent;

  readonly countryCodes: CountryCode[] = [
    { name: 'India', code: '+91', flag: '🇮🇳' },
    { name: 'United States', code: '+1', flag: '🇺🇸' },
    { name: 'United Kingdom', code: '+44', flag: '🇬🇧' },
    { name: 'Australia', code: '+61', flag: '🇦🇺' },
    { name: 'Canada', code: '+1', flag: '🇨🇦' },
    { name: 'Germany', code: '+49', flag: '🇩🇪' },
    { name: 'France', code: '+33', flag: '🇫🇷' },
    { name: 'Singapore', code: '+65', flag: '🇸🇬' },
    { name: 'UAE', code: '+971', flag: '🇦🇪' },
  ];

  selectedCountry = this.countryCodes[0];
  localNumber = '';
  turnstileToken = '';
  isLoading = false;
  showMobileError = false;
  mobileError = '';

  constructor(
    private router: Router,
    private checkoutService: CheckoutService,
    private checkoutState: CheckoutStateService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Reset any stale guest token when user lands here fresh
    if (!this.checkoutState.snapshot.guestToken) {
      // no-op; state already clean
    }
  }

  onCountryChange(code: CountryCode): void {
    this.selectedCountry = code;
  }

  onMobileInput(): void {
    this.localNumber = this.localNumber.replace(/\D/g, '');
    this.mobileError = '';
    this.showMobileError = false;
  }

  onTurnstileToken(token: string): void {
    this.turnstileToken = token;
  }

  onTurnstileExpired(): void {
    this.turnstileToken = '';
  }

  get isSendDisabled(): boolean {
    return !this.turnstileToken || !this.isValidMobile() || this.isLoading;
  }

  get formattedButtonLabel(): string {
    return this.isLoading ? 'Sending OTP…' : 'Send OTP';
  }

  isValidMobile(): boolean {
    return /^\d{7,15}$/.test(this.localNumber.trim());
  }

  compareCountry(a: any, b: any): boolean {
    return a?.code === b?.code && a?.name === b?.name;
  }

  sendOtp(): void {
    this.showMobileError = true;

    if (!this.isValidMobile()) {
      this.mobileError = 'Please enter a valid mobile number (7–15 digits).';
      return;
    }

    if (!this.turnstileToken) {
      this.toastService.error('Please complete the security check.');
      return;
    }

    this.isLoading = true;
    this.checkoutState.setMobile(this.selectedCountry.code, this.localNumber.trim());

    this.checkoutService.sendOtp({
      mobile: this.checkoutState.snapshot.mobile,
      turnstileToken: this.turnstileToken
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/checkout/verify']);
      },
      error: (err) => {
        this.isLoading = false;
        this.turnstileToken = '';
        this.turnstileWidget?.reset();
        const msg = getApiUserMessage(err, 'Failed to send OTP. Please try again.');
        this.toastService.error(msg);
      }
    });
  }
}
