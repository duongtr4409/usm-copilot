import React from 'react'
import { test, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import OrgUnitsPage from '../features/org/OrgUnitsPage'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'

const queryClient = new QueryClient()

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
    </MemoryRouter>
  )
}

test('loads and displays org units and allows creating new unit', async () => {
  renderWithProviders(<OrgUnitsPage />)

  await waitFor(() => expect(screen.getByText(/Organization Units/i)).toBeInTheDocument())

  // existing root from mock should be visible (may appear in multiple places)
  await waitFor(() => expect(screen.getAllByText(/Root Unit/i).length).toBeGreaterThan(0))

  // create a new unit
  const user = userEvent.setup()
  await user.type(screen.getByPlaceholderText(/Name/i), 'New Dept')
  await user.click(screen.getByRole('button', { name: /Create/i }))

  const matches = await screen.findAllByText(/New Dept/i)
  expect(matches.length).toBeGreaterThan(0)
})
