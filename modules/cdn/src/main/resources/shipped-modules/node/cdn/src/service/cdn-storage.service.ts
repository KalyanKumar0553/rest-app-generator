import { randomUUID } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';

import { BlobServiceClient } from '@azure/storage-blob';

import type { ResolvedCdnConfig } from '../config/cdn-config';

export type UploadedAsset = {
  storageKey: string;
  url: string;
  size: number;
  contentType: string;
  originalName: string;
  provider: 'local' | 'azure';
};

export interface CdnStorageService {
  upload(file: Express.Multer.File): Promise<UploadedAsset>;
}

export const createCdnStorageService = (config: ResolvedCdnConfig): CdnStorageService => {
  return config.provider === 'azure'
    ? new AzureBlobCdnStorageService(config)
    : new LocalDiskCdnStorageService(config);
};

class LocalDiskCdnStorageService implements CdnStorageService {
  constructor(private readonly config: ResolvedCdnConfig) {}

  async upload(file: Express.Multer.File): Promise<UploadedAsset> {
    const storageKey = buildStorageKey(file.originalname, this.config.azureBlobPrefix);
    const targetPath = path.join(this.config.localDirectory, storageKey);
    await mkdir(path.dirname(targetPath), { recursive: true });
    await writeFile(targetPath, file.buffer);

    return {
      storageKey,
      url: joinUrlPath(this.config.publicBaseUrl, storageKey),
      size: file.size,
      contentType: file.mimetype,
      originalName: file.originalname,
      provider: 'local'
    };
  }
}

class AzureBlobCdnStorageService implements CdnStorageService {
  private readonly containerClient;

  constructor(private readonly config: ResolvedCdnConfig) {
    const blobServiceClient = BlobServiceClient.fromConnectionString(config.azureConnectionString ?? '');
    this.containerClient = blobServiceClient.getContainerClient(config.azureContainerName);
  }

  async upload(file: Express.Multer.File): Promise<UploadedAsset> {
    await this.containerClient.createIfNotExists();
    const storageKey = buildStorageKey(file.originalname, this.config.azureBlobPrefix);
    const blobClient = this.containerClient.getBlockBlobClient(storageKey);
    await blobClient.uploadData(file.buffer, {
      blobHTTPHeaders: {
        blobContentType: file.mimetype
      }
    });

    return {
      storageKey,
      url: blobClient.url,
      size: file.size,
      contentType: file.mimetype,
      originalName: file.originalname,
      provider: 'azure'
    };
  }
}

const buildStorageKey = (originalName: string, blobPrefix: string): string => {
  const extension = path.extname(originalName).toLowerCase();
  const baseName = path.basename(originalName, extension)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
    || 'asset';
  const prefix = blobPrefix.trim().replace(/^\/+|\/+$/g, '');
  const datedFolder = new Date().toISOString().slice(0, 10);
  const fileName = `${baseName}-${randomUUID()}${extension}`;
  return [prefix, datedFolder, fileName].filter((segment) => segment.length > 0).join('/');
};

const joinUrlPath = (basePath: string, suffix: string): string => {
  return `${basePath.replace(/\/+$/, '')}/${suffix.replace(/^\/+/, '')}`;
};
