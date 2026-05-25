<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="会员ID" prop="id">
        <el-input
          v-model="queryParams.id"
          placeholder="请输入会员ID"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery(false)"
        />
      </el-form-item>
      <el-form-item label="会员号" prop="userNo">
        <el-input
          v-model="queryParams.userNo"
          placeholder="请输入会员号"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery(false)"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入会员手机号"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery(false)"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入会员名称"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery(false)"
        />
      </el-form-item>
      <el-form-item label="会员等级" prop="gradeId">
        <el-select
          v-model="queryParams.gradeId"
          clearable
          placeholder="会员等级"
        >
          <el-option v-for="grade in userGradeList" :key="grade.id" :label="grade.name" :value="grade.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="会员分组" prop="groupIds">
          <treeSelect :options="groupList" :multiple="true" @getValue="handleQueryGroup"></treeSelect>
      </el-form-item>
      <el-form-item label="会员标签" prop="tagIds">
        <el-select
          v-model="tagIds"
          multiple
          filterable
          clearable
          placeholder="会员标签"
        >
          <el-option
            v-for="item in userTagList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 100px"
        >
          <el-option key="A" label="启用" value="A"/>
          <el-option key="N" label="禁用" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker
          v-model="queryParams.startTime"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetime"
          style="width:200px"
          placeholder="开始时间"
        ></el-date-picker>
        <span class="sp"> ~ </span>
        <el-date-picker
          v-model="queryParams.endTime"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetime"
          style="width:200px"
          placeholder="结束时间"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="所属店铺" prop="storeIds">
        <el-select v-model="storeIds" multiple filterable clearable placeholder="请选择店铺" style="width: 100%;">
          <el-option
            v-for="item in storeList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item class="action">
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery(false)">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['member:add']"
        >新增</el-button>
      </el-form-item>
      <el-form-item>
        <el-button
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleQuery(true)"
        >导出</el-button>
      </el-form-item>
      <el-form-item>
        <el-upload
          v-hasPermi="['member:add']"
          class="upload-template"
          accept=".xls,.xlsx"
          :action="uploadAction"
          :limit="1"
          :auto-upload="true"
          :headers="uploadHeader"
          :show-file-list="false"
          :file-list="uploadMember"
          v-loading="uploading"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
        >
          <el-button
            type="danger"
            icon="el-icon-upload"
            size="mini"
          >导入</el-button>
        </el-upload>
        <span class="download-template">
          <el-button type="text" icon="el-icon-download" @click="download('/backendApi/member/downloadTemplate')">
            下载模板
          </el-button>
        </span>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="会员ID" prop="id" min-width="70"/>
      <el-table-column label="头像" align="center" width="80">
        <template slot-scope="scope">
            <img v-if="scope.row.avatar" class="list-avatar" :src="scope.row.avatar">
            <img v-else class="list-avatar" src="@/assets/images/avatar.png">
        </template>
      </el-table-column>
      <el-table-column label="会员号" prop="userNo" width="150"/>
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="手机号" align="center" prop="mobile" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.mobile ? scope.row.mobile : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="会员等级" align="center" prop="gradeId" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.gradeId ? getName(userGradeList, scope.row.gradeId) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属店铺" align="center" prop="storeId" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.storeId ? scope.row.storeName: '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="余额" align="center" prop="balance">
        <template slot-scope="scope">
          <div><span style="color:red;">{{ scope.row.balance ? scope.row.balance.toFixed(2) : '0.00' }}</span></div>
          <el-button
            class="mini-btn"
            type="primary"
            size="mini"
            @click="handleBalance(scope.row.id)"
            v-hasPermi="['balance:modify']"
          >充值</el-button>
        </template>
      </el-table-column>
      <el-table-column label="积分" align="center" prop="point">
        <template slot-scope="scope">
          <div><span>{{ scope.row.point ? scope.row.point : '0.00' }}</span></div>
          <el-button
            class="mini-btn"
            type="primary"
            size="mini"
            @click="handlePoint(scope.row.id)"
            v-hasPermi="['point:modify']"
          >变更</el-button>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" width="160" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="活跃时间" align="center" width="160" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="A"
            inactive-value="N"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="170" fixed='right'>
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['member:add']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['member:add']"
            @click="handleDelete(scope.row)"
          >删除</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:account:resetPwd', 'system:account:edit']">
                <span class="el-dropdown-link">
                  <i class="el-icon-d-arrow-right el-icon--right"></i>更多
                </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleResetPwd" icon="el-icon-key" v-hasPermi="['system:account:edit']">重置密码</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.page"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员名称" prop="name" style="width: 420px">
              <el-input v-model="form.name" placeholder="请输入会员名称" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属店铺" prop="storeId">
              <el-select
                v-model="form.storeId"
                style="width: 300px"
                placeholder="所属店铺，空则为公共所有"
              >
                <el-option :key="0" label="公共所有" v-if="!this.$store.getters.storeId" :value="0"/>
                <el-option v-for="storeInfo in storeList" :key="storeInfo.id" :label="storeInfo.name" :value="storeInfo.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员分组" prop="groupId">
               <treeSelect :options="groupList" :multiple="false" :value="form.groupInfo" @getValue="handleGroup"></treeSelect>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员标签" prop="tagIds">
              <el-select
                v-model="formTagIds"
                multiple
                filterable
                clearable
                placeholder="会员标签"
                style="width: 300px"
              >
                <el-option
                  v-for="item in userTagList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员等级" prop="gradeId">
                <el-select
                  v-model="form.gradeId"
                  placeholder="会员等级"
                  style="width: 300px"
                >
                  <el-option v-for="grade in userGradeList" :key="grade.id" :label="grade.name" :value="grade.id"/>
                </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="有效期限">
              <el-date-picker
                v-model="form.startTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="开始时间"
              ></el-date-picker>
              <span class="sp"> 至 </span>
              <el-date-picker
                v-model="form.endTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="结束时间"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员号" prop="userNo">
              <el-input v-model="form.userNo" placeholder="请输入会员号，为空系统将自动分配" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="form.sex">
                <el-radio :key="1" :label="1" :value="1">男</el-radio>
                <el-radio :key="0" :label="0" :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="身份证号" prop="idcard">
              <el-input v-model="form.idcard" placeholder="请输入身份证号" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="生日" prop="birthday">
              <el-input v-model="form.birthday" placeholder="请输入生日，格式如：1990-01-01" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="通讯地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入通讯地址" maxlength="100"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="会员状态">
              <el-radio-group v-model="form.status">
                <el-radio key="A" label="A" value="A">正常</el-radio>
                <el-radio key="N" label="N" value="N">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.id">
          <el-col :span="24">
            <el-form-item label="注册来源">
              <el-radio-group v-model="form.source">
                <el-radio label="" v-if="!form.source">未知</el-radio>
                <el-radio label="wechat_login" v-if="form.source == 'wechat_login'">微信小程序</el-radio>
                <el-radio label="wechat_mp" v-if="form.source == 'wechat_mp'">微信公众号</el-radio>
                <el-radio label="backend_add" v-if="form.source == 'backend_add'">后台添加</el-radio>
                <el-radio label="register_by_account" v-if="form.source == 'register_by_account'">H5注册</el-radio>
                <el-radio label="mobile_login" v-if="form.source == 'mobile_login'">手机号登录注册</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="备注信息">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>

    <!-- 余额充值对话框 -->
    <balanceRecharge :showDialog="openBalance" :userId="userId" @closeDialog="closeDialog" @close="closeDialog"/>

    <!-- 积分充值对话框 -->
    <pointRecharge :showDialog="openPoint" :userId="userId" @closeDialog="closeDialog" @close="closeDialog"/>
  </div>
</template>

<script>
import { getMemberList, updateMemberStatus, getMemberInfo, saveMember, deleteMember, resetMemberPwd } from "@/api/member";
import { getUserTagList } from '@/api/member/userTag'
import balanceRecharge from "./balanceRecharge";
import pointRecharge from "./pointRecharge";
import { getToken } from '@/utils/auth';
export default {
  name: "MemberIndex",
  components: { balanceRecharge, pointRecharge },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 标题
      title: "",
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 会员等级列表
      userGradeList: [],
      // 会员分组列表
      groupList: [],
      // 会员标签列表
      userTagList: [],
      tagIds: [],
      formTagIds: [],
      // 店铺列表
      storeList: [],
      storeIds: [],
      // 是否显示修改对话框
      open: false,
      // 当前操作用户
      userId: '',
      // 是否弹层充值
      openBalance: false,
      // 是否弹层积分
      openPoint: false,
      // 日期范围
      dateRange: [],
      // 默认排序
      defaultSort: {prop: 'updateTime', order: 'descending'},
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/member/uploadMemberFile',
      // 隐藏上传
      hideUpload: false,
      uploadHeader: { 'Access-Token': getToken() },
      uploading: false,
      uploadMember: [],
      // 表单参数
      form: { id: '', name: '', storeId: '', groupId: '', groupInfo: {}, gradeId: '', mobile: '', userNo: '', startTime: '', endTime: '', sex: 1, idcard: '', birthday: '', address: '', status: "A", description: '' },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        mobile: '',
        id: '',
        userNo: '',
        name: '',
        gradeId: '',
        groupIds: '',
        status: '',
        storeIds: '',
        tagIds: ''
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "会员名称不能为空", trigger: "blur" },
          { min: 2, max: 200, message: '会员名称长度必须介于2 和 100 之间', trigger: 'blur' }
        ],
        gradeId: [{ required: true, message: "请选择会员等级", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
    this.getUserTagList()
  },
  methods: {
    // 获取会员标签列表
    getUserTagList() {
      getUserTagList().then((response) => {
        this.userTagList = response.data.list || []
      })
    },
    // 查询列表
    getList() {
      this.loading = true;
      this.queryParams.storeIds = this.storeIds ? this.storeIds.join(",") : '';
      this.queryParams.tagIds = this.tagIds ? this.tagIds.join(',') : '';
      getMemberList(this.addDateRange(this.queryParams, this.dateRange)).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.userGradeList = response.data.userGradeList;
          this.storeList = response.data.storeList;
          this.groupList = response.data.groupList;
          this.loading = false;
        }
      );
    },
    // 选中会员分组
    handleGroup(groupInfo) {
      if (groupInfo) {
          this.form.groupId = groupInfo.id;
      } else {
          this.form.groupId = '';
      }
    },
    handleQueryGroup(selected) {
      let groupIds = [];
      if (selected) {
          selected.forEach(function(item) {
             groupIds.push(item.id);
          })
      }
      this.queryParams.groupIds = groupIds.toString();
    },
    // 搜索按钮操作
    handleQuery(isExport) {
      this.queryParams.page = 1
      if (isExport == true) {
        this.exportExcel()
      } else {
        this.getList()
      }
    },
    // 导出excel
    exportExcel() {
      this.queryParams.storeIds = this.storeIds ? this.storeIds.join(',') : ''
      this.queryParams.tagIds = this.tagIds ? this.tagIds.join(',') : ''
      this.download('/backendApi/member/export', {
        ...this.queryParams,
      })
    },
    // 重置按钮操作
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.queryParams.storeIds = '';
      this.storeIds = [];
      this.queryParams.tagIds = '';
      this.tagIds = [];
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order);
      this.handleQuery();
    },
    // 状态修改
    handleStatusChange(row) {
      let text = row.status == "A" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.name + '"吗？').then(function() {
        return updateMemberStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "N" ? "A" : "N";
      });
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    // 排序触发事件
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop;
      this.queryParams.isAsc = column.order;
      this.getList();
    },
    // 余额充值操作
    handleBalance(userId) {
       this.openBalance = true;
       this.userId = userId.toString();
    },
    // 积分变更操作
    handlePoint(userId) {
       this.openPoint = true
       this.userId = userId.toString();
    },
    // 关闭对话框
    closeDialog(dialog) {
      console.log('closeDialog');
      if (dialog == 'balance') {
          this.openBalance = false;
      }
      if (dialog == 'point') {
          this.openPoint = false;
      }
      this.userId = "";
      this.getList();
    },
    // 新增按钮操作
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增会员";
    },
    // 表单重置
    reset() {
      this.resetForm("form");
      this.form.id = '';
      this.form.description = '';
      this.form.name = '';
      this.form.startTime = '';
      this.form.endTime = '';
      this.form.groupId = '';
      this.form.storeId = '';
      this.form.gradeId = '';
      this.form.userNo = '';
      this.form.mobile = '';
      this.form.groupInfo = {};
      this.formTagIds = [];
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            // 将标签ID数组转为逗号分隔字符串
            const submitData = { ...this.form }
            submitData.tagIds = this.formTagIds ? this.formTagIds.join(',') : ''
            if (this.form.id) {
                saveMember(submitData).then(response => {
                  this.$modal.msgSuccess("修改会员成功");
                  this.open = false;
                  this.getList();
                });
            } else {
                saveMember(submitData).then(response => {
                  this.$modal.msgSuccess("新增会员成功");
                  this.open = false;
                  this.getList();
                });
            }
        }
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getMemberInfo(id).then(response => {
          this.form = response.data.memberInfo;
          // 设置会员标签
          if (response.data.memberInfo.tags && response.data.memberInfo.tags.length > 0) {
              this.formTagIds = response.data.memberInfo.tags.map(tag => tag.id)
          } else {
              this.formTagIds = []
          }
          this.open = true;
          this.title = "编辑会员";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.name;
      this.$modal.confirm('确定删除"' + name + '"的会员信息？').then(function() {
        return deleteMember(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        default:
          break;
      }
    },
    // 重置密码按钮操作
    handleResetPwd(row) {
      this.$prompt('重置会员号："' + row.userNo + '"的密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{6,30}$/,
        inputErrorMessage: "会员密码长度必须介于5和20之间"
      }).then(({ value }) => {
        resetMemberPwd({ userId: row.id, password: value }).then(response => {
          this.$modal.msgSuccess("修改成功，新密码是：" + value);
        });
      }).catch(() => {});
    },
    beforeUpload() {
      this.uploading = true;
    },
    handleUploadSuccess(data) {
      if (data && data.code == '200') {
        this.getList();
        this.$modal.msgSuccess('会员导入成功');
      } else {
        this.$modal.msgError((data && data.message) ? data.message : '会员导入失败');
      }
      this.uploadGoods = [];
      this.uploading = false;
    }
  }
};
</script>
<style scoped lang="scss">
::v-deep .upload-template .el-upload {
  height: auto;
  width: auto;
  line-height: 30px;
  border-radius: 4px;
}
.action {
  margin-left: 30px
}
</style>
