const TOKEN_KEY = 'hermes_dash_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  if (res.status === 401) {
    setToken(null);
    throw new Error('unauthorized');
  }
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error || `request failed (${res.status})`);
  }
  return data;
}

export function login(username, password) {
  return request('/api/auth/login', { method: 'POST', body: { username, password } })
    .then((res) => {
      setToken(res.token);
      return res;
    });
}

export function register(username, password) {
  return request('/api/auth/register', { method: 'POST', body: { username, password } });
}

export function pendingAccounts() {
  return request('/api/auth/pending');
}

export function decideAccount(username, approve) {
  return request('/api/auth/decide', { method: 'POST', body: { username, approve } });
}

export function sendMessage(message) {
  return request('/api/chat/send', { method: 'POST', body: { message } });
}

export function history() {
  return request('/api/chat/history');
}

export function services() {
  return request('/api/services');
}
