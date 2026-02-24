function buildBasicAuthHeader() {
  if (!import.meta.env.DEV) {
    return null
  }

  const user = import.meta.env.VITE_BASIC_USER
  const pass = import.meta.env.VITE_BASIC_PASS
  const hasUser = typeof user === 'string' && user.length > 0
  const hasPass = typeof pass === 'string' && pass.length > 0

  if (!hasUser || !hasPass) {
    return null
  }

  return `Basic ${btoa(`${user}:${pass}`)}`
}

export async function fetchJson(url, { auth, ...options } = {}) {
  if (auth && import.meta.env.DEV) {
    const basicAuth = buildBasicAuthHeader()
    if (!basicAuth) {
      throw new Error('No autorizado. Copia .env.development.example a .env.development.local y pon VITE_BASIC_USER/VITE_BASIC_PASS')
    }
  }

  const headers = new Headers(options.headers || {})

  headers.set('Accept', 'application/json')

  // Basic Auth solo para desarrollo local.
  if (auth) {
    const basicAuth = buildBasicAuthHeader()
    if (basicAuth) headers.set('Authorization', basicAuth)
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
