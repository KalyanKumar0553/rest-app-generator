import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';

interface IntakeForm {
  name: string;
  email: string;
  services: string[];
  budgetRange: string;
  industry: string;
  hearAboutUs: string;
  timeline: string;
  currentWebsite: string;
  additionalComments: string;
  honeypot: string;
}

interface FormErrors {
  name?: string;
  email?: string;
  services?: string;
  budgetRange?: string;
  industry?: string;
  hearAboutUs?: string;
  timeline?: string;
  currentWebsite?: string;
}

@Component({
  selector: 'app-intake-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './intake-form.component.html',
  styleUrls: ['./intake-form.component.css']
})
export class IntakeFormComponent {
  
  intakeForm: IntakeForm = {
    name: '',
    email: '',
    services: [],
    budgetRange: '',
    industry: '',
    hearAboutUs: '',
    timeline: '',
    currentWebsite: '',
    additionalComments: '',
    honeypot: ''
  };

  serviceOptions = [
    'Custom Website Design',
    'SEO and Analytics',
    'Mobile Application Development',
    'Cloud Solutions',
    'IT Consulting'
  ];

  budgetOptions = [
    'Under $5,000',
    '$5,000 - $15,000',
    '$15,000 - $30,000',
    '$30,000 - $50,000',
    'Over $50,000'
  ];

  industryOptions = [
    'Technology',
    'Healthcare',
    'Finance',
    'E-commerce',
    'Education',
    'Manufacturing',
    'Real Estate',
    'Other'
  ];

  hearAboutOptions = [
    'Google Search',
    'Social Media',
    'Referral',
    'Advertisement',
    'Other'
  ];

  timelineOptions = [
    'ASAP',
    '1-3 months',
    '3-6 months',
    '6+ months',
    'Just exploring'
  ];

  websiteOptions = [
    'Yes',
    'No',
    'In development'
  ];

  isSubmitting = false;
  submitMessage = '';
  formErrors: FormErrors = {};
  showErrors = false;
  firstErrorField: string | null = null;

  constructor(private router: Router) {}

  /**
   * Handle service checkbox changes
   */
  onServiceChange(service: string, event: any): void {
    if (event.target.checked) {
      this.intakeForm.services.push(service);
    } else {
      const index = this.intakeForm.services.indexOf(service);
      if (index > -1) {
        this.intakeForm.services.splice(index, 1);
      }
    }
    
    // Clear errors when services change
    this.clearFieldError('services');
  }

  /**
   * Clear error for specific field and reset form state
   */
  clearFieldError(field: keyof FormErrors): void {
    if (this.formErrors[field]) {
      delete this.formErrors[field];
      
      // If this was the first error field, find the next error or clear it
      if (this.firstErrorField === field) {
        // this.findNextErrorField();
      }
      
      // If no more errors, reset form state
      if (Object.keys(this.formErrors).length === 0) {
        this.showErrors = false;
        this.firstErrorField = null;
      }
    }
  }

  /**
   * Find the next error field to highlight
   */
  private findNextErrorField(): void {
    const errorFields = ['name', 'email', 'services', 'budgetRange', 'industry', 'hearAboutUs', 'timeline', 'currentWebsite'];
    
    for (const field of errorFields) {
      if (this.formErrors[field as keyof FormErrors]) {
        this.firstErrorField = field;
        return;
      }
    }
    
    // No more errors found
    this.firstErrorField = null;
  }

  /**
   * Handle input field changes
   */
  onFieldChange(field: keyof FormErrors): void {
    this.clearFieldError(field);
  }

  /**
   * Handle field blur events for validation
   */
  onFieldBlur(field: keyof FormErrors): void {
    // Only validate if the field has a value or if we've already shown errors
    const fieldValue = this.getFieldValue(field);
    if (fieldValue || this.showErrors) {
      this.validateSingleField(field);
    }
  }

  /**
   * Get the value of a specific field
   */
  private getFieldValue(field: keyof FormErrors): any {
    switch (field) {
      case 'name': return this.intakeForm.name?.trim();
      case 'email': return this.intakeForm.email?.trim();
      case 'services': return this.intakeForm.services?.length > 0;
      case 'budgetRange': return this.intakeForm.budgetRange;
      case 'industry': return this.intakeForm.industry;
      case 'hearAboutUs': return this.intakeForm.hearAboutUs;
      case 'timeline': return this.intakeForm.timeline;
      case 'currentWebsite': return this.intakeForm.currentWebsite;
      default: return null;
    }
  }

  /**
   * Validate a single field
   */
  private validateSingleField(field: keyof FormErrors): void {
    // Clear existing error for this field
    this.clearFieldError(field);

    // Validate the specific field
    switch (field) {
      case 'name':
        if (!this.intakeForm.name.trim()) {
          this.formErrors.name = 'Name is required';
        }
        break;
      case 'email':
        if (!this.intakeForm.email.trim()) {
          this.formErrors.email = 'Email is required';
        } else if (!this.isValidEmail(this.intakeForm.email)) {
          this.formErrors.email = 'Please enter a valid email address';
        }
        break;
      case 'services':
        if (this.intakeForm.services.length === 0) {
          this.formErrors.services = 'Please select at least one service';
        }
        break;
      case 'budgetRange':
        if (!this.intakeForm.budgetRange) {
          this.formErrors.budgetRange = 'Budget range is required';
        }
        break;
      case 'industry':
        if (!this.intakeForm.industry) {
          this.formErrors.industry = 'Industry is required';
        }
        break;
      case 'hearAboutUs':
        if (!this.intakeForm.hearAboutUs) {
          this.formErrors.hearAboutUs = 'Please tell us how you heard about us';
        }
        break;
      case 'timeline':
        if (!this.intakeForm.timeline) {
          this.formErrors.timeline = 'Timeline is required';
        }
        break;
      case 'currentWebsite':
        if (!this.intakeForm.currentWebsite) {
          this.formErrors.currentWebsite = 'Please select an option';
        }
        break;
    }

    // Update first error field if needed
    if (Object.keys(this.formErrors).length > 0) {
      this.findNextErrorField();
    }
  }
  /**
   * Check if service is selected
   */
  isServiceSelected(service: string): boolean {
    return this.intakeForm.services.includes(service);
  }

  /**
   * Handle form submission
   */
  onSubmit(form: NgForm): void {
    // Check honeypot field - if filled, it's likely a bot
    if (this.intakeForm.honeypot.trim() !== '') {
      console.log('Bot submission detected - honeypot field filled');
      return; // Silently reject the submission
    }

    this.showErrors = true;
    this.validateForm();
    
    if (this.isFormValid() && form.valid) {
      this.isSubmitting = true;
      
      // Simulate form submission
      setTimeout(() => {
        this.isSubmitting = false;
        this.submitMessage = 'Thank you for your submission! We\'ll get back to you soon.';
        
        // Clear success message and redirect after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
      }, 2000);
    } else {
      // Focus on first error field
      this.focusFirstErrorField();
    }
  }

  /**
   * Validate the intake form
   */
  private isFormValid(): boolean {
    this.formErrors = {};
    this.firstErrorField = null;
    let isValid = true;

    // Name validation
    if (!this.intakeForm.name.trim()) {
      this.formErrors.name = 'Name is required';
      if (!this.firstErrorField) this.firstErrorField = 'name';
      isValid = false;
    }

    // Email validation
    if (!this.intakeForm.email.trim()) {
      this.formErrors.email = 'Email is required';
      if (!this.firstErrorField) this.firstErrorField = 'email';
      isValid = false;
    } else if (!this.isValidEmail(this.intakeForm.email)) {
      this.formErrors.email = 'Please enter a valid email address';
      if (!this.firstErrorField) this.firstErrorField = 'email';
      isValid = false;
    }

    // Services validation
    if (this.intakeForm.services.length === 0) {
      this.formErrors.services = 'Please select at least one service';
      if (!this.firstErrorField) this.firstErrorField = 'services';
      isValid = false;
    }

    // Budget range validation
    if (!this.intakeForm.budgetRange) {
      this.formErrors.budgetRange = 'Budget range is required';
      if (!this.firstErrorField) this.firstErrorField = 'budgetRange';
      isValid = false;
    }

    // Industry validation
    if (!this.intakeForm.industry) {
      this.formErrors.industry = 'Industry is required';
      if (!this.firstErrorField) this.firstErrorField = 'industry';
      isValid = false;
    }

    // How did you hear about us validation
    if (!this.intakeForm.hearAboutUs) {
      this.formErrors.hearAboutUs = 'Please tell us how you heard about us';
      if (!this.firstErrorField) this.firstErrorField = 'hearAboutUs';
      isValid = false;
    }

    // Timeline validation
    if (!this.intakeForm.timeline) {
      this.formErrors.timeline = 'Timeline is required';
      if (!this.firstErrorField) this.firstErrorField = 'timeline';
      isValid = false;
    }

    // Current website validation
    if (!this.intakeForm.currentWebsite) {
      this.formErrors.currentWebsite = 'Please select an option';
      if (!this.firstErrorField) this.firstErrorField = 'currentWebsite';
      isValid = false;
    }

    return isValid;
  }

  /**
   * Validate form and update errors
   */
  validateForm(): void {
    this.isFormValid();
  }

  /**
   * Focus on the first field with an error
   */
  private focusFirstErrorField(): void {
    const errorFields = ['name', 'email', 'services', 'budgetRange', 'industry', 'hearAboutUs', 'timeline', 'currentWebsite'];
    
    for (const field of errorFields) {
      if (this.formErrors[field as keyof FormErrors]) {
        const element = document.getElementById(field);
        if (element) {
          element.focus();
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          break;
        }
      }
    }
  }

  /**
   * Check if field has error
   */
  hasError(field: keyof FormErrors): boolean {
    return this.showErrors && !!this.formErrors[field];
  }

  /**
   * Get error message for field
   */
  getErrorMessage(field: keyof FormErrors): string {
    return this.formErrors[field] || '';
  }

  /**
   * Validate email format
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Navigate back to home
   */
  goBack(): void {
    this.router.navigate(['/']);
  }

  /**
   * Get submit button text
   */
  getSubmitButtonText(): string {
    if (this.isSubmitting) {
      return 'SUBMITTING...';
    }
    return 'SUBMIT';
  }

  /**
   * Check if submit button should be disabled
   */
  isSubmitDisabled(): boolean {
    return this.isSubmitting;
  }
}