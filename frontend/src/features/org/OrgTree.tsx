import React from 'react'
import { useGetOrgUnits, useCreateOrgUnit, useRenameOrgUnit, useDeleteOrgUnit, OrgUnit } from './api/org.api'

function buildTree(items: OrgUnit[]) {
  const map = new Map<string, { node: OrgUnit; children: OrgUnit[] }>()
  items.forEach((it) => map.set(it.id, { node: it, children: [] }))
  const roots: { node: OrgUnit; children: OrgUnit[] }[] = []
  for (const { node } of map.values()) {
    if (node.parentId) {
      const parent = map.get(node.parentId)
      if (parent) parent.children.push(node)
      else roots.push({ node, children: [] })
    } else {
      roots.push(map.get(node.id)!)
    }
  }
  return roots
}

export default function OrgTree() {
  const { data = [], isLoading, isError } = useGetOrgUnits()
  const create = useCreateOrgUnit()
  const rename = useRenameOrgUnit()
  const remove = useDeleteOrgUnit()

  if (isLoading) return <div>Loading org units...</div>
  if (isError) return <div>Error loading org units</div>

  const roots = buildTree(data)

  const handleAddRoot = async () => {
    const title = window.prompt('New root org unit title')
    if (!title) return
    const code = window.prompt('Optional code for unit')
    create.mutate({ title, code: code || undefined, parentId: null })
  }

  const renderNode = (node: OrgUnit, children: OrgUnit[], level = 0) => (
    <li key={node.id} style={{ marginLeft: level * 16, listStyle: 'none', marginTop: 6 }}>
      <div>
        <strong>{node.title}</strong> {node.code ? <small>({node.code})</small> : null}
        <button aria-label={`add-child-${node.id}`} style={{ marginLeft: 8 }} onClick={() => {
          const title = window.prompt('Child title')
          if (!title) return
          const code = window.prompt('Optional code for child')
          create.mutate({ title, code: code || undefined, parentId: node.id })
        }}>Add child</button>
        <button aria-label={`rename-${node.id}`} style={{ marginLeft: 6 }} onClick={() => {
          const title = window.prompt('New title', node.title)
          if (!title) return
          rename.mutate({ id: node.id, title })
        }}>Rename</button>
        <button aria-label={`delete-${node.id}`} style={{ marginLeft: 6 }} onClick={() => {
          const msg = (children && children.length > 0)
            ? `This unit has ${children.length} child(ren). Deleting will reparent children to parent. Confirm delete ${node.title}?`
            : `Confirm delete ${node.title}?`
          if (!window.confirm(msg)) return
          remove.mutate(node.id)
        }}>Delete</button>
      </div>
      {children && children.length > 0 && (
        <ul style={{ marginTop: 6 }}>
          {children.map((c) => renderNode(c, (data.filter((d) => d.parentId === c.id) || []), level + 1))}
        </ul>
      )}
    </li>
  )

  return (
    <div>
      <h2>Organization Units</h2>
      <div style={{ marginBottom: 8 }}>
        <button onClick={handleAddRoot}>Add root unit</button>
      </div>

      <ul>
        {roots.map((r) => renderNode(r.node, r.children))}
      </ul>
    </div>
  )
}
