import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ProjectViewComponent } from '../../project-view/project-view.component';

@Component({
  selector: 'app-node-project-view',
  standalone: true,
  imports: [CommonModule, ProjectViewComponent],
  template: `
    <app-project-view
      [isOpen]="isOpen"
      [isSyncing]="isSyncing"
      [zipBlob]="zipBlob"
      [zipFileName]="zipFileName"
      [showCloseButton]="showCloseButton"
      (close)="close.emit()"
      (reload)="reload.emit()">
    </app-project-view>
  `
})
export class NodeProjectViewComponent {
  @Input() isOpen = false;
  @Input() isSyncing = false;
  @Input() zipBlob: Blob | null = null;
  @Input() zipFileName = 'project.zip';
  @Input() showCloseButton = false;
  @Output() close = new EventEmitter<void>();
  @Output() reload = new EventEmitter<void>();
}
