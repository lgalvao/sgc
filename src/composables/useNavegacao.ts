import { useRouter } from 'vue-router'
import { usePerfilStore } from '@/stores/perfil'
import { Perfil, Processo } from '@/types/tipos'

export function useNavegacao() {
  const router = useRouter()
  const perfilStore = usePerfilStore()

  function navegarParaProcesso(processo: Processo) {
    const perfilUsuario = perfilStore.perfilSelecionado
    
    if (perfilUsuario === Perfil.ADMIN || perfilUsuario === Perfil.GESTOR) {
      router.push({ name: 'Processo', params: { idProcesso: processo.id } })
    } else {
      const siglaUnidade = perfilStore.unidadeSelecionada
      if (siglaUnidade) {
        router.push({ 
          name: 'Subprocesso', 
          params: { idProcesso: processo.id, siglaUnidade } 
        })
      } else {
        console.error('Unidade do usuário não encontrada para o perfil CHEFE/SERVIDOR.')
      }
    }
  }

  return { navegarParaProcesso }
}