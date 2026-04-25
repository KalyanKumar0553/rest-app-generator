export type ApiExceptionType = 'SPECIFIC' | 'GENERIC';

export interface ApiErrorResponse {
  status?: number;
  exceptionType?: ApiExceptionType | string;
  errorCode?: string;
  message?: string;
  userMessage?: string;
  errorMsg?: string;
  path?: string;
  timestamp?: string;
  traceId?: string;
  fieldErrors?: Record<string, string>;
}

export interface ParsedApiError {
  status: number;
  exceptionType: ApiExceptionType | 'UNKNOWN';
  errorCode: string;
  message: string;
  userMessage: string;
  fieldErrors: Record<string, string>;
}

export function parseApiError(error: any, genericFallback: string): ParsedApiError {
  const raw: ApiErrorResponse = error?.error ?? error?.originalError?.error ?? {};
  const exceptionType = normalizeExceptionType(raw.exceptionType);
  const message = trimmed(raw.message || raw.errorMsg || error?.message);
  const userMessage = trimmed(raw.userMessage || raw.errorMsg || raw.message || error?.message);
  const genericMessage = genericFallback || 'Unable to process the request.';

  return {
    status: Number(raw.status || error?.status || error?.originalError?.status || 0),
    exceptionType,
    errorCode: trimmed(raw.errorCode) || 'UNKNOWN_ERROR',
    message: message || genericMessage,
    userMessage: exceptionType === 'SPECIFIC' ? (userMessage || genericMessage) : genericMessage,
    fieldErrors: raw.fieldErrors && typeof raw.fieldErrors === 'object' ? raw.fieldErrors : {}
  };
}

export function getApiUserMessage(error: any, genericFallback: string): string {
  return parseApiError(error, genericFallback).userMessage;
}

function normalizeExceptionType(value: unknown): ApiExceptionType | 'UNKNOWN' {
  const normalized = trimmed(String(value ?? '')).toUpperCase();
  if (normalized === 'SPECIFIC' || normalized === 'GENERIC') {
    return normalized;
  }
  return 'UNKNOWN';
}

function trimmed(value: string): string {
  return typeof value === 'string' ? value.trim() : '';
}
