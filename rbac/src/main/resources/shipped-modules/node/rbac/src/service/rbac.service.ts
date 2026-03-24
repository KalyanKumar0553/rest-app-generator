import type { RbacModuleConfig } from '../config/rbac-config';
import type { RoleModel, UserRoleModel } from '../model/rbac.model';
import { RbacRepository } from '../repository/rbac.repository';

export class RbacService {
  constructor(
    private readonly config: Required<RbacModuleConfig>,
    private readonly rbacRepository: RbacRepository
  ) {}

  async ensureSeedData(): Promise<void> {
    await this.rbacRepository.seedRoles(this.config.roles, this.config.permissions);
  }

  async listRoles(): Promise<RoleModel[]> {
    await this.ensureSeedData();
    return this.rbacRepository.listRoles();
  }

  async assignRole(userId: string, role: string): Promise<UserRoleModel> {
    await this.ensureSeedData();
    return this.rbacRepository.assignRole(userId, role);
  }

  async currentRole(userId: string): Promise<string> {
    await this.ensureSeedData();
    return (await this.rbacRepository.currentRole(userId)) ?? this.config.defaultRole;
  }
}
