import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'theme_preference';
  private themeSubject = new BehaviorSubject<Theme>('light');
  
  public theme$ = this.themeSubject.asObservable();

  constructor() {
    this.initializeTheme();
  }

  /**
   * Initialize theme from localStorage or default to light
   */
  private initializeTheme(): void {
    this.setTheme('light');
  }

  /**
   * Get current theme
   */
  getCurrentTheme(): Theme {
    return this.themeSubject.value;
  }

  /**
   * Set theme and persist to localStorage
   */
  setTheme(theme: Theme): void {
    this.themeSubject.next(theme);
    
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem(this.THEME_KEY, theme);
    }
    
    this.applyTheme(theme);
  }

  /**
   * Toggle between light and dark themes
   */
  toggleTheme(): void {
    this.setTheme('light');
  }

  /**
   * Apply theme to document body
   */
  private applyTheme(theme: Theme): void {
    if (typeof document !== 'undefined') {
      document.body.classList.remove('theme-light', 'theme-dark');
      document.body.classList.add(`theme-${theme}`);

      const metaThemeColor = document.querySelector('meta[name="theme-color"]');
      if (metaThemeColor) {
        const computedThemeColor = getComputedStyle(document.documentElement)
          .getPropertyValue('--theme-browser-color')
          .trim();
        if (computedThemeColor) {
          metaThemeColor.setAttribute('content', computedThemeColor);
        }
      }
    }
  }
}
