import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, Input } from '@angular/core';

@Component({
  selector: 'app-help-popover',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './help-popover.component.html',
  styleUrls: ['./help-popover.component.css']
})
export class HelpPopoverComponent {
  @Input() ariaLabel = 'Show help';
  @Input() placement: 'left' | 'top' | 'bottom' = 'left';

  isOpen = false;

  constructor(private readonly hostRef: ElementRef<HTMLElement>) {}

  onPointerEnter(_: MouseEvent): void {
    this.isOpen = true;
  }

  onPointerLeave(): void {
    this.isOpen = false;
  }

  onTriggerClick(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isOpen = !this.isOpen;
  }

  onTriggerTouchStart(event: TouchEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isOpen = !this.isOpen;
  }

  onTriggerFocus(_: FocusEvent): void {
    this.isOpen = true;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (!target) {
      return;
    }
    if (!this.hostRef.nativeElement.contains(target)) {
      this.isOpen = false;
    }
  }

  @HostListener('document:touchstart', ['$event'])
  onDocumentTouchStart(event: TouchEvent): void {
    const target = event.target as Node | null;
    if (!target) {
      return;
    }
    if (!this.hostRef.nativeElement.contains(target)) {
      this.isOpen = false;
    }
  }
}
