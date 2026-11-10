import { requestClient } from '#/api/request';

/** 菜单管理接口 */
export namespace SystemMenuApi {
  export type MenuType = 'C' | 'F' | 'M';

  export interface MenuItem {
    children?: MenuItem[];
    component?: string;
    icon?: string;
    menuId: string;
    menuName: string;
    menuType: MenuType;
    orderNum: number;
    parentId: string;
    path?: string;
    perms?: string;
    routeName?: string;
    status: '0' | '1';
    visible: '0' | '1';
  }

  export interface MenuForm {
    component?: string;
    icon?: string;
    menuId?: string;
    menuName: string;
    menuType: MenuType;
    orderNum: number;
    parentId?: string;
    path?: string;
    perms?: string;
    routeName?: string;
    status: '0' | '1';
    visible: '0' | '1';
  }
}

/** 查询菜单树 */
export async function getMenuListApi() {
  return requestClient.get<SystemMenuApi.MenuItem[]>('/system/menu/list');
}

/** 新增菜单 */
export async function addMenuApi(data: SystemMenuApi.MenuForm) {
  return requestClient.post('/system/menu', data);
}

/** 修改菜单 */
export async function updateMenuApi(data: SystemMenuApi.MenuForm) {
  return requestClient.put('/system/menu', data);
}

/** 删除菜单 */
export async function deleteMenuApi(menuId: string) {
  return requestClient.delete(`/system/menu/${menuId}`);
}
