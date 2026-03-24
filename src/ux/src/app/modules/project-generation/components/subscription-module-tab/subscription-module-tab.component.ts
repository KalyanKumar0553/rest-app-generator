import { Component } from '@angular/core';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';

@Component({
  selector: 'app-subscription-module-tab',
  standalone: true,
  imports: [ShippedModuleConfigPanelComponent],
  template: `
    <app-shipped-module-config-panel
      moduleId="Subscription"
      title="Subscription Module"
      subtitle="Subscription and entitlement management is selected for this project. This tab is the placeholder for plan, pricing, and quota defaults.">
    </app-shipped-module-config-panel>
  `
})
export class SubscriptionModuleTabComponent {}
