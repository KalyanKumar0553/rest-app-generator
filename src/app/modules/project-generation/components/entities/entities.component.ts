import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: any[] = [];
  @Input() relations: any[] = [];

  addEntity() {
    console.log('Add entity clicked');
  }
}
