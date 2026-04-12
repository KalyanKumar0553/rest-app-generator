import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export interface NavItem {
  icon: string;
  label: string;
  value: string;
}

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.css']
})
export class SidenavComponent {
  @Input() navItems: NavItem[] = [];
  @Input() activeItem: string = '';
  @Output() itemClick = new EventEmitter<string>();

  onItemClick(value: string): void {
    this.itemClick.emit(value);
  }
}
