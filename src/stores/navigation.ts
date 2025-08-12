import { defineStore } from 'pinia';

export const useNavigationStore = defineStore('navigation', {
  state: () => ({
    previousRouteName: null as string | null,
  }),
  actions: {
    setPreviousRouteName(name: string | null) {
      this.previousRouteName = name;
    },
  },
});
