import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface TimeSlot {
  time: string;
  available: boolean;
}

interface SchedulingForm {
  selectedDate: Date | null;
  selectedTime: string;
  timezone: string;
  name: string;
  email: string;
  phone: string;
  services: string[];
  message: string;
  honeypot: string;
}

interface FormErrors {
  selectedDate?: string;
  selectedTime?: string;
  name?: string;
  email?: string;
  phone?: string;
  services?: string;
}

@Component({
  selector: 'app-scheduling',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './scheduling.component.html',
  styleUrls: ['./scheduling.component.css']
})
export class SchedulingComponent implements OnInit {
  
  currentDate = new Date();
  currentMonth = new Date();
  selectedDate: Date | null = null;
  
  schedulingForm: SchedulingForm = {
    selectedDate: null,
    selectedTime: '',
    timezone: 'GMT+05:30',
    name: '',
    email: '',
    phone: '',
    services: [],
    message: '',
    honeypot: ''
  };

  formErrors: FormErrors = {};
  showErrors = false;
  isSubmitting = false;
  submitMessage = '';
  firstErrorField: string | null = null;
  
  // Timezone dropdown state
  isTimezoneDropdownOpen = false;
  timezoneSearchTerm = '';
  filteredTimezones: any[] = [];

  timezones = [
    { value: 'GMT-12:00', label: '(GMT-12:00) International Date Line West' },
    { value: 'GMT-11:00', label: '(GMT-11:00) Midway Island, Samoa' },
    { value: 'GMT-10:00', label: '(GMT-10:00) Hawaii' },
    { value: 'GMT-09:00', label: '(GMT-09:00) Alaska' },
    { value: 'GMT-08:00', label: '(GMT-08:00) Pacific Time (US & Canada)' },
    { value: 'GMT-07:00', label: '(GMT-07:00) Mountain Time (US & Canada)' },
    { value: 'GMT-06:00', label: '(GMT-06:00) Central Time (US & Canada)' },
    { value: 'GMT-05:00', label: '(GMT-05:00) Eastern Time (US & Canada)' },
    { value: 'GMT-04:00', label: '(GMT-04:00) Atlantic Time (Canada)' },
    { value: 'GMT-03:00', label: '(GMT-03:00) Brazil, Buenos Aires' },
    { value: 'GMT-02:00', label: '(GMT-02:00) Mid-Atlantic' },
    { value: 'GMT-01:00', label: '(GMT-01:00) Azores, Cape Verde Islands' },
    { value: 'GMT+00:00', label: '(GMT+00:00) London, Dublin, Edinburgh' },
    { value: 'GMT+01:00', label: '(GMT+01:00) Berlin, Stockholm, Rome' },
    { value: 'GMT+02:00', label: '(GMT+02:00) Cairo, Helsinki, Kaliningrad' },
    { value: 'GMT+03:00', label: '(GMT+03:00) Baghdad, Kuwait, Moscow' },
    { value: 'GMT+04:00', label: '(GMT+04:00) Abu Dhabi, Muscat, Baku' },
    { value: 'GMT+05:00', label: '(GMT+05:00) Ekaterinburg, Islamabad' },
    { value: 'GMT+05:30', label: '(GMT+05:30) Asia - Calcutta' },
    { value: 'GMT+06:00', label: '(GMT+06:00) Almaty, Dhaka, Colombo' },
    { value: 'GMT+07:00', label: '(GMT+07:00) Bangkok, Hanoi, Jakarta' },
    { value: 'GMT+08:00', label: '(GMT+08:00) Beijing, Perth, Singapore' },
    { value: 'GMT+09:00', label: '(GMT+09:00) Tokyo, Seoul, Osaka' },
    { value: 'GMT+10:00', label: '(GMT+10:00) Eastern Australia, Guam' },
    { value: 'GMT+11:00', label: '(GMT+11:00) Magadan, Solomon Islands' },
    { value: 'GMT+12:00', label: '(GMT+12:00) Auckland, Wellington, Fiji' }
  ];

  serviceOptions = [
    'Custom software development',
    'Cloud solutions',
    'IT consulting'
  ];

  // Sample time slots - in real app, this would come from backend
  timeSlots: TimeSlot[] = [
    { time: '09:00 AM', available: true },
    { time: '10:00 AM', available: true },
    { time: '11:00 AM', available: false },
    { time: '02:00 PM', available: true },
    { time: '03:00 PM', available: true },
    { time: '04:00 PM', available: false }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Set default date to today
    const today = new Date();
    this.selectedDate = today;
    this.schedulingForm.selectedDate = today;
    
    // Set default timezone based on user's location (simplified)
    this.schedulingForm.timezone = 'GMT+05:30';
    this.filteredTimezones = [...this.timezones];
  }

  /**
   * Toggle timezone dropdown
   */
  toggleTimezoneDropdown(): void {
    this.isTimezoneDropdownOpen = !this.isTimezoneDropdownOpen;
    if (this.isTimezoneDropdownOpen) {
      setTimeout(() => {
        const searchInput = document.getElementById('timezone-search');
        if (searchInput) {
          searchInput.focus();
        }
      }, 100);
    }
  }

  /**
   * Close timezone dropdown
   */
  closeTimezoneDropdown(): void {
    this.isTimezoneDropdownOpen = false;
    this.timezoneSearchTerm = '';
  }

  /**
   * Filter timezones based on search term
   */
  onTimezoneSearch(): void {
    const searchTerm = this.timezoneSearchTerm.toLowerCase();
    this.filteredTimezones = this.timezones.filter(tz => 
      tz.label.toLowerCase().includes(searchTerm) ||
      tz.value.toLowerCase().includes(searchTerm)
    );
  }

  /**
   * Select timezone
   */
  selectTimezone(timezone: any): void {
    this.schedulingForm.timezone = timezone.value;
    this.closeTimezoneDropdown();
  }

  /**
   * Get selected timezone label
   */
  getSelectedTimezoneLabel(): string {
    const selected = this.timezones.find(tz => tz.value === this.schedulingForm.timezone);
    return selected ? selected.label : 'Select timezone';
  }

  /**
   * Handle click outside timezone dropdown
   */
  onTimezoneDropdownClick(event: Event): void {
    event.stopPropagation();
  }

  /**
   * Get calendar days for current month
   */
  getCalendarDays(): (Date | null)[] {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const days: (Date | null)[] = [];
    const current = new Date(startDate);
    
    // Add days for 6 weeks (42 days)
    for (let i = 0; i < 42; i++) {
      if (current.getMonth() === month) {
        days.push(new Date(current));
      } else {
        days.push(null);
      }
      current.setDate(current.getDate() + 1);
    }
    
    return days;
  }

  /**
   * Get month name
   */
  getMonthName(): string {
    return this.currentMonth.toLocaleDateString('en-US', { 
      month: 'long', 
      year: 'numeric' 
    });
  }

  /**
   * Navigate to previous month
   */
  previousMonth(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
  }

  /**
   * Navigate to next month
   */
  nextMonth(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 1);
  }

  /**
   * Check if date is in the past
   */
  isPastDate(date: Date): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date < today;
  }

  /**
   * Check if date is selected
   */
  isSelectedDate(date: Date): boolean {
    if (!this.selectedDate) return false;
    return date.toDateString() === this.selectedDate.toDateString();
  }

  /**
   * Select a date
   */
  selectDate(date: Date): void {
    if (this.isPastDate(date)) return;
    
    this.selectedDate = date;
    this.schedulingForm.selectedDate = date;
    this.schedulingForm.selectedTime = ''; // Reset time selection
    
    // Clear date/time error if both are now selected
    if (this.schedulingForm.selectedDate && this.schedulingForm.selectedTime) {
      this.clearFieldError('selectedDate');
    }
  }

  /**
   * Select time slot
   */
  selectTime(timeSlot: TimeSlot): void {
    if (!timeSlot.available) return;
    
    this.schedulingForm.selectedTime = timeSlot.time;
    
    // Clear date/time error if both are now selected
    if (this.schedulingForm.selectedDate && this.schedulingForm.selectedTime) {
      this.clearFieldError('selectedDate');
    }
  }

  /**
   * Handle service checkbox changes
   */
  onServiceChange(service: string, event: any): void {
    if (event.target.checked) {
      this.schedulingForm.services.push(service);
    } else {
      const index = this.schedulingForm.services.indexOf(service);
      if (index > -1) {
        this.schedulingForm.services.splice(index, 1);
      }
    }
    
    // Clear services error when user makes a selection
    this.clearFieldError('services');
  }

  /**
   * Check if service is selected
   */
  isServiceSelected(service: string): boolean {
    return this.schedulingForm.services.includes(service);
  }

  /**
   * Clear field error
   */
  clearFieldError(field: keyof FormErrors): void {
    if (this.formErrors[field]) {
      delete this.formErrors[field];
      
      if (Object.keys(this.formErrors).length === 0) {
        this.showErrors = false;
      }
    }
  }

  /**
   * Handle field changes
   */
  onFieldChange(field: keyof FormErrors): void {
    this.clearFieldError(field);
  }

  /**
   * Validate form
   */
  isFormValid(): boolean {
    this.formErrors = {};
    this.firstErrorField = null;
    let isValid = true;

    // Date and Time validation (combined)
    if (!this.schedulingForm.selectedDate || !this.schedulingForm.selectedTime) {
      this.formErrors.selectedDate = 'Please select date & time';
      if (!this.firstErrorField) this.firstErrorField = 'selectedDate';
      isValid = false;
    }

    // Name validation
    if (!this.schedulingForm.name.trim()) {
      this.formErrors.name = 'Name is required';
      if (!this.firstErrorField) this.firstErrorField = 'name';
      isValid = false;
    }

    // Email validation
    if (!this.schedulingForm.email.trim()) {
      this.formErrors.email = 'Email is required';
      if (!this.firstErrorField) this.firstErrorField = 'email';
      isValid = false;
    } else if (!this.isValidEmail(this.schedulingForm.email)) {
      this.formErrors.email = 'Please enter a valid email address';
      if (!this.firstErrorField) this.firstErrorField = 'email';
      isValid = false;
    }

    // Phone validation
    if (!this.schedulingForm.phone.trim()) {
      this.formErrors.phone = 'Phone number is required';
      if (!this.firstErrorField) this.firstErrorField = 'phone';
      isValid = false;
    }

    // Services validation
    if (this.schedulingForm.services.length === 0) {
      this.formErrors.services = 'Please select at least one service';
      if (!this.firstErrorField) this.firstErrorField = 'services';
      isValid = false;
    }

    return isValid;
  }

  /**
   * Handle field blur events
   */
  onFieldBlur(field: keyof FormErrors): void {
    const fieldValue = this.getFieldValue(field);
    const hasValue = fieldValue !== null && fieldValue !== undefined && fieldValue !== '';
    
    // Only validate on blur if field has content OR if we've already shown errors
    if (hasValue || this.showErrors) {
      this.validateSingleField(field);
    }
  }

  /**
   * Validate a single field
   */
  validateSingleField(field: keyof FormErrors): void {
    // Clear existing error for this field
    delete this.formErrors[field];

    let hasError = false;

    switch (field) {
      case 'selectedDate':
        if (!this.schedulingForm.selectedDate || !this.schedulingForm.selectedTime) {
          this.formErrors.selectedDate = 'Please select date & time';
          hasError = true;
        }
        break;

      case 'name':
        if (!this.schedulingForm.name.trim()) {
          this.formErrors.name = 'Name is required';
          hasError = true;
        }
        break;

      case 'email':
        if (!this.schedulingForm.email.trim()) {
          this.formErrors.email = 'Email is required';
          hasError = true;
        } else if (!this.isValidEmail(this.schedulingForm.email)) {
          this.formErrors.email = 'Please enter a valid email address';
          hasError = true;
        }
        break;

      case 'phone':
        if (!this.schedulingForm.phone.trim()) {
          this.formErrors.phone = 'Phone number is required';
          hasError = true;
        }
        break;

      case 'services':
        if (this.schedulingForm.services.length === 0) {
          this.formErrors.services = 'Please select at least one service';
          hasError = true;
        }
        break;
    }

    // Update first error field
    if (hasError && !this.firstErrorField) {
      this.firstErrorField = field;
    } else if (!hasError) {
      this.findNextErrorField();
    }

    // Show errors if we found any
    if (hasError) {
      this.showErrors = true;
    }
  }

  /**
   * Get field value for validation
   */
  getFieldValue(field: keyof FormErrors): any {
    switch (field) {
      case 'selectedDate':
        return this.schedulingForm.selectedDate;
      case 'name':
        return this.schedulingForm.name;
      case 'email':
        return this.schedulingForm.email;
      case 'phone':
        return this.schedulingForm.phone;
      case 'services':
        return this.schedulingForm.services.length > 0;
      default:
        return null;
    }
  }

  /**
   * Find the next error field to focus on
   */
  findNextErrorField(): void {
    const errorFields = Object.keys(this.formErrors) as (keyof FormErrors)[];
    this.firstErrorField = errorFields.length > 0 ? errorFields[0] : null;
  }

  /**
   * Check if field has error
   */
  hasError(field: keyof FormErrors): boolean {
    return this.showErrors && !!this.formErrors[field];
  }

  /**
   * Get error message
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
   * Handle form submission
   */
  onSubmit(): void {
    // Check honeypot field - if filled, it's likely a bot
    if (this.schedulingForm.honeypot.trim() !== '') {
      console.log('Bot submission detected - honeypot field filled');
      return; // Silently reject the submission
    }

    this.showErrors = true;
    
    if (this.isFormValid()) {
      this.isSubmitting = true;
      this.findNextErrorField();
      
      // Simulate form submission
      setTimeout(() => {
        this.isSubmitting = false;
        this.submitMessage = 'Meeting scheduled successfully! We\'ll send you a confirmation email.';
        
        // Redirect after success
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
   * Focus on the first error field
   */
  focusFirstErrorField(): void {
    if (!this.firstErrorField) return;

    setTimeout(() => {
      let element: HTMLElement | null = null;

      if (this.firstErrorField === 'selectedDate') {
        element = document.querySelector('.calendar-container') as HTMLElement;
      } else if (this.firstErrorField === 'services') {
        element = document.getElementById('services') as HTMLElement;
      } else {
        element = document.getElementById(this.firstErrorField) as HTMLElement;
      }

      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        if (element.focus) {
          element.focus();
        }
      }
    }, 100);
  }
  /**
   * Get submit button text
   */
  getSubmitButtonText(): string {
    return this.isSubmitting ? 'SCHEDULING...' : 'SUBMIT';
  }

  /**
   * Check if submit button should be disabled
   */
  isSubmitDisabled(): boolean {
    return this.isSubmitting;
  }

  /**
   * Navigate back to home
   */
  goBack(): void {
    this.router.navigate(['/']);
  }

  /**
   * Get formatted selected date
   */
  getFormattedSelectedDate(): string {
    if (!this.selectedDate) return '';
    
    const options: Intl.DateTimeFormatOptions = { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    };
    return this.selectedDate.toLocaleDateString('en-US', options);
  }

  /**
   * Get available time slots for selected date
   */
  getAvailableTimeSlots(): TimeSlot[] {
    // In real app, this would fetch from backend based on selected date
    return this.timeSlots;
  }

  /**
   * Check if any time slots are available for selected date
   */
  hasAvailableSlots(): boolean {
    return this.getAvailableTimeSlots().some(slot => slot.available);
  }
}