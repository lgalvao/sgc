import axios from 'axios';
import { describe, it, expect } from 'vitest';

describe('Backend Health Check', () => {
  it('should receive a successful response from a health check endpoint', async () => {
    try {
      // This test assumes that your backend has a /health endpoint that returns a 200 OK status.
      // If your health check endpoint is different, please update the URL here.
      const response = await axios.get('/health');
      expect(response.status).toBe(200);
    } catch (error) {
      // If the request fails, it provides a more informative error message.
      console.error('Integration test failed:', error.message);
      // This assertion will fail the test if the backend is not reachable.
      expect.fail('The backend is not running or the health check endpoint is not configured.');
    }
  });
});
