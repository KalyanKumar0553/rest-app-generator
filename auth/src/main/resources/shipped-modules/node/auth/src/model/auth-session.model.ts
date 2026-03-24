export type RefreshTokenSession = {
  subject: string;
  refreshToken: string;
  issuedAt: string;
  expiresAt: string;
};

export type AuthenticatedUser = {
  id: string;
  username: string;
  email: string;
  status: 'ACTIVE' | 'LOCKED';
};
