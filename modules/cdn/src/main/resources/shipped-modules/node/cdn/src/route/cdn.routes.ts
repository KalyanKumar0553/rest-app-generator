import { Router } from 'express';
import multer from 'multer';

import type { CdnController } from '../controller/cdn.controller';
import type { ResolvedCdnConfig } from '../config/cdn-config';

export const buildCdnRouter = (
  controller: CdnController,
  config: ResolvedCdnConfig
): Router => {
  const router = Router();
  const uploadMiddleware = multer({
    storage: multer.memoryStorage(),
    limits: {
      fileSize: config.maxFileSizeBytes
    }
  });

  router.get('/health', (_request, response) => {
    response.json(controller.getHealth());
  });

  router.get('/config', (_request, response) => {
    response.json(controller.getSafeConfig());
  });

  router.get('/assets', async (_request, response, next) => {
    try {
      response.json(await controller.listAssets());
    } catch (error) {
      next(error);
    }
  });

  router.post('/uploads', uploadMiddleware.single('file'), async (request, response, next) => {
    try {
      response.status(201).json(await controller.upload(request.file as Express.Multer.File));
    } catch (error) {
      next(error);
    }
  });

  return router;
};
