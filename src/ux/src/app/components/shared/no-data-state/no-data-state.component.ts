import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-no-data-state',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './no-data-state.component.html',
  styleUrls: ['./no-data-state.component.css']
})
export class NoDataStateComponent {
  @Input() title = 'No Data Available';
  @Input() message = '';
}
