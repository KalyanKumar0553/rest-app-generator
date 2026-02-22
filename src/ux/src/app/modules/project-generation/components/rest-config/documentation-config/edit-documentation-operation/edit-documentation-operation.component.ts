import { CommonModule } from '@angular/common';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatChipsModule, MatChipInputEvent } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-edit-documentation-operation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatChipsModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  templateUrl: './edit-documentation-operation.component.html',
  styleUrls: ['./edit-documentation-operation.component.css']
})
export class EditDocumentationOperationComponent {
  @Input() endpointLabel = '';
  @Input({ required: true }) formData!: { description: string; descriptionTags: string[]; deprecated: boolean; };

  readonly separatorKeysCodes = [ENTER, COMMA] as const;

  addTag(event: MatChipInputEvent): void {
    const value = String(event.value ?? '').trim();
    if (!value) {
      event.chipInput?.clear();
      return;
    }

    if (!this.formData.descriptionTags.includes(value)) {
      this.formData.descriptionTags.push(value);
    }
    event.chipInput?.clear();
  }

  removeTag(tag: string): void {
    this.formData.descriptionTags = this.formData.descriptionTags.filter((item) => item !== tag);
  }
}
