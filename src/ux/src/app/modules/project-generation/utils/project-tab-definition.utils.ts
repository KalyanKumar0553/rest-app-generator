import { NavItem } from '../../../components/shared/sidenav/sidenav.component';
import { ProjectTabDefinition } from '../../../services/project.service';

const HIDDEN_MODULE_TAB_KEYS = new Set(['shipping']);

export interface ProjectDashboardNavConfig {
  baseNavItems: NavItem[];
  controllersNavItem: NavItem;
  mappersNavItem: NavItem;
  actuatorNavItem?: NavItem;
  moduleNavItems: NavItem[];
}

function buildNavItem(
  key: string,
  fallback: NavItem,
  byKey: Map<string, ProjectTabDefinition>
): NavItem {
  const tab = byKey.get(key);
  return tab
    ? { icon: tab.icon || fallback.icon, label: tab.label || fallback.label, value: tab.key || fallback.value }
    : fallback;
}

export function buildProjectDashboardNavConfig(
  tabDetails: ProjectTabDefinition[],
  baseFallbacks: NavItem[],
  controllersFallback: NavItem,
  mappersFallback: NavItem,
  actuatorFallback?: NavItem
): ProjectDashboardNavConfig {
  const byKey = new Map(tabDetails.map((tab) => [tab.key, tab]));
  const reservedKeys = new Set(['general', 'actuator', 'entities', 'data-objects', 'mappers', 'modules', 'controllers', 'collaborate', 'explore']);
  const baseNavItems = ['general', 'entities', 'data-objects', 'modules', 'collaborate', 'explore']
    .map((key) => {
      const fallback = baseFallbacks.find((item) => item.value === key);
      return fallback ? buildNavItem(key, fallback, byKey) : null;
    })
    .filter((item): item is NavItem => Boolean(item));
  const moduleNavItems = [...tabDetails]
    .filter((tab) => !reservedKeys.has(tab.key) && !HIDDEN_MODULE_TAB_KEYS.has(tab.key))
    .sort((left, right) => left.order - right.order)
    .map((tab) => ({
      icon: tab.icon,
      label: tab.label,
      value: tab.key
    }));

  return {
    baseNavItems,
    controllersNavItem: buildNavItem('controllers', controllersFallback, byKey),
    mappersNavItem: buildNavItem('mappers', mappersFallback, byKey),
    actuatorNavItem: actuatorFallback ? buildNavItem('actuator', actuatorFallback, byKey) : undefined,
    moduleNavItems
  };
}
