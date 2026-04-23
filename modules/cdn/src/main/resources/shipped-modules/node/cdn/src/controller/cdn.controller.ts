import { randomUUID } from 'node:crypto';

import type { CdnAssetRecord } from '../repository/cdn-manifest.repository';
import type { ResolvedCdnConfig } from '../config/cdn-config';
import type { CdnStorageService } from '../service/cdn-storage.service';
import { CdnManifestRepository } from '../repository/cdn-manifest.repository';

export class CdnController {
  constructor(
    private readonly config: ResolvedCdnConfig,
    private readonly storageService: CdnStorageService,
    private readonly manifestRepository: CdnManifestRepository
  ) {}

  getHealth() {
    return {
      status: 'UP',
      provider: this.config.provider,
      maxFileSizeBytes: this.config.maxFileSizeBytes,
      allowedMimeTypes: this.config.allowedMimeTypes
    };
  }

  getSafeConfig() {
    return {
      provider: this.config.provider,
      localDirectory: this.config.localDirectory,
      publicBaseUrl: this.config.publicBaseUrl,
      maxFileSizeBytes: this.config.maxFileSizeBytes,
      allowedMimeTypes: this.config.allowedMimeTypes,
      azureContainerName: this.config.azureContainerName,
      azureBlobPrefix: this.config.azureBlobPrefix
    };
  }

  async listAssets(): Promise<CdnAssetRecord[]> {
    return this.manifestRepository.list();
  }

  async upload(file: Express.Multer.File): Promise<CdnAssetRecord> {
    if (!file) {
      throw new Error('Upload file is required.');
    }
    if (!this.config.allowedMimeTypes.includes(file.mimetype)) {
      throw new Error(`Unsupported content type '${file.mimetype}'.`);
    }

    const uploadedAsset = await this.storageService.upload(file);
    const assetRecord: CdnAssetRecord = {
      id: randomUUID(),
      originalName: uploadedAsset.originalName,
      storageKey: uploadedAsset.storageKey,
      url: uploadedAsset.url,
      contentType: uploadedAsset.contentType,
      size: uploadedAsset.size,
      provider: uploadedAsset.provider,
      uploadedAt: new Date().toISOString()
    };
    await this.manifestRepository.append(assetRecord);
    return assetRecord;
  }
}
