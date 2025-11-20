import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import VisMapa from '../VisMapa.vue'
import { useMapasStore } from '@/stores/mapas'
import { useProcessosStore } from '@/stores/processos'
import { useSubprocessosStore } from '@/stores/subprocessos'
import { useNotificacoesStore } from '@/stores/notificacoes'
import { createRouter, createMemoryHistory } from 'vue-router'
import { SituacaoSubprocesso } from '@/types/tipos'
import AceitarMapaModal from '@/components/AceitarMapaModal.vue'
import { BButton, BModal } from 'bootstrap-vue-next'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
      { path: '/', component: { template: '<div>Home</div>' } },
      { path: '/processo/:codProcesso/:siglaUnidade/vis-mapa', name: 'SubprocessoVisMapa', component: VisMapa },
      { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
      { path: '/processo/:codProcesso/:siglaUnidade', name: 'Subprocesso', component: { template: '<div>Subprocesso</div>' } }
  ]
})

describe('VisMapa.vue', () => {
  beforeEach(async () => {
    vi.clearAllMocks()
    await router.push('/processo/1/TEST/vis-mapa')
    await router.isReady()
  })

  const mountComponent = (initialState = {}) => {
    return mount(VisMapa, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              mapas: {
                mapaVisualizacao: {
                  codigo: 1,
                  descricao: 'Mapa Test',
                  competencias: [
                    {
                      codigo: 1,
                      descricao: 'Competencia 1',
                      atividades: [
                        {
                          codigo: 1,
                          descricao: 'Atividade 1',
                          conhecimentos: [
                            { codigo: 1, descricao: 'Conhecimento 1' }
                          ]
                        }
                      ]
                    }
                  ]
                },
                ...initialState['mapas']
              },
              unidades: {
                unidades: [
                  { sigla: 'TEST', nome: 'Unidade de Teste', filhas: [] }
                ]
              },
              processos: {
                  processoDetalhe: {
                      unidades: [
                          {
                              sigla: 'TEST',
                              codUnidade: 10,
                              situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO
                          }
                      ]
                  },
                  ...initialState['processos']
              },
              perfil: {
                  perfilSelecionado: 'CHEFE',
                  ...initialState['perfil']
              }
            }
          }),
          router
        ],
        stubs: {
            AceitarMapaModal: true,
            // BModal stubs to ensure we can find buttons in footer/body if they are rendered in slots
            // But actually, if we don't stub BModal, we need to wait for it to open (it might use Teleport).
            // Best to stub BModal to render content inline or similar.
            // Or use a real BModal but we need to handle async.
            // Let's stub BModal to be a simple div that renders slots always?
            // No, BModal has v-model="modelValue".
            // Let's stub it manually.
            BModal: {
                props: ['modelValue', 'title'],
                template: `
                    <div v-if="modelValue" class="custom-modal-stub" :data-title="title">
                        <slot />
                        <div class="modal-footer">
                            <slot name="footer" />
                        </div>
                    </div>
                `
            }
        }
      }
    })
  }

  it('renders correctly with data from store', async () => {
    const wrapper = mountComponent()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe('Competencia 1')
    expect(wrapper.find('.atividade-associada-descricao').text()).toBe('Atividade 1')
    expect(wrapper.find('[data-testid="conhecimento-item"]').text()).toBe('Conhecimento 1')
  })

  it('shows buttons for CHEFE when MAPEAMENTO_CONCLUIDO', async () => {
    const wrapper = mountComponent({
        perfil: { perfilSelecionado: 'CHEFE' },
        processos: {
            processoDetalhe: {
                unidades: [
                    { sigla: 'TEST', codUnidade: 10, situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO }
                ]
            }
        }
    })
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="apresentar-sugestoes-btn"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="validar-btn"]').exists()).toBe(true)
  })

  it('shows buttons for GESTOR when MAPA_VALIDADO', async () => {
    const wrapper = mountComponent({
        perfil: { perfilSelecionado: 'GESTOR' },
        processos: {
            processoDetalhe: {
                unidades: [
                    { sigla: 'TEST', codUnidade: 10, situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO }
                ]
            }
        }
    })
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="devolver-ajustes-btn"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="btn-registrar-aceite-homologar"]').exists()).toBe(true)
  })

  it('opens validar modal and confirms', async () => {
      const wrapper = mountComponent()
      const store = useProcessosStore()
      const notificacoes = useNotificacoesStore()

      await wrapper.find('[data-testid="validar-btn"]').trigger('click')
      await wrapper.vm.$nextTick()

      const confirmBtn = wrapper.find('[data-testid="modal-validar-confirmar"]')
      expect(confirmBtn.exists()).toBe(true)

      await confirmBtn.trigger('click')

      expect(store.validarMapa).toHaveBeenCalledWith(10)
      expect(notificacoes.sucesso).toHaveBeenCalled()
  })

  it('opens sugestoes modal and confirms', async () => {
      const wrapper = mountComponent()
      const store = useProcessosStore()

      await wrapper.find('[data-testid="apresentar-sugestoes-btn"]').trigger('click')
      await wrapper.vm.$nextTick()

      const textarea = wrapper.find('[data-testid="sugestoes-textarea"]')
      await textarea.setValue('Minhas sugestões')

      const confirmBtn = wrapper.find('[data-testid="modal-apresentar-sugestoes-confirmar"]')
      await confirmBtn.trigger('click')

      expect(store.apresentarSugestoes).toHaveBeenCalledWith(10, { sugestoes: 'Minhas sugestões' })
  })

  it('opens devolucao modal and confirms (GESTOR)', async () => {
      const wrapper = mountComponent({
        perfil: { perfilSelecionado: 'GESTOR' },
        processos: {
            processoDetalhe: {
                unidades: [
                    { sigla: 'TEST', codUnidade: 10, situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO }
                ]
            }
        }
      })
      const store = useSubprocessosStore()

      await wrapper.find('[data-testid="devolver-ajustes-btn"]').trigger('click')
      await wrapper.vm.$nextTick()

      const textarea = wrapper.find('[data-testid="observacao-devolucao-textarea"]')
      await textarea.setValue('Ajustar X')

      const confirmBtn = wrapper.find('[data-testid="modal-devolucao-confirmar"]')
      await confirmBtn.trigger('click')

      expect(store.devolverRevisaoCadastro).toHaveBeenCalledWith(10, { motivo: '', observacoes: 'Ajustar X' })
  })

  it('opens aceitar modal and confirms (GESTOR)', async () => {
      const wrapper = mountComponent({
        perfil: { perfilSelecionado: 'GESTOR' },
        processos: {
            processoDetalhe: {
                unidades: [
                    { sigla: 'TEST', codUnidade: 10, situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO }
                ]
            }
        }
      })
      const store = useSubprocessosStore()

      await wrapper.find('[data-testid="btn-registrar-aceite-homologar"]').trigger('click')
      await wrapper.vm.$nextTick()

      const modal = wrapper.findComponent(AceitarMapaModal)
      expect(modal.props('mostrarModal')).toBe(true)

      await modal.vm.$emit('confirmar-aceitacao', 'Obs aceite')

      expect(store.aceitarRevisaoCadastro).toHaveBeenCalledWith(10, { observacoes: 'Obs aceite' })
  })

  it('confirms homologacao (ADMIN)', async () => {
      const wrapper = mountComponent({
        perfil: { perfilSelecionado: 'ADMIN' },
        processos: {
            processoDetalhe: {
                unidades: [
                    { sigla: 'TEST', codUnidade: 10, situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO }
                ]
            }
        }
      })
      const store = useSubprocessosStore()

      await wrapper.find('[data-testid="btn-registrar-aceite-homologar"]').trigger('click')
      await wrapper.vm.$nextTick()

      const modal = wrapper.findComponent(AceitarMapaModal)
      await modal.vm.$emit('confirmar-aceitacao', 'Obs homolog')

      expect(store.homologarRevisaoCadastro).toHaveBeenCalledWith(10, { observacoes: 'Obs homolog' })
  })
})
