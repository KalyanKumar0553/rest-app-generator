import { Component } from '@angular/core';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';

@Component({
  selector: 'app-state-machine-module-tab',
  standalone: true,
  imports: [ShippedModuleConfigPanelComponent],
  template: `
    <app-shipped-module-config-panel
      moduleId="State Machine"
      title="State Machine Module"
      subtitle="The workflow engine module is selected for this project. This tab will host state graph, transition, and execution policy options.">
    </app-shipped-module-config-panel>
  `
})
export class StateMachineModuleTabComponent {}
