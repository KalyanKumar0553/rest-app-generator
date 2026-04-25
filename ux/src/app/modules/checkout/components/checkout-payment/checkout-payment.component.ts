import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutStateService } from '../../services/checkout-state.service';
import { ToastService } from '../../../../services/toast.service';
import { getApiUserMessage } from '../../../../utils/api-error.utils';

@Component({
  selector: 'app-checkout-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout-payment.component.html',
  styleUrls: ['./checkout-payment.component.css']
})
export class CheckoutPaymentComponent implements OnInit {
  isLoading = false;
  orderPlaced = false;
  orderReference = '';

  get maskedMobile(): string {
    return this.checkoutState.maskedMobile();
  }

  get orderData(): Record<string, unknown> | null {
    return this.checkoutState.snapshot.orderData;
  }

  constructor(
    private router: Router,
    private checkoutService: CheckoutService,
    public checkoutState: CheckoutStateService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {}

  placeOrder(): void {
    this.isLoading = true;
    const data = this.orderData ?? {};

    this.checkoutService.placeOrder(data, this.checkoutState.snapshot.guestToken).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.orderPlaced = true;
        this.orderReference = res?.orderId || res?.referenceId || res?.id || 'N/A';
        this.toastService.success('Order placed successfully!');
        this.checkoutState.reset();
      },
      error: (err) => {
        this.isLoading = false;
        const msg = getApiUserMessage(err, 'Failed to place order. Please try again.');
        this.toastService.error(msg);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/checkout/verify']);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
