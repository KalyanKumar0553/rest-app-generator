import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddEntityComponent } from '../add-entity/add-entity.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ProjectDataService, Entity, Relation } from '../../../../services/project-data.service';

@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule, AddEntityComponent, ModalComponent],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent implements OnInit {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];

  showAddEntityModal = false;
  entitiesExpanded = true;
  relationsExpanded = true;
  editingEntity?: Entity;
  editingEntityIndex?: number;

  constructor(private projectDataService: ProjectDataService) {}

  ngOnInit(): void {
    const projectData = this.projectDataService.getProjectData();
    if (projectData) {
      this.entities = projectData.entities || [];
      this.relations = projectData.relations || [];
    }
  }

  addEntity(): void {
    this.editingEntity = undefined;
    this.editingEntityIndex = undefined;
    this.showAddEntityModal = true;
  }

  editEntity(entity: Entity, index: number): void {
    this.editingEntity = { ...entity };
    this.editingEntityIndex = index;
    this.showAddEntityModal = true;
  }

  deleteEntity(index: number): void {
    this.projectDataService.deleteEntity(index);
    const projectData = this.projectDataService.getProjectData();
    if (projectData) {
      this.entities = projectData.entities;
    }
  }

  onEntitySave(entity: Entity): void {
    if (this.editingEntityIndex !== undefined) {
      this.projectDataService.updateEntity(this.editingEntityIndex, entity);
    } else {
      this.projectDataService.addEntity(entity);
    }

    const projectData = this.projectDataService.getProjectData();
    if (projectData) {
      this.entities = projectData.entities;
    }

    this.closeModal();
  }

  onEntityCancel(): void {
    this.closeModal();
  }

  closeModal(): void {
    this.showAddEntityModal = false;
    this.editingEntity = undefined;
    this.editingEntityIndex = undefined;
  }

  toggleEntitiesPanel(): void {
    this.entitiesExpanded = !this.entitiesExpanded;
  }

  toggleRelationsPanel(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  addRelation(): void {
    console.log('Add relation clicked');
  }
}
