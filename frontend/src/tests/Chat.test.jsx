import React from 'react';
import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Chat from '../components/Chat.jsx';

describe('Chat', () => {
  it('shows an empty prompt when there are no messages', () => {
    render(<Chat messages={[]} onSend={vi.fn()} />);
    expect(screen.getByText(/say something to hermes/i)).toBeInTheDocument();
  });

  it('renders message history with roles', () => {
    render(
      <Chat
        messages={[
          { role: 'user', content: 'hello' },
          { role: 'assistant', content: 'hi there' }
        ]}
        onSend={vi.fn()}
      />
    );
    expect(screen.getAllByRole('chat-message')).toHaveLength(2);
    expect(screen.getByText('hi there')).toBeInTheDocument();
  });

  it('sends typed messages and clears the input', async () => {
    const onSend = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    render(<Chat messages={[]} onSend={onSend} />);
    const input = screen.getByLabelText(/message/i);
    await user.type(input, 'ping');
    await user.click(screen.getByRole('button', { name: /send/i }));
    await waitFor(() => expect(onSend).toHaveBeenCalledWith('ping'));
    expect(input.value).toBe('');
  });

  it('does not send blank messages', async () => {
    const onSend = vi.fn();
    const user = userEvent.setup();
    render(<Chat messages={[]} onSend={onSend} />);
    await user.type(screen.getByLabelText(/message/i), '   ');
    await user.click(screen.getByRole('button', { name: /send/i }));
    expect(onSend).not.toHaveBeenCalled();
  });
});
