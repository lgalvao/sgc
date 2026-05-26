import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import CadastroFluxoModais from "../CadastroFluxoModais.vue";

describe("CadastroFluxoModais", () => {
  const defaultProps = {
    mostrarModalImportar: false,
    loadingImpacto: false,
    mostrarModalImpacto: false,
    isRevisao: false,
    loadingDisponibilizacao: false,
    mostrarModalConfirmacao: false,
    historicoAnalises: [],
    mostrarModalHistorico: false,
    mostrarModalConfirmacaoRemocao: false,
    loadingRemocao: false,
    dadosRemocao: null,
    mostrarModalValidarAnalise: false,
    observacaoValidacao: "",
    loadingAnaliseCadastro: false,
    acaoPrincipalCadastro: null,
    mostrarModalDevolverAnalise: false,
    observacaoDevolucao: "",
    loadingDevolucaoAnalise: false,
  };

  const stubs = {
    ImportarAtividadesModal: true,
    ImpactoMapaModal: true,
    ConfirmacaoDisponibilizacaoModal: true,
    HistoricoAnaliseModal: true,
    ModalConfirmacao: true,
    ModalAceiteCadastro: true,
    ModalDevolucaoCadastro: true,
  };

  it("reage a emissões de sub-componentes e exerce computeds", async () => {
    const wrapper = mount(CadastroFluxoModais, {
      props: {...defaultProps, codigoSubprocesso: 123, dadosRemocao: {tipo: "atividade"}},
      global: {
        stubs: {
          ImportarAtividadesModal: {
            template: "<div><button id='btn-importar' @click=\"$emit('importar', {data: 1})\"></button><button id='btn-fechar-import' @click=\"$emit('fechar')\"></button></div>",
            emits: ["importar", "fechar"]
          },
          ImpactoMapaModal: {
            template: "<button id='btn-fechar-impacto' @click=\"$emit('fechar')\"></button>",
            emits: ["fechar"]
          },
          ConfirmacaoDisponibilizacaoModal: {
            template: "<button id='btn-conf-disp' @click=\"$emit('confirmar')\"></button><button id='btn-fechar-disp' @click=\"$emit('fechar')\"></button>",
            props: ["erro", "isRevisao", "loading", "mostrar"],
            emits: ["confirmar", "fechar"]
          },
          HistoricoAnaliseModal: {
            template: "<button id='btn-fechar-hist' @click=\"$emit('fechar')\"></button>",
            emits: ["fechar"]
          },
          ModalConfirmacao: {
            template: "<button id='btn-conf-rem' @click=\"$emit('confirmar')\"></button>",
            emits: ["confirmar"]
          },
          ModalAceiteCadastro: {
             template: "<div><button id='btn-conf-val' @click=\"$emit('confirmar')\"></button><button id='btn-upd-val' @click=\"$emit('update:observacao', 'obs val')\"></button></div>",
             emits: ["confirmar", "update:observacao"]
          },
          ModalDevolucaoCadastro: {
             template: "<div><button id='btn-conf-dev' @click=\"$emit('confirmar')\"></button><button id='btn-upd-dev' @click=\"$emit('update:observacao', 'obs dev')\"></button></div>",
             emits: ["confirmar", "update:observacao"]
          }
        }
      }
    });

    await wrapper.find("#btn-importar").trigger("click");
    expect(wrapper.emitted("importar")).toBeTruthy();

    await wrapper.find("#btn-fechar-import").trigger("click");
    expect(wrapper.emitted("update:mostrarModalImportar")?.[0]).toEqual([false]);

    await wrapper.find("#btn-fechar-impacto").trigger("click");
    expect(wrapper.emitted("fechar-impacto")).toBeTruthy();

    await wrapper.find("#btn-conf-disp").trigger("click");
    expect(wrapper.emitted("confirmar-disponibilizacao")).toBeTruthy();

    await wrapper.find("#btn-fechar-disp").trigger("click");
    expect(wrapper.emitted("update:mostrarModalConfirmacao")?.[0]).toEqual([false]);

    await wrapper.find("#btn-fechar-hist").trigger("click");
    expect(wrapper.emitted("update:mostrarModalHistorico")?.[0]).toEqual([false]);

    await wrapper.find("#btn-conf-rem").trigger("click");
    expect(wrapper.emitted("confirmar-remocao")).toBeTruthy();

    await wrapper.find("#btn-conf-val").trigger("click");
    expect(wrapper.emitted("confirmar-validacao-analise")).toBeTruthy();

    await wrapper.find("#btn-upd-val").trigger("click");
    expect(wrapper.emitted("update:observacaoValidacao")?.[0]).toEqual(["obs val"]);

    await wrapper.find("#btn-conf-dev").trigger("click");
    expect(wrapper.emitted("confirmar-devolucao-analise")).toBeTruthy();

    await wrapper.find("#btn-upd-dev").trigger("click");
    expect(wrapper.emitted("update:observacaoDevolucao")?.[0]).toEqual(["obs dev"]);
  });

  it("renderiza ModalConfirmacao com textos de conhecimento", async () => {
    const wrapper = mount(CadastroFluxoModais, {
      props: {...defaultProps, dadosRemocao: {tipo: "conhecimento"}},
      global: { stubs: { ModalConfirmacao: { template: "<div>{{ $attrs.titulo }}</div>" } } }
    });
    expect(wrapper.text().toLowerCase()).toContain("remover conhecimento");
  });
});
