import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

export interface ControllersSpecTableRow {
  key: string;
  name: string;
  totalEndpoints: number;
  mappedEntities: string[];
}

@Component({
  selector: 'app-controllers-spec-table',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatCheckboxModule],
  templateUrl: './controllers-spec-table.component.html',
  styleUrls: ['./controllers-spec-table.component.css']
})
export class ControllersSpecTableComponent {
  @Input() rows: ControllersSpecTableRow[] = [];
  @Output() create = new EventEmitter<void>();
  @Output() edit = new EventEmitter<ControllersSpecTableRow>();
  @Output() delete = new EventEmitter<ControllersSpecTableRow>();
}
