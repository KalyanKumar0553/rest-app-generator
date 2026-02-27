import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { LoadingOverlayComponent } from '../loading-overlay/loading-overlay.component';

@Component({
  selector: 'app-loading-overlay-demo',
  standalone: true,
  imports: [CommonModule, MatButtonModule, LoadingOverlayComponent],
  templateUrl: './loading-overlay-demo.component.html',
  styleUrls: ['./loading-overlay-demo.component.css']
})
export class LoadingOverlayDemoComponent {
  showOverlay = false;

  openOverlay(): void {
    this.showOverlay = true;
  }

  closeOverlay(): void {
    this.showOverlay = false;
  }
}
