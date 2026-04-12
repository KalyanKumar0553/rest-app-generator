import { Component } from '@angular/core';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';

@Component({
  selector: 'app-auth-module-tab',
  standalone: true,
  imports: [ShippedModuleConfigPanelComponent],
  template: `
    <app-shipped-module-config-panel
      moduleId="Auth"
      title="Auth Module"
      subtitle="Authentication is selected for this project. This tab is reserved for provider setup, token policies, and login flow configuration.">
    </app-shipped-module-config-panel>
  `
})
export class AuthModuleTabComponent {}
