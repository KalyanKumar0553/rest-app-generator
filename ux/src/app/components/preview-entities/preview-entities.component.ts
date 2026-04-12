import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HelpPopoverComponent } from '../help-popover/help-popover.component';

export interface PreviewEntityField {
  name: string;
  type: string;
  maxLength?: number;
  required?: boolean;
  primaryKey?: boolean;
  unique?: boolean;
}

export interface PreviewEntity {
  name: string;
  dtoType?: 'request' | 'response';
  metaTag?: string;
  fields?: PreviewEntityField[];
}

export interface PreviewRelation {
  sourceEntity: string;
  targetEntity: string;
  relationType?: string;
}

@Component({
  selector: 'app-preview-entities',
  standalone: true,
  imports: [CommonModule, HelpPopoverComponent],
  templateUrl: './preview-entities.component.html',
  styleUrls: ['./preview-entities.component.css']
})
export class PreviewEntitiesComponent {
  @Input() entities: PreviewEntity[] = [];
  @Input() relations: PreviewRelation[] = [];
  @Input() showActions = false;
  @Input() emptyMessage = 'No entities available.';

  @Output() editEntity = new EventEmitter<{ entity: PreviewEntity; index: number }>();
  @Output() deleteEntity = new EventEmitter<{ entity: PreviewEntity; index: number }>();
  @Output() viewMore = new EventEmitter<{ entity: PreviewEntity; index: number }>();
  @Output() viewRelations = new EventEmitter<{ entity: PreviewEntity; index: number }>();

  getVisibleFields(entity: PreviewEntity): PreviewEntityField[] {
    const fields = entity.fields ?? [];
    return fields.slice(0, 5);
  }

  hasMoreFields(entity: PreviewEntity): boolean {
    return (entity.fields?.length ?? 0) > 5;
  }

  getEntityRelations(entityName: string): PreviewRelation[] {
    if (!this.relations?.length) {
      return [];
    }

    return this.relations
      .filter(relation => relation.sourceEntity === entityName || relation.targetEntity === entityName);
  }

  onEdit(entity: PreviewEntity, index: number): void {
    this.editEntity.emit({ entity, index });
  }

  onDelete(entity: PreviewEntity, index: number): void {
    this.deleteEntity.emit({ entity, index });
  }

  onViewMore(entity: PreviewEntity, index: number): void {
    this.viewMore.emit({ entity, index });
  }

  onViewRelations(entity: PreviewEntity, index: number): void {
    this.viewRelations.emit({ entity, index });
  }

  getFieldConstraints(field: PreviewEntityField): string {
    const constraints: string[] = [];
    if (field.primaryKey) {
      constraints.push('Primary Key');
    }
    if (field.required) {
      constraints.push('Required');
    }
    if (field.unique) {
      constraints.push('Unique');
    }
    return constraints.length ? constraints.join(', ') : 'None';
  }

  getEntityFieldCount(entity: PreviewEntity): number {
    return (entity.fields ?? []).length;
  }

  getEntityConstraintCount(entity: PreviewEntity): number {
    const fields = entity.fields ?? [];
    return fields.reduce((count, field) => {
      let fieldCount = 0;
      if (field.primaryKey) {
        fieldCount += 1;
      }
      if (field.required) {
        fieldCount += 1;
      }
      if (field.unique) {
        fieldCount += 1;
      }
      return count + fieldCount;
    }, 0);
  }
}
