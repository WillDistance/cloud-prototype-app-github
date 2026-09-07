<script setup lang="ts">
import { useRouter } from 'vue-router'; import { showToast } from 'vant'; import { useAppStore } from '@/stores/app'; import { useI18n } from '@/i18n'
const router=useRouter(); const store=useAppStore(); const {t}=useI18n(); async function change(value:string){try{await store.updateLanguage(value);showToast(t('languageSaved'))}catch{showToast(t('languageSyncError'))}}
</script>
<template><main class="page-shell"><header class="page-header"><button @click="router.back()">‹</button><b>{{t('language')}}</b></header><p class="muted summary">{{t('languageSaved')}}</p><section class="menu"><button v-for="item in [['zh-CN','简体中文'],['en','English'],['de','Deutsch']]" :key="item[0]" class="menu-item" @click="change(item[0])"><b>{{item[1]}}</b><span>{{store.language===item[0]?'✓':''}}</span></button></section></main></template>
