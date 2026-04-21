import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CheckoutStateService } from '../services/checkout-state.service';

export const checkoutVerifyGuard: CanActivateFn = () => {
  const state = inject(CheckoutStateService);
  const router = inject(Router);
  if (state.hasMobile()) {
    return true;
  }
  router.navigate(['/checkout']);
  return false;
};

export const checkoutPaymentGuard: CanActivateFn = () => {
  const state = inject(CheckoutStateService);
  const router = inject(Router);
  if (state.hasMobile() && state.hasGuestToken()) {
    return true;
  }
  if (state.hasMobile()) {
    router.navigate(['/checkout/verify']);
  } else {
    router.navigate(['/checkout']);
  }
  return false;
};
