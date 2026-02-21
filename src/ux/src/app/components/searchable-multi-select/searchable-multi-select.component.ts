import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-searchable-multi-select',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, MatInputModule],
  templateUrl: './searchable-multi-select.component.html',
  styleUrls: ['./searchable-multi-select.component.css']
})
export class SearchableMultiSelectComponent {
  @Input() label = 'Select options';
  @Input() searchPlaceholder = 'Please Enter Text to Search';
  @Input() options: string[] = [];
  @Input() selectedValues: string[] = [];
  @Input() disabled = false;
  @Input() emptyMessage = 'No options found';

  @Output() selectedValuesChange = new EventEmitter<string[]>();

  searchTerm = '';

  get selectedTagValues(): string[] {
    return (this.selectedValues ?? [])
      .map((value) => String(value ?? '').trim())
      .filter(Boolean);
  }

  get filteredOptions(): string[] {
    const term = this.searchTerm.trim().toLowerCase();
    const selectedSet = new Set((this.selectedValues ?? []).filter(Boolean));
    const matching = this.options.filter(option => !term || option.toLowerCase().includes(term));
    const merged = [...selectedSet, ...matching];
    return Array.from(new Set(merged));
  }

  onSelectionChange(values: string[]): void {
    const normalized = Array.from(new Set((values ?? []).filter(Boolean)));
    this.selectedValuesChange.emit(normalized);
  }

  onSearchKeydown(event: KeyboardEvent): void {
    event.stopPropagation();
  }

  onOpenedChange(opened: boolean): void {
    if (!opened) {
      this.searchTerm = '';
    }
  }

  removeSelectedValue(value: string, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    const next = (this.selectedValues ?? [])
      .filter((item) => String(item ?? '').trim() !== String(value ?? '').trim());
    this.onSelectionChange(next);
  }
}
