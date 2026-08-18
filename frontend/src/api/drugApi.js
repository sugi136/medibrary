import client from './client'

export const drugApi = {
  search: (params) => client.get('/drugs/search', { params }),
  detail: (drugId) => client.get(`/drugs/${drugId}`),
  sideEffects: (drugId) => client.get(`/drugs/${drugId}/side-effects`),
  duplicates: (drugId) => client.get(`/drugs/${drugId}/duplicates`),
  contraindications: (drugId) => client.get(`/drugs/${drugId}/contraindications`),
}
