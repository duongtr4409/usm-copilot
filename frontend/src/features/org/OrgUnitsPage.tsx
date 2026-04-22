import React, { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../../lib/api'

type OrgUnit = {
  id: string
  name: string
  code?: string
  parentId?: string | null
  type?: string
}

function buildTree(items: OrgUnit[]) {
  const map = new Map<string, OrgUnit & { children?: any[] }>()
  items.forEach(item => map.set(item.id, { ...item, children: [] }))
  const roots: (OrgUnit & { children?: any[] })[] = []
  map.forEach(node => {
    if (node.parentId) {
      const parent = map.get(node.parentId)
      if (parent) parent.children!.push(node)
      else roots.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

export default function OrgUnitsPage() {
  const qc = useQueryClient()
  const { data = [], isLoading } = useQuery(['org-units'], () => api.get<OrgUnit[]>('/org-units').then(r => r.data))
  const [name, setName] = useState('')
  const [type, setType] = useState('Phòng')
  const [parentId, setParentId] = useState<string | ''>('')

  const tree = useMemo(() => buildTree(data), [data])

  const create = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name) return
    await api.post('/org-units', { name, type, parentId: parentId || undefined })
    setName('')
    setParentId('')
    qc.invalidateQueries(['org-units'])
  }

  const remove = async (id: string) => {
    try {
      await api.delete(`/org-units/${id}`)
      qc.invalidateQueries(['org-units'])
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to delete')
    }
  }

  const renderNode = (node: any, depth = 0) => (
    <div key={node.id} style={{ marginLeft: depth * 12, marginTop: 6 }}>
      <strong>{node.name}</strong> <small>({node.type})</small>
      <button onClick={() => remove(node.id)} style={{ marginLeft: 8 }}>Delete</button>
      {node.children?.map((c: any) => renderNode(c, depth + 1))}
    </div>
  )

  if (isLoading) return <div>Loading org units...</div>

  return (
    <div>
      <h2>Organization Units</h2>
      <form onSubmit={create} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} />
        <select value={type} onChange={e => setType(e.target.value)}>
          <option>Phòng</option>
          <option>Ban</option>
          <option>Văn Phòng</option>
          <option>Khoa</option>
          <option>Trung Tâm</option>
          <option>Lớp</option>
        </select>
        <select value={parentId} onChange={e => setParentId(e.target.value)}>
          <option value="">No parent</option>
          {data.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
        </select>
        <button type="submit">Create</button>
      </form>

      <div style={{ marginTop: 12 }}>
        {tree.length === 0 && <div>No organization units</div>}
        {tree.map((n: any) => renderNode(n))}
      </div>
    </div>
  )
}
