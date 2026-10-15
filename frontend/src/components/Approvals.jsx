import React, { useEffect, useState } from 'react';
import { decideAccount, pendingAccounts } from '../api.js';

export default function Approvals({ onChanged }) {
  const [pending, setPending] = useState([]);
  const [error, setError] = useState('');

  const refresh = () => {
    pendingAccounts()
      .then(setPending)
      .catch((e) => setError(e.message));
  };

  useEffect(refresh, []);

  const decide = async (username, approve) => {
    setError('');
    try {
      await decideAccount(username, approve);
      refresh();
      if (onChanged) onChanged();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div className="p-4 border-b border-gray-800">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-400 mb-3">
        Account approvals ({pending.length})
      </h2>
      {error && <p role="alert" className="text-xs text-red-400 mb-2">{error}</p>}
      {pending.length === 0 && <p className="text-gray-500 text-sm">No pending accounts.</p>}
      <ul className="space-y-2">
        {pending.map((a, i) => (
          <li key={i} className="rounded-lg border border-gray-800 bg-gray-950 px-3 py-2.5 flex items-center justify-between gap-2">
            <span className="text-sm text-gray-100">{a.username}</span>
            <span className="flex gap-1.5">
              <button
                onClick={() => decide(a.username, true)}
                className="rounded-md bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold px-2.5 py-1"
              >
                Approve
              </button>
              <button
                onClick={() => decide(a.username, false)}
                className="rounded-md border border-gray-700 hover:bg-gray-800 text-gray-300 text-xs px-2.5 py-1"
              >
                Reject
              </button>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
