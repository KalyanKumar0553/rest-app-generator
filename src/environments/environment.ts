// const urlPrefix = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;

const urlPrefix = `http://localhost:8080`;

export const environment = {
  production: false,
  API_ENDPOINT: `${urlPrefix}/api`,
  apiUrl: `${urlPrefix}/api`,
  baseUrl: `${urlPrefix}/api`
};
