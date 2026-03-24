export type UserStatus = 'ACTIVE' | 'LOCKED';

export type UserModel = {
  id: string;
  username: string;
  email: string;
  passwordHash: string;
  status: UserStatus;
  timezone?: string;
};

export type UserCreateModel = {
  username: string;
  email: string;
  passwordHash: string;
  status: UserStatus;
  timezone?: string;
};
