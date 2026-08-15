import client from './client'

export const favoriteApi = {
  list: () => client.get('/favorites'),
  create: (drugId) => client.post('/favorites', { drugId }),
  remove: (favoriteId) => client.delete(`/favorites/${favoriteId}`),
}
