import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { NoDataStateComponent } from '../../../../components/shared/no-data-state/no-data-state.component';
import { AuthService } from '../../../../services/auth.service';
import {
  ArchivedProjectCollaboration,
  ProjectCollaborationInvite,
  ProjectContributorPermissions,
  ProjectService
} from '../../../../services/project.service';
import { ToastService } from '../../../../services/toast.service';
import { APP_SETTINGS } from '../../../../settings/app-settings';
import { resolveProjectGenerationRoute } from '../../utils/project-generation-route.utils';

@Component({
  selector: 'app-project-collaboration-invite',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCheckboxModule, MatButtonModule, MatCardModule, ModalComponent, NoDataStateComponent],
  templateUrl: './project-collaboration-invite.component.html',
  styleUrls: ['./project-collaboration-invite.component.css']
})
export class ProjectCollaborationInviteComponent implements OnInit {
  readonly appSettings = APP_SETTINGS;
  invite: ProjectCollaborationInvite | null = null;
  archivedCollaborations: ArchivedProjectCollaboration[] = [];
  isLoading = true;
  isSubmitting = false;
  isLoadingArchivedCollaborations = false;
  isResubscribing = false;
  showArchivedRequestsModal = false;
  requestedPermissions: ProjectContributorPermissions = {
    canEditDraft: true,
    canGenerate: false,
    canManageCollaboration: false
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly projectService: ProjectService,
    private readonly toastService: ToastService
  ) {}

  async ngOnInit(): Promise<void> {
    if (!this.authService.getAccessToken()) {
      this.toastService.error('Please login to request collaboration access.');
      this.router.navigate(['/']);
      return;
    }

    const inviteToken = String(this.route.snapshot.paramMap.get('inviteToken') || '').trim();
    if (!inviteToken) {
      this.toastService.error('Invalid collaboration invite.');
      this.router.navigate(['/user/dashboard']);
      return;
    }

    try {
      await this.loadArchivedCollaborations();
      this.invite = await firstValueFrom(this.projectService.getCollaborationInvite(inviteToken));
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to load collaboration invite.');
      this.router.navigate(['/user/dashboard']);
    } finally {
      this.isLoading = false;
    }
  }

  async requestAccess(): Promise<void> {
    const inviteToken = this.invite?.inviteToken?.trim();
    if (!inviteToken || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;
    try {
      await firstValueFrom(this.projectService.requestCollaboration(inviteToken, this.requestedPermissions));
      this.toastService.success('Collaboration request sent to the project owner.');
      this.invite = this.invite ? { ...this.invite, requestPending: true } : this.invite;
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to send collaboration request.');
    } finally {
      this.isSubmitting = false;
    }
  }

  openArchivedRequestsModal(): void {
    this.showArchivedRequestsModal = true;
  }

  closeArchivedRequestsModal(): void {
    if (this.isResubscribing) {
      return;
    }
    this.showArchivedRequestsModal = false;
  }

  async resubscribeToArchivedCollaboration(collaboration: ArchivedProjectCollaboration): Promise<void> {
    const contributorId = String(collaboration?.contributorId ?? '').trim();
    if (!contributorId || this.isResubscribing) {
      return;
    }
    this.isResubscribing = true;
    try {
      await firstValueFrom(this.projectService.resubscribeArchivedCollaboration(contributorId));
      await this.loadArchivedCollaborations();
      this.toastService.success('Collaboration request sent to the project owner.');
    } catch (error: any) {
      this.toastService.error(error?.message || 'Failed to resubscribe to this project.');
    } finally {
      this.isResubscribing = false;
    }
  }

  openProject(): void {
    if (!this.invite?.projectId) {
      return;
    }
    this.router.navigate([resolveProjectGenerationRoute(this.invite.generator || 'java')], {
      queryParams: { projectId: this.invite.projectId }
    });
  }

  private async loadArchivedCollaborations(): Promise<void> {
    this.isLoadingArchivedCollaborations = true;
    try {
      this.archivedCollaborations = await firstValueFrom(this.projectService.getArchivedCollaborations());
    } finally {
      this.isLoadingArchivedCollaborations = false;
    }
  }
}
