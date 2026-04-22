import React from 'react'
import { test, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ClassPicker from '../features/classes/ClassPicker'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { server } from '../mocks/server'
import { rest } from 'msw'
import { vi } from 'vitest'

const API_BASE = 'http://localhost:8080/api/v1'

function renderWithClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

test('loads classes and calls onChange when selection changes', async () => {
  const handleChange = vi.fn()

  renderWithClient(<ClassPicker value="" onChange={handleChange} />)

  // shows loading first
  expect(screen.getByText(/Loading classes.../i)).toBeInTheDocument()

  // then the select appears with options from the mock
  await waitFor(() => expect(screen.getByRole('combobox', { name: /Class picker/i })).toBeInTheDocument())

  const select = screen.getByRole('combobox', { name: /Class picker/i }) as HTMLSelectElement
  expect(screen.getByRole('option', { name: /Class 1/i })).toBeInTheDocument()

  const user = userEvent.setup()
  await user.selectOptions(select, ['class-1-uuid'])
  expect(handleChange).toHaveBeenCalledWith('class-1-uuid')
})

test('shows error state when API fails', async () => {
  // make the classes endpoint return 500 for this test
  server.use(
    rest.get(`${API_BASE}/classes`, (req, res, ctx) => {
      return res(ctx.status(500))
    })
  )

  renderWithClient(<ClassPicker value="" onChange={() => {}} />)

  await waitFor(() => expect(screen.getByText(/Error loading classes/i)).toBeInTheDocument())
})
