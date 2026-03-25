import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface DataEncryptionRule {
  id: string;
  tableName: string;
  columnName?: string | null;
  hashShadowColumn?: string | null;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DataEncryptionRulePayload {
  tableName: string;
  columnName?: string | null;
  hashShadowColumn?: string | null;
  enabled: boolean;
}

export interface ConfigPropertyValue {
  valueKey: string;
  valueLabel: string;
}

export interface ConfigProperty {
  category: string;
  label: string;
  propertyKey: string;
  currentValueKey?: string | null;
  values: ConfigPropertyValue[];
}

export interface ConfigFeatureValuePayload {
  category: string;
  propertyKey: string;
  valueKey: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private http: HttpClient) {}

  getDataEncryptionRules(): Observable<DataEncryptionRule[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.DATA_ENCRYPTION_RULES}`;
    return this.http.get<DataEncryptionRule[]>(url);
  }

  createDataEncryptionRule(payload: DataEncryptionRulePayload): Observable<DataEncryptionRule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.DATA_ENCRYPTION_RULES}`;
    return this.http.post<DataEncryptionRule>(url, payload);
  }

  updateDataEncryptionRule(ruleId: string, payload: DataEncryptionRulePayload): Observable<DataEncryptionRule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_DATA_ENCRYPTION_RULE(ruleId)}`;
    return this.http.put<DataEncryptionRule>(url, payload);
  }

  deleteDataEncryptionRule(ruleId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_DATA_ENCRYPTION_RULE(ruleId)}`;
    return this.http.delete<void>(url);
  }

  getConfigFeatures(): Observable<ConfigProperty[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.CONFIG_FEATURES}`;
    return this.http.get<ConfigProperty[]>(url);
  }

  updateConfigFeatureValue(payload: ConfigFeatureValuePayload): Observable<ConfigProperty> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_CONFIG_FEATURE_VALUE}`;
    return this.http.put<ConfigProperty>(url, payload);
  }
}
