import { Component } from '@angular/core';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';

@Component({
  selector: 'app-rbac-module-tab',
  standalone: true,
  imports: [ShippedModuleConfigPanelComponent],
  template: `
    <app-shipped-module-config-panel
      moduleId="RBAC"
      title="RBAC Module"
      subtitle="Role-based access control is selected for this project. Use this tab as the dedicated space for module-specific settings and policy defaults.">
    </app-shipped-module-config-panel>
  `
})
export class RbacModuleTabComponent {}
