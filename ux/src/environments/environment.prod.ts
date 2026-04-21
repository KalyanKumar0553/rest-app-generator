const urlPrefix = `https://bootrid.com`;

export const environment = {
  production: true,
  API_ENDPOINT: `${urlPrefix}/api`,
  apiUrl: `${urlPrefix}/api`,
  baseUrl: `${urlPrefix}/api`,
  authBaseUrl: urlPrefix,
  // Replace with your real Cloudflare Turnstile site key
  turnstileSiteKey: 'YOUR_TURNSTILE_SITE_KEY_HERE'
};
