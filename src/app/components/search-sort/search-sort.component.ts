import { Component, EventEmitter, Input, Output, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface SearchConfig {
  placeholder: string;
  properties: string[];
}

export interface SortOption {
  label: string;
  property: string;
  direction: 'asc' | 'desc';
}

export interface SearchSortEvent {
  searchTerm: string;
  sortOption: SortOption | null;
}

@Component({
  selector: 'app-search-sort',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-sort.component.html',
  styleUrls: ['./search-sort.component.css']
})
export class SearchSortComponent {
  @Input() searchConfig: SearchConfig = {
    placeholder: 'Search...',
    properties: []
  };
  @Input() sortOptions: SortOption[] = [];
  @Output() searchSortChange = new EventEmitter<SearchSortEvent>();
  @ViewChild('sortButton', { read: ElementRef }) sortButton!: ElementRef;

  searchTerm: string = '';
  selectedSortOption: SortOption | null = null;
  isDropdownOpen: boolean = false;
  dropdownPosition = { top: '0px', right: '0px' };

  onSearchChange(): void {
    this.emitChange();
  }

  onSortSelect(option: SortOption): void {
    if (this.selectedSortOption?.property === option.property &&
        this.selectedSortOption?.direction === option.direction) {
      this.selectedSortOption = null;
    } else {
      this.selectedSortOption = option;
    }
    this.isDropdownOpen = false;
    this.emitChange();
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    if (this.isDropdownOpen && this.sortButton) {
      this.calculateDropdownPosition();
    }
  }

  private calculateDropdownPosition(): void {
    if (this.sortButton && this.sortButton.nativeElement) {
      const rect = this.sortButton.nativeElement.getBoundingClientRect();
      const viewportWidth = window.innerWidth;
      const rightSpace = viewportWidth - rect.right;

      this.dropdownPosition = {
        top: `${rect.bottom + 8}px`,
        right: `${rightSpace}px`
      };
    }
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.emitChange();
  }

  clearSort(event: Event): void {
    event.stopPropagation();
    this.selectedSortOption = null;
    this.emitChange();
  }

  private emitChange(): void {
    this.searchSortChange.emit({
      searchTerm: this.searchTerm,
      sortOption: this.selectedSortOption
    });
  }

  getSortLabel(): string {
    if (!this.selectedSortOption) {
      return 'Sort by';
    }
    const direction = this.selectedSortOption.direction === 'asc' ? '↑' : '↓';
    return `${this.selectedSortOption.label} ${direction}`;
  }
}
