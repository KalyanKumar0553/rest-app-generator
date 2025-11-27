# Theme Customization System - Implementation Guide

## Current Theme Architecture

### ✅ What We Have

**1. Centralized Color System** (`src/app/styles/colors.css`)
- CSS Custom Properties (CSS Variables) defined in `:root`
- Organized color palette:
  - Brand colors (Bootify design)
  - Neutral colors (50-900 scale)
  - Success, Warning, Error colors
  - Semantic mappings
  - Shadow system

**Current Brand Colors:**
```css
--brand-background: #dff1ef;   /* Light mint background */
--brand-text: #49565d;          /* Dark gray text */
--brand-accent: #e89b8e;        /* Coral accent */
--brand-accent-hover: #d17d6e;  /* Darker coral */
--brand-secondary: #97a9ae;     /* Muted blue-gray */
```

**2. Theme Service** (`src/app/services/theme.service.ts`)
- Basic light/dark theme support
- Currently disabled (always light theme)
- Uses BehaviorSubject for reactive updates
- Persists to localStorage

### ❌ What's Missing

1. Components don't use CSS variables consistently
2. No database storage for user preferences
3. No user settings UI
4. No custom color picker support
5. Theme presets not implemented

---

## Recommended Architecture

### 1. Database Schema (Supabase)

Create a `user_preferences` table to store theme settings:

```sql
-- User Preferences Table
CREATE TABLE IF NOT EXISTS user_preferences (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,

  -- Theme Settings
  theme_mode text NOT NULL DEFAULT 'light' CHECK (theme_mode IN ('light', 'dark', 'custom')),

  -- Custom Brand Colors
  brand_primary text DEFAULT '#e89b8e',
  brand_secondary text DEFAULT '#97a9ae',
  brand_background text DEFAULT '#dff1ef',
  brand_text text DEFAULT '#49565d',
  brand_accent text DEFAULT '#e89b8e',

  -- Layout Preferences
  sidebar_collapsed boolean DEFAULT false,
  compact_mode boolean DEFAULT false,

  -- Accessibility
  high_contrast boolean DEFAULT false,
  reduce_motion boolean DEFAULT false,
  font_size text DEFAULT 'medium' CHECK (font_size IN ('small', 'medium', 'large')),

  -- Metadata
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now(),

  UNIQUE(user_id)
);

-- Enable RLS
ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own preferences"
  ON user_preferences FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own preferences"
  ON user_preferences FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own preferences"
  ON user_preferences FOR UPDATE
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- Updated At Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_preferences_updated_at
  BEFORE UPDATE ON user_preferences
  FOR EACH ROW
  EXECUTE PROCEDURE update_updated_at_column();
```

### 2. Enhanced Theme Service

**File: `src/app/services/theme.service.ts`**

```typescript
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export type ThemeMode = 'light' | 'dark' | 'custom';
export type FontSize = 'small' | 'medium' | 'large';

export interface ThemeColors {
  brandPrimary: string;
  brandSecondary: string;
  brandBackground: string;
  brandText: string;
  brandAccent: string;
}

export interface UserPreferences {
  id?: string;
  userId?: string;
  themeMode: ThemeMode;
  colors: ThemeColors;
  sidebarCollapsed: boolean;
  compactMode: boolean;
  highContrast: boolean;
  reduceMotion: boolean;
  fontSize: FontSize;
}

export const DEFAULT_THEME: UserPreferences = {
  themeMode: 'light',
  colors: {
    brandPrimary: '#e89b8e',
    brandSecondary: '#97a9ae',
    brandBackground: '#dff1ef',
    brandText: '#49565d',
    brandAccent: '#e89b8e'
  },
  sidebarCollapsed: false,
  compactMode: false,
  highContrast: false,
  reduceMotion: false,
  fontSize: 'medium'
};

// Theme Presets
export const THEME_PRESETS = {
  default: {
    name: 'Bootify Default',
    colors: {
      brandPrimary: '#e89b8e',
      brandSecondary: '#97a9ae',
      brandBackground: '#dff1ef',
      brandText: '#49565d',
      brandAccent: '#e89b8e'
    }
  },
  ocean: {
    name: 'Ocean Blue',
    colors: {
      brandPrimary: '#0ea5e9',
      brandSecondary: '#64748b',
      brandBackground: '#e0f2fe',
      brandText: '#1e293b',
      brandAccent: '#0ea5e9'
    }
  },
  forest: {
    name: 'Forest Green',
    colors: {
      brandPrimary: '#10b981',
      brandSecondary: '#6b7280',
      brandBackground: '#d1fae5',
      brandText: '#1f2937',
      brandAccent: '#10b981'
    }
  },
  sunset: {
    name: 'Sunset Orange',
    colors: {
      brandPrimary: '#f97316',
      brandSecondary: '#78716c',
      brandBackground: '#fed7aa',
      brandText: '#292524',
      brandAccent: '#f97316'
    }
  },
  lavender: {
    name: 'Lavender Purple',
    colors: {
      brandPrimary: '#a855f7',
      brandSecondary: '#71717a',
      brandBackground: '#f3e8ff',
      brandText: '#27272a',
      brandAccent: '#a855f7'
    }
  },
  monochrome: {
    name: 'Monochrome',
    colors: {
      brandPrimary: '#374151',
      brandSecondary: '#6b7280',
      brandBackground: '#f3f4f6',
      brandText: '#111827',
      brandAccent: '#374151'
    }
  }
};

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private supabase: SupabaseClient;
  private preferencesSubject = new BehaviorSubject<UserPreferences>(DEFAULT_THEME);
  public preferences$ = this.preferencesSubject.asObservable();

  constructor(private authService: AuthService) {
    this.supabase = createClient(
      environment.supabaseUrl,
      environment.supabaseKey
    );
    this.initializeTheme();
  }

  /**
   * Initialize theme from database or localStorage
   */
  private async initializeTheme(): Promise<void> {
    // Check if user is authenticated
    const user = this.authService.getUserData();

    if (user) {
      // Load from database
      await this.loadUserPreferences();
    } else {
      // Load from localStorage as fallback
      this.loadLocalPreferences();
    }
  }

  /**
   * Load user preferences from Supabase
   */
  async loadUserPreferences(): Promise<void> {
    const user = this.authService.getUserData();
    if (!user) return;

    const { data, error } = await this.supabase
      .from('user_preferences')
      .select('*')
      .eq('user_id', user.id)
      .maybeSingle();

    if (error) {
      console.error('Error loading preferences:', error);
      this.loadLocalPreferences();
      return;
    }

    if (data) {
      const preferences: UserPreferences = {
        id: data.id,
        userId: data.user_id,
        themeMode: data.theme_mode,
        colors: {
          brandPrimary: data.brand_primary,
          brandSecondary: data.brand_secondary,
          brandBackground: data.brand_background,
          brandText: data.brand_text,
          brandAccent: data.brand_accent
        },
        sidebarCollapsed: data.sidebar_collapsed,
        compactMode: data.compact_mode,
        highContrast: data.high_contrast,
        reduceMotion: data.reduce_motion,
        fontSize: data.font_size
      };

      this.applyPreferences(preferences);
    } else {
      // Create default preferences
      await this.saveUserPreferences(DEFAULT_THEME);
    }
  }

  /**
   * Load preferences from localStorage
   */
  private loadLocalPreferences(): void {
    if (typeof localStorage === 'undefined') return;

    const stored = localStorage.getItem('user_preferences');
    if (stored) {
      try {
        const preferences = JSON.parse(stored) as UserPreferences;
        this.applyPreferences(preferences);
      } catch (e) {
        console.error('Error parsing preferences:', e);
        this.applyPreferences(DEFAULT_THEME);
      }
    } else {
      this.applyPreferences(DEFAULT_THEME);
    }
  }

  /**
   * Save user preferences to database
   */
  async saveUserPreferences(preferences: UserPreferences): Promise<void> {
    const user = this.authService.getUserData();

    // Save to localStorage first for immediate effect
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('user_preferences', JSON.stringify(preferences));
    }

    if (!user) {
      this.applyPreferences(preferences);
      return;
    }

    const dbPreferences = {
      user_id: user.id,
      theme_mode: preferences.themeMode,
      brand_primary: preferences.colors.brandPrimary,
      brand_secondary: preferences.colors.brandSecondary,
      brand_background: preferences.colors.brandBackground,
      brand_text: preferences.colors.brandText,
      brand_accent: preferences.colors.brandAccent,
      sidebar_collapsed: preferences.sidebarCollapsed,
      compact_mode: preferences.compactMode,
      high_contrast: preferences.highContrast,
      reduce_motion: preferences.reduceMotion,
      font_size: preferences.fontSize
    };

    const { error } = await this.supabase
      .from('user_preferences')
      .upsert(dbPreferences, {
        onConflict: 'user_id'
      });

    if (error) {
      console.error('Error saving preferences:', error);
    }

    this.applyPreferences(preferences);
  }

  /**
   * Apply preferences to DOM
   */
  private applyPreferences(preferences: UserPreferences): void {
    this.preferencesSubject.next(preferences);

    if (typeof document === 'undefined') return;

    const root = document.documentElement;

    // Apply theme mode
    root.setAttribute('data-theme', preferences.themeMode);

    // Apply custom colors
    if (preferences.themeMode === 'custom' || preferences.themeMode === 'light') {
      root.style.setProperty('--brand-accent', preferences.colors.brandPrimary);
      root.style.setProperty('--brand-secondary', preferences.colors.brandSecondary);
      root.style.setProperty('--brand-background', preferences.colors.brandBackground);
      root.style.setProperty('--brand-text', preferences.colors.brandText);
      root.style.setProperty('--color-primary', preferences.colors.brandAccent);
    }

    // Apply font size
    root.setAttribute('data-font-size', preferences.fontSize);

    // Apply accessibility settings
    if (preferences.highContrast) {
      root.setAttribute('data-high-contrast', 'true');
    } else {
      root.removeAttribute('data-high-contrast');
    }

    if (preferences.reduceMotion) {
      root.setAttribute('data-reduce-motion', 'true');
    } else {
      root.removeAttribute('data-reduce-motion');
    }

    // Apply compact mode
    if (preferences.compactMode) {
      document.body.classList.add('compact-mode');
    } else {
      document.body.classList.remove('compact-mode');
    }
  }

  /**
   * Get current preferences
   */
  getCurrentPreferences(): UserPreferences {
    return this.preferencesSubject.value;
  }

  /**
   * Update theme colors
   */
  async updateColors(colors: ThemeColors): Promise<void> {
    const current = this.getCurrentPreferences();
    await this.saveUserPreferences({
      ...current,
      colors,
      themeMode: 'custom'
    });
  }

  /**
   * Apply theme preset
   */
  async applyPreset(presetName: keyof typeof THEME_PRESETS): Promise<void> {
    const preset = THEME_PRESETS[presetName];
    if (!preset) return;

    const current = this.getCurrentPreferences();
    await this.saveUserPreferences({
      ...current,
      colors: preset.colors,
      themeMode: 'custom'
    });
  }

  /**
   * Reset to default theme
   */
  async resetToDefault(): Promise<void> {
    await this.saveUserPreferences(DEFAULT_THEME);
  }

  /**
   * Toggle dark mode
   */
  async toggleDarkMode(): Promise<void> {
    const current = this.getCurrentPreferences();
    const newMode: ThemeMode = current.themeMode === 'dark' ? 'light' : 'dark';
    await this.saveUserPreferences({
      ...current,
      themeMode: newMode
    });
  }

  /**
   * Update font size
   */
  async updateFontSize(fontSize: FontSize): Promise<void> {
    const current = this.getCurrentPreferences();
    await this.saveUserPreferences({
      ...current,
      fontSize
    });
  }

  /**
   * Toggle accessibility feature
   */
  async toggleAccessibility(feature: 'highContrast' | 'reduceMotion'): Promise<void> {
    const current = this.getCurrentPreferences();
    await this.saveUserPreferences({
      ...current,
      [feature]: !current[feature]
    });
  }
}
```

### 3. Update Colors CSS for Dark Theme Support

**File: `src/app/styles/colors.css`**

Add dark theme variables:

```css
/* Dark Theme */
[data-theme="dark"] {
  --brand-background: #1a1a2e;
  --brand-text: #e5e7eb;
  --brand-accent: #f59e0b;
  --brand-accent-hover: #d97706;
  --brand-secondary: #6b7280;

  --neutral-50: #1f2937;
  --neutral-100: #374151;
  --neutral-200: #4b5563;
  --neutral-300: #6b7280;
  --neutral-400: #9ca3af;
  --neutral-500: #d1d5db;
  --neutral-600: #e5e7eb;
  --neutral-700: #f3f4f6;
  --neutral-800: #f9fafb;
  --neutral-900: #ffffff;

  --color-background: #1a1a2e;
  --color-background-white: #16213e;
  --color-background-secondary: #0f3460;
  --color-text-primary: #e5e7eb;
  --color-text-secondary: #9ca3af;
  --color-border: #374151;
}

/* Font Size Variations */
[data-font-size="small"] {
  font-size: 14px;
}

[data-font-size="medium"] {
  font-size: 16px;
}

[data-font-size="large"] {
  font-size: 18px;
}

/* High Contrast Mode */
[data-high-contrast="true"] {
  --brand-text: #000000;
  --color-text-primary: #000000;
  --color-background: #ffffff;
  --color-border: #000000;
  --brand-accent: #0066cc;
}

/* Reduced Motion */
[data-reduce-motion="true"] * {
  animation-duration: 0.01ms !important;
  animation-iteration-count: 1 !important;
  transition-duration: 0.01ms !important;
}

/* Compact Mode */
body.compact-mode {
  --spacing-unit: 0.75rem;
}
```

### 4. Settings Component Structure

**File: `src/app/modules/user/components/settings/settings.component.ts`**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThemeService, THEME_PRESETS, UserPreferences } from '../../../../services/theme.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  preferences!: UserPreferences;
  presets = Object.entries(THEME_PRESETS);
  activeTab: 'appearance' | 'accessibility' | 'account' = 'appearance';

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.themeService.preferences$.subscribe(prefs => {
      this.preferences = { ...prefs };
    });
  }

  async applyPreset(presetName: string): Promise<void> {
    await this.themeService.applyPreset(presetName as any);
  }

  async updateColor(colorKey: keyof typeof this.preferences.colors, value: string): Promise<void> {
    this.preferences.colors[colorKey] = value;
    await this.themeService.updateColors(this.preferences.colors);
  }

  async toggleDarkMode(): Promise<void> {
    await this.themeService.toggleDarkMode();
  }

  async updateFontSize(size: 'small' | 'medium' | 'large'): Promise<void> {
    await this.themeService.updateFontSize(size);
  }

  async toggleAccessibility(feature: 'highContrast' | 'reduceMotion'): Promise<void> {
    await this.themeService.toggleAccessibility(feature);
  }

  async resetToDefault(): Promise<void> {
    if (confirm('Are you sure you want to reset all settings to default?')) {
      await this.themeService.resetToDefault();
    }
  }

  setActiveTab(tab: 'appearance' | 'accessibility' | 'account'): void {
    this.activeTab = tab;
  }
}
```

### 5. Settings Component HTML Template

```html
<div class="settings-container">
  <div class="settings-header">
    <h1>Settings</h1>
    <button class="btn-reset" (click)="resetToDefault()">
      Reset to Default
    </button>
  </div>

  <div class="settings-tabs">
    <button
      [class.active]="activeTab === 'appearance'"
      (click)="setActiveTab('appearance')">
      Appearance
    </button>
    <button
      [class.active]="activeTab === 'accessibility'"
      (click)="setActiveTab('accessibility')">
      Accessibility
    </button>
    <button
      [class.active]="activeTab === 'account'"
      (click)="setActiveTab('account')">
      Account
    </button>
  </div>

  <!-- Appearance Tab -->
  <div *ngIf="activeTab === 'appearance'" class="settings-content">

    <!-- Theme Mode -->
    <section class="settings-section">
      <h2>Theme</h2>
      <div class="theme-selector">
        <button
          [class.active]="preferences.themeMode === 'light'"
          (click)="themeService.saveUserPreferences({...preferences, themeMode: 'light'})">
          <svg><!-- Light icon --></svg>
          Light
        </button>
        <button
          [class.active]="preferences.themeMode === 'dark'"
          (click)="toggleDarkMode()">
          <svg><!-- Dark icon --></svg>
          Dark
        </button>
        <button
          [class.active]="preferences.themeMode === 'custom'"
          (click)="themeService.saveUserPreferences({...preferences, themeMode: 'custom'})">
          <svg><!-- Custom icon --></svg>
          Custom
        </button>
      </div>
    </section>

    <!-- Theme Presets -->
    <section class="settings-section" *ngIf="preferences.themeMode !== 'dark'">
      <h2>Color Presets</h2>
      <div class="preset-grid">
        <button
          *ngFor="let preset of presets"
          class="preset-card"
          (click)="applyPreset(preset[0])">
          <div class="preset-colors">
            <span [style.background]="preset[1].colors.brandPrimary"></span>
            <span [style.background]="preset[1].colors.brandSecondary"></span>
            <span [style.background]="preset[1].colors.brandBackground"></span>
          </div>
          <span class="preset-name">{{ preset[1].name }}</span>
        </button>
      </div>
    </section>

    <!-- Custom Colors -->
    <section class="settings-section" *ngIf="preferences.themeMode === 'custom'">
      <h2>Custom Colors</h2>
      <div class="color-pickers">
        <div class="color-picker-item">
          <label>Primary Color</label>
          <input
            type="color"
            [value]="preferences.colors.brandPrimary"
            (change)="updateColor('brandPrimary', $event.target.value)">
          <span>{{ preferences.colors.brandPrimary }}</span>
        </div>
        <div class="color-picker-item">
          <label>Secondary Color</label>
          <input
            type="color"
            [value]="preferences.colors.brandSecondary"
            (change)="updateColor('brandSecondary', $event.target.value)">
          <span>{{ preferences.colors.brandSecondary }}</span>
        </div>
        <div class="color-picker-item">
          <label>Background Color</label>
          <input
            type="color"
            [value]="preferences.colors.brandBackground"
            (change)="updateColor('brandBackground', $event.target.value)">
          <span>{{ preferences.colors.brandBackground }}</span>
        </div>
        <div class="color-picker-item">
          <label>Text Color</label>
          <input
            type="color"
            [value]="preferences.colors.brandText"
            (change)="updateColor('brandText', $event.target.value)">
          <span>{{ preferences.colors.brandText }}</span>
        </div>
        <div class="color-picker-item">
          <label>Accent Color</label>
          <input
            type="color"
            [value]="preferences.colors.brandAccent"
            (change)="updateColor('brandAccent', $event.target.value)">
          <span>{{ preferences.colors.brandAccent }}</span>
        </div>
      </div>
    </section>

    <!-- Font Size -->
    <section class="settings-section">
      <h2>Text Size</h2>
      <div class="font-size-selector">
        <button
          [class.active]="preferences.fontSize === 'small'"
          (click)="updateFontSize('small')">
          Small
        </button>
        <button
          [class.active]="preferences.fontSize === 'medium'"
          (click)="updateFontSize('medium')">
          Medium
        </button>
        <button
          [class.active]="preferences.fontSize === 'large'"
          (click)="updateFontSize('large')">
          Large
        </button>
      </div>
    </section>
  </div>

  <!-- Accessibility Tab -->
  <div *ngIf="activeTab === 'accessibility'" class="settings-content">
    <section class="settings-section">
      <h2>Accessibility Options</h2>

      <div class="toggle-item">
        <div>
          <h3>High Contrast</h3>
          <p>Increase contrast for better visibility</p>
        </div>
        <label class="toggle">
          <input
            type="checkbox"
            [checked]="preferences.highContrast"
            (change)="toggleAccessibility('highContrast')">
          <span class="slider"></span>
        </label>
      </div>

      <div class="toggle-item">
        <div>
          <h3>Reduce Motion</h3>
          <p>Minimize animations and transitions</p>
        </div>
        <label class="toggle">
          <input
            type="checkbox"
            [checked]="preferences.reduceMotion"
            (change)="toggleAccessibility('reduceMotion')">
          <span class="slider"></span>
        </label>
      </div>
    </section>
  </div>

  <!-- Account Tab -->
  <div *ngIf="activeTab === 'account'" class="settings-content">
    <section class="settings-section">
      <h2>Account Settings</h2>
      <p>Account management features coming soon...</p>
    </section>
  </div>
</div>
```

---

## Implementation Steps

### Phase 1: Database Setup (Immediate)

1. Run migration to create `user_preferences` table
2. Set up RLS policies
3. Test CRUD operations

### Phase 2: Refactor Components (Week 1)

1. Replace hardcoded colors with CSS variables
2. Update all components to use `var(--color-name)`
3. Test with different themes

### Phase 3: Settings UI (Week 2)

1. Create settings component
2. Add color picker functionality
3. Implement preset system
4. Test theme switching

### Phase 4: Polish & Testing (Week 3)

1. Add smooth transitions
2. Test accessibility features
3. Mobile responsiveness
4. Cross-browser testing

---

## Usage Examples

### In Components (CSS)

**Before:**
```css
.header {
  background-color: #dff1ef;
  color: #49565d;
}
```

**After:**
```css
.header {
  background-color: var(--brand-background);
  color: var(--brand-text);
}
```

### In TypeScript

```typescript
constructor(private themeService: ThemeService) {}

ngOnInit() {
  // Listen to theme changes
  this.themeService.preferences$.subscribe(prefs => {
    console.log('Theme updated:', prefs);
  });
}

// Apply preset
async applyOceanTheme() {
  await this.themeService.applyPreset('ocean');
}

// Custom colors
async setCustomColors() {
  await this.themeService.updateColors({
    brandPrimary: '#ff6b6b',
    brandSecondary: '#4ecdc4',
    brandBackground: '#ffe66d',
    brandText: '#2c3e50',
    brandAccent: '#ff6b6b'
  });
}
```

---

## Benefits

### For Users
✅ Personalized experience
✅ Brand matching for white-label
✅ Accessibility options
✅ Dark mode support
✅ Synced across devices

### For Developers
✅ Centralized theme management
✅ Easy to maintain
✅ Type-safe color system
✅ Reactive updates
✅ Database-backed persistence

### For Business
✅ Premium feature opportunity
✅ Better user engagement
✅ Accessibility compliance
✅ Professional appearance
✅ White-label ready

---

## Next Steps

1. **Run the database migration** to create the preferences table
2. **Update the theme service** with the enhanced version
3. **Create the settings component** using the provided template
4. **Refactor existing components** to use CSS variables
5. **Test thoroughly** across different themes and browsers

This architecture is scalable, maintainable, and provides a professional user experience with full theme customization capabilities.
