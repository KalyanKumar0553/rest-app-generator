import { Component } from '@angular/core';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';

@Component({
  selector: 'app-cdn-module-tab',
  standalone: true,
  imports: [ShippedModuleConfigPanelComponent],
  template: `
    <app-shipped-module-config-panel
      moduleId="CDN"
      title="CDN Module"
      subtitle="The CDN upload module is selected for this project. The generated Node app includes upload endpoints, local or Azure-backed storage support, and an asset manifest for generated media workflows.">
    </app-shipped-module-config-panel>
  `
})
export class CdnModuleTabComponent {}
