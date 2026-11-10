<script lang="ts" setup>
import type { DataTableColumns, FormRules } from 'naive-ui';
import type { SystemUserApi } from '#/api/system/user';
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
  NModal,
  NPopconfirm,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NTag,
} from 'naive-ui';

import {
  addUserApi,
  deleteUserApi,
  getUserPageApi,
  getUserRoleOptionsApi,
  updateUserApi,
} from '#/api/system/user';
import { message } from '#/adapter/naive';

/** 路由 name 与后端菜单表 route_name 保持一致，供 keep-alive 匹配 */
defineOptions({ name: 'SystemUser' });

const { hasAccessByCodes } = useAccess();

// ---------- 列表查询状态 ----------
const loading = ref(false);
const dataList = ref<SystemUserApi.SysUser[]>([]);
const total = ref(0);
// 搜索条件（与后端 SysUserBo 字段对齐）
const searchForm = reactive({ phonenumber: '', status: null, userName: '' });
// 分页参数（v-model:page 同步给 NDataTable）
const pagination = reactive({ page: 1, pageSize: 10 });

const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' },
];
const roleOptions = ref<{ label: string; value: string }[]>([]);

async function loadRoleOptions() {
  const roles = await getUserRoleOptionsApi();
  roleOptions.value = roles.map((role) => ({
    label: role.roleName,
    value: role.roleId,
  }));
}

async function loadUserList() {
  loading.value = true;
  try {
    const page = await getUserPageApi({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      phonenumber: searchForm.phonenumber || undefined,
      status: searchForm.status ?? undefined,
      userName: searchForm.userName || undefined,
    });
    dataList.value = page.rows;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  loadUserList();
}

function handleReset() {
  searchForm.userName = '';
  searchForm.phonenumber = '';
  searchForm.status = null;
  handleSearch();
}

// ---------- 新增 / 编辑弹窗 ----------
const modalVisible = ref(false);
const modalTitle = ref('新增用户');
const formRef = ref();
const editForm = reactive<SystemUserApi.SysUserForm>({
  email: '',
  nickName: '',
  password: '',
  phonenumber: '',
  remark: '',
  roleIds: [],
  sex: '0',
  status: '0',
  userId: undefined,
  userName: '',
});

const formRules: FormRules = {
  nickName: { message: '请输入用户昵称', required: true, trigger: 'blur' },
  roleIds: {
    message: '请选择角色',
    required: true,
    trigger: 'change',
    type: 'array',
  },
  userName: { message: '请输入登录账号', required: true, trigger: 'blur' },
};

function openAddModal() {
  modalTitle.value = '新增用户';
  Object.assign(editForm, {
    email: '', nickName: '', password: '', phonenumber: '',
    remark: '', roleIds: [], sex: '0', status: '0', userId: undefined, userName: '',
  });
  modalVisible.value = true;
}

function openEditModal(row: SystemUserApi.SysUser) {
  modalTitle.value = '编辑用户';
  // 编辑时不回填密码：留空表示不修改
  Object.assign(editForm, {
    email: row.email,
    nickName: row.nickName,
    password: '',
    phonenumber: row.phonenumber,
    remark: row.remark,
    roleIds: [...row.roleIds],
    sex: row.sex,
    status: row.status,
    userId: row.userId,
    userName: row.userName,
  });
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();
  const isAdd = !editForm.userId;
  if (isAdd) {
    await addUserApi(editForm);
    message.success('新增成功');
  } else {
    await updateUserApi(editForm);
    message.success('修改成功');
  }
  modalVisible.value = false;
  loadUserList();
}

// ---------- 删除 ----------
async function handleDelete(row: SystemUserApi.SysUser) {
  await deleteUserApi([row.userId]);
  message.success('删除成功');
  if (dataList.value.length === 1 && pagination.page > 1) {
    pagination.page -= 1;
  }
  loadUserList();
}

// ---------- 表格列 ----------
const columns: DataTableColumns<SystemUserApi.SysUser> = [
  { key: 'userId', title: '用户ID', width: 80 },
  { key: 'userName', title: '登录账号', width: 120 },
  { key: 'nickName', title: '用户昵称', width: 120 },
  { key: 'phonenumber', title: '手机号', width: 130 },
  {
    key: 'roleNames',
    title: '角色',
    minWidth: 140,
    render(row) {
      const roleTags = row.roleNames.map((roleName) =>
        h(NTag, { key: roleName, size: 'small' }, { default: () => roleName }),
      );
      return h(NSpace, { size: 4 }, { default: () => roleTags });
    },
  },
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
  { key: 'createTime', title: '创建时间', width: 170 },
  {
    key: 'action',
    title: '操作',
    width: 150,
    render(row) {
      const actions: VNode[] = [];
      if (hasAccessByCodes(['system:user:edit'])) {
        actions.push(
          h(
            NButton,
            {
              disabled: row.userId === '1',
              size: 'small',
              tertiary: true,
              type: 'primary',
              onClick: () => openEditModal(row),
            },
            { default: () => '编辑' },
          ),
        );
      }
      if (hasAccessByCodes(['system:user:remove'])) {
        actions.push(
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    disabled: row.userId === '1',
                    size: 'small',
                    tertiary: true,
                    type: 'error',
                  },
                  { default: () => '删除' },
                ),
              default: () => `确认删除用户「${row.nickName}」吗？`,
            },
          ),
        );
      }
      return h(NSpace, null, { default: () => actions });
    },
  },
];

onMounted(() => {
  loadRoleOptions();
  loadUserList();
});
</script>

<template>
  <Page description="系统用户的增删改查（示例业务模块）" title="用户管理">
    <!-- 搜索区 -->
    <NSpace class="mb-4" align="center">
      <NInput
        v-model:value="searchForm.userName"
        clearable
        placeholder="登录账号"
        style="width: 180px"
        @keyup.enter="handleSearch"
      />
      <NInput
        v-model:value="searchForm.phonenumber"
        clearable
        placeholder="手机号"
        style="width: 180px"
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
      <div v-access:code="'system:user:add'">
        <NButton type="primary" @click="openAddModal">新增用户</NButton>
      </div>
    </NSpace>

    <!-- 列表 -->
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
        onChange: (page: number) => { pagination.page = page; loadUserList(); },
        onUpdatePageSize: (size: number) => { pagination.pageSize = size; pagination.page = 1; loadUserList(); },
      }"
      :row-key="(row: SystemUserApi.SysUser) => row.userId"
    />

    <!-- 新增/编辑弹窗 -->
    <NModal
      v-model:show="modalVisible"
      :title="modalTitle"
      preset="card"
      style="width: 520px"
    >
      <NForm
        ref="formRef"
        :model="editForm"
        :rules="formRules"
        label-placement="left"
        label-width="90"
      >
        <NFormItem label="登录账号" path="userName">
          <NInput
            v-model:value="editForm.userName"
            :disabled="!!editForm.userId"
            placeholder="3-30 位字符"
          />
        </NFormItem>
        <NFormItem label="用户昵称" path="nickName">
          <NInput v-model:value="editForm.nickName" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem label="角色" path="roleIds">
          <NSelect
            v-model:value="editForm.roleIds"
            :options="roleOptions"
            multiple
            placeholder="请选择角色"
          />
        </NFormItem>
        <NFormItem label="密码" path="password">
          <NInput
            v-model:value="editForm.password"
            :placeholder="editForm.userId ? '留空表示不修改' : '留空默认 123456'"
            show-password-on="click"
            type="password"
          />
        </NFormItem>
        <NFormItem label="手机号" path="phonenumber">
          <NInput v-model:value="editForm.phonenumber" placeholder="选填" />
        </NFormItem>
        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="editForm.email" placeholder="选填" />
        </NFormItem>
        <NFormItem label="性别" path="sex">
          <NRadioGroup v-model:value="editForm.sex">
            <NRadioButton value="0" label="男" />
            <NRadioButton value="1" label="女" />
            <NRadioButton value="2" label="未知" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NRadioGroup v-model:value="editForm.status">
            <NRadioButton value="0" label="正常" />
            <NRadioButton value="1" label="停用" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="备注" path="remark">
          <NInput
            v-model:value="editForm.remark"
            placeholder="选填"
            type="textarea"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </Page>
</template>
