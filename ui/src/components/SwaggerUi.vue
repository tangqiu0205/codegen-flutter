<template>
  <div class="swagger" ref="swagger"></div>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator'
import { SwaggerUIBundle } from 'swagger-ui-dist'

@Component
export default class SwaggerUi extends Vue {
  @Prop({
    default: ''
  })
  private url!: string

  private ui!: SwaggerUIBundle

  @Watch('url')
  onUrlChanged (url: string) {
    const config = {
      url,
      domNode: this.$refs.swagger as HTMLElement
    }
    this.ui = SwaggerUIBundle(config)
  }
}
</script>

<style scoped>
.swagger {
  height: 100%;
}
</style>
