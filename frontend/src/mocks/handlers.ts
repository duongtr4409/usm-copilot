import { rest } from 'msw'
import { v4 as uuidv4 } from 'uuid'

const API_BASE = (import.meta.env.VITE_API_BASE as string) || 'http://localhost:8080/api/v1'

// simple in-memory mock store for organization units
let orgUnits = [
  { id: 'ou-root-1', name: 'Root Unit', type: 'Khoa', parentId: null },
  { id: 'class-1-uuid', name: 'Class 1', type: 'Lớp', parentId: 'ou-root-1' },
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

  // org-units: list
  rest.get(`${API_BASE}/org-units`, (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(orgUnits))
  }),

  // org-units: create
  rest.post(`${API_BASE}/org-units`, async (req, res, ctx) => {
    const body = await req.json()
    const id = uuidv4()
    const newUnit = { id, name: body.name, type: body.type || 'Phòng', parentId: body.parentId || null }
    orgUnits.push(newUnit)
    return res(ctx.status(201), ctx.json(newUnit))
  }),

  // org-units: delete
  rest.delete(`${API_BASE}/org-units/:id`, (req, res, ctx) => {
    const { id } = req.params as { id: string }
    const hasChildren = orgUnits.some((u) => u.parentId === id)
    if (hasChildren) {
      return res(ctx.status(409), ctx.json({ code: 'HAS_CHILDREN', message: 'Unit has children' }))
    }
    orgUnits = orgUnits.filter((u) => u.id !== id)
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
