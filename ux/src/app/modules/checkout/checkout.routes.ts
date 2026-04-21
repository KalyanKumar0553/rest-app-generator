import { Routes } from '@angular/router';
import { checkoutVerifyGuard, checkoutPaymentGuard } from './guards/checkout-step.guard';

export const CHECKOUT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/checkout-phone/checkout-phone.component').then(
        m => m.CheckoutPhoneComponent
      )
  },
  {
    path: 'verify',
    canActivate: [checkoutVerifyGuard],
    loadComponent: () =>
      import('./components/checkout-otp/checkout-otp.component').then(
        m => m.CheckoutOtpComponent
      )
  },
  {
    path: 'payment',
    canActivate: [checkoutPaymentGuard],
    loadComponent: () =>
      import('./components/checkout-payment/checkout-payment.component').then(
        m => m.CheckoutPaymentComponent
      )
  }
];
