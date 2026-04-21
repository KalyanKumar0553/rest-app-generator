import type { CaptchaChallengeDto } from '../dto/auth.dto';

export class CaptchaService {
  createChallenge(): CaptchaChallengeDto {
    const left = 2 + Math.floor(Math.random() * 8);
    const right = 1 + Math.floor(Math.random() * 9);
    return {
      challengeId: `captcha-${Date.now()}`,
      question: `${left} + ${right} = ?`
    };
  }
}
