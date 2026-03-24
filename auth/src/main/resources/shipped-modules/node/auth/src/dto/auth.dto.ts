export type LoginRequestDto = {
  username: string;
  password: string;
};

export type RegisterRequestDto = {
  username: string;
  email: string;
  password: string;
};

export type TokenPairDto = {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
};

export type UserProfileDto = {
  id: string;
  username: string;
  email: string;
  status: 'ACTIVE' | 'LOCKED';
};

export type AuthProviderStatusDto = {
  provider: string;
  enabled: boolean;
  clientId?: string;
};

export type CaptchaChallengeDto = {
  challengeId: string;
  question: string;
};
