import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ControllersSpecTableComponent, ControllersSpecTableRow } from '../../controllers-spec-table/controllers-spec-table.component';

@Component({
  selector: 'app-node-controllers-spec-table',
  standalone: true,
  imports: [CommonModule, ControllersSpecTableComponent],
  template: `
    <app-controllers-spec-table
      [rows]="rows"
      (create)="create.emit()"
      (edit)="edit.emit($event)"
      (delete)="delete.emit($event)">
    </app-controllers-spec-table>
  `
})
export class NodeControllersSpecTableComponent {
  @Input() rows: ControllersSpecTableRow[] = [];
  @Output() create = new EventEmitter<void>();
  @Output() edit = new EventEmitter<ControllersSpecTableRow>();
  @Output() delete = new EventEmitter<ControllersSpecTableRow>();
}
