import React from 'react';
import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import Services from '../components/Services.jsx';

describe('Services', () => {
  it('lists skills with name and description', () => {
    render(
      <Services
        skills={[
          { name: 'pdf-tools', description: 'Merge and split PDFs' },
          { name: 'web-search', description: '' }
        ]}
      />
    );
    expect(screen.getByText('pdf-tools')).toBeInTheDocument();
    expect(screen.getByText('Merge and split PDFs')).toBeInTheDocument();
    expect(screen.getByText(/services \(2\)/i)).toBeInTheDocument();
  });

  it('shows empty state without skills', () => {
    render(<Services skills={[]} />);
    expect(screen.getByText(/no services found/i)).toBeInTheDocument();
  });
});
