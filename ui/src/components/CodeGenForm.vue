<template>
  <el-form label-width="120px" ref="gen" :model="genForm" :rules="genRules">
    <el-form-item label="工程名" prop="pubName" required>
      <el-input v-model="genForm.pubName"/>
    </el-form-item>
    <el-form-item label="版本号" prop="pubVersion" required>
      <el-input v-model="genForm.pubVersion"/>
    </el-form-item>
    <el-form-item label="实例类名前缀" prop="pubClassName">
      <el-input v-model="genForm.pubClassName"/>
    </el-form-item>
    <el-form-item label="仓储Host" prop="apiLibHost" required>
      <el-input v-model="genForm.apiLibHost"/>
    </el-form-item>
    <el-form-item label="依赖库版本" prop="apiLibVersion" required>
      <el-input v-model="genForm.apiLibVersion"/>
    </el-form-item>
    <el-form-item label="根路径" prop="basePath" required>
      <el-input v-model="genForm.basePath"/>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitGen">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import axios from 'axios'
import { ElForm } from 'element-ui/types/form'
import { Auth } from '@/api/apis'

@Component
export default class CodeGenForm extends Vue {
  @Prop({
    required: true
  })
  private path!: string

  private genForm: {
    url: string;
    pubName: string;
    pubVersion: string;
    pubClassName?: string;
    apiLibHost: string;
    apiLibVersion: string;
    basePath?: string;
  } = {
    url: '',
    pubName: '',
    pubVersion: '',
    apiLibHost: '',
    apiLibVersion: 'master'
  }

  private genRules = {
    pubName: [{ required: true, message: '请输入PUB名称', trigger: 'change' }],
    pubVersion: [{ required: true, message: '请输入PUB版本号', trigger: 'change' }],
    apiLibHost: [{ required: true, message: '请输入仓储Host', trigger: 'change' }],
    apiLibVersion: [{ required: true, message: '请输入依赖库版本号', trigger: 'change' }]
  }

  submitGen () {
    (this.$refs.gen as ElForm).validate(async (valid) => {
      if (valid) {
        this.genForm.url = this.path
        axios({
          url: '/generate/flutter', // your url
          method: 'POST',
          responseType: 'blob', // important
          data: {
            ...this.genForm,
            accessToken: Auth.getToken()
          }
        }).then((response) => {
          const url = window.URL.createObjectURL(new Blob([response.data]))
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', `${this.genForm.pubName}_${this.genForm.pubVersion}.zip`) // or any other extension
          document.body.appendChild(link)
          link.click()
          this.$message({
            message: '导出成功！',
            type: 'success'
          })
        }).catch(() => {
          this.$message.error('导出失败！')
        })
      } else {
        this.$message.error('导出失败！')
        return false
      }
    })
  }
}
</script>
