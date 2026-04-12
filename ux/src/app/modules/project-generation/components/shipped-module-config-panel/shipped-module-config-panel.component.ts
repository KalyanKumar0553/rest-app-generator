import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-shipped-module-config-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './shipped-module-config-panel.component.html',
  styleUrls: ['./shipped-module-config-panel.component.css']
})
export class ShippedModuleConfigPanelComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() moduleId = '';
}
