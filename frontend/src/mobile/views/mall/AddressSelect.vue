<template>
  <div class="select-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="addresses.length === 0" icon="fas fa-map-marker-alt" description="暂无收货地址" />
    <div v-else class="addr-list">
      <MobileCard v-for="a in addresses" :key="a.id" class="addr-item" clickable @click="selectAddr(a)">
        <div class="addr-top">
          <span class="addr-contact">{{ a.contact }} {{ a.phone }}</span>
          <span v-if="a.isDefault" class="default-tag">默认</span>
          <i v-if="selectedId === a.id" class="fas fa-check-circle check-icon"></i>
        </div>
        <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAddressList } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const addresses = ref([]); const loading = ref(true); const selectedId = ref(null)

onMounted(async () => { try { addresses.value = await getAddressList() } catch {} finally { loading.value = false } })

function selectAddr(a) {
  router.replace({ path: '/mall/checkout', query: { addressId: a.id } })
}
</script>

<style scoped>
.select-page { padding: 12px 16px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.addr-item { margin-bottom: 10px; }
.addr-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.addr-contact { font-size: 15px; font-weight: 500; color: #5a4a42; }
.default-tag { font-size: 11px; padding: 1px 8px; border-radius: 4px; background: rgba(139,184,160,0.12); color: #8ab8a0; }
.addr-detail { font-size: 13px; color: #a09080; line-height: 1.4; }
.check-icon { margin-left: auto; color: #8ab8a0; font-size: 18px; }
</style>
