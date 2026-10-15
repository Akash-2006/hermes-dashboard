import React from 'react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Approvals from '../components/Approvals.jsx';

vi.mock('../api.js', () => ({
  pendingAccounts: vi.fn(),
  decideAccount: vi.fn()
}));

import { pendingAccounts, decideAccount } from '../api.js';

describe('Approvals (host panel)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('lists pending accounts', async () => {
    pendingAccounts.mockResolvedValue([{ username: 'bob' }, { username: 'alice' }]);
    render(<Approvals />);
    await waitFor(() => expect(screen.getByText('bob')).toBeInTheDocument());
    expect(screen.getByText(/account approvals \(2\)/i)).toBeInTheDocument();
  });

  it('shows empty state', async () => {
    pendingAccounts.mockResolvedValue([]);
    render(<Approvals />);
    await waitFor(() => expect(screen.getByText(/no pending accounts/i)).toBeInTheDocument());
  });

  it('approve button calls decideAccount(username, true) and refreshes', async () => {
    pendingAccounts.mockResolvedValue([{ username: 'bob' }]);
    decideAccount.mockResolvedValue({});
    const user = userEvent.setup();
    render(<Approvals />);
    await waitFor(() => expect(screen.getByText('bob')).toBeInTheDocument());
    await user.click(screen.getByRole('button', { name: /approve/i }));
    await waitFor(() => expect(decideAccount).toHaveBeenCalledWith('bob', true));
    await waitFor(() => expect(pendingAccounts).toHaveBeenCalledTimes(2));
  });

  it('reject button calls decideAccount(username, false)', async () => {
    pendingAccounts.mockResolvedValue([{ username: 'bob' }]);
    decideAccount.mockResolvedValue({});
    const user = userEvent.setup();
    render(<Approvals />);
    await waitFor(() => expect(screen.getByText('bob')).toBeInTheDocument());
    await user.click(screen.getByRole('button', { name: /reject/i }));
    await waitFor(() => expect(decideAccount).toHaveBeenCalledWith('bob', false));
  });
});
