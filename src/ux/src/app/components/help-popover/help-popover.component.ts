import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-help-popover',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './help-popover.component.html',
  styleUrls: ['./help-popover.component.css']
})
export class HelpPopoverComponent {
  @Input() ariaLabel = 'Show help';
  @Input() placement: 'left' | 'top' = 'left';

  isOpen = false;
  overlayLeft = 0;
  overlayTop = 0;

  onPointerEnter(event: MouseEvent): void {
    this.isOpen = true;
    this.updateOverlayPosition(event.clientX, event.clientY);
  }

  onPointerMove(event: MouseEvent): void {
    if (!this.isOpen) {
      return;
    }
    this.updateOverlayPosition(event.clientX, event.clientY);
  }

  onPointerLeave(): void {
    this.isOpen = false;
  }

  onTriggerFocus(event: FocusEvent): void {
    this.isOpen = true;
    const target = event.target as HTMLElement | null;
    if (!target) {
      return;
    }
    const rect = target.getBoundingClientRect();
    this.updateOverlayPosition(rect.right, rect.top + rect.height / 2);
  }

  private updateOverlayPosition(clientX: number, clientY: number): void {
    const panelWidth = 320;
    const panelHeight = 180;
    const offsetX = 16;
    const offsetY = 12;
    const viewportPadding = 12;

    const maxLeft = Math.max(viewportPadding, window.innerWidth - panelWidth - viewportPadding);
    const maxTop = Math.max(viewportPadding, window.innerHeight - panelHeight - viewportPadding);

    this.overlayLeft = Math.min(maxLeft, Math.max(viewportPadding, clientX + offsetX));
    this.overlayTop = Math.min(maxTop, Math.max(viewportPadding, clientY - offsetY));
  }
}
