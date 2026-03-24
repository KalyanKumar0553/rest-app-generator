import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { AdminService, DataEncryptionRule, DataEncryptionRulePayload } from '../../../../services/admin.service';
import { ToastService } from '../../../../services/toast.service';
import { UserService, UserRoles } from '../../../../services/user.service';

interface DataEncryptionRuleFormModel {
  id: string | null;
  tableName: string;
  columnName: string;
  hashShadowColumn: string;
  enabled: boolean;
}

@Component({
  selector: 'app-settings-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule],
  templateUrl: './settings-panel.component.html',
  styleUrls: ['./settings-panel.component.css']
})
export class SettingsPanelComponent implements OnInit {
  userRoles: string[] = [];
  userPermissions: string[] = [];
  encryptionRules: DataEncryptionRule[] = [];
  isLoadingEncryptionRules = false;
  isSavingEncryptionRule = false;
  encryptionRuleColumns: string[] = ['tableName', 'columnName', 'hashShadowColumn', 'enabled', 'updatedAt', 'actions'];
  encryptionRuleForm: DataEncryptionRuleFormModel = this.createEmptyEncryptionRuleForm();

  constructor(
    private userService: UserService,
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.userService.getUserRoles().subscribe({
      next: (rolesData: UserRoles) => {
        this.userRoles = rolesData.roles || [];
        this.userPermissions = rolesData.permissions || [];
        if (this.canManageEncryptionRules()) {
          this.loadEncryptionRules();
        }
      },
      error: () => {
        this.toastService.error('Failed to load user roles');
      }
    });
  }

  hasPermission(permission: string): boolean {
    return this.userPermissions.includes(permission);
  }

  canManageEncryptionRules(): boolean {
    return this.hasPermission('config.encryption.read') || this.hasPermission('config.encryption.manage');
  }

  canEditEncryptionRules(): boolean {
    return this.hasPermission('config.encryption.manage');
  }

  loadEncryptionRules(): void {
    if (!this.canManageEncryptionRules()) {
      return;
    }
    this.isLoadingEncryptionRules = true;
    this.adminService.getDataEncryptionRules().subscribe({
      next: (rules) => {
        this.encryptionRules = Array.isArray(rules) ? rules : [];
        this.isLoadingEncryptionRules = false;
      },
      error: () => {
        this.isLoadingEncryptionRules = false;
        this.toastService.error('Failed to load data encryption rules.');
      }
    });
  }

  editEncryptionRule(rule: DataEncryptionRule): void {
    this.encryptionRuleForm = {
      id: rule.id,
      tableName: rule.tableName || '',
      columnName: rule.columnName || '',
      hashShadowColumn: rule.hashShadowColumn || '',
      enabled: !!rule.enabled
    };
  }

  resetEncryptionRuleForm(): void {
    this.encryptionRuleForm = this.createEmptyEncryptionRuleForm();
  }

  saveEncryptionRule(): void {
    if (!this.canEditEncryptionRules()) {
      return;
    }
    const payload = this.buildEncryptionRulePayload();
    if (!payload.tableName.trim()) {
      this.toastService.error('Table name is required.');
      return;
    }
    this.isSavingEncryptionRule = true;
    const request$ = this.encryptionRuleForm.id
      ? this.adminService.updateDataEncryptionRule(this.encryptionRuleForm.id, payload)
      : this.adminService.createDataEncryptionRule(payload);
    request$.subscribe({
      next: () => {
        this.isSavingEncryptionRule = false;
        this.toastService.success(this.encryptionRuleForm.id ? 'Encryption rule updated.' : 'Encryption rule created.');
        this.resetEncryptionRuleForm();
        this.loadEncryptionRules();
      },
      error: (error) => {
        this.isSavingEncryptionRule = false;
        this.toastService.error(error?.message || 'Failed to save encryption rule.');
      }
    });
  }

  deleteEncryptionRule(rule: DataEncryptionRule): void {
    if (!this.canEditEncryptionRules()) {
      return;
    }
    this.adminService.deleteDataEncryptionRule(rule.id).subscribe({
      next: () => {
        this.toastService.success('Encryption rule deleted.');
        if (this.encryptionRuleForm.id === rule.id) {
          this.resetEncryptionRuleForm();
        }
        this.loadEncryptionRules();
      },
      error: (error) => {
        this.toastService.error(error?.message || 'Failed to delete encryption rule.');
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(',', '');
  }

  private buildEncryptionRulePayload(): DataEncryptionRulePayload {
    return {
      tableName: this.encryptionRuleForm.tableName.trim(),
      columnName: this.encryptionRuleForm.columnName.trim() || null,
      hashShadowColumn: this.encryptionRuleForm.hashShadowColumn.trim() || null,
      enabled: this.encryptionRuleForm.enabled
    };
  }

  private createEmptyEncryptionRuleForm(): DataEncryptionRuleFormModel {
    return {
      id: null,
      tableName: '',
      columnName: '',
      hashShadowColumn: '',
      enabled: true
    };
  }
}
