<script lang="ts" setup>
import type { DataTableColumns, FormRules } from 'naive-ui';
import type { SystemMenuApi } from '#/api/system/menu';
import type { SystemRoleApi } from '#/api/system/role';
import type { VNode } from 'vue';

import { h, onMounted, reactive, ref } from 'vue';

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
  NSelect,
  NSpace,
  NTag,
  NTree,
} from 'naive-ui';

import {
  addRoleApi,
  deleteRoleApi,
  getRoleDetailApi,
  getRoleMenuTreeApi,
  getRolePageApi,
  updateRoleApi,
} from '#/api/system/role';
import { message } from '#/adapter/naive';

defineOptions({ name: 'SystemRole' });

const { hasAccessByCodes } = useAccess();
const loading = ref(false);
const dataList = ref<SystemRoleApi.Role[]>([]);
const total = ref(0);
const searchForm = reactive({ roleKey: '', roleName: '', status: null });
const pagination = reactive({ page: 1, pageSize: 10 });
const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' },
];

async function loadRoleList() {
  loading.value = true;
  try {
    const page = await getRolePageApi({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      roleKey: searchForm.roleKey || undefined,
      roleName: searchForm.roleName || undefined,
      status: searchForm.status ?? undefined,
    });
    dataList.value = page.rows;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  loadRoleList();
}

function handleReset() {
  searchForm.roleName = '';
  searchForm.roleKey = '';
  searchForm.status = null;
  handleSearch();
}

const modalVisible = ref(false);
const modalTitle = ref('新增角色');
const submitLoading = ref(false);
const formRef = ref();
const menuTree = ref<SystemMenuApi.MenuItem[]>([]);
const menuTreeLoading = ref(false);
const editForm = reactive<SystemRoleApi.RoleForm>({
  menuIds: [],
  remark: '',
  roleId: undefined,
  roleKey: '',
  roleName: '',
  roleSort: 0,
  status: '0',
});
const formRules: FormRules = {
  menuIds: {
    message: '请选择菜单权限',
    required: true,
    trigger: 'change',
    type: 'array',
  },
  roleKey: { message: '请输入权限字符', required: true, trigger: 'blur' },
  roleName: { message: '请输入角色名称', required: true, trigger: 'blur' },
  roleSort: {
    message: '请输入显示顺序',
    required: true,
    trigger: ['blur', 'change'],
    type: 'number',
  },
};

async function loadMenuTree() {
  menuTreeLoading.value = true;
  try {
    menuTree.value = await getRoleMenuTreeApi();
  } finally {
    menuTreeLoading.value = false;
  }
}

function openAddModal() {
  modalTitle.value = '新增角色';
  Object.assign(editForm, {
    menuIds: [],
    remark: '',
    roleId: undefined,
    roleKey: '',
    roleName: '',
    roleSort: 0,
    status: '0',
  });
  modalVisible.value = true;
}

async function openEditModal(row: SystemRoleApi.Role) {
  const detail = await getRoleDetailApi(row.roleId);
  modalTitle.value = '编辑角色';
  Object.assign(editForm, {
    menuIds: [...detail.menuIds],
    remark: detail.remark ?? '',
    roleId: detail.roleId,
    roleKey: detail.roleKey,
    roleName: detail.roleName,
    roleSort: detail.roleSort,
    status: detail.status,
  });
  modalVisible.value = true;
}

function handleMenuCheck(keys: Array<number | string>) {
  editForm.menuIds = keys.map(String);
}

async function handleSubmit() {
  await formRef.value?.validate();
  submitLoading.value = true;
  try {
    if (editForm.roleId) {
      await updateRoleApi(editForm);
      message.success('修改成功');
    } else {
      await addRoleApi(editForm);
      message.success('新增成功');
    }
    modalVisible.value = false;
    await loadRoleList();
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: SystemRoleApi.Role) {
  await deleteRoleApi([row.roleId]);
  message.success('删除成功');
  if (dataList.value.length === 1 && pagination.page > 1) {
    pagination.page -= 1;
  }
  loadRoleList();
}

const columns: DataTableColumns<SystemRoleApi.Role> = [
  { key: 'roleId', title: '角色ID', width: 80 },
  { key: 'roleName', title: '角色名称', minWidth: 120 },
  { key: 'roleKey', title: '权限字符', minWidth: 120 },
  { key: 'roleSort', title: '排序', width: 80 },
  {
    key: 'status',
    title: '状态',
    width: 80,
    render(row) {
      return row.status === '0'
        ? h(NTag, { size: 'small', type: 'success' }, { default: () => '正常' })
        : h(NTag, { size: 'small', type: 'error' }, { default: () => '停用' });
    },
  },
  { key: 'remark', title: '备注', minWidth: 140 },
  { key: 'createTime', title: '创建时间', width: 170 },
  {
    key: 'action',
    title: '操作',
    width: 150,
    render(row) {
      const actions: VNode[] = [];
      if (hasAccessByCodes(['system:role:edit'])) {
        actions.push(
          h(
            NButton,
            {
              disabled: row.roleId === '1',
              size: 'small',
              tertiary: true,
              type: 'primary',
              onClick: () => openEditModal(row),
            },
            { default: () => '编辑' },
          ),
        );
      }
      if (hasAccessByCodes(['system:role:remove'])) {
        actions.push(
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    disabled: row.roleId === '1',
                    size: 'small',
                    tertiary: true,
                    type: 'error',
                  },
                  { default: () => '删除' },
                ),
              default: () => `确认删除角色「${row.roleName}」吗？`,
            },
          ),
        );
      }
      return h(NSpace, null, { default: () => actions });
    },
  },
];

onMounted(() => {
  loadRoleList();
  loadMenuTree();
});
</script>

<template>
  <Page description="维护角色及其可访问的菜单权限" title="角色管理">
    <NSpace class="mb-4" align="center">
      <NInput
        v-model:value="searchForm.roleName"
        clearable
        placeholder="角色名称"
        style="width: 160px"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="searchForm.roleKey"
        clearable
        placeholder="权限字符"
        style="width: 160px"
        @keyup.enter="handleSearch"
      />
      <NSelect
        v-model:value="searchForm.status"
        :options="statusOptions"
        clearable
        placeholder="状态"
        style="width: 120px"
      />
      <NButton type="primary" @click="handleSearch">查询</NButton>
      <NButton @click="handleReset">重置</NButton>
      <div v-access:code="'system:role:add'">
        <NButton type="primary" @click="openAddModal">新增角色</NButton>
      </div>
    </NSpace>

    <NDataTable
      remote
      :columns="columns"
      :data="dataList"
      :loading="loading"
      :pagination="{
        page: pagination.page,
        pageSize: pagination.pageSize,
        itemCount: total,
        showSizePicker: true,
        pageSizes: [10, 20, 50],
        onChange: (page: number) => { pagination.page = page; loadRoleList(); },
        onUpdatePageSize: (size: number) => { pagination.pageSize = size; pagination.page = 1; loadRoleList(); },
      }"
      :row-key="(row: SystemRoleApi.Role) => row.roleId"
    />

    <NModal
      v-model:show="modalVisible"
      :title="modalTitle"
      preset="card"
      style="width: 560px"
    >
      <NForm
        ref="formRef"
        :model="editForm"
        :rules="formRules"
        label-placement="left"
        label-width="90"
      >
        <NFormItem label="角色名称" path="roleName">
          <NInput v-model:value="editForm.roleName" placeholder="请输入角色名称" />
        </NFormItem>
        <NFormItem label="权限字符" path="roleKey">
          <NInput v-model:value="editForm.roleKey" placeholder="例如：system:role" />
        </NFormItem>
        <NFormItem label="显示顺序" path="roleSort">
          <NInputNumber v-model:value="editForm.roleSort" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NRadioGroup v-model:value="editForm.status">
            <NRadioButton value="0" label="正常" />
            <NRadioButton value="1" label="停用" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="菜单权限" path="menuIds">
          <div class="max-h-80 w-full overflow-auto rounded border p-2">
            <NTree
              block-line
              checkable
              children-field="children"
              :cascade="false"
              :checked-keys="editForm.menuIds"
              :data="menuTree"
              default-expand-all
              key-field="menuId"
              label-field="menuName"
              :loading="menuTreeLoading"
              @update:checked-keys="handleMenuCheck"
            />
          </div>
        </NFormItem>
        <NFormItem label="备注" path="remark">
          <NInput v-model:value="editForm.remark" placeholder="选填" type="textarea" />
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
