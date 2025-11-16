import { vi } from 'vitest';

vi.mock('bootstrap', () => ({
  Tooltip: class Tooltip {
    constructor() {}
    dispose() {}
  },
}));
