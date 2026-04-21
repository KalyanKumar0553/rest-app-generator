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
  authBaseUrl: getBaseUrl(),
  // Cloudflare Turnstile — "always passes" test key for development
  // Replace with your real site key before going to production
  turnstileSiteKey: '1x00000000000000000000AA'
};
