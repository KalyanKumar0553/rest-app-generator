import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ModalButton {
  text: string;
  type: 'ok' | 'cancel' | 'confirm' | 'danger';
  action: 'confirm' | 'cancel';
}

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.css']
})
export class ConfirmationModalComponent {
  @Input() title: string = 'Confirm Action';
  @Input() message: string | string[] = 'Are you sure you want to proceed?';
  @Input() buttons: ModalButton[] = [
    { text: 'Cancel', type: 'cancel', action: 'cancel' },
    { text: 'Confirm', type: 'confirm', action: 'confirm' }
  ];
  @Input() isLoading: boolean = false;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  get messageLines(): string[] {
    return Array.isArray(this.message) ? this.message : [this.message];
  }

  getButtonClass(button: ModalButton): string {
    const baseClass = button.action === 'cancel' ? 'btn-cancel' : 'btn-action';
    const typeClass = button.type === 'danger' ? 'btn-confirm' :
                      button.type === 'confirm' ? 'btn-confirm' :
                      button.type === 'ok' ? 'btn-primary' : '';
    return `${baseClass} ${typeClass}`.trim();
  }

  onButtonClick(button: ModalButton): void {
    if (this.isLoading) return;

    if (button.action === 'cancel') {
      this.cancel.emit();
    } else if (button.action === 'confirm') {
      this.confirm.emit();
    }
  }

  onOverlayClick(): void {
    if (!this.isLoading) {
      this.cancel.emit();
    }
  }
}
