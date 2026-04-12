import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ToastService } from '../../../../services/toast.service';
import { AuthService } from '../../../../services/auth.service';
import { ChangePasswordPayload, UserProfile, UserRoles, UserService } from '../../../../services/user.service';
import { matchingFieldsValidator, passwordStrengthValidator } from '../../../../validators/reactive-form.validators';
import { COUNTRY_TIMEZONE_OPTIONS, CountryTimezoneOption } from './profile-timezone.data';

@Component({
  selector: 'app-profile-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatTooltipModule],
  templateUrl: './profile-panel.component.html',
  styleUrls: ['./profile-panel.component.css']
})
export class ProfilePanelComponent implements OnInit {
  readonly profileHelpText = 'Your account details, security settings, and timezone preferences.';
  readonly accessProfileHelpText = 'Review the authenticated identity and granted access rights for this account.';
  readonly changePasswordHelpText = 'Update your credentials with your current password and a stronger new password.';
  readonly changeTimezoneHelpText = 'Pick your country first, then select the city or timezone that should drive your account preferences.';
  userEmail = '';
  userName = '';
  userRoles: string[] = [];
  userPermissions: string[] = [];
  availableCountries: CountryTimezoneOption[] = COUNTRY_TIMEZONE_OPTIONS;
  availableTimeZones: string[] = [];
  isSavingPassword = false;
  isSavingTimeZone = false;

  readonly passwordForm = this.formBuilder.group(
    {
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8), passwordStrengthValidator()]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: matchingFieldsValidator('newPassword', 'confirmPassword', 'passwordMismatch') }
  );

  readonly timezoneForm = this.formBuilder.group({
    countryCode: ['', [Validators.required]],
    timeZoneId: ['', [Validators.required]]
  });

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const userData = this.authService.getUserData();
    if (userData) {
      this.userEmail = userData.email;
      this.userName = userData.name || 'User';
    }

    this.userService.getUserRoles().subscribe({
      next: (rolesData: UserRoles) => {
        this.userRoles = rolesData.roles || [];
        this.userPermissions = rolesData.permissions || [];
      },
      error: () => {
        this.toastService.error('Failed to load user roles');
      }
    });

    this.userService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        this.userEmail = profile.email || this.userEmail;
        this.userName = profile.name || this.userName;
        this.applyTimeZoneSelection(profile.timeZoneId || Intl.DateTimeFormat().resolvedOptions().timeZone);
      },
      error: () => {
        this.applyTimeZoneSelection(Intl.DateTimeFormat().resolvedOptions().timeZone);
        this.toastService.error('Failed to load user profile');
      }
    });

    this.timezoneForm.controls.countryCode.valueChanges.subscribe((countryCode) => {
      this.onCountryChange(countryCode || '');
    });
  }

  get currentPasswordControl(): AbstractControl | null {
    return this.passwordForm.get('currentPassword');
  }

  get newPasswordControl(): AbstractControl | null {
    return this.passwordForm.get('newPassword');
  }

  get confirmPasswordControl(): AbstractControl | null {
    return this.passwordForm.get('confirmPassword');
  }

  updatePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const payload: ChangePasswordPayload = {
      currentPassword: this.passwordForm.value.currentPassword || '',
      newPassword: this.passwordForm.value.newPassword || ''
    };
    this.isSavingPassword = true;
    this.userService.changePassword(payload).subscribe({
      next: () => {
        this.isSavingPassword = false;
        this.passwordForm.reset();
        this.toastService.success('Password updated successfully.');
      },
      error: (error) => {
        this.isSavingPassword = false;
        this.toastService.error(error?.message || 'Failed to update password.');
      }
    });
  }

  updateTimeZone(): void {
    if (this.timezoneForm.invalid) {
      this.timezoneForm.markAllAsTouched();
      return;
    }
    this.isSavingTimeZone = true;
    this.userService.updateUserProfile({
      timeZoneId: this.timezoneForm.value.timeZoneId || null
    }).subscribe({
      next: () => {
        this.isSavingTimeZone = false;
        this.toastService.success('Timezone updated successfully.');
      },
      error: (error) => {
        this.isSavingTimeZone = false;
        this.toastService.error(error?.message || 'Failed to update timezone.');
      }
    });
  }

  getTimeZoneLabel(timeZoneId: string): string {
    const city = timeZoneId.split('/').pop()?.replace(/_/g, ' ') || timeZoneId;
    return `${city} (${this.getUtcOffset(timeZoneId)})`;
  }

  getPasswordError(controlName: 'currentPassword' | 'newPassword' | 'confirmPassword'): string {
    const control = this.passwordForm.get(controlName);
    if (!control || !control.touched || !control.errors) {
      return '';
    }
    if (control.errors['required']) {
      return 'This field is required.';
    }
    if (control.errors['minlength'] || control.errors['passwordStrength']) {
      return 'Use at least 8 characters for the new password.';
    }
    if (control.errors['passwordMismatch']) {
      return 'Passwords do not match.';
    }
    return '';
  }

  getTimeZoneError(controlName: 'countryCode' | 'timeZoneId'): string {
    const control = this.timezoneForm.get(controlName);
    if (!control || !control.touched || !control.errors) {
      return '';
    }
    if (control.errors['required']) {
      return controlName === 'countryCode' ? 'Select a country.' : 'Select a timezone city.';
    }
    return '';
  }

  private applyTimeZoneSelection(timeZoneId: string): void {
    const matchedCountry = this.availableCountries.find((country) => country.timeZones.includes(timeZoneId)) || this.availableCountries[0];
    this.availableTimeZones = matchedCountry.timeZones;
    this.timezoneForm.patchValue(
      {
        countryCode: matchedCountry.code,
        timeZoneId: matchedCountry.timeZones.includes(timeZoneId) ? timeZoneId : matchedCountry.timeZones[0]
      },
      { emitEvent: false }
    );
  }

  private onCountryChange(countryCode: string): void {
    const selectedCountry = this.availableCountries.find((country) => country.code === countryCode);
    this.availableTimeZones = selectedCountry?.timeZones || [];
    const currentTimeZone = this.timezoneForm.value.timeZoneId;
    const nextTimeZone = currentTimeZone && this.availableTimeZones.includes(currentTimeZone)
      ? currentTimeZone
      : this.availableTimeZones[0] || '';
    this.timezoneForm.patchValue({ timeZoneId: nextTimeZone }, { emitEvent: false });
  }

  private getUtcOffset(timeZoneId: string): string {
    const formatter = new Intl.DateTimeFormat('en-US', {
      timeZone: timeZoneId,
      timeZoneName: 'shortOffset',
      hour: '2-digit'
    });
    const offsetPart = formatter.formatToParts(new Date()).find((part) => part.type === 'timeZoneName')?.value || 'GMT';
    if (offsetPart === 'GMT') {
      return 'GMT+00:00';
    }
    const normalized = offsetPart.replace('GMT', '');
    const sign = normalized.startsWith('-') ? '-' : '+';
    const numeric = normalized.replace('+', '').replace('-', '');
    const [hours, minutes = '00'] = numeric.includes(':') ? numeric.split(':') : [numeric, '00'];
    return `GMT${sign}${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
  }
}
