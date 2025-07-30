import type { User, CreateUserRequest } from '@/types/user';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export async function getUser(id: string): Promise<User> {
  const res = await fetch(`${API_BASE_URL}/user/${id}`);
  if (!res.ok) throw new Error('유저 조회 실패');
  return res.json();
}

export async function createUser(user: CreateUserRequest): Promise<User> {
  const res = await fetch(`${API_BASE_URL}/user`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  });
  if (!res.ok) throw new Error('유저 생성 실패');
  return res.json();
} 