import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import OrgUnitsPage from '../features/org/OrgUnitsPage'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient()

function renderWithProviders(ui: React.ReactElement) {
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

test('loads and displays org units and allows creating new unit', async () => {
  renderWithProviders(<OrgUnitsPage />)

  await waitFor(() => expect(screen.getByText(/Organization Units/i)).toBeInTheDocument())

  // existing root from mock should be visible
  await waitFor(() => expect(screen.getByText(/Root Unit/i)).toBeInTheDocument())

  // create a new unit
  userEvent.type(screen.getByPlaceholderText(/Name/i), 'New Dept')
  userEvent.click(screen.getByRole('button', { name: /Create/i }))

  await waitFor(() => expect(screen.getByText(/New Dept/i)).toBeInTheDocument())
})
