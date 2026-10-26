// Scalar plugin: attaches the Spring Security CSRF token to state-changing requests sent from the docs.
// The token is fetched per request because Spring rotates it on login and logout.
const CSRF_TOKEN_URL = new URL('../../v1/auth/csrf-token', import.meta.url);
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

async function fetchCsrfToken() {
  const response = await fetch(CSRF_TOKEN_URL, { credentials: 'same-origin' });
  if (!response.ok) {
    throw new Error(`CSRF token request failed with status ${response.status}`);
  }
  const { data } = await response.json();
  return data;
}

// Runs on the final Request (after Scalar applies the csrfToken auth field), so the header is
// replaced rather than appended to whatever Scalar put there.
export default () => ({
  name: 'instello-csrf',
  extensions: [],
  apiClientPlugins: [
    {
      hooks: {
        requestBuilt: async (payload) => {
          const { request } = payload;
          if (SAFE_METHODS.has(request.method) || new URL(request.url).origin !== CSRF_TOKEN_URL.origin) {
            return payload;
          }
          const { token, headerName } = await fetchCsrfToken();
          request.headers.set(headerName, token);
          return payload;
        },
      },
    },
  ],
});
