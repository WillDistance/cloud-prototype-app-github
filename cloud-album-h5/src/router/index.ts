import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import AuthFlowView from '@/views/AuthFlowView.vue'
import AlbumView from '@/views/AlbumView.vue'
import MineView from '@/views/MineView.vue'
import DeviceView from '@/views/DeviceView.vue'
import EntitlementsView from '@/views/EntitlementsView.vue'
import PlansView from '@/views/PlansView.vue'
import LanguageView from '@/views/LanguageView.vue'
import PhotoDetailView from '@/views/PhotoDetailView.vue'
export const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/', redirect: '/login' }, { path: '/login', component: LoginView },
  { path: '/register', component: AuthFlowView }, { path: '/forgot-password', component: AuthFlowView, props: { reset: true } },
  { path: '/album', component: AlbumView, meta: { auth: true } }, { path: '/mine', component: MineView, meta: { auth: true } },
  { path: '/device', component: DeviceView, meta: { auth: true } }, { path: '/entitlements', component: EntitlementsView, meta: { auth: true } },
  { path: '/plans', component: PlansView, meta: { auth: true } }, { path: '/language', component: LanguageView, meta: { auth: true } }, { path: '/photo', component: PhotoDetailView, meta: { auth: true } },
] })
router.beforeEach((to) => { const token=localStorage.getItem('cloud_album_access_token'); if(to.meta.auth&&!token)return {path:'/login',query:{redirect:to.fullPath}}; if(to.path==='/login'&&token)return '/album' })
export default router
