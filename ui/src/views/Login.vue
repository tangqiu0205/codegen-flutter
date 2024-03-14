<template>
  <div class="login">
    <el-card class="login-panel">
      <span slot="header">登录Gitlab</span>
      <el-form size="small" :model="form">
        <el-form-item>
          <el-input prefix-icon="el-icon-user" placeholder="请输入Gitlab用户名" v-model="form.username"/>
        </el-form-item>
        <el-form-item>
          <el-input type="password" prefix-icon="el-icon-lock" placeholder="请输入Gitlab密码" v-model="form.password"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="login" :disabled="logining" :icon="logining ? 'el-icon-loading' : ''">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { Auth, gitService } from '@/api/apis'

@Component
export default class Login extends Vue {
  private form = {
    username: '',
    password: ''
  }

  private logining = false

  private login () {
    if (this.logining) return
    this.logining = true
    gitService.auth('password', this.form.username, this.form.password).then(value => {
      Auth.setToken(value.data || null)
      this.$router.replace('/')
    }).catch(reason => this.$message.error(reason.message || '')).finally(() => {
      this.logining = false
    })
  }
}
</script>

<style lang="scss" scoped>
.login {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  .login-panel {
    width: 320px;
  }
  .login-button {
    width: 100%;
  }
}
</style>
