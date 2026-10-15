import React from 'react';

export default function Services({ skills }) {
  return (
    <div className="p-4">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-400 mb-3">
        Services ({skills.length})
      </h2>
      {skills.length === 0 && (
        <p className="text-gray-500 text-sm">No services found.</p>
      )}
      <ul className="space-y-2">
        {skills.map((s, i) => (
          <li
            key={i}
            className="rounded-lg border border-gray-800 bg-gray-950 px-3 py-2.5"
          >
            <p className="text-sm font-medium text-emerald-400">{s.name}</p>
            {s.description && (
              <p className="text-xs text-gray-400 mt-0.5">{s.description}</p>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
