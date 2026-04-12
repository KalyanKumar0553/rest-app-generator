import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ToastService } from '../../../../services/toast.service';

@Component({
  selector: 'app-add-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './add-profile.component.html',
  styleUrls: ['./add-profile.component.css']
})
export class AddProfileComponent implements OnChanges {
  @Input() profiles: string[] = [];
  @Input() isOpen = false;
  @Output() save = new EventEmitter<string[]>();
  @Output() cancel = new EventEmitter<void>();

  profileInput = '';
  profileDrafts: string[] = [];

  constructor(private toastService: ToastService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['profiles'] || (changes['isOpen'] && this.isOpen)) {
      this.profileDrafts = [...(this.profiles ?? [])];
      this.profileInput = '';
    }
  }

  onProfileInputKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter') {
      return;
    }
    event.preventDefault();
    this.addProfileFromInput();
  }

  addProfileFromInput(): void {
    const normalized = this.normalizeProfileName(this.profileInput);
    if (!normalized) {
      this.profileInput = '';
      return;
    }

    if (!this.isValidProfileName(normalized)) {
      this.toastService.error('Profile must contain only alphabets.');
      return;
    }

    const existing = this.profileDrafts.map((profile) => profile.toLowerCase());
    if (existing.includes(normalized.toLowerCase())) {
      this.toastService.error('Profile already added.');
      return;
    }

    this.profileDrafts = [...this.profileDrafts, normalized];
    this.profileInput = '';
  }

  removeProfileDraft(profile: string): void {
    this.profileDrafts = this.profileDrafts.filter((item) => item !== profile);
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onSave(): void {
    this.save.emit([...this.profileDrafts]);
  }

  private normalizeProfileName(value: unknown): string | null {
    if (value === null || value === undefined) {
      return null;
    }
    const trimmed = String(value).trim();
    if (!trimmed) {
      return null;
    }
    return trimmed.toLowerCase();
  }

  private isValidProfileName(profile: string): boolean {
    return /^[a-z]+$/.test(profile);
  }
}
