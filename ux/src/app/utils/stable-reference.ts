/**
 * Stable reference cache for Angular change-detection safety.
 *
 * Angular's default change detection uses reference equality (===) to decide
 * whether an @Input or *ngFor binding has changed. Getters that return a
 * fresh array/object literal on every call force Angular to re-render child
 * views on every CD cycle, potentially causing infinite loops.
 *
 * `stableArray` / `stableValue` compare the new result against a cached
 * version (via JSON serialization). If structurally identical, the **same
 * cached reference** is returned, preventing unnecessary re-renders.
 *
 * Usage:
 *   private _fooCache = emptyCache<string[]>();
 *   get foo(): string[] {
 *     return stableArray(this.items.filter(x => x.enabled), this._fooCache);
 *   }
 */

export interface StableCache<T> {
  ref: T;
  json: string;
}

export function emptyCache<T extends unknown[] | Record<string, unknown>>(): StableCache<T> {
  return { ref: (Array.isArray([] as unknown as T) ? [] : {}) as T, json: '' };
}

export function stableArray<T>(newResult: T[], cache: StableCache<T[]>): T[] {
  const json = JSON.stringify(newResult);
  if (json !== cache.json) {
    cache.json = json;
    cache.ref = newResult;
  }
  return cache.ref;
}
