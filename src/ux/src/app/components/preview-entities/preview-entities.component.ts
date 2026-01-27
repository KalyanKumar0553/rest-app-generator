import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface PreviewEntityField {
  name: string;
  type: string;
  maxLength?: number;
  required?: boolean;
}

export interface PreviewEntity {
  name: string;
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
  imports: [CommonModule],
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

  getVisibleFields(entity: PreviewEntity): PreviewEntityField[] {
    const fields = entity.fields ?? [];
    return fields.slice(0, 5);
  }

  hasMoreFields(entity: PreviewEntity): boolean {
    return (entity.fields?.length ?? 0) > 5;
  }

  getEntityRelations(entityName: string): string[] {
    if (!this.relations?.length) {
      return [];
    }

    return this.relations
      .filter(relation => relation.sourceEntity === entityName || relation.targetEntity === entityName)
      .map(relation => {
        const otherEntity = relation.sourceEntity === entityName ? relation.targetEntity : relation.sourceEntity;
        return relation.relationType ? `${relation.relationType}: ${otherEntity}` : otherEntity;
      });
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
}
