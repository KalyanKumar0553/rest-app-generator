export type RoleModel = {
  code: string;
  displayName?: string;
  description?: string;
  systemRole?: boolean;
  active?: boolean;
  permissions: string[];
};

export type UserRoleModel = {
  userId: string;
  role: string;
};
