import type { SystemMenuApi } from '#/api/system/menu';

import { requestClient } from '#/api/request';

/** 角色管理接口 */
export namespace SystemRoleApi {
  export interface Role {
    createTime?: string;
    remark?: string;
    roleId: string;
    roleKey: string;
    roleName: string;
    roleSort: number;
    status: '0' | '1';
  }

  export interface RoleForm {
    menuIds: string[];
    remark?: string;
    roleId?: string;
    roleKey: string;
    roleName: string;
    roleSort: number;
    status: '0' | '1';
  }

  export interface RoleDetail extends Role {
    menuIds: string[];
  }

  export interface RolePage {
    rows: Role[];
    total: number;
  }
}

/** 分页查询角色列表 */
export async function getRolePageApi(params: {
  pageNum: number;
  pageSize: number;
  roleKey?: string;
  roleName?: string;
  status?: string;
}) {
  return requestClient.get<SystemRoleApi.RolePage>('/system/role/list', {
    params,
  });
}

/** 查询角色详情及已分配菜单 */
export async function getRoleDetailApi(roleId: string) {
  return requestClient.get<SystemRoleApi.RoleDetail>(`/system/role/${roleId}`);
}

/** 查询可分配给角色的菜单树 */
export async function getRoleMenuTreeApi() {
  return requestClient.get<SystemMenuApi.MenuItem[]>('/system/role/menu/tree');
}

/** 新增角色 */
export async function addRoleApi(data: SystemRoleApi.RoleForm) {
  return requestClient.post('/system/role', data);
}

/** 修改角色 */
export async function updateRoleApi(data: SystemRoleApi.RoleForm) {
  return requestClient.put('/system/role', data);
}

/** 批量删除角色 */
export async function deleteRoleApi(roleIds: string[]) {
  return requestClient.delete(`/system/role/${roleIds.join(',')}`);
}
