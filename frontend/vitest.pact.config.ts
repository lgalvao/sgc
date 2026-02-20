import {defineConfig, mergeConfig} from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(viteConfig, defineConfig({
  test: {
    include: ['**/*.pact.spec.ts'],
    fileParallelism: false,
    environment: 'node', // Pact tests run in Node
  }
}))
