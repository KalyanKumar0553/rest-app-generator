import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-project-spec',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './project-spec.component.html',
  styleUrls: ['./project-spec.component.css']
})
export class ProjectSpecComponent {
  @Input() spec = '';
}

