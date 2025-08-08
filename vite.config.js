import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import istanbul from 'vite-plugin-istanbul';

export default defineConfig({
  plugins: [
    vue(),
    process.env.VITE_COVERAGE ? istanbul({
      include: 'src/*',
      exclude: ['node_modules', 'test-results', 'spec'],
      extension: ['.js', '.ts', '.vue'],
      forceBuildInstrument: true,
    }) : null,
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
