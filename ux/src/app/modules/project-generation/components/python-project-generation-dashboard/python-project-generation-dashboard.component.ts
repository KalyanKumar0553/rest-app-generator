import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../../services/auth.service';
import { LocalStorageService } from '../../../../services/local-storage.service';
import { ToastService } from '../../../../services/toast.service';
import { UserService } from '../../../../services/user.service';
import { ValidatorService } from '../../../../services/validator.service';
import { ProjectService } from '../../../../services/project.service';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { SearchableSelectComponent } from '../../../../components/searchable-select/searchable-select.component';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { LoadingOverlayComponent } from '../../../../components/shared/loading-overlay/loading-overlay.component';
import { SidenavComponent } from '../../../../components/shared/sidenav/sidenav.component';
import { NodeGeneralTabComponent } from '../general-tab/node/node-general-tab.component';
import { RbacModuleTabComponent } from '../rbac-module-tab/rbac-module-tab.component';
import { AuthModuleTabComponent } from '../auth-module-tab/auth-module-tab.component';
import { StateMachineModuleTabComponent } from '../state-machine-module-tab/state-machine-module-tab.component';
import { SubscriptionModuleTabComponent } from '../subscription-module-tab/subscription-module-tab.component';
import { ModulesSelectionComponent } from '../modules-selection/modules-selection.component';
import { NodeControllersSpecTableComponent } from '../node/controllers-spec-table/node-controllers-spec-table.component';
import { NodeDataObjectsComponent } from '../node/data-objects/node-data-objects.component';
import { NodeEntitiesComponent } from '../node/entities/node-entities.component';
import { NodeProjectSpecComponent } from '../node/project-spec/node-project-spec.component';
import { NodeRestConfigComponent } from '../node/rest-config/node-rest-config.component';
import { AddProfileComponent } from '../add-profile/add-profile.component';
import { NodeProjectGenerationExploreSectionComponent } from '../node-project-generation-dashboard/node-project-generation-explore-section.component';
import { ProjectSpecMapperService } from '../../services/project-spec-mapper.service';
import { NodeProjectGenerationDashboardComponent } from '../node-project-generation-dashboard/node-project-generation-dashboard.component';
import { PYTHON_PROJECT_GENERATION_CONFIG } from './python-project-generation-dashboard.config';

@Component({
  selector: 'app-python-project-generation-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatAutocompleteModule,
    MatTableModule,
    ConfirmationModalComponent,
    ModalComponent,
    AddProfileComponent,
    NodeGeneralTabComponent,
    NodeEntitiesComponent,
    NodeDataObjectsComponent,
    NodeProjectSpecComponent,
    NodeControllersSpecTableComponent,
    NodeRestConfigComponent,
    NodeProjectGenerationExploreSectionComponent,
    RbacModuleTabComponent,
    AuthModuleTabComponent,
    StateMachineModuleTabComponent,
    SubscriptionModuleTabComponent,
    ModulesSelectionComponent,
    LoadingOverlayComponent,
    SidenavComponent,
    InfoBannerComponent,
    SearchableSelectComponent
  ],
  templateUrl: '../node-project-generation-dashboard/node-project-generation-dashboard.component.html',
  styleUrls: ['../node-project-generation-dashboard/node-project-generation-dashboard.component.css']
})
export class PythonProjectGenerationDashboardComponent extends NodeProjectGenerationDashboardComponent {
  constructor(
    router: Router,
    route: ActivatedRoute,
    authService: AuthService,
    projectService: ProjectService,
    toastService: ToastService,
    userService: UserService,
    http: HttpClient,
    validatorService: ValidatorService,
    localStorageService: LocalStorageService,
    cdr: ChangeDetectorRef,
    specMapper: ProjectSpecMapperService
  ) {
    super(
      router,
      route,
      authService,
      projectService,
      toastService,
      userService,
      http,
      validatorService,
      localStorageService,
      cdr,
      specMapper
    );
    this.projectSettings = {
      ...this.projectSettings,
      ...PYTHON_PROJECT_GENERATION_CONFIG.defaultProjectSettings
    };
    this.developerPreferences = {
      ...this.developerPreferences,
      ...PYTHON_PROJECT_GENERATION_CONFIG.defaultDeveloperPreferences
    };
  }
}
