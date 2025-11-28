import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ComponentTheme {
  // Modal themes
  modalOverlayBg: string;
  modalContainerBg: string;
  modalHeaderBg: string;
  modalHeaderText: string;
  modalBodyText: string;
  modalBorderColor: string;

  // Button themes
  primaryButtonBg: string;
  primaryButtonHoverBg: string;
  primaryButtonText: string;
  secondaryButtonBg: string;
  secondaryButtonHoverBg: string;
  secondaryButtonText: string;
  confirmButtonBg: string;
  confirmButtonHoverBg: string;
  cancelButtonBg: string;
  cancelButtonBorder: string;
  cancelButtonText: string;

  // Input themes
  inputBorderColor: string;
  inputFocusBorderColor: string;
  inputFocusShadow: string;
  inputErrorBorderColor: string;
  inputPlaceholderColor: string;
  inputTextColor: string;
  inputBg: string;

  // Link themes
  linkColor: string;
  linkHoverColor: string;

  // Error themes
  errorTextColor: string;
  errorBg: string;

  // Neutral colors
  neutralText: string;
  neutralBorder: string;
  neutralBg: string;
}

const DEFAULT_THEME: ComponentTheme = {
  // Modal themes
  modalOverlayBg: 'rgba(0, 0, 0, 0.6)',
  modalContainerBg: '#ffffff',
  modalHeaderBg: '#8b9fa6',
  modalHeaderText: '#ffffff',
  modalBodyText: '#374151',
  modalBorderColor: '#e5e7eb',

  // Button themes
  primaryButtonBg: '#dc6454',
  primaryButtonHoverBg: '#c55544',
  primaryButtonText: '#ffffff',
  secondaryButtonBg: '#8b9fa6',
  secondaryButtonHoverBg: '#7a8e95',
  secondaryButtonText: '#ffffff',
  confirmButtonBg: '#dc6454',
  confirmButtonHoverBg: '#b91c1c',
  cancelButtonBg: '#ffffff',
  cancelButtonBorder: '#d1d5db',
  cancelButtonText: '#374151',

  // Input themes
  inputBorderColor: '#d1d5db',
  inputFocusBorderColor: '#8b9fa6',
  inputFocusShadow: 'rgba(139, 159, 166, 0.1)',
  inputErrorBorderColor: '#ef4444',
  inputPlaceholderColor: '#9ca3af',
  inputTextColor: '#374151',
  inputBg: '#ffffff',

  // Link themes
  linkColor: '#1f2937',
  linkHoverColor: '#374151',

  // Error themes
  errorTextColor: '#ef4444',
  errorBg: '#fef2f2',

  // Neutral colors
  neutralText: '#6b7280',
  neutralBorder: '#e5e7eb',
  neutralBg: '#f3f4f6',
};

@Injectable({
  providedIn: 'root'
})
export class ComponentThemeService {
  private readonly THEME_STORAGE_KEY = 'component-theme-preferences';
  private themeSubject = new BehaviorSubject<ComponentTheme>(DEFAULT_THEME);

  public theme$: Observable<ComponentTheme> = this.themeSubject.asObservable();

  constructor() {
    this.loadThemeFromStorage();
    this.applyThemeToCSSVariables(this.themeSubject.value);
  }

  /**
   * Get current component theme
   */
  getCurrentTheme(): ComponentTheme {
    return { ...this.themeSubject.value };
  }

  /**
   * Update specific theme properties
   */
  updateTheme(partialTheme: Partial<ComponentTheme>): void {
    const currentTheme = this.themeSubject.value;
    const newTheme = { ...currentTheme, ...partialTheme };

    this.themeSubject.next(newTheme);
    this.saveThemeToStorage(newTheme);
    this.applyThemeToCSSVariables(newTheme);
  }

  /**
   * Reset theme to default
   */
  resetToDefault(): void {
    this.themeSubject.next(DEFAULT_THEME);
    this.saveThemeToStorage(DEFAULT_THEME);
    this.applyThemeToCSSVariables(DEFAULT_THEME);
  }

  /**
   * Get a specific theme color by key
   */
  getThemeColor(key: keyof ComponentTheme): string {
    return this.themeSubject.value[key];
  }

  /**
   * Load theme from localStorage
   */
  private loadThemeFromStorage(): void {
    if (typeof localStorage === 'undefined') return;

    try {
      const storedTheme = localStorage.getItem(this.THEME_STORAGE_KEY);
      if (storedTheme) {
        const parsedTheme = JSON.parse(storedTheme);
        // Merge with default theme to ensure all properties exist
        const mergedTheme = { ...DEFAULT_THEME, ...parsedTheme };
        this.themeSubject.next(mergedTheme);
      }
    } catch (error) {
      console.error('Failed to load theme from storage:', error);
    }
  }

  /**
   * Save theme to localStorage
   */
  private saveThemeToStorage(theme: ComponentTheme): void {
    if (typeof localStorage === 'undefined') return;

    try {
      localStorage.setItem(this.THEME_STORAGE_KEY, JSON.stringify(theme));
    } catch (error) {
      console.error('Failed to save theme to storage:', error);
    }
  }

  /**
   * Apply theme colors to CSS custom properties
   */
  private applyThemeToCSSVariables(theme: ComponentTheme): void {
    if (typeof document === 'undefined') return;

    const root = document.documentElement;

    // Modal variables
    root.style.setProperty('--theme-modal-overlay-bg', theme.modalOverlayBg);
    root.style.setProperty('--theme-modal-container-bg', theme.modalContainerBg);
    root.style.setProperty('--theme-modal-header-bg', theme.modalHeaderBg);
    root.style.setProperty('--theme-modal-header-text', theme.modalHeaderText);
    root.style.setProperty('--theme-modal-body-text', theme.modalBodyText);
    root.style.setProperty('--theme-modal-border-color', theme.modalBorderColor);

    // Button variables
    root.style.setProperty('--theme-primary-btn-bg', theme.primaryButtonBg);
    root.style.setProperty('--theme-primary-btn-hover-bg', theme.primaryButtonHoverBg);
    root.style.setProperty('--theme-primary-btn-text', theme.primaryButtonText);
    root.style.setProperty('--theme-secondary-btn-bg', theme.secondaryButtonBg);
    root.style.setProperty('--theme-secondary-btn-hover-bg', theme.secondaryButtonHoverBg);
    root.style.setProperty('--theme-secondary-btn-text', theme.secondaryButtonText);
    root.style.setProperty('--theme-confirm-btn-bg', theme.confirmButtonBg);
    root.style.setProperty('--theme-confirm-btn-hover-bg', theme.confirmButtonHoverBg);
    root.style.setProperty('--theme-cancel-btn-bg', theme.cancelButtonBg);
    root.style.setProperty('--theme-cancel-btn-border', theme.cancelButtonBorder);
    root.style.setProperty('--theme-cancel-btn-text', theme.cancelButtonText);

    // Input variables
    root.style.setProperty('--theme-input-border-color', theme.inputBorderColor);
    root.style.setProperty('--theme-input-focus-border-color', theme.inputFocusBorderColor);
    root.style.setProperty('--theme-input-focus-shadow', theme.inputFocusShadow);
    root.style.setProperty('--theme-input-error-border-color', theme.inputErrorBorderColor);
    root.style.setProperty('--theme-input-placeholder-color', theme.inputPlaceholderColor);
    root.style.setProperty('--theme-input-text-color', theme.inputTextColor);
    root.style.setProperty('--theme-input-bg', theme.inputBg);

    // Link variables
    root.style.setProperty('--theme-link-color', theme.linkColor);
    root.style.setProperty('--theme-link-hover-color', theme.linkHoverColor);

    // Error variables
    root.style.setProperty('--theme-error-text-color', theme.errorTextColor);
    root.style.setProperty('--theme-error-bg', theme.errorBg);

    // Neutral variables
    root.style.setProperty('--theme-neutral-text', theme.neutralText);
    root.style.setProperty('--theme-neutral-border', theme.neutralBorder);
    root.style.setProperty('--theme-neutral-bg', theme.neutralBg);
  }

  /**
   * Export current theme as JSON
   */
  exportTheme(): string {
    return JSON.stringify(this.themeSubject.value, null, 2);
  }

  /**
   * Import theme from JSON string
   */
  importTheme(themeJson: string): boolean {
    try {
      const importedTheme = JSON.parse(themeJson);
      const mergedTheme = { ...DEFAULT_THEME, ...importedTheme };
      this.updateTheme(mergedTheme);
      return true;
    } catch (error) {
      console.error('Failed to import theme:', error);
      return false;
    }
  }
}
