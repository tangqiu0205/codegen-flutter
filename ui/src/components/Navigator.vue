<template>
  <el-breadcrumb class="breadcrumb" separator-class="el-icon-arrow-right">
    <el-breadcrumb-item :to="{ path: '/' }" @click.native="home">
      <i class="el-icon-house"></i>
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="(section, index) in sections" :to="{ path: '/' }"
      :key="section.node.name"
      @click.native="pick(index)"
    >
      <i :class="icon(section)"></i>
      {{section.node.name}}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script lang="ts">
import { Component, Emit, Prop, Vue } from 'vue-property-decorator'
import { PathSection, sectionIcon } from '@/structs'

@Component
export default class Navigator extends Vue {
  @Prop({
    default: []
  })
  private sections!: PathSection[]

  private icon (section: PathSection): string {
    return sectionIcon(section.type)
  }

  @Emit('pick')
  private pick (index: number): number {
    return index
  }

  @Emit('home')
  private home () {
    return ''
  }
}
</script>

<style lang="scss" scoped>
.breadcrumb {
  flex: 1;
}
</style>
