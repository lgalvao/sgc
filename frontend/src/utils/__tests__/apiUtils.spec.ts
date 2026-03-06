import { describe, it, expect, vi } from 'vitest';
import { apiGet, apiPost } from '../apiUtils';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('apiUtils', () => {
  it('apiGet', async () => {
    (apiClient.get as any).mockResolvedValueOnce({ data: 'result' });
    const res = await apiGet('/test', { p: 1 });
    expect(apiClient.get).toHaveBeenCalledWith('/test', { params: { p: 1 } });
    expect(res).toBe('result');
  });

  it('apiPost', async () => {
    (apiClient.post as any).mockResolvedValueOnce({ data: 'result2' });
    const res = await apiPost('/test', { d: 2 });
    expect(apiClient.post).toHaveBeenCalledWith('/test', { d: 2 });
    expect(res).toBe('result2');
  });
});
