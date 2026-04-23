import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';

export type CdnAssetRecord = {
  id: string;
  originalName: string;
  storageKey: string;
  url: string;
  contentType: string;
  size: number;
  provider: 'local' | 'azure';
  uploadedAt: string;
};

export class CdnManifestRepository {
  private readonly manifestFilePath: string;

  constructor(localDirectory: string) {
    this.manifestFilePath = path.join(localDirectory, '.manifest', 'assets.json');
  }

  async list(): Promise<CdnAssetRecord[]> {
    try {
      const content = await readFile(this.manifestFilePath, 'utf-8');
      const parsed = JSON.parse(content);
      return Array.isArray(parsed) ? parsed as CdnAssetRecord[] : [];
    } catch (error) {
      if (isMissingFileError(error)) {
        return [];
      }
      throw error;
    }
  }

  async append(record: CdnAssetRecord): Promise<void> {
    const existing = await this.list();
    existing.unshift(record);
    await mkdir(path.dirname(this.manifestFilePath), { recursive: true });
    await writeFile(this.manifestFilePath, JSON.stringify(existing, null, 2), 'utf-8');
  }
}

const isMissingFileError = (error: unknown): boolean => {
  return Boolean(error && typeof error === 'object' && 'code' in error && error.code === 'ENOENT');
};
