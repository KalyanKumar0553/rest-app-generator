import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  constructor(private toastService: ToastService) {}

  submit(emailInput: HTMLInputElement): void {
    const email = emailInput.value.trim();
    if (!this.isValidEmail(email)) {
      this.toastService.error('Please enter a valid email address.');
      return;
    }

    this.toastService.success('Successfully subscrbied for news letter.');
    emailInput.value = '';
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
