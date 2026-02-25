import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-edit-documentation-operation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './edit-documentation-operation.component.html',
  styleUrls: ['./edit-documentation-operation.component.css']
})
export class EditDocumentationOperationComponent implements OnChanges {
  @Input() endpointLabel = '';
  @Input({ required: true }) formData!: { description: string; group: string; descriptionTags: string[]; deprecated: boolean; };

  tagInputValue = '';
  showValidationErrors = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formData'] || changes['endpointLabel']) {
      this.showValidationErrors = false;
    }
  }

  validate(): boolean {
    this.showValidationErrors = true;
    return !this.groupRequiredError && !this.descriptionRequiredError && !this.tagsRequiredError;
  }

  get groupRequiredError(): boolean {
    return !String(this.formData?.group ?? '').trim();
  }

  get descriptionRequiredError(): boolean {
    return !String(this.formData?.description ?? '').trim();
  }

  get tagsRequiredError(): boolean {
    return !Array.isArray(this.formData?.descriptionTags) || this.formData.descriptionTags.length === 0;
  }

  addTag(value: string): void {
    const tag = String(value ?? '').trim();
    if (!tag) {
      this.tagInputValue = '';
      return;
    }

    if (!this.formData.descriptionTags.includes(tag)) {
      this.formData.descriptionTags.push(tag);
    }
    this.showValidationErrors = false;
    this.tagInputValue = '';
  }

  onTagInputKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter' && event.key !== ',') {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    this.addTag(this.tagInputValue);
  }

  addTagFromBlur(): void {
    if (String(this.tagInputValue ?? '').trim()) {
      this.addTag(this.tagInputValue);
    }
  }

  removeTag(tag: string): void {
    this.formData.descriptionTags = this.formData.descriptionTags.filter((item) => item !== tag);
  }

  trackByTag(_: number, tag: string): string {
    return tag;
  }
}
