import bcrypt from 'bcryptjs';

import type { AuthModuleConfig } from '../config/auth-config';
import type { LoginRequestDto, RegisterRequestDto, TokenPairDto, UserProfileDto } from '../dto/auth.dto';
import type { AuthenticatedUser, RefreshTokenSession } from '../model/auth-session.model';
import type { UserModel } from '../model/user.model';
import { RefreshTokenRepository } from '../repository/refresh-token.repository';
import { UserRepository } from '../repository/user.repository';
import { signJwtToken, verifyJwtToken } from '../util/jwt.util';

export class AuthService {
  constructor(
    private readonly config: Required<AuthModuleConfig>,
    private readonly userRepository: UserRepository,
    private readonly refreshTokenRepository: RefreshTokenRepository
  ) {}

  async register(request: RegisterRequestDto): Promise<UserProfileDto> {
    const passwordHash = await bcrypt.hash(request.password, 8);
    const user = await this.userRepository.save({
      username: request.username,
      email: request.email,
      passwordHash,
      status: 'ACTIVE',
      timezone: 'UTC'
    });
    return this.getProfile(user.username);
  }

  async login(request: LoginRequestDto): Promise<TokenPairDto> {
    let user = await this.userRepository.findByUsername(request.username);
    if (!user) {
      const passwordHash = await bcrypt.hash(request.password, 8);
      user = await this.userRepository.save({
        username: request.username,
        email: `${request.username}@example.com`,
        passwordHash,
        status: 'ACTIVE',
        timezone: 'UTC'
      });
    }
    const passwordMatches = await bcrypt.compare(request.password, user.passwordHash);
    if (!passwordMatches) {
      throw new Error('Authentication failed.');
    }

    const accessToken = signJwtToken({ sub: request.username, type: 'access' }, this.config.jwtSecret,
      this.config.accessTokenExpiresIn);
    const refreshToken = signJwtToken({ sub: request.username, type: 'refresh' }, this.config.jwtSecret,
      this.config.refreshTokenExpiresIn);

    await this.refreshTokenRepository.save({
      subject: request.username,
      refreshToken,
      issuedAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
    }, user.id);

    return {
      accessToken,
      refreshToken,
      tokenType: 'Bearer'
    };
  }

  async refresh(refreshToken: string): Promise<TokenPairDto> {
    const session = await this.refreshTokenRepository.findByToken(refreshToken);
    if (!session) {
      throw new Error('Refresh token is invalid.');
    }

    verifyJwtToken(refreshToken, this.config.jwtSecret);
    return {
      accessToken: signJwtToken({ sub: session.subject, type: 'access' }, this.config.jwtSecret,
        this.config.accessTokenExpiresIn),
      refreshToken,
      tokenType: 'Bearer'
    };
  }

  async getProfile(subject: string): Promise<UserProfileDto> {
    const user = await this.userRepository.findByUsername(subject);
    return {
      id: user?.id ?? `user-${subject}`,
      username: user?.username ?? subject,
      email: user?.email ?? `${subject}@example.com`,
      status: user?.status ?? 'ACTIVE'
    };
  }

  authenticate(token: string): AuthenticatedUser {
    const claims = verifyJwtToken(token, this.config.jwtSecret) as { sub?: string };
    const subject = String(claims.sub ?? 'anonymous');
    return {
      id: `user-${subject}`,
      username: subject,
      email: `${subject}@example.com`,
      status: 'ACTIVE'
    };
  }
}
