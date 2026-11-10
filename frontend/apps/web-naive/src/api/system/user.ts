import { requestClient } from '#/api/request';

/**
 * 用户管理接口（对应 seed-backend SysUserController）
 */
export namespace SystemUserApi {
  /** 用户列表行 */
  export interface SysUser {
    createTime: string;
    email: string;
    nickName: string;
    phonenumber: string;
    remark: string;
    roleIds: string[];
    roleNames: string[];
    sex: string;
    status: string;
    userId: string;
    userName: string;
  }

  /** 用户可分配角色，super 角色不会下发 */
  export interface RoleOption {
    roleId: string;
    roleKey: string;
    roleName: string;
  }

  /** 分页响应（后端 TableDataInfo，平级返回不带 code） */
  export interface UserPage {
    rows: SysUser[];
    total: number;
  }

  /** 新增/修改入参（userId 为空表示新增） */
  export interface SysUserForm {
    email?: string;
    nickName: string;
    password?: string;
    phonenumber?: string;
    remark?: string;
    roleIds: string[];
    sex?: string;
    status?: string;
    userId?: string;
    userName: string;
  }
}

/** 分页查询用户列表 */
export async function getUserPageApi(params: {
  pageNum: number;
  pageSize: number;
  phonenumber?: string;
  status?: string;
  userName?: string;
}) {
  return requestClient.get<SystemUserApi.UserPage>('/system/user/list', {
    params,
  });
}

/** 获取当前用户可分配的角色选项 */
export async function getUserRoleOptionsApi() {
  return requestClient.get<SystemUserApi.RoleOption[]>(
    '/system/user/role/options',
  );
}

/** 新增用户（密码留空时后端默认 123456） */
export async function addUserApi(data: SystemUserApi.SysUserForm) {
  return requestClient.post('/system/user', data);
}

/** 修改用户（密码留空表示不修改） */
export async function updateUserApi(data: SystemUserApi.SysUserForm) {
  return requestClient.put('/system/user', data);
}

/** 批量删除用户 */
export async function deleteUserApi(userIds: string[]) {
  return requestClient.delete(`/system/user/${userIds.join(',')}`);
}
