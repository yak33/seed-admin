<script lang="ts" setup>
import type { DataTableColumns, TreeSelectOption } from 'naive-ui';
import type { SystemMenuApi } from '#/api/system/menu';
import type { VNode } from 'vue';

import { computed, h, onMounted, reactive, ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';

import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NRadioButton,
  NRadioGroup,
  NSpace,
  NTag,
  NTreeSelect,
} from 'naive-ui';

import {
  addMenuApi,
  deleteMenuApi,
  getMenuListApi,
  updateMenuApi,
} from '#/api/system/menu';
import { message } from '#/adapter/naive';

defineOptions({ name: 'SystemMenu' });

const { hasAccessByCodes, hasAccessByRoles } = useAccess();
const loading = ref(false);
const dataList = ref<SystemMenuApi.MenuItem[]>([]);
const modalVisible = ref(false);
const modalTitle = ref('新增菜单');
const submitLoading = ref(false);
const formRef = ref();
const editForm = reactive<SystemMenuApi.MenuForm>({
  icon: 'lucide:menu',
  menuName: '',
  menuType: 'M',
  orderNum: 0,
  parentId: '0',
  status: '0',
  visible: '0',
});
const formRules = {
  menuName: { message: '请输入菜单名称', required: true, trigger: 'blur' },
  parentId: { message: '请选择父菜单', required: true, trigger: 'change' },
};
const canManage = computed(() => hasAccessByRoles(['super']));

async function loadMenuList() {
  loading.value = true;
  try {
    dataList.value = await getMenuListApi();
  } finally {
    loading.value = false;
  }
}

function filterTree(nodes: SystemMenuApi.MenuItem[], type: string): TreeSelectOption[] {
  const result: TreeSelectOption[] = [];
  for (const node of nodes) {
    if (node.menuType !== type) {
      continue;
    }
    result.push({
      children: filterTree(node.children ?? [], type),
      menuId: node.menuId,
      menuName: node.menuName,
    });
  }
  return result;
}

const parentOptions = computed<TreeSelectOption[]>(() => {
  if (editForm.menuType === 'F') {
    return filterTree(dataList.value, 'C');
  }
  const directories = filterTree(dataList.value, 'M');
  return editForm.menuType === 'M'
    ? [{ children: directories, menuId: '0', menuName: '根目录' }]
    : directories;
});

function resetForm() {
  Object.assign(editForm, {
    component: undefined,
    icon: 'lucide:menu',
    menuId: undefined,
    menuName: '',
    menuType: 'M',
    orderNum: 0,
    parentId: '0',
    path: undefined,
    perms: undefined,
    routeName: undefined,
    status: '0',
    visible: '0',
  });
}

function openAddModal(row?: SystemMenuApi.MenuItem) {
  resetForm();
  if (row) {
    editForm.menuType = row.menuType === 'M' ? 'C' : 'F';
    editForm.parentId = row.menuId;
  }
  modalTitle.value = row ? `新增下级 - ${row.menuName}` : '新增根目录';
  modalVisible.value = true;
}

function openEditModal(row: SystemMenuApi.MenuItem) {
  Object.assign(editForm, {
    component: row.component,
    icon: row.icon ?? '#',
    menuId: row.menuId,
    menuName: row.menuName,
    menuType: row.menuType,
    orderNum: row.orderNum,
    parentId: row.parentId,
    path: row.path,
    perms: row.perms,
    routeName: row.routeName,
    status: row.status,
    visible: row.visible,
  });
  modalTitle.value = `编辑菜单 - ${row.menuName}`;
  modalVisible.value = true;
}

function handleTypeChange() {
  editForm.parentId = editForm.menuType === 'M' ? '0' : undefined;
  editForm.routeName = undefined;
  editForm.path = undefined;
  editForm.component = undefined;
  editForm.perms = undefined;
}

function validateConditionalFields() {
  if (editForm.menuType !== 'F' && !editForm.routeName) {
    message.error('请输入路由名称');
    return false;
  }
  if (editForm.menuType !== 'F' && !editForm.path) {
    message.error('请输入路由地址');
    return false;
  }
  if (editForm.menuType === 'C' && !editForm.component) {
    message.error('请输入组件路径');
    return false;
  }
  if (editForm.menuType === 'F' && !editForm.perms) {
    message.error('请输入按钮权限标识');
    return false;
  }
  return true;
}

async function handleSubmit() {
  await formRef.value?.validate();
  if (!validateConditionalFields()) {
    return;
  }
  submitLoading.value = true;
  try {
    if (editForm.menuId) {
      await updateMenuApi(editForm);
      message.success('修改成功');
    } else {
      await addMenuApi(editForm);
      message.success('新增成功');
    }
    modalVisible.value = false;
    await loadMenuList();
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: SystemMenuApi.MenuItem) {
  await deleteMenuApi(row.menuId);
  message.success('删除成功');
  loadMenuList();
}

function typeName(type: string) {
  return type === 'M' ? '目录' : type === 'C' ? '页面' : '按钮';
}

const columns: DataTableColumns<SystemMenuApi.MenuItem> = [
  { key: 'menuName', title: '菜单名称', minWidth: 140 },
  {
    key: 'menuType',
    title: '类型',
    width: 70,
    render(row) {
      return h(NTag, { size: 'small' }, { default: () => typeName(row.menuType) });
    },
  },
  { key: 'routeName', title: '路由名称', minWidth: 110 },
  { key: 'path', title: '路由地址', minWidth: 100 },
  { key: 'perms', title: '权限标识', minWidth: 150 },
  { key: 'orderNum', title: '排序', width: 65 },
  {
    key: 'visible',
    title: '显示',
    width: 65,
    render(row) {
      return row.visible === '0' ? '显示' : '隐藏';
    },
  },
  {
    key: 'status',
    title: '状态',
    width: 65,
    render(row) {
      return row.status === '0' ? '正常' : '停用';
    },
  },
  {
    key: 'action',
    title: '操作',
    width: 220,
    render(row) {
      const actions: VNode[] = [];
      if (canManage.value && hasAccessByCodes(['system:menu:add']) && row.menuType !== 'F') {
        actions.push(h(NButton, {
          size: 'small', tertiary: true, onClick: () => openAddModal(row),
        }, { default: () => '新增下级' }));
      }
      if (canManage.value && hasAccessByCodes(['system:menu:edit'])) {
        actions.push(h(NButton, {
          size: 'small', tertiary: true, type: 'primary', onClick: () => openEditModal(row),
        }, { default: () => '编辑' }));
      }
      if (canManage.value && hasAccessByCodes(['system:menu:remove'])) {
        actions.push(h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row),
        }, {
          trigger: () => h(NButton, {
            disabled: (row.children?.length ?? 0) > 0,
            size: 'small', tertiary: true, type: 'error',
          }, { default: () => '删除' }),
          default: () => `确认删除「${row.menuName}」吗？`,
        }));
      }
      return h(NSpace, null, { default: () => actions });
    },
  },
];

onMounted(loadMenuList);
</script>

<template>
  <Page description="维护目录、页面和按钮权限；写操作仅超级管理员可用" title="菜单管理">
    <div class="mb-4 flex justify-end">
      <div v-if="canManage" v-access:code="'system:menu:add'">
        <NButton type="primary" @click="openAddModal()">新增根目录</NButton>
      </div>
    </div>
    <NDataTable
      :columns="columns"
      :data="dataList"
      :loading="loading"
      :pagination="false"
      :row-key="(row: SystemMenuApi.MenuItem) => row.menuId"
      default-expand-all
    />

    <NModal v-model:show="modalVisible" :title="modalTitle" preset="card" style="width: 560px">
      <NForm ref="formRef" :model="editForm" :rules="formRules" label-placement="left" label-width="90">
        <NFormItem label="菜单类型" path="menuType">
          <NRadioGroup v-model:value="editForm.menuType" @update:value="handleTypeChange">
            <NRadioButton value="M" label="目录" />
            <NRadioButton value="C" label="页面" />
            <NRadioButton value="F" label="按钮" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="父菜单" path="parentId">
          <NTreeSelect
            v-model:value="editForm.parentId"
            :options="parentOptions"
            children-field="children"
            default-expand-all
            key-field="menuId"
            label-field="menuName"
            placeholder="请选择父菜单"
          />
        </NFormItem>
        <NFormItem label="菜单名称" path="menuName">
          <NInput v-model:value="editForm.menuName" placeholder="请输入菜单名称" />
        </NFormItem>
        <NFormItem label="显示顺序" path="orderNum">
          <NInputNumber v-model:value="editForm.orderNum" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType !== 'F'" label="路由名称" path="routeName">
          <NInput v-model:value="editForm.routeName" placeholder="全局唯一，例如 SystemMenu" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType !== 'F'" label="路由地址" path="path">
          <NInput v-model:value="editForm.path" :placeholder="editForm.menuType === 'M' ? '/system' : 'menu'" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType === 'C'" label="组件路径" path="component">
          <NInput v-model:value="editForm.component" placeholder="/system/menu/index" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType !== 'M'" label="权限标识" path="perms">
          <NInput v-model:value="editForm.perms" placeholder="例如 system:menu:list" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType !== 'F'" label="图标" path="icon">
          <NInput v-model:value="editForm.icon" placeholder="例如 lucide:menu" />
        </NFormItem>
        <NFormItem v-if="editForm.menuType !== 'F'" label="显示状态" path="visible">
          <NRadioGroup v-model:value="editForm.visible">
            <NRadioButton value="0" label="显示" />
            <NRadioButton value="1" label="隐藏" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="菜单状态" path="status">
          <NRadioGroup v-model:value="editForm.status">
            <NRadioButton value="0" label="正常" />
            <NRadioButton value="1" label="停用" />
          </NRadioGroup>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton :loading="submitLoading" type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </Page>
</template>
