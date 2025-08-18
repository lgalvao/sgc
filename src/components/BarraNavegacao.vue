<template>
  <div class="d-flex align-items-center gap-3">
    <button v-if="shouldShowBackButton"
            class="btn btn-outline-secondary btn-sm"
            type="button"
            @click="goBack">
      <i class="bi bi-arrow-left"></i> Voltar
    </button>

    <nav v-if="shouldShowBreadcrumbs" aria-label="breadcrumb" data-testid="breadcrumbs">
      <ol class="breadcrumb mb-0">
        <li v-for="(crumb, index) in crumbs" :key="index" class="breadcrumb-item"
            :class="{ active: index === crumbs.length - 1 }" aria-current="page" data-testid="breadcrumb-item">
          <template v-if="index < crumbs.length - 1">
            <template v-if="crumb.to">
              <RouterLink :to="crumb.to" :title="crumb.title || undefined">
                <i v-if="crumb.label === '__home__'" class="bi bi-house-door" aria-label="Início"
                   data-testid="breadcrumb-home-icon"></i>
                <span v-else>{{ crumb.label }}</span>
              </RouterLink>
            </template>
            <template v-else>
              <span :title="crumb.title || undefined">
                <i v-if="crumb.label === '__home__'" class="bi bi-house-door" aria-label="Início"
                   data-testid="breadcrumb-home-icon"></i>
                <span v-else>{{ crumb.label }}</span>
              </span>
            </template>
          </template>
          <template v-else>
            <span :title="crumb.title || undefined">
              <template v-if="crumb.label === '__home__'">
                <i class="bi bi-house-door" aria-label="Início" data-testid="breadcrumb-home-icon"></i>
              </template>
              <template v-else>
                {{ crumb.label }}
                </template>
            </span>
          </template>
        </li>
      </ol>
    </nav>
    <!-- Fim do conteúdo do Breadcrumbs.vue -->
  </div>
</template>

<script setup lang="ts">
import {computed, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUnidadesStore} from '@/stores/unidades'
import {type TrailCrumb, useNavigationTrail} from '@/stores/navigationTrail'

const route = useRoute()
const router = useRouter()

const fallbackPath = '/painel'

const shouldShowBackButton = computed(() => {
  return route.path !== '/login';
})

function goBack() {
  if (crumbs.value.length > 1) {
    const parentCrumb = crumbs.value[crumbs.value.length - 2];
    if (parentCrumb && parentCrumb.to) {
      router.push(parentCrumb.to);
      return;
    }
  }
  router.push(fallbackPath)
}

const unidadesStore = useUnidadesStore()
const trailStore = useNavigationTrail()

const handleNavbarReset = () => {
  let cameFromNavbar = false
  try {
    cameFromNavbar = sessionStorage.getItem('cameFromNavbar') === '1'
  } catch {
  }
  if (cameFromNavbar) {
    trailStore.reset()
    try {
      sessionStorage.removeItem('cameFromNavbar')
    } catch {
    }
  }
}

const handlePopToExistingCrumb = (): boolean => {
  const idxByTo = trailStore.crumbs.findIndex(c => {
    if (!c.to) return false
    try {
      const resolved = router.resolve(c.to as any)
      return resolved.fullPath === route.fullPath
    } catch {
      return false
    }
  })
  if (idxByTo >= 0) {
    trailStore.popTo(idxByTo)
    return true
  }
  return false
}

const processProcessoContext = () => {
  const idProcessoParam = (route.params as any).id || (route.params as any).idProcesso || route.query.idProcesso
  const processoLink = idProcessoParam
      ? {name: 'Processo', params: {idProcesso: Number(idProcessoParam)}}
      : undefined
  const idxProcCrumb = trailStore.crumbs.findIndex(c => c.label === 'Processo')
  if (idxProcCrumb === -1) {
    trailStore.push({label: 'Processo', to: processoLink})
  } else if (processoLink) {
    trailStore.crumbs[idxProcCrumb].to = processoLink
  }

  let siglaPU = String((route.params as any).siglaUnidade || (route.params as any).unidadeId || '')
  if (!siglaPU && /\/processos\/.+\/unidade\//.test(route.path)) {
    const m = route.path.match(/\/processos\/[^/]+\/unidade\/([^/]+)/)
    if (m && m[1]) siglaPU = decodeURIComponent(m[1])
  }
  if (siglaPU) {
    const u = unidadesStore.pesquisarUnidade(siglaPU)
    const idxProc = trailStore.crumbs.findIndex(c => c.label === 'Processo')
    if (idxProc >= 0 && idxProc < trailStore.crumbs.length - 1) {
      trailStore.popTo(idxProc)
    }
    const last = trailStore.crumbs[trailStore.crumbs.length - 1]
    const basePU = route.path.replace(/\/(mapa|atribuicao|atividades|cadastro)(\/.*)?$/, '')
    const pid = Number((route.params as any).id || (route.params as any).idProcesso || route.query.idProcesso)
    const siglaTo = isFinite(pid)
        ? {name: 'ProcessoUnidade', params: {idProcesso: pid, siglaUnidade: siglaPU}}
        : {path: basePU}
    if (!last || last.label !== siglaPU) {
      trailStore.push({label: siglaPU, to: siglaTo, title: u?.nome})
    } else {
      trailStore.crumbs[trailStore.crumbs.length - 1].to = siglaTo
    }

    const matchedChildren = route.matched.filter(m => /\/(mapa|atribuicao|atividades|cadastro)(\/.*)?$/.test(m.path))
    const lastMatch = matchedChildren[matchedChildren.length - 1]
    const metaBc = lastMatch ? (typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb) : undefined
    let childLabel: string
    if (/\/atividades(\/.*)?$/.test(route.path)) childLabel = 'Cadastro'
    else childLabel = metaBc ? String(metaBc) : ''
    if (childLabel) {
      const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
      if (!lastCrumb || lastCrumb.label !== childLabel) {
        trailStore.push({label: childLabel, to: route.fullPath})
      }
    }
  }
}

const processUnidadeContext = () => {
  const sigla = String(route.params.siglaUnidade || '')
  const idProcesso = String(route.query.idProcesso || '')
  const u = unidadesStore.pesquisarUnidade(sigla)
  if (idProcesso) {
    const idxProc = trailStore.crumbs.findIndex(c => c.label === 'Processo')
    if (idxProc >= 0 && idxProc < trailStore.crumbs.length - 1) {
      trailStore.popTo(idxProc)
    }
    if (idxProc === -1) {
      trailStore.push({label: 'Processo', to: {name: 'Processo', params: {idProcesso: Number(idProcesso)}}})
    }
  } else {
    if (trailStore.crumbs.length > 1) trailStore.popTo(0)
  }
  const last = trailStore.crumbs[trailStore.crumbs.length - 1]
  if (!last || last.label !== sigla) {
    const siglaTo = idProcesso
        ? {name: 'ProcessoUnidade', params: {idProcesso: Number(idProcesso), siglaUnidade: sigla}}
        : {path: `/unidade/${sigla}`}
    trailStore.push({label: sigla, to: siglaTo, title: u?.nome})
  }

  const matchedChildren = route.matched.filter(m => m.path !== '/unidade/:siglaUnidade')
  if (matchedChildren.length > 0) {
    const lastMatch = matchedChildren[matchedChildren.length - 1]
    const metaBc = typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb
    let childLabel = metaBc ? String(metaBc) : ''
    if (!childLabel && /\/atividades(\/.*)?$/.test(route.path)) childLabel = 'Cadastro'
    if (childLabel) {
      const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
      if (!lastCrumb || lastCrumb.label !== childLabel) {
        trailStore.push({label: childLabel, to: route.fullPath})
      }
    }
  }
}

const processGenericRoute = () => {
  const matched = route.matched
  const lastMatch = matched[matched.length - 1]
  if (lastMatch) {
    const metaBc = typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb
    const label = metaBc ? String(metaBc) : ''
    if (label) {
      const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
      if (!lastCrumb || lastCrumb.label !== label) {
        trailStore.push({label, to: route.fullPath})
      }
    }
  }
}

const buildProcessoFallbackCrumbs = (baseCrumbs: Array<{ label: string; to?: any; title?: string }>): Array<{
  label: string;
  to?: any;
  title?: string
}> => {
  const idProcessoParam = (route.params as any).idProcesso || route.query.idProcesso
  const processoLink = idProcessoParam ? {name: 'Processo', params: {idProcesso: Number(idProcessoParam)}} : undefined
  baseCrumbs.push({label: 'Processo', to: processoLink})
  const siglaPU = String((route.params as any).siglaUnidade || '')
  if (siglaPU) {
    const u = unidadesStore.pesquisarUnidade(siglaPU)
    const idProcessoParam2 = (route.params as any).idProcesso || route.query.idProcesso
    const siglaTo = idProcessoParam2
        ? {name: 'ProcessoUnidade', params: {idProcesso: Number(idProcessoParam2), siglaUnidade: siglaPU}}
        : undefined
    baseCrumbs.push({label: siglaPU, to: siglaTo, title: u?.nome})
    const matchedChildren = route.matched.filter(m => /\/(mapa|atribuicao|atividades)(\/.*)?$/.test(m.path))
    const last = matchedChildren[matchedChildren.length - 1]
    const metaBc = last ? (typeof last.meta?.breadcrumb === 'function' ? last.meta.breadcrumb(route) : last.meta?.breadcrumb) : undefined
    const label = /\/atividades(\/.*)?$/.test(route.path)
        ? 'Cadastro'
        : (metaBc ? String(metaBc) : '')
    if (label) baseCrumbs.push({label})
  }
  return baseCrumbs
}

const buildUnidadeFallbackCrumbs = (baseCrumbs: Array<{ label: string; to?: any; title?: string }>): Array<{
  label: string;
  to?: any;
  title?: string
}> => {
  const sigla = String(route.params.siglaUnidade || '')
  const idProcesso = String(route.query.idProcesso || '')
  const u = unidadesStore.pesquisarUnidade(sigla)
  const siglaTo = idProcesso
      ? {name: 'ProcessoUnidade', params: {idProcesso: Number(idProcesso), siglaUnidade: sigla}}
      : {path: `/unidade/${sigla}`}
  if (idProcesso) baseCrumbs.push({
    label: 'Processo',
    to: {name: 'Processo', params: {idProcesso: Number(idProcesso)}}
  })
  baseCrumbs.push({label: sigla, to: siglaTo, title: u?.nome})

  const matchedChildren = route.matched.filter(m => m.path !== '/unidade/:siglaUnidade')
  if (matchedChildren.length > 0) {
    const last = matchedChildren[matchedChildren.length - 1]
    const metaBc = typeof last.meta?.breadcrumb === 'function' ? last.meta.breadcrumb(route) : last.meta?.breadcrumb
    if (metaBc) baseCrumbs.push({label: String(metaBc)})
  }
  return baseCrumbs
}

const buildGenericFallbackCrumbs = (baseCrumbs: Array<{ label: string; to?: any; title?: string }>): Array<{
  label: string;
  to?: any;
  title?: string
}> => {
  const matchedAll = route.matched
  matchedAll.forEach((m, idx) => {
    if (idx === 0 && (m.path === '/' || m.path === '/painel')) return // já temos Painel
    const metaBc = typeof m.meta?.breadcrumb === 'function' ? m.meta.breadcrumb(route) : m.meta?.breadcrumb
    if (metaBc) baseCrumbs.push({label: String(metaBc)})
  })
  return baseCrumbs
}

const shouldShowBreadcrumbs = computed(() => {
  // Esconde breadcrumbs: login e painel. Ocultação via navbar é controlada pelo layout (App.vue)
  if (route.path === '/login') return false
  return route.path !== '/painel';

})

// Atualiza a trilha com base na navegação real
const updateTrail = () => {
  handleNavbarReset()

  if (handlePopToExistingCrumb()) {
    return
  }

  trailStore.ensureBase()

  if (route.path.startsWith('/processo') || route.path.startsWith('/processos')) {
    processProcessoContext()
    return
  }

  const sigla = String(route.params.siglaUnidade || '')
  if (route.path.startsWith('/unidade/') && sigla) {
    processUnidadeContext()
    return
  }

  // Fora do contexto de unidade: empilha o último breadcrumb de matched, se houver
  processGenericRoute()
}

watch(() => route.fullPath, () => updateTrail(), {immediate: true})

const crumbs = computed(() => {
  // 1) Se há trilha de navegação explícita, use-a
  if (trailStore.crumbs.length > 0) return trailStore.crumbs as TrailCrumb[]

  // Monta um caminho essencial em vez de usar hierarquia de unidades
  const baseCrumbs: Array<{ label: string; to?: any; title?: string }> = []
  baseCrumbs.push({label: '__home__', to: {path: '/painel'}, title: 'Painel'})

  // Deep-link em contexto de Processo
  if (route.path.startsWith('/processo')) {
    return buildProcessoFallbackCrumbs(baseCrumbs)
  }

  // Deep-link em contexto de Unidade
  const sigla = String(route.params.siglaUnidade || '')
  if (route.path.startsWith('/unidade/') && sigla) {
    return buildUnidadeFallbackCrumbs(baseCrumbs)
  }

  // Deep-link fora de contexto de unidade/processo: usa route.matched/meta como mínimo
  return buildGenericFallbackCrumbs(baseCrumbs)
})
</script>

<style scoped>
.breadcrumb {
  --bs-breadcrumb-divider: '›';
}
</style>