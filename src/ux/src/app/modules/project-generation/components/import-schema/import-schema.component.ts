import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-import-schema',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './import-schema.component.html',
  styleUrls: ['./import-schema.component.css']
})
export class ImportSchemaComponent {
  @Input() sqlScript = '';
  @Output() sqlScriptChange = new EventEmitter<string>();

  onSqlChange(value: string): void {
    this.sqlScript = value;
    this.sqlScriptChange.emit(value);
  }
}
