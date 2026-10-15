import React, { useEffect, useRef, useState } from 'react';

function bubbleClasses(role) {
  return role === 'user'
    ? 'bg-emerald-600 text-white ml-auto'
    : 'bg-gray-800 text-gray-100';
}

export default function Chat({ messages, onSend }) {
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length]);

  const submit = async (e) => {
    e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed || busy) return;
    setText('');
    setBusy(true);
    try {
      await onSend(trimmed);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-49px)]">
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {messages.length === 0 && (
          <p className="text-gray-500 text-center mt-10 text-sm">
            Say something to Hermes...
          </p>
        )}
        {messages.map((m, i) => (
          <div
            key={i}
            role="chat-message"
            className={`max-w-[85%] rounded-xl px-4 py-2.5 text-sm whitespace-pre-wrap ${bubbleClasses(m.role)}`}
          >
            {m.content}
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={submit} className="border-t border-gray-800 bg-gray-900 p-3 flex gap-2">
        <input
          aria-label="Message"
          className="flex-1 rounded-lg bg-gray-800 border border-gray-700 px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:border-emerald-500"
          placeholder={busy ? 'Hermes is thinking...' : 'Type a message'}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button
          type="submit"
          disabled={busy}
          className="rounded-lg bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold px-5"
        >
          {busy ? '...' : 'Send'}
        </button>
      </form>
    </div>
  );
}
