import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from '../../../lib/api'

export type OrgUnit = {
  id: string
  title: string
  code?: string
  parentId?: string | null
}

const QUERY_KEY = ['orgUnits'] as const

export const useGetOrgUnits = () =>
  useQuery({ queryKey: QUERY_KEY, queryFn: () => api.get<OrgUnit[]>('/org-units').then((r) => r.data) })

export const useCreateOrgUnit = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: { title: string; code?: string; parentId?: string | null }) =>
      api.post<OrgUnit>('/org-units', data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: QUERY_KEY }),
  })
}

export const useRenameOrgUnit = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: { id: string; title?: string; code?: string }) =>
      api.put<OrgUnit>(`/org-units/${data.id}`, data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: QUERY_KEY }),
  })
}

export const useDeleteOrgUnit = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => api.delete(`/org-units/${id}`).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: QUERY_KEY }),
  })
}
