const getBaseUrl = (): string => {
  if (typeof window !== 'undefined') {
    return `${window.location.protocol}//${window.location.hostname}${window.location.port ? ':' + window.location.port : ''}`;
  }
  return 'http://localhost:8080';
};

export const environment = {
  production: false,
  API_ENDPOINT: `${getBaseUrl()}/api`,
  apiUrl: `${getBaseUrl()}/api`,
  baseUrl: getBaseUrl(),
  useMockApi: false
};
