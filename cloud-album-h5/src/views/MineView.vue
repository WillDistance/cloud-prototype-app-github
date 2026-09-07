<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { deviceApi } from '@/api'
const router=useRouter(); const store=useAppStore(); const deviceId=ref(''); const password=ref(''); const loading=ref(false)
const percent=computed(()=>{const o=store.overview; return o?.totalCapacityBytes?Math.min(100,Math.round(o.usedBytes/o.totalCapacityBytes*100)):0})
onMounted(async()=>{try{if(!store.device){const r=await deviceApi.mine();store.device=r.data.data}}catch{}})
async function bind(){if(!/^CC-\d{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$/.test(deviceId.value))return showToast('设备 ID 格式错误');loading.value=true;try{await store.bindDevice(deviceId.value,password.value);showToast('绑定成功');router.push('/mine')}catch{showToast('设备 ID 或密码错误')}finally{loading.value=false}}
function mb(v:number){return `${Math.round(v/1048576)}MB`};function bytes(v:number){return v>=1073741824?`${Math.round(v/1073741824)}GB`:mb(v)}
async function logout(){await store.logout();router.push('/login')}
</script>
<template><main class="page-shell mine-page"><header class="topbar"><b>我的</b><button @click="router.push('/language')">语言设置</button></header><section class="profile"><div class="avatar">Y</div><div><strong>云相册用户</strong><span>{{store.profile?.email||'user@example.com'}}</span></div></section><section class="storage-card" @click="router.push('/entitlements')"><b>云端存储空间</b><div class="storage-line"><strong>{{store.overview?mb(store.overview.usedBytes):'0MB'}} <small>/ {{store.overview?bytes(store.overview.totalCapacityBytes):'0GB'}}</small></strong><span>剩余 {{store.overview?mb(store.overview.remainingBytes):'0MB'}}</span></div><div class="progress"><i :style="{width:`${percent}%`}"/></div><small>已使用 {{percent}}%</small><div class="warning">最近到期：{{store.overview?.nearestExpireTime||'暂无权益'}}<br>空间不足时，系统将从最早上传的照片开始自动清理。</div></section><section class="menu"><button class="menu-item" @click="router.push('/device')"><span>◉ <b>我的设备</b><small>{{store.device?store.device.model:'尚未绑定设备'}}</small></span><b>›</b></button><button class="menu-item" @click="router.push('/plans')"><span>▣ <b>容量购买</b><small>购买的容量与现有权益叠加</small></span><b>›</b></button><button class="menu-item" @click="router.push('/entitlements')"><span>◷ <b>存储权益</b><small>查看容量明细和到期规则</small></span><b>›</b></button><button class="menu-item danger" @click="logout"><span>◎ <b>退出登录</b><small>清除本地登录状态</small></span><b>›</b></button></section><nav class="bottom-nav"><button @click="router.push('/album')">▦<span>相册</span></button><button class="active">◉<span>我的</span></button></nav></main></template>
