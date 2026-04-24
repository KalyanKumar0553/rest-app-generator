import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ShippedModuleConfigPanelComponent } from '../shipped-module-config-panel/shipped-module-config-panel.component';
import { ProjectGenerationStateService } from '../../services/project-generation-state.service';

type RbacRoleDraft = {
  code: string;
  displayName: string;
  description: string;
  systemRole: boolean;
  active: boolean;
};

type RbacPermissionDraft = {
  code: string;
  displayName: string;
  description: string;
  category: string;
  active: boolean;
};

type RbacMappingDraft = {
  roleCode: string;
  permissionCodesInput: string;
};

type RbacConfigDraft = {
  defaultRole: string;
  roles: RbacRoleDraft[];
  permissions: RbacPermissionDraft[];
  rolePermissions: RbacMappingDraft[];
  routes: Array<{
    pathPattern: string;
    httpMethod: string;
    authoritiesInput: string;
    priority: number;
    active: boolean;
  }>;
};

@Component({
  selector: 'app-rbac-module-tab',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    ShippedModuleConfigPanelComponent
  ],
  templateUrl: './rbac-module-tab.component.html',
  styleUrls: ['./rbac-module-tab.component.css']
})
export class RbacModuleTabComponent implements OnInit {
  config: RbacConfigDraft = this.buildDefaultConfig();

  constructor(private readonly projectGenerationState: ProjectGenerationStateService) {}

  ngOnInit(): void {
    const savedConfig = this.projectGenerationState.getModuleConfigsSnapshot()['rbac'];
    this.config = this.normalizeConfig(savedConfig);
  }

  addRole(): void {
    this.config = {
      ...this.config,
      roles: [
        ...this.config.roles,
        {
          code: '',
          displayName: '',
          description: '',
          systemRole: false,
          active: true
        }
      ]
    };
    this.persist();
  }

  removeRole(index: number): void {
    const removedCode = this.config.roles[index]?.code?.trim().toUpperCase() ?? '';
    const roles = this.config.roles.filter((_, roleIndex) => roleIndex !== index);
    this.config = {
      ...this.config,
      roles,
      rolePermissions: this.config.rolePermissions.filter((mapping) => mapping.roleCode.trim().toUpperCase() !== removedCode)
    };
    if (this.config.defaultRole.trim().toUpperCase() === removedCode) {
      this.config.defaultRole = roles[0]?.code ?? '';
    }
    this.persist();
  }

  addPermission(): void {
    this.config = {
      ...this.config,
      permissions: [
        ...this.config.permissions,
        {
          code: '',
          displayName: '',
          description: '',
          category: 'RBAC',
          active: true
        }
      ]
    };
    this.persist();
  }

  removePermission(index: number): void {
    const removedCode = this.config.permissions[index]?.code?.trim() ?? '';
    this.config = {
      ...this.config,
      permissions: this.config.permissions.filter((_, permissionIndex) => permissionIndex !== index),
      rolePermissions: this.config.rolePermissions.map((mapping) => ({
        ...mapping,
        permissionCodesInput: mapping.permissionCodesInput
          .split(',')
          .map((item) => item.trim())
          .filter((item) => item && item !== removedCode)
          .join(', ')
      }))
    };
    this.persist();
  }

  addMapping(): void {
    this.config = {
      ...this.config,
      rolePermissions: [
        ...this.config.rolePermissions,
        {
          roleCode: this.config.roles[0]?.code ?? '',
          permissionCodesInput: ''
        }
      ]
    };
    this.persist();
  }

  addRoute(): void {
    this.config = {
      ...this.config,
      routes: [
        ...this.config.routes,
        {
          pathPattern: '/api/v1/example/**',
          httpMethod: 'GET',
          authoritiesInput: '',
          priority: 50,
          active: true
        }
      ]
    };
    this.persist();
  }

  removeRoute(index: number): void {
    this.config = {
      ...this.config,
      routes: this.config.routes.filter((_, routeIndex) => routeIndex !== index)
    };
    this.persist();
  }

  removeMapping(index: number): void {
    this.config = {
      ...this.config,
      rolePermissions: this.config.rolePermissions.filter((_, mappingIndex) => mappingIndex !== index)
    };
    this.persist();
  }

  onConfigChange(): void {
    this.persist();
  }

  trackByIndex(index: number): number {
    return index;
  }

  private persist(): void {
    const roles = this.config.roles
      .map((role) => ({
        code: role.code.trim().toUpperCase(),
        displayName: role.displayName.trim() || role.code.trim(),
        description: role.description.trim(),
        systemRole: role.systemRole,
        active: role.active
      }))
      .filter((role) => role.code);
    const permissions = this.config.permissions
      .map((permission) => ({
        code: permission.code.trim(),
        displayName: permission.displayName.trim() || permission.code.trim(),
        description: permission.description.trim(),
        category: permission.category.trim() || 'RBAC',
        active: permission.active
      }))
      .filter((permission) => permission.code);
    const rolePermissions = this.config.rolePermissions
      .map((mapping) => ({
        roleCode: mapping.roleCode.trim().toUpperCase(),
        permissionCodes: mapping.permissionCodesInput
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean)
      }))
      .filter((mapping) => mapping.roleCode && mapping.permissionCodes.length);

    this.projectGenerationState.updateModuleConfig('rbac', {
      defaultRole: this.config.defaultRole.trim().toUpperCase() || roles[0]?.code || 'ROLE_USER',
      roles,
      permissions,
      rolePermissions,
      routes: this.config.routes
        .map((route) => ({
          pathPattern: route.pathPattern.trim(),
          httpMethod: route.httpMethod.trim().toUpperCase() || null,
          authorities: route.authoritiesInput.split(',').map((item) => item.trim()).filter(Boolean),
          priority: Number(route.priority || 100),
          active: route.active
        }))
        .filter((route) => route.pathPattern && route.authorities.length)
    });
  }

  private normalizeConfig(rawConfig: Record<string, any> | undefined): RbacConfigDraft {
    const defaults = this.buildDefaultConfig();
    if (!rawConfig || typeof rawConfig !== 'object') {
      this.persist();
      return defaults;
    }
    const roles = Array.isArray(rawConfig['roles']) && rawConfig['roles'].length
      ? rawConfig['roles'].map((role) => ({
          code: String((role as Record<string, unknown>)['code'] ?? '').trim().toUpperCase(),
          displayName: String((role as Record<string, unknown>)['displayName'] ?? '').trim(),
          description: String((role as Record<string, unknown>)['description'] ?? '').trim(),
          systemRole: Boolean((role as Record<string, unknown>)['systemRole']),
          active: typeof (role as Record<string, unknown>)['active'] === 'boolean' ? Boolean((role as Record<string, unknown>)['active']) : true
        }))
      : defaults.roles;
    const permissions = Array.isArray(rawConfig['permissions']) && rawConfig['permissions'].length
      ? rawConfig['permissions'].map((permission) => ({
          code: String((permission as Record<string, unknown>)['code'] ?? '').trim(),
          displayName: String((permission as Record<string, unknown>)['displayName'] ?? '').trim(),
          description: String((permission as Record<string, unknown>)['description'] ?? '').trim(),
          category: String((permission as Record<string, unknown>)['category'] ?? 'RBAC').trim(),
          active: typeof (permission as Record<string, unknown>)['active'] === 'boolean' ? Boolean((permission as Record<string, unknown>)['active']) : true
        }))
      : defaults.permissions;
    const rolePermissions = Array.isArray(rawConfig['rolePermissions']) && rawConfig['rolePermissions'].length
      ? rawConfig['rolePermissions'].map((mapping) => ({
          roleCode: String((mapping as Record<string, unknown>)['roleCode'] ?? '').trim().toUpperCase(),
          permissionCodesInput: Array.isArray((mapping as Record<string, unknown>)['permissionCodes'])
            ? ((mapping as Record<string, unknown>)['permissionCodes'] as unknown[])
                .map((item) => String(item ?? '').trim())
                .filter(Boolean)
                .join(', ')
            : ''
        }))
      : defaults.rolePermissions;
    const normalized: RbacConfigDraft = {
      defaultRole: String(rawConfig['defaultRole'] ?? roles[0]?.code ?? defaults.defaultRole).trim().toUpperCase(),
      roles,
      permissions,
      rolePermissions,
      routes: Array.isArray(rawConfig['routes']) && rawConfig['routes'].length
        ? rawConfig['routes'].map((route) => ({
            pathPattern: String((route as Record<string, unknown>)['pathPattern'] ?? '').trim(),
            httpMethod: String((route as Record<string, unknown>)['httpMethod'] ?? '').trim().toUpperCase(),
            authoritiesInput: Array.isArray((route as Record<string, unknown>)['authorities'])
              ? ((route as Record<string, unknown>)['authorities'] as unknown[])
                  .map((item) => String(item ?? '').trim())
                  .filter(Boolean)
                  .join(', ')
              : '',
            priority: Number((route as Record<string, unknown>)['priority'] ?? 100),
            active: typeof (route as Record<string, unknown>)['active'] === 'boolean'
              ? Boolean((route as Record<string, unknown>)['active'])
              : true
          }))
        : defaults.routes
    };
    this.projectGenerationState.updateModuleConfig('rbac', {
      defaultRole: normalized.defaultRole,
      roles: normalized.roles,
      permissions: normalized.permissions,
      rolePermissions: normalized.rolePermissions.map((mapping) => ({
        roleCode: mapping.roleCode,
        permissionCodes: mapping.permissionCodesInput.split(',').map((item) => item.trim()).filter(Boolean)
      })),
      routes: normalized.routes.map((route) => ({
        pathPattern: route.pathPattern,
        httpMethod: route.httpMethod || null,
        authorities: route.authoritiesInput.split(',').map((item) => item.trim()).filter(Boolean),
        priority: route.priority,
        active: route.active
      }))
    });
    return normalized;
  }

  private buildDefaultConfig(): RbacConfigDraft {
    return {
      defaultRole: 'ROLE_USER',
      roles: [
        {
          code: 'ROLE_USER',
          displayName: 'User',
          description: 'Standard authenticated access.',
          systemRole: true,
          active: true
        },
        {
          code: 'ROLE_ADMIN',
          displayName: 'Admin',
          description: 'Administrative access for managed operations.',
          systemRole: true,
          active: true
        }
      ],
      permissions: [
        {
          code: 'project.read',
          displayName: 'View Projects',
          description: 'Read project details.',
          category: 'PROJECT',
          active: true
        },
        {
          code: 'project.manage',
          displayName: 'Manage Projects',
          description: 'Create and update project definitions.',
          category: 'PROJECT',
          active: true
        }
      ],
      rolePermissions: [
        {
          roleCode: 'ROLE_USER',
          permissionCodesInput: 'project.read'
        },
        {
          roleCode: 'ROLE_ADMIN',
          permissionCodesInput: 'project.read, project.manage'
        }
      ],
      routes: [
        {
          pathPattern: '/api/v1/projects/**',
          httpMethod: 'GET',
          authoritiesInput: 'project.read',
          priority: 50,
          active: true
        },
        {
          pathPattern: '/api/v1/projects/**',
          httpMethod: 'POST',
          authoritiesInput: 'project.manage',
          priority: 50,
          active: true
        }
      ]
    };
  }
}
