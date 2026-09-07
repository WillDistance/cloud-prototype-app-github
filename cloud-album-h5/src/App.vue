<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
const router=useRouter(); const store=useAppStore()
function expired(){store.logout();router.push('/login');showToast('登录状态已过期')}
onMounted(()=>window.addEventListener('cloud-album-auth-expired',expired)); onUnmounted(()=>window.removeEventListener('cloud-album-auth-expired',expired))
</script>
<template><router-view/></template>
