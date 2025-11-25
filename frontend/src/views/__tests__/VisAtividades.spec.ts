import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { computed, nextTick } from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as mapaService from "@/services/mapaService";
import * as processoService from "@/services/processoService";
import * as unidadesService from "@/services/unidadesService";
import { Perfil, SituacaoSubprocesso, TipoProcesso } from "@/types/tipos";
import VisAtividades from "@/views/VisAtividades.vue";

const pushMock = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push: pushMock,
  }),
  createRouter: () => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  }),
  createWebHistory: () => ({}),
}));

vi.mock("@/composables/usePerfil", () => ({
  usePerfil: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
  obterMapaVisualizacao: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
  obterDetalhesProcesso: vi.fn(),
}));

vi.mock("@/services/unidadesService", () => ({
  buscarUnidadePorSigla: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
  listarAnalisesCadastro: vi.fn(),
}));

describe("VisAtividades.vue", () => {
  let wrapper: any;

  function createWrapper(perfil: Perfil, situacao: SituacaoSubprocesso) {
    const wrapper = mount(VisAtividades, {
      props: {
        codProcesso: 1,
        sigla: "TESTE",
      },
      global: {
        plugins: [
          createTestingPinia({
            stubActions: false,
            initialState: {
              perfil: {
                perfilSelecionado: perfil,
              },
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: TipoProcesso.REVISAO,
                  unidades: [
                    {
                      codUnidade: 123,
                      sigla: "TESTE",
                      situacaoSubprocesso: situacao,
                    },
                  ],
                },
              },
              unidades: {
                unidade: {
                  codigo: 1,
                  nome: "Unidade de Teste",
                  sigla: "TESTE",
                },
              },
            },
          }),
        ],
      },
    });

    return { wrapper };
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it('deve mostrar o botão "Impacto no mapa" para GESTOR em REVISAO_CADASTRO_DISPONIBILIZADA', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.GESTOR,
      SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
    );
    wrapper = w;
    wrapper.vm.subprocesso.situacaoSubprocesso = SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
    await flushPromises();
    await nextTick();

    console.log(wrapper.vm.perfilSelecionado);
    if (wrapper.vm.subprocesso) {
      console.log(wrapper.vm.subprocesso.situacaoSubprocesso);
    }
    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('deve mostrar o botão "Impacto no mapa" para ADMIN em REVISAO_CADASTRO_DISPONIBILIZADA', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.ADMIN,
      SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
    );
    wrapper = w;
    wrapper.vm.subprocesso.situacaoSubprocesso = SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('não deve mostrar o botão "Impacto no mapa" para GESTOR em outra situação', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.GESTOR,
      SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
    );
    wrapper = w;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(false);
  });
});
