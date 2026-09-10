<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { paymentApi, storageApi, type StoragePlan } from '@/api'

const router = useRouter()
const plans = ref<StoragePlan[]>([])
const selected = ref<StoragePlan | null>(null)
const loading = ref(false)
const paymentMessage = ref('')

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  const paymentState = params.get('payment')
  const orderNo = params.get('orderNo')

  if (paymentState === 'return' && orderNo) {
    try {
      loading.value = true
      const response = await paymentApi.captureOrder(orderNo)
      paymentMessage.value = response.data.data.status === 'PAID'
        ? '支付成功，存储权益已开通'
        : '支付已提交，正在等待支付平台确认'
      showToast(paymentMessage.value)
    } catch {
      showToast('支付确认失败，请稍后在订单中重试')
    } finally {
      loading.value = false
    }
    window.history.replaceState({}, document.title, window.location.pathname)
  } else if (paymentState === 'cancel') {
    showToast('已取消支付')
    window.history.replaceState({}, document.title, window.location.pathname)
  }

  try {
    plans.value = (await storageApi.plans()).data.data || []
  } catch {
    showToast('套餐加载失败')
  }
})

async function create() {
  if (!selected.value) return
  loading.value = true
  try {
    const response = await paymentApi.createOrder({
      planCode: selected.value.planCode,
      planVersion: selected.value.planVersion,
      clientRequestId: crypto.randomUUID(),
    })
    const order = response.data.data
    if (!order.checkoutUrl) {
      showToast('支付链接暂不可用')
      return
    }
    window.location.assign(order.checkoutUrl)
  } catch {
    showToast('订单创建失败')
  } finally {
    loading.value = false
  }
}

function formatMoney(plan: StoragePlan) {
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: plan.currency,
  }).format(plan.priceCent / 100)
}

function formatCapacity(bytes: number) {
  const gib = bytes / 1024 / 1024 / 1024
  return gib >= 1 ? gib.toFixed(gib % 1 === 0 ? 0 : 2) + ' GB' : Math.round(bytes / 1024 / 1024) + ' MB'
}
</script>

<template>
  <main class="page-shell">
    <header class="page-header">
      <button @click="router.back()">‹</button>
      <b>容量购买</b>
    </header>
    <p class="muted summary">新购容量会与现有有效权益叠加。</p>
    <p v-if="paymentMessage" class="payment-message">{{ paymentMessage }}</p>
    <section class="plans">
      <button
        v-for="plan in plans"
        :key="plan.planCode + '-' + plan.planVersion"
        class="card plan"
        :class="{ selected: selected === plan }"
        @click="selected = plan"
      >
        <span>
          <b>{{ plan.planName }}</b>
          <small>{{ formatCapacity(plan.capacityBytes) }} · {{ plan.durationValue }} {{ plan.durationUnit }}</small>
        </span>
        <strong>{{ formatMoney(plan) }}</strong>
      </button>
    </section>
    <van-button type="primary" block round :disabled="!selected" :loading="loading" @click="create">
      前往安全支付
    </van-button>
  </main>
</template>
