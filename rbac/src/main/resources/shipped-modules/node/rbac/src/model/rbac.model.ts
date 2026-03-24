export type RoleModel = {
  name: string;
  permissions: string[];
};

export type UserRoleModel = {
  userId: string;
  role: string;
};
