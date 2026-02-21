import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { SearchableMultiSelectComponent } from '../../../../../components/searchable-multi-select/searchable-multi-select.component';

@Component({
  selector: 'app-rest-config-pagination-filtering',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatSelectModule,
    MatRadioModule,
    SearchableMultiSelectComponent
  ],
  templateUrl: './pagination-filtering.component.html',
  styleUrls: ['./pagination-filtering.component.css']
})
export class PaginationFilteringComponent {
  @Input({ required: true }) draft: any;
  @Input() searchFieldOptions: string[] = [];

  readonly sortFieldOptions = ['createdAt', 'updatedAt', 'name', 'id'];

  onSearchableFieldsChange(values: string[]): void {
    this.draft.searchFiltering.searchableFields = Array.isArray(values)
      ? values.map((value) => String(value ?? '').trim()).filter(Boolean)
      : [];
  }
}
