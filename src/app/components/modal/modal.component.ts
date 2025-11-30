import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent {
  @Input() isOpen = false;
  @Input() title = '';
  @Input() size: 'small' | 'medium' | 'large' | 'xlarge' = 'medium';
  @Input() showHeader = true;
  @Input() closeOnBackdropClick = true;
  @Output() close = new EventEmitter<void>();

  onBackdropClick(): void {
    if (this.closeOnBackdropClick) {
      this.close.emit();
    }
  }

  onClose(): void {
    this.close.emit();
  }
}
