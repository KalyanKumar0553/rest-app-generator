import type { Request, Response } from 'express';

export class HealthController {
  health = (_request: Request, response: Response): void => {
    response.json({
      status: 'UP',
      components: {
        authModule: 'UP'
      }
    });
  };
}
