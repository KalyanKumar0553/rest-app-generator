export interface LombokSettings {
  generateBuilder: boolean;
  generateToString: boolean;
  generateEqualsAndHashCode: boolean;
}

export interface GeneralSettings {
  softDelete: boolean;
  auditing: boolean;
  makeImmutable: boolean;
  naturalIdCache: boolean;
}
