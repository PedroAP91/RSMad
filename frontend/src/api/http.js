function buildBasicAuthHeader() {
  if (!import.meta.env.DEV) {
    return null
  }

  const user = import.meta.env.VITE_BASIC_USER
  const pass = import.meta.env.VITE_BASIC_PASS

  if (!user || !pass) {
    return null
  }

  return `Basic ${btoa(`${user}:${pass}`)}`
}

export async function fetchJson(url, { auth, ...options } = {}) {
  const headers = new Headers(options.headers || {})

  headers.set('Accept', 'application/json')

  // Basic Auth solo para desarrollo local.
  if (auth) {
    const basicAuth = buildBasicAuthHeader()
    if (basicAuth) {
      headers.set('Authorization', basicAuth)
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status} ${response.statusText}`)
  }
  return response.json()
}
