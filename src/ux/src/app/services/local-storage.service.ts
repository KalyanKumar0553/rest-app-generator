import { Injectable } from '@angular/core';

export interface ProjectZipCacheEntry {
  yamlSpec: string;
  zipBase64: string;
  fileName: string;
  updatedAt: number;
}

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
  private readonly projectZipCacheStorageKey = 'project_zip_cache_v1';

  constructor() { }

  setItem(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch (error) {
      console.error('Error saving to localStorage', error);
    }
  }

  getItem(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch (error) {
      console.error('Error reading from localStorage', error);
      return null;
    }
  }

  removeItem(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Error deleting from localStorage', error);
    }
  }

  set(key: string, value: any): void {
    try {
      const jsonString = JSON.stringify(value);
      const base64Value = btoa(jsonString);
      localStorage.setItem(key, base64Value);
    } catch (error) {
      console.error('Error saving to localStorage', error);
    }
  }

  get(key: string): any {
    try {
      const base64Value = localStorage.getItem(key);
      if (!base64Value) {
        return null;
      }
      const jsonString = atob(base64Value);
      return JSON.parse(jsonString);
    } catch (error) {
      console.error('Error reading from localStorage', error);
      return null;
    }
  }

  update(key: string, value: any): void {
    this.set(key, value);
  }

  delete(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Error deleting from localStorage', error);
    }
  }

  clear(): void {
    try {
      localStorage.clear();
    } catch (error) {
      console.error('Error clearing localStorage', error);
    }
  }

  has(key: string): boolean {
    return localStorage.getItem(key) !== null;
  }

  getProjectZipCache(cacheKey: string): ProjectZipCacheEntry | null {
    if (!cacheKey) {
      return null;
    }

    const allEntries = this.getAllProjectZipCaches();
    return allEntries[cacheKey] ?? null;
  }

  setProjectZipCache(cacheKey: string, entry: ProjectZipCacheEntry): void {
    if (!cacheKey || !entry?.yamlSpec || !entry?.zipBase64) {
      return;
    }

    const allEntries = this.getAllProjectZipCaches();
    allEntries[cacheKey] = {
      yamlSpec: entry.yamlSpec,
      zipBase64: entry.zipBase64,
      fileName: entry.fileName || 'project.zip',
      updatedAt: entry.updatedAt || Date.now()
    };

    this.set(this.projectZipCacheStorageKey, allEntries);
  }

  clearProjectZipCache(cacheKey: string): void {
    if (!cacheKey) {
      return;
    }

    const allEntries = this.getAllProjectZipCaches();
    if (!allEntries[cacheKey]) {
      return;
    }

    delete allEntries[cacheKey];
    this.set(this.projectZipCacheStorageKey, allEntries);
  }

  private getAllProjectZipCaches(): Record<string, ProjectZipCacheEntry> {
    const stored = this.get(this.projectZipCacheStorageKey);
    if (!stored || typeof stored !== 'object') {
      return {};
    }
    return stored as Record<string, ProjectZipCacheEntry>;
  }
}
