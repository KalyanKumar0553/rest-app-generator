import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { PreviewEntitiesComponent } from '../../../../components/preview-entities/preview-entities.component';
import {
  SearchSortComponent,
  SearchConfig,
  SearchSortEvent,
  SortOption
} from '../../../../components/search-sort/search-sort.component';
import { Field } from '../field-item/field-item.component';
import { AddDataObjectComponent } from '../add-data-object/add-data-object.component';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';

interface DataObject {
  name: string;
  dtoType?: 'request' | 'response';
  fields: Field[];
}

@Component({
  selector: 'app-data-objects',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    ModalComponent,
    ConfirmationModalComponent,
    EntityDetailViewComponent,
    PreviewEntitiesComponent,
    SearchSortComponent,
    AddDataObjectComponent,
    InfoBannerComponent
  ],
  templateUrl: './data-objects.component.html',
  styleUrls: ['./data-objects.component.css']
})
export class DataObjectsComponent {
  @Input() dataObjects: DataObject[] = [];
  @Input() entities: Array<{ name: string }> = [];
  @Output() dataObjectsChange = new EventEmitter<DataObject[]>();

  @ViewChild(AddDataObjectComponent) addDataObjectComponent!: AddDataObjectComponent;
  @ViewChild(SearchSortComponent) dataObjectSearchSortComponent?: SearchSortComponent;

  dataObjectsExpanded = true;
  showAddDataObjectModal = false;
  showDeleteConfirmation = false;
  showDataObjectFilters = false;

  editingDataObject: DataObject | null = null;
  editingDataObjectIndex: number | null = null;

  deletingDataObjectIndex: number | null = null;

  showDataObjectDetailModal = false;
  viewingDataObject: DataObject | null = null;
  viewingDataObjectIndex: number | null = null;
  showPropertyDeleteConfirmation = false;
  propertyDeleteIndex: number | null = null;

  dataObjectSearchTerm = '';
  dataObjectSortOption: SortOption | null = null;
  visibleDataObjects: DataObject[] = [];

  searchConfig: SearchConfig = {
    placeholder: 'Search data objects by name...',
    properties: ['name']
  };

  sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' }
  ];

  deleteModalConfig = {
    title: 'Delete Data Object',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  propertyDeleteModalConfig = {
    title: 'Delete Property',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  ngOnInit(): void {
    this.updateVisibleDataObjects();
  }

  toggleDataObjectsPanel(): void {
    this.dataObjectsExpanded = !this.dataObjectsExpanded;
  }

  addDataObject(): void {
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
    this.showAddDataObjectModal = true;
  }

  editDataObject(dataObject: DataObject, index: number): void {
    this.editingDataObject = JSON.parse(JSON.stringify(dataObject));
    this.editingDataObjectIndex = index;
    this.showAddDataObjectModal = true;
  }

  deleteDataObject(index: number): void {
    this.deletingDataObjectIndex = index;
    this.deleteModalConfig.message = [
      `Are you sure you want to delete the data object "${this.dataObjects[index].name}"?`,
      'This action cannot be undone and all associated properties will be removed.'
    ];
    this.showDeleteConfirmation = true;
  }

  confirmDelete(): void {
    if (this.deletingDataObjectIndex !== null) {
      this.dataObjects.splice(this.deletingDataObjectIndex, 1);
      if (this.dataObjects.length === 0) {
        this.showDataObjectFilters = false;
        this.resetDataObjectSearch();
      }
      this.emitDataObjects();
      this.updateVisibleDataObjects();
    }
    this.cancelDelete();
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.deletingDataObjectIndex = null;
  }

  onDataObjectSave(dataObject: DataObject): void {
    if (this.editingDataObjectIndex !== null) {
      this.dataObjects[this.editingDataObjectIndex] = dataObject;
    } else {
      this.dataObjects.push(dataObject);
    }

    this.emitDataObjects();
    this.updateVisibleDataObjects();
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  onDataObjectCancel(): void {
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  closeModal(): void {
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  saveDataObject(): void {
    if (this.addDataObjectComponent) {
      this.addDataObjectComponent.onSave();
    }
  }

  toggleDataObjectFilters(): void {
    if (this.dataObjects.length === 0) {
      return;
    }
    this.showDataObjectFilters = !this.showDataObjectFilters;
    if (!this.showDataObjectFilters) {
      this.resetDataObjectSearch();
      this.updateVisibleDataObjects();
    }
  }

  onDataObjectSearchSortChange(event: SearchSortEvent): void {
    this.dataObjectSearchTerm = event.searchTerm;
    this.dataObjectSortOption = event.sortOption;
    this.updateVisibleDataObjects();
  }

  openDataObjectDetail(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.viewingDataObjectIndex = index;
      this.viewingDataObject = this.dataObjects[index];
      this.showDataObjectDetailModal = true;
    }
  }

  closeDataObjectDetail(): void {
    this.showDataObjectDetailModal = false;
    this.viewingDataObject = null;
    this.viewingDataObjectIndex = null;
  }

  deleteProperty(fieldIndex: number): void {
    if (this.viewingDataObjectIndex === null) {
      return;
    }

    const target = this.dataObjects[this.viewingDataObjectIndex];
    const propertyName = target?.fields?.[fieldIndex]?.name ?? 'this property';
    this.propertyDeleteIndex = fieldIndex;
    this.propertyDeleteModalConfig.message = [
      `Are you sure you want to delete "${propertyName}"?`,
      'This action cannot be undone.'
    ];
    this.showPropertyDeleteConfirmation = true;
  }

  confirmDeleteProperty(): void {
    if (this.viewingDataObjectIndex === null || this.propertyDeleteIndex === null) {
      this.cancelDeleteProperty();
      return;
    }

    const target = this.dataObjects[this.viewingDataObjectIndex];
    if (target?.fields) {
      target.fields.splice(this.propertyDeleteIndex, 1);
      this.emitDataObjects();
      this.updateVisibleDataObjects();
    }

    this.cancelDeleteProperty();
  }

  cancelDeleteProperty(): void {
    this.showPropertyDeleteConfirmation = false;
    this.propertyDeleteIndex = null;
  }

  editDataObjectFromList(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.editDataObject(this.dataObjects[index], index);
    }
  }

  deleteDataObjectFromList(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.deleteDataObject(index);
    }
  }

  private updateVisibleDataObjects(): void {
    const normalizedSearch = this.dataObjectSearchTerm.trim().toLowerCase();
    let results = [...this.dataObjects];

    if (normalizedSearch) {
      results = results.filter(dataObject =>
        dataObject.name?.toLowerCase().includes(normalizedSearch)
      );
    }

    if (this.dataObjectSortOption) {
      const direction = this.dataObjectSortOption.direction;
      results = results.sort((a, b) => {
        const aValue = a.name?.toLowerCase() ?? '';
        const bValue = b.name?.toLowerCase() ?? '';
        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return direction === 'asc' ? comparison : -comparison;
      });
    }

    this.visibleDataObjects = results;
  }

  private resetDataObjectSearch(): void {
    this.dataObjectSearchTerm = '';
    if (this.dataObjectSearchSortComponent) {
      this.dataObjectSearchSortComponent.clearSearch();
    }
  }

  private getDataObjectIndex(dataObject: DataObject): number | null {
    const name = dataObject?.name?.toLowerCase();
    if (!name) {
      return null;
    }

    const index = this.dataObjects.findIndex(item => item.name?.toLowerCase() === name);
    return index >= 0 ? index : null;
  }

  private emitDataObjects(): void {
    this.dataObjectsChange.emit(JSON.parse(JSON.stringify(this.dataObjects)));
  }
}
