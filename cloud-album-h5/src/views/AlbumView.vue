<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useRouter } from 'vue-router'
import { photoApi } from '@/api'
import { useAppStore } from '@/stores/app'
import { useI18n } from '@/i18n'

const router = useRouter()
const store = useAppStore()
const photos = ref<any[]>([])
const loading = ref(false)
const filter = ref('ALL')
const selected = ref<string[]>([])
const selecting = ref(false)
const deleting = ref(false)
const { t } = useI18n()

async function load() {
  loading.value = true
  try { photos.value = (await photoApi.list({ filter: filter.value, size: 20 })).data.data.items || [] }
  catch { photos.value = [] }
  finally { loading.value = false }
}
function toggle(photoNo: string) {
  if (selected.value.includes(photoNo)) selected.value = selected.value.filter((item) => item !== photoNo)
  else if (selected.value.length < 9) selected.value.push(photoNo)
  else showToast('一次最多选择 9 张照片')
}
async function remove() {
  if (!selected.value.length) return
  try { await showConfirmDialog({ title: t('delete'), message: t('deleteConfirm') }) } catch { return }
  deleting.value = true
  try { await photoApi.delete(selected.value); showToast(t('deleteSuccess')); selected.value = []; selecting.value = false; await load() }
  catch { showToast(t('operationError')) } finally { deleting.value = false }
}
onMounted(load)
</script>

<template>
  <main class="page-shell">
    <header class="topbar"><b>云相册</b><button @click="selecting = !selecting">{{ selecting ? '取消' : '选择' }}</button></header>
    <div class="filters"><button v-for="item in [['TODAY','今天'],['SEVEN_DAYS','最近 7 天'],['ONE_MONTH','最近 1 个月'],['ALL','全部']]" :key="item[0]" :class="{ active: filter === item[0] }" @click="filter = item[0]; load()">{{ item[1] }}</button></div>
    <van-loading v-if="loading" class="center" />
    <section v-else-if="!store.hasDevice" class="empty-state"><div class="empty-icon">⌁</div><h2>尚未绑定设备</h2><p>绑定相机设备后，设备上传的照片会显示在这里。</p><van-button type="primary" block round @click="router.push('/device')">立即绑定设备</van-button></section>
    <section v-else-if="!photos.length" class="empty-state"><div class="empty-icon">▧</div><h2>云端还没有照片</h2><p>设备上传并完成云端处理后，照片会显示在这里。</p></section>
    <section v-else class="photo-grid"><button v-for="photo in photos" :key="photo.photoNo" class="photo-card" :class="{ selected: selected.includes(photo.photoNo) }" @click="selecting ? toggle(photo.photoNo) : router.push({ path: '/photo', query: { photoNo: photo.photoNo } })"><img :src="photo.thumbnailUrl" :alt="photo.fileName"><span v-if="selecting" class="check">{{ selected.includes(photo.photoNo) ? '✓' : '' }}</span></button></section>
    <div v-if="selecting" class="selection-bar"><b>{{ t('selected', { n: selected.length }) }}</b><van-button size="small" type="danger" :loading="deleting" :disabled="!selected.length || deleting" @click="remove">{{ t('delete') }}</van-button></div>
    <nav class="bottom-nav"><button class="active" @click="router.push('/album')">▦<span>相册</span></button><button @click="router.push('/mine')">◉<span>我的</span></button></nav>
  </main>
</template>
