export interface User {
  id: number;
  userLoginId: string;
  name: string;
}

export interface CreateUserRequest {
  userLoginId: string;
  password: string;
  name: string;
} 