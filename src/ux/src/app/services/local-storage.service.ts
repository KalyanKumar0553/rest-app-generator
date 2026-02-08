import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

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
}
