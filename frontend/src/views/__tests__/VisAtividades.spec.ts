import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick} from "vue";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import VisAtividades from "@/views/VisAtividades.vue";
import {usePerfilStore} from "@/stores/perfil";
import {useAtividadesStore} from "@/stores/atividades";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";

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

  function createWrapper(
    perfil: Perfil,
    situacao: SituacaoSubprocesso,
    tipoProcesso: TipoProcesso = TipoProcesso.REVISAO,
  ) {
    const wrapper = mount(VisAtividades, {
      props: {
        codProcesso: 1,
        sigla: "TESTE",
      },
      global: {
        plugins: [
          createTestingPinia({
            stubActions: true,
            initialState: {
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: tipoProcesso,
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

    const perfilStore = usePerfilStore();
    perfilStore.perfilSelecionado = perfil;

    return { wrapper };
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it('deve mostrar o botão "Impacto no mapa" para GESTOR em CADASTRO_DISPONIBILIZADO', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.GESTOR,
      SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
    await flushPromises();
    await nextTick();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('deve mostrar o botão "Impacto no mapa" para ADMIN em CADASTRO_DISPONIBILIZADO', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.ADMIN,
      SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
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

  it("deve listar atividades e conhecimentos", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.EM_ANDAMENTO,
    );
    wrapper = w;
    const atividadesStore = useAtividadesStore();
    atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue([
      {
        codigo: 1,
        descricao: "Atividade 1",
        conhecimentos: [{ id: 10, descricao: "Conhecimento 1" }],
      },
    ]);

    await flushPromises();
    // Force re-compute
    wrapper.vm.$forceUpdate();
    await nextTick();

    expect(wrapper.text()).toContain("Atividade 1");
    expect(wrapper.text()).toContain("Conhecimento 1");
  });

  it("deve abrir e fechar modal de historico", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.EM_ANDAMENTO,
    );
    wrapper = w;
    await flushPromises();

    const btn = wrapper.findAll("button").find((b: any) => b.text() === "Histórico de análise");
    await btn.trigger("click");
    expect(wrapper.vm.mostrarModalHistoricoAnalise).toBe(true);
  });

  it("deve validar cadastro (Homologar) e redirecionar", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.ADMIN,
        SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_ATIVIDADES,
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.homologarRevisaoCadastro = vi.fn();

    await flushPromises();

    // Click validate button
    const btn = wrapper.find('[data-testid="btn-acao-principal-analise"]');
    await btn.trigger("click");
    expect(wrapper.vm.mostrarModalValidar).toBe(true);

    // Confirm
    const btnConfirm = wrapper.find('[data-testid="btn-modal-confirmar-aceite"]');
    await btnConfirm.trigger("click");

    expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalled();
    expect(pushMock).toHaveBeenCalledWith("/painel");
  });

  it("deve validar cadastro (Aceitar) e redirecionar", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.aceitarRevisaoCadastro = vi.fn();

    await flushPromises();

    // Click validate button
    const btn = wrapper.find('[data-testid="btn-acao-principal-analise"]');
    await btn.trigger("click");

    // Confirm
    const btnConfirm = wrapper.find('[data-testid="btn-modal-confirmar-aceite"]');
    await btnConfirm.trigger("click");

    expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalled();
    expect(pushMock).toHaveBeenCalledWith("/painel");
  });

  it("deve devolver cadastro e redirecionar", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.devolverRevisaoCadastro = vi.fn();

    await flushPromises();

    // Click return button
    const btn = wrapper.find('[data-testid="btn-devolver"]');
    await btn.trigger("click");
    expect(wrapper.vm.mostrarModalDevolver).toBe(true);

    // Fill observation
    const textarea = wrapper.find('[data-testid="input-observacao-devolucao"]');
    await textarea.setValue("Devolvendo");

    // Confirm
    const btnConfirm = wrapper.find('[data-testid="btn-modal-confirmar-devolucao"]');
    await btnConfirm.trigger("click");

    expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(123, {
      observacoes: "Devolvendo",
    });
    expect(pushMock).toHaveBeenCalledWith("/painel");
  });

  it("deve chamar aceitarCadastro se não for revisao", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
        TipoProcesso.MAPEAMENTO,
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.aceitarCadastro = vi.fn();

    await flushPromises();

    // Click validate button
    await wrapper.find('[data-testid="btn-acao-principal-analise"]').trigger("click");
    // Confirm
    await wrapper.find('[data-testid="btn-modal-confirmar-aceite"]').trigger("click");

    expect(subprocessosStore.aceitarCadastro).toHaveBeenCalled();
  });

  it("deve encontrar unidade em hierarquia complexa", async () => {
      // Setup store with nested units
      const { wrapper: w } = createWrapper(Perfil.GESTOR, SituacaoSubprocesso.EM_ANDAMENTO);
      wrapper = w;
      const unidadesStore = useUnidadesStore();
      unidadesStore.unidades = [{
          codigo: 99,
          sigla: "ROOT",
          nome: "Root",
          filhas: [{
              codigo: 1,
              sigla: "TESTE",
              nome: "Unidade de Teste",
              filhas: []
          }]
      }] as any;

      // Force update or wait for computed
      await flushPromises();
      await nextTick();
      expect(w.text()).toContain("Unidade de Teste");
  });

  it("deve validar cadastro (Homologar Mapeamento)", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.ADMIN,
        SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_ATIVIDADES,
        TipoProcesso.MAPEAMENTO
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.homologarCadastro = vi.fn();

    await flushPromises();

    const btn = wrapper.find('[data-testid="btn-acao-principal-analise"]');
    await btn.trigger("click");
    await wrapper.find('[data-testid="btn-modal-confirmar-aceite"]').trigger("click");

    expect(subprocessosStore.homologarCadastro).toHaveBeenCalled();
  });

  it("deve devolver cadastro (Mapeamento)", async () => {
    const { wrapper: w } = createWrapper(
        Perfil.GESTOR,
        SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
        TipoProcesso.MAPEAMENTO
    );
    wrapper = w;
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.devolverCadastro = vi.fn();

    await flushPromises();

    const btn = wrapper.find('[data-testid="btn-devolver"]');
    await btn.trigger("click");
    await wrapper.find('[data-testid="input-observacao-devolucao"]').setValue("Obs");
    await wrapper.find('[data-testid="btn-modal-confirmar-devolucao"]').trigger("click");

    expect(subprocessosStore.devolverCadastro).toHaveBeenCalled();
  });
});
