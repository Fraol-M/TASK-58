import type { User } from '@/types/user'

export const mockUsers: (User & { password: string })[] = [
  {
    id: 1,
    username: 'student01',
    email: 'student01@campus.edu',
    roles: ['REGULAR_USER'],
    status: 'ACTIVE',
    password: 'Student1!',
  },
  {
    id: 2,
    username: 'warehouse_op',
    email: 'ops@campus.edu',
    roles: ['OPERATIONS_STAFF'],
    status: 'ACTIVE',
    password: 'Operator1!',
  },
  {
    id: 3,
    username: 'admin_user',
    email: 'admin@campus.edu',
    roles: ['ADMIN'],
    status: 'ACTIVE',
    password: 'AdminPass1!',
  },
]

export function findMockUser(username: string, password: string) {
  return mockUsers.find((u) => u.username === username && u.password === password) ?? null
}

export function getMockUserById(id: number) {
  return mockUsers.find((u) => u.id === id) ?? null
}
