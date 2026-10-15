import React, { useState } from 'react';
import { getToken, setToken, login as apiLogin } from './api.js';
import Login from './components/Login.jsx';
import Dashboard from './components/Dashboard.jsx';

export default function App() {
  const [authed, setAuthed] = useState(Boolean(getToken()));
  const [username, setUsername] = useState('');
  const [host, setHost] = useState(false);

  const handleLogin = async (user, password) => {
    const res = await apiLogin(user, password);
    setToken(res.token);
    setUsername(res.username);
    setHost(Boolean(res.host));
    setAuthed(true);
  };

  const handleLogout = () => {
    setToken(null);
    setAuthed(false);
  };

  if (!authed) return <Login onLogin={handleLogin} />;
  return <Dashboard username={username} host={host} onLogout={handleLogout} />;
}
