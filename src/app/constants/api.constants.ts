import { environment } from '../../environments/environment';

export const API_CONFIG = {
  BASE_URL: environment.baseUrl,
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000
};

export const API_ENDPOINTS = {
  AUTH: {
    SIGNUP: '/api/auth/signup',
    LOGIN: '/api/auth/login',
    LOGOUT: '/api/auth/logout',
    REFRESH_TOKEN: '/api/auth/refresh-token',
    SEND_OTP: '/api/auth/send-otp',
    VERIFY_OTP: '/api/auth/verify-otp',
    FORGOT_PASSWORD: '/api/auth/forgot-password',
    RESET_PASSWORD_WITH_OTP: '/api/auth/reset-password-with-otp'
  },
  USER: {
    PROFILE: '/api/user/profile',
    UPDATE_PROFILE: '/api/user/profile/update'
  },
  PROJECT: {
    LIST: '/api/projects',
    CREATE: '/api/projects/create',
    GET: (id: string) => `/api/projects/${id}`,
    UPDATE: (id: string) => `/api/projects/${id}/update`,
    DELETE: (id: string) => `/api/projects/${id}/delete`
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
