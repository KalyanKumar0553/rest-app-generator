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
    SEND_OTP: '/api/v1/auth/otp/generate',
    VERIFY_OTP: '/api/v1/auth/otp/verify',
    FORGOT_PASSWORD: '/api/v1/auth/password/forgot',
    RESET_PASSWORD_WITH_OTP: '/api/v1/auth/password/reset',
    ROLES: '/api/v1/auth/roles',
    GOOGLE_OAUTH_START: '/oauth2/authorization/google'
  },
  USER: {
    PROFILE: '/api/user/profile',
    UPDATE_PROFILE: '/api/user/profile/update'
  },
  PROJECT: {
    LIST: '/api/projects',
    CREATE: '/api/projects',
    GET: (id: string) => `/api/projects/${id}`,
    UPDATE: (id: string) => `/api/projects/${id}/update`,
    DELETE: (id: string) => `/api/projects/${id}/delete`
  },
  RUN: {
    LIST_BY_PROJECT: (projectId: string) => `/api/runs/project/${projectId}`,
    DOWNLOAD: (runId: string) => `/api/runs/${runId}/download`
  },
  PROJECT_VIEW: {
    GENERATE_ZIP: '/api/project-view/generate-zip'
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
