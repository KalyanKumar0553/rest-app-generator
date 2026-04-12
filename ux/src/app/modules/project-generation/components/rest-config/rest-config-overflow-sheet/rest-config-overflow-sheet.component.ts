import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { MatIconModule } from '@angular/material/icon';

type RestTabId = 'basic' | 'endpoints' | 'request' | 'error' | 'docs';

interface OverflowSheetData {
  tabs: Array<{ id: RestTabId; label: string; icon: string }>;
  activeTab: RestTabId;
}

@Component({
  selector: 'app-rest-config-overflow-sheet',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './rest-config-overflow-sheet.component.html',
  styleUrls: ['./rest-config-overflow-sheet.component.css']
})
export class RestConfigOverflowSheetComponent {
  constructor(
    @Inject(MAT_BOTTOM_SHEET_DATA) public data: OverflowSheetData,
    private readonly bottomSheetRef: MatBottomSheetRef<RestConfigOverflowSheetComponent, RestTabId>
  ) {}

  selectTab(tabId: RestTabId): void {
    this.bottomSheetRef.dismiss(tabId);
  }
}
