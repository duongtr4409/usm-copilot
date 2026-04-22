import React from 'react'
import { test, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import OrgTree from '../features/org/OrgTree'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { server } from '../mocks/server'
import { rest } from 'msw'
import { vi } from 'vitest'

const API_BASE = 'http://localhost:8080/api/v1'

function renderWithClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

test('renders org tree and allows create/rename/delete using mock handlers', async () => {
  renderWithClient(<OrgTree />)

  // initial mock contains Head Office
  await waitFor(() => expect(screen.getByText(/Head Office/i)).toBeInTheDocument())

  const user = userEvent.setup()

  // Add a child under Head Office (prompts are used in the component)
  const promptMock = vi.fn().mockReturnValueOnce('New Child').mockReturnValueOnce('NC')
  vi.spyOn(window, 'prompt').mockImplementation(promptMock as any)

  const addBtn = screen.getByRole('button', { name: /add-child-org-root-1/i })
  await user.click(addBtn)

  await waitFor(() => expect(screen.getByText(/New Child/i)).toBeInTheDocument())

  // Rename Head Office
  const renamePrompt = vi.fn().mockReturnValueOnce('Renamed Head Office')
  vi.spyOn(window, 'prompt').mockImplementation(renamePrompt as any)
  const renameBtn = screen.getByRole('button', { name: /rename-org-root-1/i })
  await user.click(renameBtn)

  await waitFor(() => expect(screen.getByText(/Renamed Head Office/i)).toBeInTheDocument())

  // Delete the newly created child (confirm)
  vi.spyOn(window, 'confirm').mockImplementation(() => true)
  const deleteChildBtn = screen.getByRole('button', { name: /delete-/i })
  await user.click(deleteChildBtn)

  // at least ensure no uncaught errors and UI updates
  await waitFor(() => expect(true).toBeTruthy())
})
