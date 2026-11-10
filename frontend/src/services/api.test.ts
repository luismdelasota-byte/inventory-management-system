import type { AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import { unwrap } from './api'

function response(data: unknown): Promise<AxiosResponse<unknown>> {
  return Promise.resolve({ data } as AxiosResponse<unknown>)
}

describe('unwrap', () => {
  it('returns data from a successful API response', async () => {
    await expect(unwrap<{ id: number }>(response({
      success: true,
      message: 'OK',
      data: { id: 7 },
    }))).resolves.toEqual({ id: 7 })
  })

  it('rejects unsuccessful API responses with the server message', async () => {
    await expect(unwrap(response({
      success: false,
      message: 'No autorizado',
      data: null,
    }))).rejects.toThrow('No autorizado')
  })

  it('rejects responses that do not match the API envelope', async () => {
    await expect(unwrap(response({ data: [] })))
      .rejects.toThrow('La respuesta del servidor no tiene el formato esperado.')
  })
})
