import React from 'react'
import { useQuery } from '@tanstack/react-query'
import api from '../../lib/api'

type ClassItem = {
  id: string
  code?: string
  title?: string
}

export default function ClassPicker({
  value,
  onChange,
}: {
  value?: string
  onChange: (id: string) => void
}) {
  const { data = [], isLoading, isError } = useQuery({ queryKey: ['classes'], queryFn: () => api.get<ClassItem[]>('/classes').then(r => r.data) })

  if (isLoading) return <div>Loading classes...</div>
  if (isError) return <div>Error loading classes</div>

  return (
    <select aria-label="Class picker" value={value || ''} onChange={(e) => onChange(e.target.value)}>
      <option value="">Select class</option>
      {data.map((c) => (
        <option key={c.id} value={c.id}>
          {c.title || c.code || c.id}
        </option>
      ))}
    </select>
  )
}
