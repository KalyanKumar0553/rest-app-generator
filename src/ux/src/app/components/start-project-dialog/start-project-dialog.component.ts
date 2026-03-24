import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import { ModalComponent } from '../modal/modal.component';

@Component({
  selector: 'app-start-project-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, MatButtonModule, ModalComponent],
  templateUrl: './start-project-dialog.component.html',
  styleUrls: ['./start-project-dialog.component.css']
})
export class StartProjectDialogComponent {
  @Input() isOpen = false;
  @Output() proceed = new EventEmitter<'java' | 'node' | 'python'>();
  @Output() cancel = new EventEmitter<void>();

  selectedLanguage: 'java' | 'node' | 'python' = 'java';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']?.currentValue) {
      this.selectedLanguage = 'java';
    }
  }

  onProceed(): void {
    this.proceed.emit(this.selectedLanguage);
  }

  onCancel(): void {
    this.selectedLanguage = 'java';
    this.cancel.emit();
  }
}
