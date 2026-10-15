import React, { useState } from 'react';
import { register as apiRegister } from '../api.js';

export default function Login({ onLogin }) {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setNotice('');
    setBusy(true);
    try {
      if (mode === 'register') {
        await apiRegister(username.trim(), password);
        setNotice('Account created — waiting for the host to approve it.');
        setMode('login');
      } else {
        await onLogin(username.trim(), password);
      }
    } catch (err) {
      if (mode === 'register' && /taken/i.test(err.message)) {
        setError('That username is already taken.');
      } else {
        setError(err.message === 'unauthorized' ? 'Invalid username or password' : err.message);
      }
    } finally {
      setBusy(false);
    }
  };

  const isRegister = mode === 'register';

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-950 px-4">
      <form
        onSubmit={submit}
        className="w-full max-w-sm bg-gray-900 border border-gray-800 rounded-xl p-8 space-y-4"
      >
        <h1 className="text-2xl font-bold text-white text-center">Hermes</h1>
        <p className="text-sm text-gray-400 text-center">
          {isRegister ? 'Create an account (host must approve)' : 'Sign in to your dashboard'}
        </p>

        <input
          aria-label="Username"
          className="w-full rounded-lg bg-gray-800 border border-gray-700 px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:border-emerald-500"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          aria-label="Password"
          type="password"
          className="w-full rounded-lg bg-gray-800 border border-gray-700 px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:border-emerald-500"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
        {notice && <p role="status" className="text-sm text-emerald-400">{notice}</p>}

        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-lg bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold py-2.5"
        >
          {busy ? '...' : isRegister ? 'Create account' : 'Sign in'}
        </button>

        <button
          type="button"
          onClick={() => { setMode(isRegister ? 'login' : 'register'); setError(''); setNotice(''); }}
          className="w-full text-sm text-gray-400 hover:text-gray-200"
        >
          {isRegister ? '← Back to sign in' : 'Need an account? Create one'}
        </button>
      </form>
    </div>
  );
}
