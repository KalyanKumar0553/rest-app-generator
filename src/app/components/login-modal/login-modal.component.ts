import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.css']
})
export class LoginModalComponent {
  @Output() close = new EventEmitter<void>();

  isSignupMode = true;
  email = '';
  password = '';
  acceptTerms = false;
  receiveUpdates = false;

  toggleMode(): void {
    this.isSignupMode = !this.isSignupMode;
    this.resetForm();
  }

  resetForm(): void {
    this.email = '';
    this.password = '';
    this.acceptTerms = false;
    this.receiveUpdates = false;
  }

  closeModal(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (this.isSignupMode) {
      console.log('Signup:', { email: this.email, password: this.password, acceptTerms: this.acceptTerms, receiveUpdates: this.receiveUpdates });
    } else {
      console.log('Login:', { email: this.email, password: this.password });
    }
  }
}
