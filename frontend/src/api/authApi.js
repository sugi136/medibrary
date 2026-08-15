import client from './client'

export const authApi = {
  signup: (payload) => client.post('/auth/signup', payload),
  login: (payload) => client.post('/auth/login', payload),
}
