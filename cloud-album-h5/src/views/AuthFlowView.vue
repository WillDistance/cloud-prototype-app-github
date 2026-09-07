<script setup lang="ts">
import { computed, ref } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { useAppStore } from '@/stores/app'
import { useI18n } from '@/i18n'
const props = defineProps<{ reset?: boolean }>(); const router=useRouter(); const store=useAppStore(); const {t}=useI18n()
const step=ref(1); const email=ref(''); const code=ref(''); const password=ref(''); const confirmPassword=ref(''); const loading=ref(false)
const title=computed(()=>props.reset?t('flowReset'):t('flowRegister'))
async function run(fn:()=>Promise<any>, success?:()=>void){loading.value=true;try{await fn();success?.()}catch(e:any){showToast(e?.response?.data?.errorMag||t('operationError'))}finally{loading.value=false}}
function validEmail(){if(!email.value.includes('@')){showToast(t('emailError'));return false}return true}
function send(){if(!validEmail())return;run(()=>props.reset?authApi.sendResetPasswordCode(email.value):authApi.sendRegisterCode(email.value),()=>{showToast(t('codeSent'));step.value=2})}
function verify(){if(!code.value.trim())return showToast(t('codeHint'));run(()=>props.reset?authApi.verifyResetPasswordCode(email.value,code.value):authApi.verifyRegisterCode(email.value,code.value),()=>step.value=3)}
function finish(){if(password.value.length<6)return showToast(t('passwordError'));if(password.value!==confirmPassword.value)return showToast(t('confirmPassword'));run(()=>props.reset?authApi.resetPassword({email:email.value,newPassword:password.value,confirmPassword:confirmPassword.value}):authApi.register({email:email.value,password:password.value,confirmPassword:confirmPassword.value},Intl.DateTimeFormat().resolvedOptions().timeZone,store.language),()=>{showToast(props.reset?t('resetSuccess'):t('registerSuccess'));router.replace('/login')})}
</script>
<template><main class="auth-page"><div class="brand"><span class="logo">⌁</span><b>{{t('brand')}}</b></div><section class="auth-content"><button class="back-link" @click="router.push('/login')">‹ {{t('backLogin')}}</button><h1>{{title}}</h1><div class="steps"><i :class="{active:step>=1}">1</i><span/><i :class="{active:step>=2}">2</i><span/><i :class="{active:step>=3}">3</i></div><template v-if="step===1"><van-field v-model="email" :label="t('email')" type="email" placeholder="name@example.com" inset /><van-button type="primary" block round :loading="loading" @click="send">{{t('sendCode')}}</van-button></template><template v-else-if="step===2"><van-field v-model="code" :label="t('code')" :placeholder="t('codeHint')" maxlength="8" inset /><van-button type="primary" block round :loading="loading" @click="verify">{{t('next')}}</van-button></template><template v-else><van-field v-model="password" :label="props.reset?t('newPassword'):t('password')" type="password" placeholder="••••••••" inset /><van-field v-model="confirmPassword" :label="t('confirmPassword')" type="password" placeholder="••••••••" inset /><van-button type="primary" block round :loading="loading" @click="finish">{{t('done')}}</van-button></template></section></main></template>
