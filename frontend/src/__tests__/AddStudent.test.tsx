import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddStudentForm from '../features/classes/AddStudentForm'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { server } from '../mocks/server'
import { rest } from 'msw'

const queryClient = new QueryClient()

function renderWithProviders(ui: React.ReactElement) {
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

test('adds student successfully (201)', async () => {
  renderWithProviders(<AddStudentForm classId="class-1-uuid" />)

  userEvent.type(screen.getByLabelText(/Username/i), 'newstudent')
  userEvent.type(screen.getByLabelText(/Initial Password/i), 'pass123')
  userEvent.type(screen.getByLabelText(/First name/i), 'John')
  userEvent.type(screen.getByLabelText(/Last name/i), 'Doe')

  userEvent.click(screen.getByRole('button', { name: /Add Student/i }))

  await waitFor(() => expect(screen.getByText(/Student added successfully/i)).toBeInTheDocument())
})

test('handles 409 conflict when username exists', async () => {
  renderWithProviders(<AddStudentForm classId="class-1-uuid" />)

  userEvent.type(screen.getByLabelText(/Username/i), 'existing')
  userEvent.type(screen.getByLabelText(/Initial Password/i), 'pass123')
  userEvent.type(screen.getByLabelText(/First name/i), 'Jane')
  userEvent.type(screen.getByLabelText(/Last name/i), 'Smith')

  userEvent.click(screen.getByRole('button', { name: /Add Student/i }))

  await waitFor(() => expect(screen.getByText(/username already exists/i)).toBeInTheDocument())
})
