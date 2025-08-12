<template>
  <nav v-if="shouldShow" aria-label="breadcrumb">
    <ol class="breadcrumb mb-0">
      <li v-for="(crumb, index) in crumbs" :key="index" class="breadcrumb-item" :class="{ active: index === crumbs.length - 1 }" aria-current="page">
        <template v-if="index < crumbs.length - 1">
          <template v-if="crumb.to">
            <RouterLink :to="crumb.to" :title="crumb.title || undefined">
              <template v-if="crumb.label === '__home__'">
                <i class="bi bi-house-door" aria-label="Início"></i>
              </template>
              <template v-else>
                {{ crumb.label }}
              </template>
            </RouterLink>
          </template>
          <template v-else>
            <span :title="crumb.title || undefined">
              <template v-if="crumb.label === '__home__'">
                <i class="bi bi-house-door" aria-label="Início"></i>
              </template>
              <template v-else>
                {{ crumb.label }}
              </template>
            </span>
          </template>
        </template>
        <template v-else>
          <span :title="crumb.title || undefined">
            <template v-if="crumb.label === '__home__'">
              <i class="bi bi-house-door" aria-label="Início"></i>
            </template>
            <template v-else>
              {{ crumb.label }}
            </template>
          </span>
        </template>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUnidadesStore } from '@/stores/unidades'
import { useProcessosStore } from '@/stores/processos'
import { useNavigationTrail, type TrailCrumb } from '@/stores/navigationTrail'

const route = useRoute()
const router = useRouter()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const trailStore = useNavigationTrail()

const shouldShow = computed(() => {
  // Esconde breadcrumbs: login, painel e quando vindo diretamente da navbar
  if (route.path === '/login') return false
  if (route.path === '/painel') return false
  if (route.query.fromNavbar) return false
  return true
})

function fillPathParams(path: string, params: Record<string, any>): string {
  return path.replace(/:(\w+)/g, (_, key) => encodeURIComponent(params[key] ?? ''))
}

// Atualiza a trilha com base na navegação real
const updateTrail = () => {
  // Resetar quando vier explicitamente pela navbar nesta navegação
  if (route.query && typeof route.query.fromNavbar !== 'undefined') {
    trailStore.reset()
  }

  // Se a rota atual coincide com algum crumb.to, consideramos como navegação "voltar" e apararmos até ele
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
    return
  }

  trailStore.ensureBase()

  // Heurística por contexto
  // 1) Contexto de Processo (qualquer rota iniciando com /processo ou /processos)
  if (route.path.startsWith('/processo') || route.path.startsWith('/processos')) {
    const processoIdParam = (route.params as any).id || (route.params as any).processoId || route.query.processoId
    const processoLink = processoIdParam
      ? { name: 'Processo', params: { processoId: Number(processoIdParam) } }
      : undefined
    // Garante crumb "Processo" após Home e atualiza seu link quando disponível
    const idxProcCrumb = trailStore.crumbs.findIndex(c => c.label === 'Processo')
    if (idxProcCrumb === -1) {
      trailStore.push({ label: 'Processo', to: processoLink })
    } else if (processoLink) {
      trailStore.crumbs[idxProcCrumb].to = processoLink
    }

    // Se estiver no contexto de unidade do processo, adicionar a sigla e, depois, a subpágina (Mapa/Atribuição/Atividades etc.)
    let siglaPU = String((route.params as any).sigla || (route.params as any).unidadeId || '')
    if (!siglaPU && /\/processos\/.+\/unidade\//.test(route.path)) {
      const m = route.path.match(/\/processos\/[^/]+\/unidade\/([^/]+)/)
      if (m && m[1]) siglaPU = decodeURIComponent(m[1])
    }
    if (siglaPU) {
      const u = unidadesStore.pesquisarUnidade(siglaPU)
      // Antes de adicionar uma sigla nova em contexto de processo,
      // eliminamos quaisquer crumbs após "Processo" para evitar duplicações
      const idxProc = trailStore.crumbs.findIndex(c => c.label === 'Processo')
      if (idxProc >= 0 && idxProc < trailStore.crumbs.length - 1) {
        trailStore.popTo(idxProc)
      }
      const last = trailStore.crumbs[trailStore.crumbs.length - 1]
      const basePU = route.path.replace(/\/(mapa|atribuicao|atividades|cadastro)(\/.*)?$/, '')
      const pid = Number((route.params as any).id || (route.params as any).processoId || route.query.processoId)
      const siglaTo = isFinite(pid)
        ? { name: 'ProcessoUnidade', params: { processoId: pid, sigla: siglaPU } }
        : { path: basePU }
      if (!last || last.label !== siglaPU) {
        // navegar para rota padrão de unidade dentro do processo
        trailStore.push({ label: siglaPU, to: siglaTo, title: u?.nome })
      } else {
        // Atualiza link da sigla se mudou (ex.: chegou o processoId)
        trailStore.crumbs[trailStore.crumbs.length - 1].to = siglaTo
      }

      // Subpágina
      const matchedChildren = route.matched.filter(m => /\/(mapa|atribuicao|atividades|cadastro)(\/.*)?$/.test(m.path))
      const lastMatch = matchedChildren[matchedChildren.length - 1]
      const metaBc = lastMatch ? (typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb) : undefined
      let childLabel = ''
      if (/\/atividades(\/.*)?$/.test(route.path)) childLabel = 'Cadastro'
      else childLabel = metaBc ? String(metaBc) : ''
      if (childLabel) {
        const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
        if (!lastCrumb || lastCrumb.label !== childLabel) {
          trailStore.push({ label: childLabel, to: route.fullPath })
        }
      }
    } else {
      // Sem unidade: manter apenas "Processo" (NÃO empilhar 'Unidades' ou outras labels genéricas)
    }
    return
  }

  // (rotas legadas /processo-unidade foram removidas)

  const sigla = String(route.params.sigla || '')
  if (route.path.startsWith('/unidade/') && sigla) {
    const processoId = String(route.query.processoId || '')
    // Empilhar sigla (somente se ainda não existir como último passo coerente)
    const u = unidadesStore.pesquisarUnidade(sigla)
    // Ao entrar numa unidade, limpamos a trilha para evitar histórico residual:
    if (processoId) {
      // Em contexto de processo: manter até "Processo"
      const idxProc = trailStore.crumbs.findIndex(c => c.label === 'Processo')
      if (idxProc >= 0 && idxProc < trailStore.crumbs.length - 1) {
        trailStore.popTo(idxProc)
      }
      if (idxProc === -1) {
        // Garantir crumb Processo quando houver processoId
        trailStore.push({ label: 'Processo', to: { name: 'Processo', params: { processoId: Number(processoId) } } })
      }
    } else {
      // Fora de processo: manter apenas Home
      if (trailStore.crumbs.length > 1) trailStore.popTo(0)
    }
    const last = trailStore.crumbs[trailStore.crumbs.length - 1]
    if (!last || last.label !== sigla) {
      // Se em contexto de processo (query.processoId), link da sigla deve ir ao processo da unidade
      const siglaTo = processoId
        ? { name: 'ProcessoUnidade', params: { processoId: Number(processoId), sigla } }
        : { path: `/unidade/${sigla}` }
      // Antes da sigla, se processoId existe, garantir crumb Processo
      // (já garantido acima quando processoId existe)
      trailStore.push({ label: sigla, to: siglaTo, title: u?.nome })
    }

    // Se houver rota filha, empilhar o último meta.breadcrumb
    const matchedChildren = route.matched.filter(m => m.path !== '/unidade/:sigla')
    if (matchedChildren.length > 0) {
      const lastMatch = matchedChildren[matchedChildren.length - 1]
      const metaBc = typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb
      let childLabel = metaBc ? String(metaBc) : ''
      if (!childLabel && /\/atividades(\/.*)?$/.test(route.path)) childLabel = 'Cadastro'
      if (childLabel) {
        const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
        if (!lastCrumb || lastCrumb.label !== childLabel) {
          trailStore.push({ label: childLabel, to: route.fullPath })
        }
      }
    }
    return
  }

  // Fora do contexto de unidade: empilha o último breadcrumb de matched, se houver
  const matched = route.matched
  const lastMatch = matched[matched.length - 1]
  if (lastMatch) {
    const metaBc = typeof lastMatch.meta?.breadcrumb === 'function' ? lastMatch.meta.breadcrumb(route) : lastMatch.meta?.breadcrumb
    const label = metaBc ? String(metaBc) : ''
    if (label) {
      const lastCrumb = trailStore.crumbs[trailStore.crumbs.length - 1]
      if (!lastCrumb || lastCrumb.label !== label) {
        trailStore.push({ label, to: route.fullPath })
      }
    }
  }
}

watch(() => route.fullPath, () => updateTrail(), { immediate: true })

const crumbs = computed(() => {
  const matched = route.matched.filter(m => m.meta && m.meta.breadcrumb !== false)

  // 1) Se há trilha de navegação explícita, use-a
  if (trailStore.crumbs.length > 0) return trailStore.crumbs as TrailCrumb[]

  // 2) Fallback mínimo (deep-link ou primeira carga):
  // Monta um caminho essencial em vez de usar hierarquia de unidades
  const baseCrumbs: Array<{ label: string; to?: any; title?: string }> = []
  baseCrumbs.push({ label: '__home__', to: { path: '/painel' }, title: 'Painel' })

  // Deep-link em contexto de Processo
  if (route.path.startsWith('/processo')) {
    const processoIdParam = (route.params as any).processoId || route.query.processoId
    const processoLink = processoIdParam ? { name: 'Processo', params: { processoId: Number(processoIdParam) } } : undefined
    baseCrumbs.push({ label: 'Processo', to: processoLink })
    const siglaPU = String((route.params as any).sigla || '')
    if (siglaPU) {
      const u = unidadesStore.pesquisarUnidade(siglaPU)
      const processoIdParam2 = (route.params as any).processoId || route.query.processoId
      const siglaTo = processoIdParam2
        ? { name: 'ProcessoUnidade', params: { processoId: Number(processoIdParam2), sigla: siglaPU } }
        : undefined
      baseCrumbs.push({ label: siglaPU, to: siglaTo, title: u?.nome })
      const matchedChildren = route.matched.filter(m => /\/(mapa|atribuicao|atividades)(\/.*)?$/.test(m.path))
      const last = matchedChildren[matchedChildren.length - 1]
      const metaBc = last ? (typeof last.meta?.breadcrumb === 'function' ? last.meta.breadcrumb(route) : last.meta?.breadcrumb) : undefined
      const label = /\/atividades(\/.*)?$/.test(route.path)
        ? 'Cadastro'
        : (metaBc ? String(metaBc) : '')
      if (label) baseCrumbs.push({ label })
    }
    return baseCrumbs
  }

  // (fallback para processo-unidade removido)

  // Deep-link em contexto de Unidade
  const sigla = String(route.params.sigla || '')
  if (route.path.startsWith('/unidade/') && sigla) {
    const processoId = String(route.query.processoId || '')
    // Opcional: se veio por "Minha unidade" via navbar, inserir o passo
    let cameFromNavbar = false
    try { cameFromNavbar = sessionStorage.getItem('cameFromNavbar') === '1' } catch {}
    if (cameFromNavbar) {
      baseCrumbs.push({ label: 'Minha unidade', to: { path: `/unidade/${sigla}` } })
    }

    // Adiciona apenas a SIGLA (SEDOC já não entra pois não usamos a hierarquia aqui)
    const u = unidadesStore.pesquisarUnidade(sigla)
    const siglaTo = processoId
      ? { name: 'ProcessoUnidade', params: { processoId: Number(processoId), sigla } }
      : { path: `/unidade/${sigla}` }
    if (processoId) baseCrumbs.push({ label: 'Processo', to: { name: 'Processo', params: { processoId: Number(processoId) } } })
    baseCrumbs.push({ label: sigla, to: siglaTo, title: u?.nome })

    // Se estiver em rota filha (ex.: /mapa, /atribuicao), adiciona o último segmento como crumb final
    const matchedChildren = route.matched.filter(m => m.path !== '/unidade/:sigla')
    if (matchedChildren.length > 0) {
      const last = matchedChildren[matchedChildren.length - 1]
      const metaBc = typeof last.meta?.breadcrumb === 'function' ? last.meta.breadcrumb(route) : last.meta?.breadcrumb
      if (metaBc) baseCrumbs.push({ label: String(metaBc) })
    }
    return baseCrumbs
  }

  // Deep-link fora de contexto de unidade/processo: usa route.matched/meta como mínimo
  const matchedAll = route.matched
  matchedAll.forEach((m, idx) => {
    if (idx === 0 && (m.path === '/' || m.path === '/painel')) return // já temos Painel
    const metaBc = typeof m.meta?.breadcrumb === 'function' ? m.meta.breadcrumb(route) : m.meta?.breadcrumb
    if (metaBc) baseCrumbs.push({ label: String(metaBc) })
  })

  return baseCrumbs
})

function buildCrumbForMatch(m: any, idx: number, total: number) {
  let label: string = ''
  const bc = (m.meta as any)?.breadcrumb
  if (typeof bc === 'function') {
    try { label = bc(route) ?? '' } catch { label = '' }
  } else if (typeof bc === 'string') {
    label = bc
  }
  if (!label) label = (m.meta as any)?.title || String(m.name || '') || 'Início'
  const to = { path: fillPathParams(m.path, route.params as any) }
  if (idx === total - 1) return { label }
  return { label, to }
}
</script>

<style scoped>
.breadcrumb {
  --bs-breadcrumb-divider: '›';
}
</style>
