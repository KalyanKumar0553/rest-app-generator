export type CdnProvider = 'local' | 'azure';

export type ResolvedCdnConfig = {
  provider: CdnProvider;
  localDirectory: string;
  publicBaseUrl: string;
  maxFileSizeBytes: number;
  allowedMimeTypes: string[];
  azureConnectionString?: string;
  azureContainerName: string;
  azureBlobPrefix: string;
};

const DEFAULT_ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

export const resolveCdnConfig = (config: Record<string, unknown>): ResolvedCdnConfig => {
  const provider = normalizeProvider(readString(config['provider']) || process.env.CDN_PROVIDER || 'local');
  const localDirectory = readString(config['localDirectory']) || process.env.CDN_LOCAL_DIRECTORY || 'storage/cdn';
  const publicBaseUrl = normalizePublicBaseUrl(
    readString(config['publicBaseUrl']) || process.env.CDN_PUBLIC_BASE_URL || '/cdn-assets'
  );
  const maxFileSizeBytes = readNumber(config['maxFileSizeBytes'])
    || readNumber(process.env.CDN_MAX_FILE_SIZE_BYTES)
    || 10 * 1024 * 1024;
  const allowedMimeTypes = readStringArray(config['allowedMimeTypes'])
    || readStringArray(process.env.CDN_ALLOWED_MIME_TYPES)
    || DEFAULT_ALLOWED_MIME_TYPES;
  const azureConnectionString = readString(config['azureConnectionString'])
    || process.env.AZURE_STORAGE_CONNECTION_STRING
    || '';
  const azureContainerName = readString(config['azureContainerName'])
    || process.env.AZURE_STORAGE_CONTAINER_NAME
    || 'cdn-assets';
  const azureBlobPrefix = readString(config['azureBlobPrefix'])
    || process.env.CDN_AZURE_BLOB_PREFIX
    || 'uploads';

  if (provider === 'azure' && !azureConnectionString.trim()) {
    throw new Error('CDN provider is set to azure but AZURE_STORAGE_CONNECTION_STRING is missing.');
  }

  return {
    provider,
    localDirectory,
    publicBaseUrl,
    maxFileSizeBytes,
    allowedMimeTypes,
    azureConnectionString: provider === 'azure' ? azureConnectionString : undefined,
    azureContainerName,
    azureBlobPrefix
  };
};

const normalizeProvider = (value: string): CdnProvider => {
  return value.trim().toLowerCase() === 'azure' ? 'azure' : 'local';
};

const normalizePublicBaseUrl = (value: string): string => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return '/cdn-assets';
  }
  return trimmedValue.startsWith('/') ? trimmedValue : `/${trimmedValue}`;
};

const readString = (value: unknown): string => String(value ?? '').trim();

const readNumber = (value: unknown): number | null => {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) && numericValue > 0 ? numericValue : null;
};

const readStringArray = (value: unknown): string[] | null => {
  if (Array.isArray(value)) {
    const items = value
      .map((item) => String(item ?? '').trim())
      .filter((item) => item.length > 0);
    return items.length > 0 ? items : null;
  }

  const normalizedValue = String(value ?? '').trim();
  if (!normalizedValue) {
    return null;
  }

  const items = normalizedValue
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
  return items.length > 0 ? items : null;
};
