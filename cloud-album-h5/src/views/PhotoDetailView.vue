<script setup lang="ts">
import { onMounted, ref } from 'vue'; import { useRoute, useRouter } from 'vue-router'; import { showToast } from 'vant'; import { photoApi } from '@/api'
const route=useRoute(); const router=useRouter(); const photo=ref<any>(null); const loading=ref(true)
onMounted(async()=>{try{photo.value=(await photoApi.detail(String(route.query.photoNo))).data.data}catch{showToast('照片加载失败')}finally{loading.value=false}})
</script>
<template><main class="page-shell"><header class="page-header"><button @click="router.back()">‹</button><b>照片详情</b></header><van-loading v-if="loading" class="center"/><section v-else-if="photo" class="detail"><img :src="photo.previewUrl" :alt="photo.fileName"/><h2>{{photo.fileName}}</h2><p class="muted">{{photo.originalMimeType}} · {{photo.originalSizeBytes}} 字节</p><van-button type="primary" block round @click="photoApi.originalUrl(String(route.query.photoNo)).then(r=>window.open(r.data.data.downloadUrl))">下载原图</van-button></section></main></template>
