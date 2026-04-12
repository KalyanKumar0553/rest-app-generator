import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';
import { DdlEntity } from '../../../../services/ddl-import.service';
import { PreviewEntitiesComponent } from '../../../../components/preview-entities/preview-entities.component';
import { PreviewEntity } from '../../../../components/preview-entities/preview-entities.component';

@Component({
  selector: 'app-import-schema-preview',
  standalone: true,
  imports: [CommonModule, FormsModule, MatRadioModule, PreviewEntitiesComponent],
  templateUrl: './import-schema-preview.component.html',
  styleUrls: ['./import-schema-preview.component.css']
})
export class ImportSchemaPreviewComponent {
  @Input() entities: DdlEntity[] = [];
  @Input() addRestEndpoints = false;
  @Output() addRestEndpointsChange = new EventEmitter<boolean>();
  @Output() viewEntity = new EventEmitter<{ entity: PreviewEntity; index: number }>();
  @Output() editEntity = new EventEmitter<{ entity: PreviewEntity; index: number }>();
  @Output() deleteEntity = new EventEmitter<{ entity: PreviewEntity; index: number }>();

  onAddRestEndpointsChange(value: boolean): void {
    this.addRestEndpoints = value;
    this.addRestEndpointsChange.emit(value);
  }

  onViewEntity(event: { entity: PreviewEntity; index: number }): void {
    this.viewEntity.emit(event);
  }

  onEditEntity(event: { entity: PreviewEntity; index: number }): void {
    this.editEntity.emit(event);
  }

  onDeleteEntity(event: { entity: PreviewEntity; index: number }): void {
    this.deleteEntity.emit(event);
  }
}
