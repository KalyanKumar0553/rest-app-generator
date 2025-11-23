import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ContactForm {
  name: string;
  email: string;
  phone: string;
  message: string;
  agreeToTerms: boolean;
  honeypot: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent {
  
  sectionData = {
    label: 'GET IN TOUCH',
    title: "We're here to assist you!"
  };

  contactInfo = {
    email: 'admin@quadprosol.com',
    location: 'Chennai, TN IN',
    hours: [
      { day: 'Monday', time: '9:00am — 10:00pm' },
      { day: 'Tuesday', time: '9:00am — 10:00pm' },
      { day: 'Wednesday', time: '9:00am — 10:00pm' },
      { day: 'Thursday', time: '9:00am — 10:00pm' },
      { day: 'Friday', time: '9:00am — 10:00pm' },
      { day: 'Saturday', time: '9:00am — 6:00pm' },
      { day: 'Sunday', time: '9:00am — 12:00pm' }
    ]
  };

  /**
   * Contact form model
   */
  contactForm: ContactForm = {
    name: '',
    email: '',
    phone: '',
    message: '',
    agreeToTerms: false,
    honeypot: ''
  };

  isSubmitting = false;
  submitMessage = '';

  /**
   * Handle form submission
   */
  onSubmit(): void {
    // Check honeypot field - if filled, it's likely a bot
    if (this.contactForm.honeypot.trim() !== '') {
      console.log('Bot submission detected - honeypot field filled');
      return; // Silently reject the submission
    }

    if (this.isFormValid()) {
      this.isSubmitting = true;
      
      // Simulate form submission
      setTimeout(() => {
        this.isSubmitting = false;
        this.submitMessage = 'Thank you for your message! We\'ll get back to you soon.';
        this.resetForm();
        
        // Clear success message after 5 seconds
        setTimeout(() => {
          this.submitMessage = '';
        }, 5000);
      }, 2000);
    }
  }

  /**
   * Validate the contact form
   */
  private isFormValid(): boolean {
    return !!(
      this.contactForm.name.trim() &&
      this.contactForm.email.trim() &&
      this.contactForm.message.trim() &&
      this.contactForm.agreeToTerms &&
      this.isValidEmail(this.contactForm.email)
    );
  }

  /**
   * Validate email format
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Reset the contact form
   */
  private resetForm(): void {
    this.contactForm = {
      name: '',
      email: '',
      phone: '',
      message: '',
      agreeToTerms: false,
      honeypot: ''
    };
  }

  /**
   * Get form button text based on state
   */
  getButtonText(): string {
    if (this.isSubmitting) {
      return 'SENDING...';
    }
    return 'SUBMIT';
  }

  /**
   * Check if submit button should be disabled
   */
  isSubmitDisabled(): boolean {
    return this.isSubmitting || !this.isFormValid();
  }
}