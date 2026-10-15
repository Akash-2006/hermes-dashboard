import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

describe('api', () => {
  let api;

  beforeEach(async () => {
    vi.resetModules();
    api = await import('../api.js');
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('login posts credentials and stores the token', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ token: 'tok', username: 'admin' }), { status: 200 })
    );
    vi.stubGlobal('fetch', fetchMock);

    const res = await api.login('admin', 'secret');
    expect(res.token).toBe('tok');
    expect(api.getToken()).toBe('tok');

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/auth/login');
    expect(init.method).toBe('POST');
    expect(JSON.parse(init.body)).toEqual({ username: 'admin', password: 'secret' });
  });

  it('sendMessage attaches the bearer token', async () => {
    api.setToken('tok');
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ role: 'assistant', content: 'reply' }), { status: 200 })
    );
    vi.stubGlobal('fetch', fetchMock);

    await api.sendMessage('hi');
    const [, init] = fetchMock.mock.calls[0];
    expect(init.headers.Authorization).toBe('Bearer tok');
  });

  it('register posts credentials without storing a token', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response('"ok"', { status: 200 })
    );
    vi.stubGlobal('fetch', fetchMock);

    await api.register('bob', 'pw');
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/auth/register');
    expect(init.method).toBe('POST');
    expect(JSON.parse(init.body)).toEqual({ username: 'bob', password: 'pw' });
    expect(api.getToken()).toBeNull();
  });

  it('decideAccount posts username and approve flag with bearer token', async () => {
    api.setToken('tok');
    const fetchMock = vi.fn().mockResolvedValue(new Response('{}', { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await api.decideAccount('bob', true);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/auth/decide');
    expect(init.headers.Authorization).toBe('Bearer tok');
    expect(JSON.parse(init.body)).toEqual({ username: 'bob', approve: true });
  });

  it('clears the token and throws on 401', async () => {
    api.setToken('tok');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{"error":"unauthorized"}', { status: 401 })));

    await expect(api.history()).rejects.toThrow('unauthorized');
    expect(api.getToken()).toBeNull();
  });

  it('throws on non-401 error status', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{"error":"boom"}', { status: 502 })));
    await expect(api.services()).rejects.toThrow('boom');
  });
});
