export type Role = 'USER' | 'ADMIN';

export interface UserDTO {
  userId: number;
  username: string;
  role: Role;
}

export interface CreateUserRequest {
  userName: string;
  password: string;
  role: Role;
}
