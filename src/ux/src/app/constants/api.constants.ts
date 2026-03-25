import { environment } from '../../environments/environment';

export const API_CONFIG = {
  BASE_URL: environment.baseUrl,
  AUTH_BASE_URL: environment.authBaseUrl || environment.baseUrl,
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000
};

export const API_ENDPOINTS = {
  AUTH: {
    CAPTCHA: '/api/v1/auth/captcha',
    SIGNUP: '/api/v1/auth/signup',
    LOGIN: '/api/v1/auth/login',
    LOGOUT: '/api/v1/auth/logout',
    REFRESH_TOKEN: '/api/v1/auth/token/refresh',
    PROVIDERS: '/api/v1/auth/providers',
    SEND_OTP: '/api/v1/auth/otp/generate',
    VERIFY_OTP: '/api/v1/auth/otp/verify',
    FORGOT_PASSWORD: '/api/v1/auth/password/forgot',
    RESET_PASSWORD_WITH_OTP: '/api/v1/auth/password/reset',
    ROLES: '/api/v1/auth/roles',
    GOOGLE_OAUTH_START: '/oauth2/authorization/google',
    KEYCLOAK_OAUTH_START: '/oauth2/authorization/keycloak'
  },
  USER: {
    PROFILE: '/api/user/profile',
    UPDATE_PROFILE: '/api/user/profile/update',
    CHANGE_PASSWORD: '/api/user/password/change',
    SEARCH: '/api/users/search'
  },
  ADMIN: {
    DATA_ENCRYPTION_RULES: '/api/admin/data-encryption-rules',
    UPDATE_DATA_ENCRYPTION_RULE: (ruleId: string) => `/api/admin/data-encryption-rules/${ruleId}`,
    CONFIG_FEATURES: '/api/config/features',
    UPDATE_CONFIG_FEATURE_VALUE: '/api/config/features/value',
    ARTIFACT_APPS: '/api/v1/admin/artifacts/apps',
    ARTIFACT_APP: (appId: string) => `/api/v1/admin/artifacts/apps/${appId}`,
    ARTIFACT_APP_VERSIONS: (appId: string) => `/api/v1/admin/artifacts/apps/${appId}/versions`,
    ARTIFACT_APP_PUBLISH: (appId: string) => `/api/v1/admin/artifacts/apps/${appId}/publish`
  },
  PROJECT: {
    LIST: '/api/projects',
    IMPORT: '/api/projects/import',
    CREATE_DRAFT: '/api/projects/draft',
    GET: (id: string) => `/api/projects/${id}`,
    GET_DRAFT_TAB: (id: string) => `/api/projects/${id}/draft-tab`,
    UPDATE_DRAFT: (id: string) => `/api/projects/${id}/draft`,
    PATCH_DRAFT_TAB: (id: string) => `/api/projects/${id}/draft-tab`,
    DELETE: (id: string) => `/api/projects/${id}`,
    GENERATE: (id: string) => `/api/projects/${id}/generate`,
    RETRY_STAGE: (id: string) => `/api/projects/${id}/retry-stage`,
    COLLABORATION: (id: string) => `/api/projects/${id}/collaboration`,
    COLLABORATION_REQUESTS: (id: string) => `/api/projects/${id}/collaboration/requests`,
    REVIEW_COLLABORATION_REQUEST: (id: string, requestId: string) => `/api/projects/${id}/collaboration/requests/${requestId}`,
    COLLABORATION_INVITE: (token: string) => `/api/projects/collaboration/invites/${token}`,
    REQUEST_COLLABORATION: (token: string) => `/api/projects/collaboration/invites/${token}/requests`,
    ARCHIVED_COLLABORATIONS: '/api/projects/collaboration/archived',
    RESUBSCRIBE_ARCHIVED_COLLABORATION: (contributorId: string) => `/api/projects/collaboration/archived/${contributorId}/resubscribe`,
    REGISTER_PRESENCE: (id: string) => `/api/projects/${id}/collaboration/presence`,
    HEARTBEAT_PRESENCE: (id: string, sessionId: string) => `/api/projects/${id}/collaboration/presence/${sessionId}`,
    RECORD_ACTION: (id: string) => `/api/projects/${id}/collaboration/actions`,
    UPDATE_CONTRIBUTOR_PERMISSIONS: (projectId: string, contributorId: string) => `/api/projects/${projectId}/contributors/${contributorId}/permissions`,
    DETACH_CONTRIBUTOR: (projectId: string) => `/api/projects/${projectId}/contributors/detach`,
    TAB_DETAILS: '/api/projects/tab-details'
  },
  RUN: {
    LIST_BY_PROJECT: (projectId: string) => `/api/runs/project/${projectId}`,
    DOWNLOAD: (runId: string) => `/api/runs/${runId}/download`
  },
  PROJECT_VIEW: {
    GENERATE_ZIP: '/api/project-view/generate-zip'
  },
  AI_LABS: {
    AVAILABILITY: '/api/ai-labs/availability',
    JOBS: '/api/ai-labs/jobs',
    JOB: (jobId: string) => `/api/ai-labs/jobs/${jobId}`,
    JOB_EVENTS: (jobId: string) => `/api/ai-labs/jobs/${jobId}/events`
  },
  ANALYTICS: {
    TRACK_HOME_VISIT: '/api/analytics/visits/home'
  },
  NEWSLETTER: {
    SUBSCRIBE: '/api/newsletter/subscriptions'
  },
  DEPENDENCIES: {
    LIST: '/api/openapi/dependencies'
  }
};

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503
};

export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your internet connection.',
  TIMEOUT_ERROR: 'Request timeout. Please try again.',
  UNAUTHORIZED: 'Session expired. Please login again.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  SERVER_ERROR: 'An error occurred. Please try again later.',
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.'
};

export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_DATA: 'user_data',
  THEME: 'theme_preference',
  LANGUAGE: 'language_preference'
};
