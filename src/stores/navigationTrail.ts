import {defineStore} from 'pinia'

export type TrailCrumb = {
  label: string
  to?: any
  title?: string
}

export const useNavigationTrail = defineStore('navigationTrail', {
  state: () => ({
    crumbs: [] as TrailCrumb[],
  }),
  actions: {
    reset() {
      this.crumbs = []
    },
    set(crumbs: TrailCrumb[]) {
      this.crumbs = crumbs
    },
    push(crumb: TrailCrumb) {
      this.crumbs.push(crumb)
    },
    popTo(index: number) {
      if (index >= 0 && index < this.crumbs.length) {
        this.crumbs = this.crumbs.slice(0, index + 1)
      }
    },
    ensureBase() {
      if (this.crumbs.length === 0) {
        // crumb especial de Home (ícone), usa label __home__ e linka para /painel
        this.crumbs = [{ label: '__home__', to: { path: '/painel' }, title: 'Painel' }]
      }
    }
  }
})
