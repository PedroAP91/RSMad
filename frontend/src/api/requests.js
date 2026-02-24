import { fetchJson } from './http'

export async function listRequests(params = {}) {
  const searchParams = new URLSearchParams()

  if (params.q) searchParams.set('q', params.q)
  if (params.status) searchParams.set('status', params.status)
  if (params.type) searchParams.set('type', params.type)
  if (params.assigned !== '') searchParams.set('assigned', params.assigned)

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 10))
  if (params.sort) searchParams.set('sort', params.sort)

  const query = searchParams.toString()
  const url = query ? `/api/requests?${query}` : '/api/requests'

  return fetchJson(url, { auth: true })
}
