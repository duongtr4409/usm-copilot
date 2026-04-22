import React from 'react'
import { test, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddStudentForm from '../features/classes/AddStudentForm'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { server } from '../mocks/server'
import { rest } from 'msw'

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
    </MemoryRouter>
  )
}

test('adds student successfully (201)', async () => {
  renderWithProviders(<AddStudentForm classId="class-1-uuid" />)

  const user = userEvent.setup()
  await user.type(screen.getByLabelText(/Username/i), 'newstudent')
  await user.type(screen.getByLabelText(/Initial Password/i), 'pass123')
  await user.type(screen.getByLabelText(/First name/i), 'John')
  await user.type(screen.getByLabelText(/Last name/i), 'Doe')

  await user.click(screen.getByRole('button', { name: /Add Student/i }))

  await screen.findByText(/Student added successfully/i)
})

test('handles 409 conflict when username exists', async () => {
  renderWithProviders(<AddStudentForm classId="class-1-uuid" />)

  const user = userEvent.setup()
  await user.type(screen.getByLabelText(/Username/i), 'existing')
  await user.type(screen.getByLabelText(/Initial Password/i), 'pass123')
  await user.type(screen.getByLabelText(/First name/i), 'Jane')
  await user.type(screen.getByLabelText(/Last name/i), 'Smith')

  await user.click(screen.getByRole('button', { name: /Add Student/i }))

  await screen.findByText(/username already exists/i)
})
