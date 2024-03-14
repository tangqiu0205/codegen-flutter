<template>
  <el-container class="home">
    <el-header class="header">
      <navigator :sections="sections" @pick="select" @home="home"/>
      <el-button icon="el-icon-download" type="primary" :disabled="!currentApiUrl" @click="genDialogVisible = true">
        导出工程
      </el-button>
    </el-header>
    <el-container class="main-container">
      <el-aside class="nav">
        <section-tree v-model="currentSection" @pick="pick" @pop="pop" @branch="branchChanged"/>
      </el-aside>
      <el-main class="main-workspace">
        <swagger-ui :url="currentApiUrl"/>
      </el-main>
    </el-container>

    <el-dialog :visible.sync="genDialogVisible" title="导出工程">
      <code-gen-form :path="currentGenUrl"/>
    </el-dialog>
  </el-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import SwaggerUi from '@/components/SwaggerUi.vue'
import CodeGenForm from '@/components/CodeGenForm.vue'
import Navigator from '@/components/Navigator.vue'
import { HomePathSection, PathSection, PathSectionType } from '@/structs'
import SectionTree from '@/components/SectionTree.vue'
import { Auth } from '@/api/apis'
import { Project } from '@/api/models'

@Component({
  components: { SectionTree, Navigator, CodeGenForm, SwaggerUi }
})
export default class Home extends Vue {
  private sections: PathSection[] = []

  private genDialogVisible = false

  private currentSection: PathSection = HomePathSection

  private currentBranch = ''

  private get currentProject (): Project | undefined {
    return this.sections.find(value => value.type === PathSectionType.PROJECT)?.node as Project
  }

  private get currentApiUrl (): string | undefined {
    return this.filePath()
  }

  private get currentGenUrl (): string | undefined {
    return this.filePath(false)
  }

  private select (index: number) {
    this.currentSection = this.sections[index]
    this.sections.splice(index + 1)
  }

  private home () {
    this.currentSection = HomePathSection
    this.sections = []
  }

  private pick (section?: PathSection) {
    if (section) {
      if (this.sections.length > 0 && this.sections[this.sections.length - 1].type === PathSectionType.FILE) {
        this.sections.pop()
      }
      this.sections.push(section)
    } else {
      this.sections.pop()
    }
  }

  private pop () {
    if (this.sections.length > 1) {
      if (this.sections[this.sections.length - 1].type === PathSectionType.FILE) {
        this.select(this.sections.length - 3)
      } else {
        this.select(this.sections.length - 2)
      }
    } else {
      this.home()
    }
  }

  private branchChanged (branch: string) {
    this.currentBranch = branch
  }

  private filePath (auth = true): string | undefined {
    const list = this.sections || []
    if (list.length === 0) {
      return undefined
    } else if (list[list.length - 1].type !== PathSectionType.FILE) {
      return undefined
    } else if (auth) {
      const filePath = list[list.length - 1]?.node?.path || ''
      return Auth.authFilePath(`${this.currentProject?.web_url}/raw/${this.currentBranch}/${filePath}`)
    } else {
      const filePath = list[list.length - 1]?.node?.path || ''
      return `${this.currentProject?.web_url}/raw/${this.currentBranch}/${filePath}`
    }
  }
}
</script>

<style lang="scss" scoped>
.home {
  height: 100%;
  overflow: hidden;

  .branches {
    margin: 10px;
  }
}

.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  border-bottom: 1px solid #ddd;
}

.main-container {
  overflow: hidden;

  .main-workspace {
    overflow: auto;
  }

  .nav {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}
</style>
