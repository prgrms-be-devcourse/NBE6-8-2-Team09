import type { User, CreateUserRequest } from '@/types/user';
import { apiCall } from './client';

export async function getUser(id: string): Promise<User> {
  const response = await apiCall<User>(`/v1/users/me`);
  if (!response) throw new Error('유저 조회 실패');
  return response;
}

export async function createUser(user: CreateUserRequest): Promise<User> {
  const response = await apiCall<User>('/v1/users/register', {
    method: 'POST',
    body: JSON.stringify(user),
  });
  if (!response) throw new Error('유저 생성 실패');
  return response;
}
