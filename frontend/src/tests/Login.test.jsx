import React from 'react';
import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from '../components/Login.jsx';

vi.mock('../api.js', () => ({
  register: vi.fn()
}));

import { register } from '../api.js';

describe('Login', () => {
  it('renders username and password fields', () => {
    render(<Login onLogin={vi.fn()} />);
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('calls onLogin with credentials', async () => {
    const onLogin = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    render(<Login onLogin={onLogin} />);
    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.type(screen.getByLabelText(/password/i), 'secret');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => expect(onLogin).toHaveBeenCalledWith('admin', 'secret'));
  });

  it('shows an error on failure', async () => {
    const onLogin = vi.fn().mockRejectedValue(new Error('unauthorized'));
    const user = userEvent.setup();
    render(<Login onLogin={onLogin} />);
    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.type(screen.getByLabelText(/password/i), 'bad');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent(/invalid/i));
  });

  it('register mode creates the account and shows the approval notice', async () => {
    register.mockResolvedValue('account created — waiting for host approval');
    const user = userEvent.setup();
    render(<Login onLogin={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: /need an account/i }));
    await user.type(screen.getByLabelText(/username/i), 'newbie');
    await user.type(screen.getByLabelText(/password/i), 'pw');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => expect(register).toHaveBeenCalledWith('newbie', 'pw'));
    await waitFor(() =>
      expect(screen.getByRole('status')).toHaveTextContent(/waiting for the host/i));
  });

  it('duplicate username shows a taken error', async () => {
    register.mockRejectedValue(new Error('username already taken'));
    const user = userEvent.setup();
    render(<Login onLogin={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: /need an account/i }));
    await user.type(screen.getByLabelText(/username/i), 'bob');
    await user.type(screen.getByLabelText(/password/i), 'pw');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent(/taken/i));
  });
});
