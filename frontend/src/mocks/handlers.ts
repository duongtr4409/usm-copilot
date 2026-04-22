import { rest } from 'msw'
import { v4 as uuidv4 } from 'uuid'

const API_BASE = (import.meta.env.VITE_API_BASE as string) || 'http://localhost:8080/api/v1'
// In-memory org-units store for local/mock behavior when backend endpoints are not available.
// TODO: Remove this mock once the backend exposes the org-units CRUD endpoints
let orgUnits: Array<{ id: string; title: string; code?: string; parentId?: string | null }> = [
  { id: 'org-root-1', title: 'Head Office', code: 'HO', parentId: null },
  { id: 'org-dept-1', title: 'Department A', code: 'DPT-A', parentId: 'org-root-1' },
  { id: 'org-root-2', title: 'Branch 1', code: 'BR1', parentId: null },
]

export const handlers = [
  // login
  rest.post(`${API_BASE}/auth/login`, async (req, res, ctx) => {
    const body = await req.json()
    const { username } = body
    const role = username?.toLowerCase().includes('admin')
      ? 'ADMIN'
      : username?.toLowerCase().includes('classadmin')
      ? 'CLASS_ADMIN'
      : 'STUDENT'

    return res(
      ctx.status(200),
      ctx.json({
        accessToken: 'mock-token-' + uuidv4(),
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: { id: uuidv4(), firstName: 'Test', lastName: username || 'User', role },
      })
    )
  }),

  // list classes (simple)
  rest.get(`${API_BASE}/classes`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json([
        { id: 'class-1-uuid', code: 'C101', title: 'Class 1' },
        { id: 'class-2-uuid', code: 'C102', title: 'Class 2' },
      ])
    )
  }),

  // Org-units mock CRUD (client-side mock while backend is missing)
  rest.get(`${API_BASE}/org-units`, (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(orgUnits))
  }),

  rest.post(`${API_BASE}/org-units`, async (req, res, ctx) => {
    const body = await req.json()
    const newUnit = { id: uuidv4(), title: body?.title || 'Untitled', code: body?.code, parentId: body?.parentId ?? null }
    orgUnits.push(newUnit)
    return res(ctx.status(201), ctx.json(newUnit))
  }),

  rest.put(`${API_BASE}/org-units/:id`, async (req, res, ctx) => {
    const { id } = req.params as { id: string }
    const body = await req.json()
    const u = orgUnits.find((x) => x.id === id)
    if (!u) return res(ctx.status(404))
    u.title = body?.title ?? u.title
    u.code = body?.code ?? u.code
    if (body && Object.prototype.hasOwnProperty.call(body, 'parentId')) u.parentId = body.parentId
    return res(ctx.status(200), ctx.json(u))
  }),

  rest.delete(`${API_BASE}/org-units/:id`, (req, res, ctx) => {
    const { id } = req.params as { id: string }
    const node = orgUnits.find((x) => x.id === id)
    if (!node) return res(ctx.status(404))
    // remove node
    orgUnits = orgUnits.filter((x) => x.id !== id)
    // reparent children to the deleted node's parent (or root/null)
    orgUnits = orgUnits.map((u) => (u.parentId === id ? { ...u, parentId: node.parentId ?? null } : u))
    return res(ctx.status(204))
  }),

  // add student to class
  rest.post(`${API_BASE}/classes/:classId/students`, async (req, res, ctx) => {
    const { classId } = req.params as { classId: string }
    const body = await req.json()
    const username = body?.username
    if (!username) {
      return res(ctx.status(422), ctx.json({ code: 'VALIDATION_ERROR', message: 'username is required' }))
    }

    if (username === 'existing' || username === 'duplicate') {
      return res(ctx.status(409), ctx.json({ code: 'USERNAME_CONFLICT', message: 'username already exists' }))
    }

    return res(
      ctx.status(201),
      ctx.json({ studentId: uuidv4(), accountId: uuidv4(), links: { self: `/classes/${classId}/students` } })
    )
  }),
]
