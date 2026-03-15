import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ProjectSpecComponent } from '../../project-spec/project-spec.component';

@Component({
  selector: 'app-node-project-spec',
  standalone: true,
  imports: [CommonModule, ProjectSpecComponent],
  template: `
    <app-project-spec [spec]="spec"></app-project-spec>
  `
})
export class NodeProjectSpecComponent {
  @Input() spec = '';
}
