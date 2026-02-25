import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-field-type-select',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './field-type-select.component.html',
  styleUrls: ['./field-type-select.component.css']
})
export class FieldTypeSelectComponent {
  @Input() label = 'Field type';
  @Input() appearance: 'fill' | 'outline' = 'fill';
  @Input() options: string[] = [];
  @Input() value = '';
  @Input() disabled = false;
  @Input() className = '';
  @Output() valueChange = new EventEmitter<string>();

  onValueChange(value: string): void {
    this.valueChange.emit(value);
  }
}
