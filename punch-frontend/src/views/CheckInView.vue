<template>
  <div class="container mt-5">
    <div class="card shadow-sm p-4 mx-auto" style="max-width: 500px;">
      <h2 class="text-center mb-4">Web Check-in System</h2>
      
      <div class="mb-3 text-center">
        <h4>{{ currentTime }}</h4>
      </div>

      <div class="mb-3">
        <label for="employeeId" class="form-label">員工編號</label>
        <input 
          type="text" 
          class="form-control" 
          id="employeeId" 
          v-model="employeeId" 
          placeholder="請輸入員編 (例: 00000000)"
          :disabled="loading"
        >
      </div>

      <div class="d-flex justify-content-between mb-4">
        <button class="btn btn-primary w-45" @click="handlePunch('1')" :disabled="loading">上班打卡</button>
        <button class="btn btn-secondary w-45" @click="handlePunch('0')" :disabled="loading">下班打卡</button>
      </div>

      <hr>

      <button class="btn btn-outline-info w-100 mb-3" @click="loadRecords" :disabled="loading">查詢紀錄</button>

      <div v-if="records.length > 0">
        <h5>打卡紀錄</h5>
        <ul class="list-group">
          <li class="list-group-item d-flex justify-content-between align-items-center" v-for="r in records" :key="r.id">
            <span>{{ r.punchTime }}</span>
            <span class="badge" :class="r.punchType === 1 ? 'bg-success' : 'bg-secondary'">
              {{ r.punchType === 1 ? '上班' : '下班' }}
            </span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axiosClient from '../api/axiosClient';
import { encryptData, fetchPublicKey } from '../utils/crypto';

const employeeId = ref('');
const currentTime = ref('');
const records = ref([]);
const loading = ref(false);
let timer;

const updateTime = () => {
  currentTime.value = new Date().toLocaleString();
};

onMounted(async () => {
  updateTime();
  timer = setInterval(updateTime, 1000);
  await fetchPublicKey();
});

onUnmounted(() => {
  clearInterval(timer);
});

const handlePunch = async (punchType) => {
  if (!employeeId.value.trim()) {
    alert("員編不可為空白");
    return;
  }
  
  loading.value = true;
  try {
    const payload = {
      employeeId: employeeId.value.trim(),
      punchType: punchType
    };
    const encryptedText = encryptData(payload);
    
    const res = await axiosClient.post('/api/punches', { data: encryptedText });
    if (res.status === 200) {
      alert(`打卡成功！時間: ${res.data.punchTime}`);
      employeeId.value = '';
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const loadRecords = async () => {
  loading.value = true;
  try {
    const res = await axiosClient.get('/api/punches');
    records.value = res.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.w-45 {
  width: 48%;
}
</style>
