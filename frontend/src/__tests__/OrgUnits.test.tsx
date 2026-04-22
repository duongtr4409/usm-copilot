import React from 'react'
import { test, expect } from 'vitest'
import { render, screen, waitFor, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import OrgUnitsPage from '../features/org/OrgUnitsPage'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'

function renderWithProviders(ui: React.ReactElement) {
  // create a fresh QueryClient per render to isolate test state
  const qc = new QueryClient()
  return render(
    <MemoryRouter>
      <QueryClientProvider client={qc}>{ui}</QueryClientProvider>
    </MemoryRouter>
  )
}

test('loads and displays org units and allows creating new unit', async () => {
  renderWithProviders(<OrgUnitsPage />)

  await screen.findByText(/Organization Units/i)

  // existing root from mock should be visible (may appear in multiple places)
  const roots = await screen.findAllByText(/Root Unit/i)
  expect(roots.length).toBeGreaterThan(0)

  // create a new unit
  const user = userEvent.setup()
  await act(async () => {
    await user.type(screen.getByPlaceholderText(/Name/i), 'New Dept')
    await user.click(screen.getByRole('button', { name: /Create/i }))
  })

  const matches = await screen.findAllByText(/New Dept/i)
  expect(matches.length).toBeGreaterThan(0)
})
