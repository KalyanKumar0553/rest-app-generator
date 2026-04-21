export type RoleDto = {
  name: string;
  permissions: string[];
};

export type RoleAssignmentDto = {
  userId: string;
  role: string;
};
