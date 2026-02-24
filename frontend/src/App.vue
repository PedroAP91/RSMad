<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { listRequests } from './api/requests'

const filters = reactive({
  q: '',
  status: '',
  type: '',
  assigned: '',
  size: 10,
  sort: 'updatedAt,desc',
})

const page = ref(0)
const items = ref([])
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')

const hasPrev = computed(() => page.value > 0)
const from = computed(() => (total.value === 0 ? 0 : page.value * Number(filters.size) + 1))
const to = computed(() => (total.value === 0 ? 0 : Math.min(page.value * Number(filters.size) + items.value.length, total.value)))
const hasNext = computed(() => to.value < total.value)

function normalizeResponse(data) {
  if (Array.isArray(data)) {
    return { content: data, totalElements: data.length }
  }
  if (Array.isArray(data?.content) && typeof data?.totalElements === 'number') {
    return { content: data.content, totalElements: data.totalElements }
  }
  return { content: [], totalElements: 0 }
}

function mapErrorMessage(error) {
  const text = String(error?.message || error)
  if (text.includes('401')) return 'No autorizado (Basic Auth). Revisa .env.development.local'
  if (text.includes('400')) return 'Parametros invalidos'
  return text || 'Error inesperado'
}

async function loadRequests() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await listRequests({
      q: filters.q.trim(),
      status: filters.status.trim(),
      type: filters.type.trim(),
      assigned: filters.assigned,
      page: page.value,
      size: Number(filters.size),
      sort: filters.sort.trim(),
    })
    const normalized = normalizeResponse(data)
    items.value = normalized.content
    total.value = normalized.totalElements
  } catch (error) {
    items.value = []
    total.value = 0
    errorMessage.value = mapErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  loadRequests()
}

function clearFilters() {
  filters.q = ''
  filters.status = ''
  filters.type = ''
  filters.assigned = ''
  filters.size = 10
  filters.sort = 'updatedAt,desc'
  page.value = 0
  loadRequests()
}

function prevPage() {
  if (!hasPrev.value) return
  page.value -= 1
  loadRequests()
}

function nextPage() {
  if (!hasNext.value) return
  page.value += 1
  loadRequests()
}

onMounted(loadRequests)
</script>

<template>
  <div class="container">
    <h1>Requests</h1>

    <form class="filters" @submit.prevent="search">
      <label>
        q
        <input v-model="filters.q" type="text" />
      </label>

      <label>
        status
        <input v-model="filters.status" type="text" />
      </label>

      <label>
        type
        <input v-model="filters.type" type="text" />
      </label>

      <label>
        assigned
        <select v-model="filters.assigned">
          <option value="">(vacio)</option>
          <option value="true">true</option>
          <option value="false">false</option>
        </select>
      </label>

      <label>
        page size
        <select v-model.number="filters.size">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
      </label>

      <label>
        sort
        <input v-model="filters.sort" type="text" />
      </label>

      <div class="actions">
        <button type="submit" :disabled="loading">Buscar</button>
        <button type="button" :disabled="loading" @click="clearFilters">Limpiar</button>
      </div>
    </form>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="loading">Cargando...</p>

    <table v-else class="table">
      <thead>
        <tr>
          <th>id</th>
          <th>titulo</th>
          <th>estado</th>
          <th>tipo</th>
          <th>district</th>
          <th>resourceId</th>
          <th>updatedAt</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="items.length === 0">
          <td colspan="7">Sin resultados</td>
        </tr>
        <tr v-for="request in items" :key="request.id">
          <td>{{ request.id }}</td>
          <td>{{ request.titulo }}</td>
          <td>{{ request.estado }}</td>
          <td>{{ request.tipo }}</td>
          <td>{{ request.district }}</td>
          <td>{{ request.resourceId }}</td>
          <td>{{ request.updatedAt }}</td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button type="button" :disabled="loading || !hasPrev" @click="prevPage">Prev</button>
      <button type="button" :disabled="loading || !hasNext" @click="nextPage">Next</button>
      <span>Mostrando {{ from }}-{{ to }} de {{ total }}</span>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem;
  font-family: Arial, sans-serif;
}
.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}
label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
  gap: 0.25rem;
}
input,
select,
button {
  padding: 0.45rem;
  font-size: 0.95rem;
}
.actions {
  display: flex;
  gap: 0.5rem;
  align-items: end;
}
.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.75rem;
}
.table th,
.table td {
  border: 1px solid #ddd;
  padding: 0.45rem;
  text-align: left;
  font-size: 0.9rem;
}
.error {
  color: #b00020;
  font-weight: 600;
}
.pagination {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-top: 0.75rem;
}
.pagination span {
  margin-left: 0.5rem;
  font-size: 0.9rem;
}
</style>
