import { Component, EventEmitter, Input, Output, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

export interface SelectOption {
  id: string;
  label: string;
  subtitle?: string;
  avatarUrl?: string | null;
}

@Component({
  selector: 'app-searchable-select',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatIconModule],
  templateUrl: './searchable-select.component.html',
  styleUrls: ['./searchable-select.component.css']
})
export class SearchableSelectComponent implements OnDestroy {
  @Input() label = 'Search';
  @Input() placeholder = 'Type to search';
  @Input() searchResults: SelectOption[] = [];
  @Input() isSearching = false;
  @Input() selectedItems: SelectOption[] = [];
  @Input() excludeIds: string[] = [];
  @Input() emptyMessage = 'No results found.';

  @Output() searchChange = new EventEmitter<string>();
  @Output() selectedItemsChange = new EventEmitter<SelectOption[]>();

  searchTerm = '';
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  constructor() {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe((term) => {
      this.searchChange.emit(term);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get filteredResults(): SelectOption[] {
    const selectedIds = new Set(this.selectedItems.map((item) => item.id));
    const excludedIds = new Set(this.excludeIds);
    return this.searchResults.filter((item) => !selectedIds.has(item.id) && !excludedIds.has(item.id));
  }

  onSearchInput(value: string): void {
    this.searchTerm = value;
    this.searchSubject.next(value.trim());
  }

  selectItem(item: SelectOption): void {
    if (!this.selectedItems.some((s) => s.id === item.id)) {
      this.selectedItems = [...this.selectedItems, item];
      this.selectedItemsChange.emit(this.selectedItems);
    }
  }

  removeItem(item: SelectOption): void {
    this.selectedItems = this.selectedItems.filter((s) => s.id !== item.id);
    this.selectedItemsChange.emit(this.selectedItems);
  }

  getInitial(item: SelectOption): string {
    return (item.label || item.id).charAt(0).toUpperCase();
  }

  reset(): void {
    this.searchTerm = '';
    this.selectedItems = [];
  }
}
