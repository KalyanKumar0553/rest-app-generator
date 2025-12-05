# Quick Theme Reference Card

## 🎨 Your Brand Colors

```
┌─────────────────────────────────────────────────┐
│  BOOTIFY COLOR PALETTE                          │
├─────────────────────────────────────────────────┤
│                                                 │
│  Primary Accent:    #e89b8e  ████  Coral       │
│  Secondary:         #97a9ae  ████  Blue-Gray   │
│  Background:        #dff1ef  ████  Mint        │
│  Text:              #49565d  ████  Slate       │
│                                                 │
└─────────────────────────────────────────────────┘
```

## 📋 Quick Copy-Paste Reference

### Most Used Variables

```css
/* Backgrounds */
--brand-background          /* #dff1ef - Main app background */
--color-background-white    /* #ffffff - Card/panel backgrounds */
--color-background-secondary /* #f5faf9 - Subtle backgrounds */

/* Text Colors */
--brand-text               /* #49565d - Primary text */
--brand-secondary          /* #97a9ae - Secondary text */
--color-text-muted         /* #6b7280 - Disabled/muted text */
--color-text-inverse       /* #ffffff - White text */

/* Accent & Actions */
--brand-accent             /* #e89b8e - Primary buttons */
--brand-accent-hover       /* #d17d6e - Button hover */
--color-primary            /* Same as accent */

/* Borders */
--color-border             /* #e5e7eb - Default borders */
--color-border-light       /* #e5f0ee - Subtle borders */

/* Status Colors */
--color-success            /* #16a34a - Green for success */
--color-warning            /* #d97706 - Yellow for warnings */
--color-error              /* #dc2626 - Red for errors */

/* Neutrals (Common) */
--neutral-100              /* #f3f4f6 - Very light gray */
--neutral-200              /* #e5e7eb - Light gray */
--neutral-500              /* #6b7280 - Medium gray */
--neutral-800              /* #1f2937 - Dark gray */

/* Shadows */
--shadow-sm                /* Subtle shadow */
--shadow-md                /* Medium shadow (most used) */
--shadow-lg                /* Large shadow */
```

## 🔄 Common Replacements

### Buttons
```css
/* Before */
background-color: #e89b8e;
color: #ffffff;

/* After */
background-color: var(--brand-accent);
color: var(--color-text-inverse);
```

### Cards/Panels
```css
/* Before */
background-color: #ffffff;
border: 1px solid #e5e7eb;
box-shadow: 0 4px 6px rgba(0,0,0,0.1);

/* After */
background-color: var(--color-background-white);
border: 1px solid var(--color-border);
box-shadow: var(--shadow-md);
```

### Text
```css
/* Before */
color: #49565d;             /* Primary text */
color: #6b7280;             /* Secondary text */
color: #9ca3af;             /* Muted text */

/* After */
color: var(--brand-text);
color: var(--color-text-muted);
color: var(--neutral-400);
```

### Hover States
```css
/* Before */
background-color: #d17d6e;

/* After */
background-color: var(--brand-accent-hover);
```

### Sidebar/Navigation
```css
/* Before */
background-color: #8b9fa6;
color: #ffffff;

/* After */
background-color: var(--brand-secondary);
color: var(--color-text-inverse);
```

## 🎯 Component-Specific Guide

### Dashboard
```css
.sidebar {
  background: var(--color-background-white);
  border-right: 1px solid var(--color-border);
}

.nav-item {
  color: var(--neutral-500);
}

.nav-item:hover {
  background: var(--neutral-100);
  color: var(--neutral-800);
}

.nav-item.active {
  background: var(--brand-secondary);
  color: var(--color-text-inverse);
}
```

### Header
```css
.header {
  background: var(--color-background-white);
  border-bottom: 1px solid var(--color-border);
}

.nav-link {
  color: var(--brand-text);
}

.nav-link:hover {
  color: var(--brand-accent);
}
```

### Buttons
```css
.btn-primary {
  background: var(--brand-accent);
  color: var(--color-text-inverse);
}

.btn-primary:hover {
  background: var(--brand-accent-hover);
}

.btn-secondary {
  background: var(--brand-secondary);
  color: var(--color-text-inverse);
}

.btn-outline {
  border: 1px solid var(--color-border);
  color: var(--brand-text);
  background: transparent;
}
```

### Forms
```css
input, textarea, select {
  background: var(--color-background-white);
  border: 1px solid var(--color-border);
  color: var(--brand-text);
}

input:focus {
  border-color: var(--brand-accent);
  outline: 2px solid var(--brand-accent);
  outline-offset: 2px;
}

input::placeholder {
  color: var(--color-text-muted);
}

input:disabled {
  background: var(--neutral-100);
  color: var(--neutral-400);
}
```

### Toasts
```css
.toast.success {
  background: var(--success-50);
  border-left: 4px solid var(--color-success);
  color: var(--success-800);
}

.toast.error {
  background: var(--error-50);
  border-left: 4px solid var(--color-error);
  color: var(--error-800);
}

.toast.warning {
  background: var(--warning-50);
  border-left: 4px solid var(--color-warning);
  color: var(--warning-800);
}
```

### Modals
```css
.modal-overlay {
  background: rgba(0, 0, 0, 0.5);
}

.modal-container {
  background: var(--color-background-white);
  box-shadow: var(--shadow-2xl);
  border-radius: 16px;
}

.modal-header {
  border-bottom: 1px solid var(--color-border);
}
```

## 🌙 Dark Theme Preview

When dark theme is enabled:

```css
[data-theme="dark"] {
  --brand-background: #1a1a2e;
  --brand-text: #e5e7eb;
  --brand-accent: #f59e0b;
  --color-background-white: #16213e;
  --color-border: #374151;
}
```

All your components automatically adapt!

## 📱 Responsive Considerations

Variables work with media queries:

```css
@media (max-width: 768px) {
  .header {
    background: var(--color-background-white);
    padding: 12px 16px;
  }
}
```

## ✅ Quick Validation

Search your CSS files for hardcoded colors:

```bash
# Find all hex colors
grep -r "#[0-9a-fA-F]\{6\}" src/app --include="*.css"

# Find rgb/rgba
grep -r "rgba\?(" src/app --include="*.css"
```

Replace with variables from this reference!

## 🚀 Pro Tips

1. **Always use variables** - Never hardcode colors
2. **Use semantic names** - `--color-border` not `--gray-200`
3. **Maintain contrast** - Check text readability
4. **Test dark theme** - All colors should adapt
5. **Consistent spacing** - Use shadow variables

## 🔗 Related Files

- Full implementation: `THEME-CUSTOMIZATION-GUIDE.md`
- Current status: `THEME-CURRENT-STATE.md`
- Variable definitions: `src/app/styles/colors.css`
- Theme service: `src/app/services/theme.service.ts`

---

**Print this reference and keep it handy while refactoring!**
