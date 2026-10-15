import React, { useEffect, useState } from 'react';
import { history, services, sendMessage } from '../api.js';
import Chat from './Chat.jsx';
import Services from './Services.jsx';
import Approvals from './Approvals.jsx';

export default function Dashboard({ username, host, onLogout }) {
  const [messages, setMessages] = useState([]);
  const [skills, setSkills] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    history()
      .then(setMessages)
      .catch((e) => setError(e.message));
    services()
      .then(setSkills)
      .catch(() => {});
  }, []);

  const send = async (text) => {
    // optimistic: show the user's message right away
    setMessages((m) => [...m, { role: 'user', content: text, createdAt: null }]);
    try {
      const reply = await sendMessage(text);
      setMessages((m) => [...m, reply]);
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 flex flex-col">
      <header className="border-b border-gray-800 bg-gray-900 px-4 py-3 flex items-center justify-between">
        <h1 className="font-bold text-white">Hermes Dashboard</h1>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-gray-400">{username}</span>
          <button onClick={onLogout} className="rounded-lg border border-gray-700 px-3 py-1 hover:bg-gray-800">
            Logout
          </button>
        </div>
      </header>

      {error && <p role="alert" className="bg-red-900/50 text-red-200 text-sm px-4 py-2">{error}</p>}

      <main className="flex-1 grid md:grid-cols-[1fr_320px] gap-0">
        <section className="min-h-0">
          <Chat messages={messages} onSend={send} />
        </section>
        <aside className="border-l border-gray-800 bg-gray-900 overflow-y-auto">
          {host && <Approvals />}
          <Services skills={skills} />
        </aside>
      </main>
    </div>
  );
}
