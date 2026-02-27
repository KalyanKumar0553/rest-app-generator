import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ToastService } from '../../../services/toast.service';
import { NewsletterService } from '../../../services/newsletter.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  isSubmitting = false;

  constructor(
    private toastService: ToastService,
    private newsletterService: NewsletterService
  ) {}

  submit(emailInput: HTMLInputElement): void {
    if (this.isSubmitting) {
      return;
    }

    const email = emailInput.value.trim();
    if (!this.isValidEmail(email)) {
      this.toastService.error('Please enter a valid email address.');
      return;
    }

    this.isSubmitting = true;
    this.toastService.success('Subscribed successfully for product updates.');
    // this.newsletterService.subscribe(email).subscribe({
    //   next: () => {
    //     this.toastService.success('Subscribed successfully for product updates.');
    //     emailInput.value = '';
    //     this.isSubmitting = false;
    //   },
    //   error: () => {
    //     this.toastService.error('Failed to subscribe. Please try again.');
    //     this.isSubmitting = false;
    //   }
    // });
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
