import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../../../constants/api.constants';

export interface SendCheckoutOtpRequest {
  mobile: string;
  turnstileToken: string;
}

export interface VerifyCheckoutOtpRequest {
  mobile: string;
  otp: string;
}

export interface CheckoutOtpVerifyResponse {
  guestToken: string;
  message?: string;
}

export interface ResendCheckoutOtpRequest {
  mobile: string;
  turnstileToken: string;
}

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  constructor(private http: HttpClient) {}

  sendOtp(data: SendCheckoutOtpRequest): Observable<{ message: string }> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CHECKOUT.SEND_OTP}`;
    return this.http.post<any>(url, data).pipe(
      map(res => res?.data || res),
      catchError(err => throwError(() => err))
    );
  }

  resendOtp(data: ResendCheckoutOtpRequest): Observable<{ message: string }> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CHECKOUT.RESEND_OTP}`;
    return this.http.post<any>(url, data).pipe(
      map(res => res?.data || res),
      catchError(err => throwError(() => err))
    );
  }

  verifyOtp(data: VerifyCheckoutOtpRequest): Observable<CheckoutOtpVerifyResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CHECKOUT.VERIFY_OTP}`;
    return this.http.post<any>(url, data).pipe(
      map(res => res?.data || res),
      catchError(err => throwError(() => err))
    );
  }

  placeOrder(orderData: Record<string, unknown>, guestToken: string): Observable<any> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CHECKOUT.PLACE_ORDER}`;
    return this.http.post<any>(url, { ...orderData, guestToken }).pipe(
      map(res => res?.data || res),
      catchError(err => throwError(() => err))
    );
  }
}
