# UI Confirmation Toggle Pattern

When a checkbox change requires confirmation (destructive/off switch), do not use two-way binding.

Use:
- `[ngModel]="state"`
- `(ngModelChange)="onToggleRequested($event)"`

Behavior:
- If user toggles ON: apply immediately.
- If user toggles OFF: keep the checkbox visually ON, open confirmation modal.
- On modal `Continue`: commit OFF state and run cleanup.
- On modal `Cancel`: keep ON state (no cleanup).

Reason:
- Prevents accidental state drift where `[(ngModel)]` commits unchecked value before confirmation result.

## Material Checkbox Reliability (Mobile + Desktop)

Preferred implementation:
- Use `(change)="onToggleRequested($event)"` with `MatCheckboxChange`.
- Store `event.source` temporarily.
- Before modal decision, keep source checked (`event.source.checked = true`) and keep model state enabled.
- On confirm: set model false, set `event.source.checked = false`, run cleanup.
- On cancel: set model true, set `event.source.checked = true`, call `ChangeDetectorRef.detectChanges()`.

Why:
- Some mobile flows visually uncheck even when model is reset unless source checkbox is explicitly restored.
