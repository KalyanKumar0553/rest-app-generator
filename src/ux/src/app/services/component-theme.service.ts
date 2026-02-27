import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ComponentTheme {
  // Modal themes
  modalOverlayBg: string;
  modalContainerBg: string;
  modalShadow: string;
  modalHeaderBg: string;
  modalHeaderText: string;
  modalBodyText: string;
  modalBorderColor: string;

  // Button themes
  primaryButtonBg: string;
  primaryButtonHoverBg: string;
  primaryButtonText: string;
  primaryButtonShadow: string;
  secondaryButtonBg: string;
  secondaryButtonHoverBg: string;
  secondaryButtonText: string;
  secondaryButtonShadow: string;
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
  inputErrorShadow: string;
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

  // Plain colors
  plaintText: string;
}

const DEFAULT_THEME: ComponentTheme = {
  // Modal themes
  modalOverlayBg: 'color-mix(in srgb, var(--color-background) 65%, transparent)',
  modalContainerBg: 'var(--color-background-white)',
  modalShadow: 'var(--shadow-xl)',
  modalHeaderBg: 'var(--color-background-secondary)',
  modalHeaderText: 'var(--color-text-primary)',
  modalBodyText: 'var(--color-text-primary)',
  modalBorderColor: 'var(--color-border)',

  // Button themes
  primaryButtonBg: 'var(--color-primary)',
  primaryButtonHoverBg: 'var(--color-primary-hover)',
  primaryButtonText: 'var(--color-text-inverse)',
  primaryButtonShadow: 'var(--shadow-md)',
  secondaryButtonBg: 'var(--color-text-secondary)',
  secondaryButtonHoverBg: 'var(--color-primary)',
  secondaryButtonText: 'var(--color-text-inverse)',
  secondaryButtonShadow: 'var(--shadow-md)',
  confirmButtonBg: 'var(--color-primary)',
  confirmButtonHoverBg: 'var(--color-primary-hover)',
  cancelButtonBg: 'var(--color-background-white)',
  cancelButtonBorder: 'var(--color-border-dark)',
  cancelButtonText: 'var(--color-text-primary)',

  // Input themes
  inputBorderColor: 'var(--color-border-dark)',
  inputFocusBorderColor: 'var(--color-text-secondary)',
  inputFocusShadow: 'color-mix(in srgb, var(--color-text-secondary) 12%, transparent)',
  inputErrorBorderColor: 'var(--color-error)',
  inputErrorShadow: 'color-mix(in srgb, var(--color-error) 16%, transparent)',
  inputPlaceholderColor: 'var(--color-text-muted)',
  inputTextColor: 'var(--color-text-primary)',
  inputBg: 'var(--color-background-white)',

  // Link themes
  linkColor: 'var(--color-text-primary)',
  linkHoverColor: 'var(--color-primary)',

  // Error themes
  errorTextColor: 'var(--color-error)',
  errorBg: 'color-mix(in srgb, var(--color-error) 12%, transparent)',

  // Neutral colors
  neutralText: 'var(--color-text-muted)',
  neutralBorder: 'var(--color-border)',
  neutralBg: 'var(--color-background-muted)',

  plaintText: 'var(--color-text-inverse)',
};

@Injectable({
  providedIn: 'root'
})
export class ComponentThemeService {
  private readonly THEME_STORAGE_KEY = 'component-theme-preferences-v2';
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
    root.style.setProperty('--theme-modal-shadow', theme.modalShadow);
    root.style.setProperty('--theme-modal-header-bg', theme.modalHeaderBg);
    root.style.setProperty('--theme-modal-header-text', theme.modalHeaderText);
    root.style.setProperty('--theme-modal-body-text', theme.modalBodyText);
    root.style.setProperty('--theme-modal-border-color', theme.modalBorderColor);

    // Button variables
    root.style.setProperty('--theme-primary-btn-bg', theme.primaryButtonBg);
    root.style.setProperty('--theme-primary-btn-hover-bg', theme.primaryButtonHoverBg);
    root.style.setProperty('--theme-primary-btn-text', theme.primaryButtonText);
    root.style.setProperty('--theme-primary-btn-shadow', theme.primaryButtonShadow);
    root.style.setProperty('--theme-secondary-btn-bg', theme.secondaryButtonBg);
    root.style.setProperty('--theme-secondary-btn-hover-bg', theme.secondaryButtonHoverBg);
    root.style.setProperty('--theme-secondary-btn-text', theme.secondaryButtonText);
    root.style.setProperty('--theme-secondary-btn-shadow', theme.secondaryButtonShadow);
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
    root.style.setProperty('--theme-input-error-shadow', theme.inputErrorShadow);
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

    // Plain Text
    root.style.setProperty('--theme-plain-text', theme.plaintText);
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
