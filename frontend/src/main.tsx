import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'

// Auto-start MSW in dev for local development
if (import.meta.env.DEV) {
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  import('./mocks/browser').then(({ worker }) => worker.start())
}

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>
)
