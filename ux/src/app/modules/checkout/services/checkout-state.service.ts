import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CheckoutState {
  mobile: string;         // full number e.g. "+919876543210"
  countryCode: string;    // e.g. "+91"
  guestToken: string;     // JWT returned after OTP verify
  orderData: Record<string, unknown> | null;
}

const INITIAL_STATE: CheckoutState = {
  mobile: '',
  countryCode: '+91',
  guestToken: '',
  orderData: null
};

@Injectable({ providedIn: 'root' })
export class CheckoutStateService {
  private state = new BehaviorSubject<CheckoutState>({ ...INITIAL_STATE });
  readonly state$ = this.state.asObservable();

  get snapshot(): CheckoutState {
    return this.state.value;
  }

  setMobile(countryCode: string, localNumber: string): void {
    const mobile = `${countryCode}${localNumber.replace(/^0+/, '')}`;
    this.state.next({ ...this.state.value, countryCode, mobile });
  }

  setGuestToken(token: string): void {
    this.state.next({ ...this.state.value, guestToken: token });
  }

  setOrderData(data: Record<string, unknown>): void {
    this.state.next({ ...this.state.value, orderData: data });
  }

  reset(): void {
    this.state.next({ ...INITIAL_STATE });
  }

  hasMobile(): boolean {
    return Boolean(this.state.value.mobile);
  }

  hasGuestToken(): boolean {
    return Boolean(this.state.value.guestToken);
  }

  maskedMobile(): string {
    const { countryCode, mobile } = this.state.value;
    const local = mobile.replace(countryCode, '');
    if (local.length <= 4) return mobile;
    const visible = local.slice(-4);
    const masked = '*'.repeat(local.length - 4);
    return `${countryCode} ${masked}${visible}`;
  }
}
