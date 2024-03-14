<template>
  <div class="nav">
    <el-select class="branches" placeholder="请选择分支" v-model="branch" :disabled="branches.length === 0">
      <el-option v-for="branch in branches" :key="branch.name" :value="branch.name" :label="branch.name"></el-option>
    </el-select>
    <div class="project-container">
      <ul class="project-list">
        <li v-if="showBack">
          <el-button @click="onPop" icon="el-icon-back">
            <a>..</a>
          </el-button>
        </li>
        <li v-for="node in filteredNodes" :key="node.node.id">
          <el-button @click="onPick(node)" :icon="icon(node)">
            <a>{{ node.node.name }}</a>
          </el-button>
        </li>
        <li v-if="filteredNodes.length === 0">
          <el-button disabled>暂无数据</el-button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Emit, Model, Vue, Watch } from 'vue-property-decorator'
import { Branch, Group, Repo } from '@/api/models'
import { PathSection, PathSectionType, sectionIcon } from '@/structs'
import { gitService } from '@/api/apis'

@Component
export default class SectionTree extends Vue {
  @Model('pick')
  private pathSection?: PathSection

  private branch = ''
  private branches: Branch[] = []

  private nodes: PathSection[] = []

  private async mounted () {
    await this.onPathSectionChanged(this.pathSection)
  }

  private get showBack () {
    return this.pathSection?.type !== PathSectionType.HOME
  }

  private get filteredNodes (): PathSection[] {
    return this.nodes.filter(value => this.shouldShow(value))
  }

  private icon (section: PathSection): string {
    return sectionIcon(section.type)
  }

  @Emit('pick')
  private onPick (node?: PathSection): PathSection | undefined {
    return node
  }

  @Emit('pop')
  private onPop () {
    return ''
  }

  @Watch('pathSection')
  private async onPathSectionChanged (newVal?: PathSection, oldVal?: PathSection) {
    if (newVal?.type === oldVal?.type && newVal?.node?.id === oldVal?.node?.id) {
      return
    }
    if (!newVal) {
      this.clear()
      return
    }
    switch (newVal.type) {
      case PathSectionType.HOME:
      case PathSectionType.GROUP:
      case PathSectionType.PROJECT:
        this.nodes = []
        this.clearBranches()
        break
      case PathSectionType.DIR:
        this.nodes = []
        break
    }
    switch (newVal.type) {
      case PathSectionType.HOME:
        await this.fetchGroups()
        break
      case PathSectionType.GROUP:
        await this.fetchSubGroups(newVal.node?.id as number)
        await this.fetchGroupProjects(newVal.node?.id as number)
        break
      case PathSectionType.PROJECT:
        await this.fetchBranches(newVal.node?.id as number)
        break
      case PathSectionType.DIR:
        await this.fetchRepos(newVal.parent, (newVal.node as Repo)?.path)
        break
    }
  }

  @Watch('branch')
  private async onBranchChanged (newVal: string) {
    this.$emit('branch', newVal)
    await this.fetchRepos(this.pathSection?.node?.id as number)
  }

  private async fetchGroups () {
    const groups = await gitService.listGroups(false, 1, 100).then(value => value.data)
    this.nodes.push(...groups.map(value => {
      return {
        type: PathSectionType.GROUP,
        node: value
      }
    }))
  }

  private async fetchSubGroups (id?: number) {
    const groups = await gitService.listSubGroups(id).then(value => value.data)
    this.nodes.push(...groups.map(value => {
      return {
        type: PathSectionType.GROUP,
        parent: id,
        node: value
      }
    }))
  }

  private async fetchGroupProjects (id: number) {
    const projects = await gitService.listGroupProjects(id).then(value => value.data)
    this.nodes.push(...projects.map(value => {
      return {
        type: PathSectionType.PROJECT,
        parent: id,
        node: value
      }
    }))
  }

  private async fetchBranches (id?: number) {
    this.branches = await gitService.listBranches(id).then(value => value.data)
    this.branch = this.branches && this.branches.length > 0 ? this.branches[0].name || '' : ''
  }

  private async fetchRepos (id?: number, path?: string) {
    if (this.branch.length <= 0) return
    const repos = await gitService.listFiles(id, path, this.branch).then(value => value.data)
    this.nodes.push(...repos.map(value => {
      return {
        type: value.type === 'tree' ? PathSectionType.DIR : PathSectionType.FILE,
        parent: id,
        node: value
      }
    }))
  }

  private shouldShow (node: PathSection): boolean {
    switch (node.type) {
      case PathSectionType.GROUP:
        return this.isChildOf(node.node as Group, this.pathSection?.node as Group)
      case PathSectionType.FILE:
        return node.node?.name?.endsWith('.json') || node.node?.name?.endsWith('.yml') || node.node?.name?.endsWith('.yaml') || false
      default:
        return true
    }
  }

  private isChildOf (child?: Group, parent?: Group): boolean {
    return child?.full_path?.split('/')?.length === (parent?.full_path?.split('/')?.length || 0) + 1
  }

  private clearBranches () {
    this.branch = ''
    this.branches = []
  }

  private clear () {
    this.clearBranches()
    this.nodes = []
    this.onPick()
  }
}
</script>

<style lang="scss" scoped>
.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .branches {
    margin: 10px;
  }

  .project-container {
    flex: 1;
    overflow: auto;
  }

  .project-list {
    margin: 0;
    padding: 0;

    li {
      margin: 0 10px 10px;

      .el-button {
        width: 100%;
        text-align: left;
      }

      a {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
