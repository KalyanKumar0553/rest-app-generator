import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-info-banner',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './info-banner.component.html',
  styleUrls: ['./info-banner.component.css']
})
export class InfoBannerComponent implements OnInit {
  @Input() storageKey = '';
  @Input() show = true;

  isVisible = true;

  ngOnInit(): void {
    const dismissed = this.storageKey
      ? sessionStorage.getItem(this.buildStorageKey())
      : null;
    this.isVisible = this.show && !dismissed;
  }

  close(): void {
    this.isVisible = false;
    if (this.storageKey) {
      sessionStorage.setItem(this.buildStorageKey(), 'dismissed');
    }
  }

  private buildStorageKey(): string {
    return `info-banner:${this.storageKey}`;
  }
}
