import axios from 'axios'
import { tokenStorage } from '../utils/tokenStorage'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // Render Free 인스턴스의 콜드 스타트 시간을 고려해 요청을 충분히 기다린다.
  timeout: 30000,
})

client.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default client
