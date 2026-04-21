import React from 'react'
import { Link } from 'react-router-dom'
import ClassPicker from '../features/classes/ClassPicker'
import { useState } from 'react'

export default function Home() {
  const [classId, setClassId] = useState<string | undefined>(undefined)

  return (
    <div>
      <h1>USM Dashboard</h1>
      <div style={{ marginBottom: 12 }}>
        <ClassPicker value={classId} onChange={setClassId} />
        {classId && (
          <div style={{ marginTop: 8 }}>
            <Link to={`/classes/${classId}/add-student`}>Add student to selected class</Link>
          </div>
        )}
      </div>
    </div>
  )
}
