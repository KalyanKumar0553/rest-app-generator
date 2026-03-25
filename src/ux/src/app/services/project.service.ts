import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface ProjectTabDefinition {
  key: string;
  label: string;
  icon: string;
  componentKey: string;
  order: number;
}

export interface ProjectDraftPayload {
  draftData: Record<string, any>;
  draftVersion?: number;
}

export interface ProjectDraftTabPatchPayload {
  tabKey: string;
  tabData: Record<string, any>;
  draftVersion: number;
}

export interface ProjectDraftResponse {
  projectId: string;
  draftVersion?: number;
}

export interface ProjectDraftTabData {
  tabKey: string;
  tabData: Record<string, any>;
}

export interface ProjectCollaborationEditor {
  sessionId: string;
  userId: string;
  label?: string;
  lastSeenAt?: string;
}

export interface ProjectCollaborationAction {
  actionId: string;
  projectId: string;
  sessionId: string;
  userId: string;
  tabKey: string;
  actionType: string;
  draftVersion?: number;
  message?: string;
  createdAt?: string;
}

export interface ProjectCollaborationState {
  activeEditors: number;
  editors: ProjectCollaborationEditor[];
  recentActions: ProjectCollaborationAction[];
}

export interface ProjectCollaborationPresenceResponse extends ProjectCollaborationState {
  sessionId: string;
}

export interface ProjectSummary {
  projectId: string;
  artifact?: string;
  id?: string;
  name?: string;
  description?: string;
  generator?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectContributor {
  id?: string;
  userId: string;
  canEditDraft?: boolean;
  canGenerate?: boolean;
  canManageCollaboration?: boolean;
  disabled?: boolean;
  disabledAt?: string;
  createdAt?: string;
}

export interface ProjectContributorPermissions {
  canEditDraft: boolean;
  canGenerate: boolean;
  canManageCollaboration: boolean;
}

export interface ProjectCollaborationRequest {
  id: string;
  requesterId: string;
  status: string;
  requestedPermissions: ProjectContributorPermissions;
  grantedPermissions: ProjectContributorPermissions;
  reviewedBy?: string;
  reviewedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectCollaborationInvite {
  inviteToken: string;
  projectId: string;
  projectName: string;
  generator?: string;
  ownerId: string;
  contributorAccess: boolean;
  requestPending: boolean;
}

export interface ArchivedProjectCollaboration {
  contributorId: string;
  projectId: string;
  projectName?: string;
  ownerId: string;
  generator?: string;
  inviteToken?: string;
  disabledAt?: string;
}

export interface ProjectDetails extends ProjectSummary {
  yaml?: string;
  draftData?: Record<string, any>;
  draftVersion?: number;
  tabDetails?: ProjectTabDefinition[];
  ownerId?: string;
  contributorAccess?: boolean;
  canManageContributors?: boolean;
  collaborationInviteToken?: string;
  contributors?: ProjectContributor[];
  collaborationRequests?: ProjectCollaborationRequest[];
  latestRunId?: string;
  latestRunStatus?: string;
  latestRunNumber?: number;
  latestRunHasZip?: boolean;
  latestRunZipBase64?: string;
  latestRunZipFileName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  constructor(private http: HttpClient) {}

  getProjects(): Observable<ProjectSummary[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.LIST}`;
    return this.http.get<ProjectSummary[]>(url);
  }

  importProject(projectUrl: string): Observable<ProjectSummary> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.IMPORT}`;
    return this.http.post<ProjectSummary>(url, { projectUrl });
  }

  getProjectTabDetails(generator?: string, dependencies: string[] = []): Observable<ProjectTabDefinition[]> {
    const queryParts: string[] = [];
    if (generator) {
      queryParts.push(`generator=${encodeURIComponent(generator)}`);
    }
    dependencies
      .filter((dependency) => Boolean(dependency))
      .forEach((dependency) => queryParts.push(`dependency=${encodeURIComponent(dependency)}`));
    const query = queryParts.length ? `?${queryParts.join('&')}` : '';
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.TAB_DETAILS}${query}`;
    return this.http.get<ProjectTabDefinition[]>(url);
  }

  getProject(projectId: string): Observable<ProjectDetails> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}`;
    return this.http.get<ProjectDetails>(url);
  }

  getProjectDraftTab(projectId: string, tabKey: string): Observable<ProjectDraftTabData> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET_DRAFT_TAB(projectId)}?tabKey=${encodeURIComponent(tabKey)}`;
    return this.http.get<ProjectDraftTabData>(url);
  }

  createProjectDraft(payload: ProjectDraftPayload): Observable<ProjectDraftResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.CREATE_DRAFT}`;
    return this.http.post<ProjectDraftResponse>(url, payload);
  }

  updateProjectDraft(projectId: string, payload: ProjectDraftPayload): Observable<ProjectDraftResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.UPDATE_DRAFT(projectId)}`;
    return this.http.put<ProjectDraftResponse>(url, payload);
  }

  patchProjectDraftTab(projectId: string, payload: ProjectDraftTabPatchPayload): Observable<ProjectDraftResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.PATCH_DRAFT_TAB(projectId)}`;
    return this.http.patch<ProjectDraftResponse>(url, payload);
  }

  generateProject(projectId: string): Observable<{ id?: string; runId?: string; status?: string }> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GENERATE(projectId)}`;
    return this.http.post<{ id?: string; runId?: string; status?: string }>(url, {});
  }

  retryProjectStage(projectId: string, stage: string, runId?: string): Observable<{ id?: string; runId?: string; status?: string }> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.RETRY_STAGE(projectId)}`;
    return this.http.post<{ id?: string; runId?: string; status?: string }>(url, { stage, runId });
  }

  getProjectCollaboration(projectId: string): Observable<ProjectCollaborationState> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.COLLABORATION(projectId)}`;
    return this.http.get<ProjectCollaborationState>(url);
  }

  registerProjectPresence(projectId: string, sessionId?: string): Observable<ProjectCollaborationPresenceResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.REGISTER_PRESENCE(projectId)}`;
    return this.http.post<ProjectCollaborationPresenceResponse>(url, { sessionId: sessionId ?? null });
  }

  heartbeatProjectPresence(projectId: string, sessionId: string): Observable<ProjectCollaborationState> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.HEARTBEAT_PRESENCE(projectId, sessionId)}`;
    return this.http.put<ProjectCollaborationState>(url, {});
  }

  leaveProjectPresence(projectId: string, sessionId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.HEARTBEAT_PRESENCE(projectId, sessionId)}`;
    return this.http.delete<void>(url);
  }

  recordProjectCollaborationAction(
    projectId: string,
    sessionId: string,
    tabKey: string,
    actionType: string,
    draftVersion?: number,
    message?: string
  ): Observable<ProjectCollaborationState> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.RECORD_ACTION(projectId)}`;
    return this.http.post<ProjectCollaborationState>(url, {
      sessionId,
      tabKey,
      actionType,
      draftVersion,
      message
    });
  }

  getProjectContributors(projectId: string): Observable<ProjectContributor[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors`;
    return this.http.get<ProjectContributor[]>(url);
  }

  addProjectContributor(projectId: string, userId: string): Observable<ProjectContributor[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors`;
    return this.http.post<ProjectContributor[]>(url, { userId });
  }

  updateProjectContributorPermissions(projectId: string, contributorId: string, permissions: ProjectContributorPermissions): Observable<ProjectContributor[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.UPDATE_CONTRIBUTOR_PERMISSIONS(projectId, contributorId)}`;
    return this.http.patch<ProjectContributor[]>(url, permissions);
  }

  removeProjectContributor(projectId: string, userId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors?userId=${encodeURIComponent(userId)}`;
    return this.http.delete<void>(url);
  }

  detachProjectContributor(projectId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.DETACH_CONTRIBUTOR(projectId)}`;
    return this.http.post<void>(url, {});
  }

  getProjectCollaborationRequests(projectId: string): Observable<ProjectCollaborationRequest[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.COLLABORATION_REQUESTS(projectId)}`;
    return this.http.get<ProjectCollaborationRequest[]>(url);
  }

  reviewProjectCollaborationRequest(
    projectId: string,
    requestId: string,
    payload: { status: string } & ProjectContributorPermissions
  ): Observable<ProjectCollaborationRequest> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.REVIEW_COLLABORATION_REQUEST(projectId, requestId)}`;
    return this.http.patch<ProjectCollaborationRequest>(url, payload);
  }

  getCollaborationInvite(inviteToken: string): Observable<ProjectCollaborationInvite> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.COLLABORATION_INVITE(inviteToken)}`;
    return this.http.get<ProjectCollaborationInvite>(url);
  }

  requestCollaboration(inviteToken: string, permissions: ProjectContributorPermissions): Observable<ProjectCollaborationRequest> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.REQUEST_COLLABORATION(inviteToken)}`;
    return this.http.post<ProjectCollaborationRequest>(url, permissions);
  }

  getArchivedCollaborations(): Observable<ArchivedProjectCollaboration[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.ARCHIVED_COLLABORATIONS}`;
    return this.http.get<ArchivedProjectCollaboration[]>(url);
  }

  resubscribeArchivedCollaboration(contributorId: string): Observable<ProjectCollaborationRequest> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.RESUBSCRIBE_ARCHIVED_COLLABORATION(contributorId)}`;
    return this.http.post<ProjectCollaborationRequest>(url, {});
  }

  deleteProject(projectId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.DELETE(projectId)}`;
    return this.http.delete<void>(url);
  }
}
