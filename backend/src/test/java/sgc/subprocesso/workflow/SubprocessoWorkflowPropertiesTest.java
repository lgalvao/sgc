package sgc.subprocesso.workflow;

import net.jqwik.api.*;
import net.jqwik.api.stateful.Action;
import net.jqwik.api.stateful.ActionSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import sgc.analise.AnaliseService;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.processo.model.Processo;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.service.UnidadeService;
import sgc.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@DisplayName("MBT: Workflow de Cadastro de Subprocesso")
class SubprocessoWorkflowPropertiesTest {

    @Provide
    Arbitrary<ActionSequence<Model>> actions() {
        return Arbitraries.sequences(
            Arbitraries.of(
                new DisponibilizarAction(),
                new DevolverAction(),
                new AceitarAction(),
                new HomologarAction()
            )
        );
    }

    @Property
    void verificarTransicoesDeEstado(@ForAll("actions") ActionSequence<Model> actions) {
        actions.run(new Model());
    }

    static class Model {
        private final SubprocessoCadastroWorkflowService service;
        private final SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
        private final UnidadeService unidadeService = mock(UnidadeService.class);
        private final SubprocessoWorkflowExecutor workflowExecutor = mock(SubprocessoWorkflowExecutor.class);
        private final SubprocessoService subprocessoService = mock(SubprocessoService.class);
        private final ImpactoMapaService impactoMapaService = mock(ImpactoMapaService.class);
        private final SubprocessoTransicaoService transicaoService = mock(SubprocessoTransicaoService.class);
        private final AnaliseService analiseService = mock(AnaliseService.class);

        private Subprocesso subprocesso;
        private Usuario usuarioAtual;

        Model() {
            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(UnidadeFixture.unidadePadrao());

            // Ordem correta dos argumentos baseada nos campos da classe Service
            service = new SubprocessoCadastroWorkflowService(
                subprocessoRepo,
                transicaoService,
                unidadeService,
                analiseService,
                subprocessoService,
                impactoMapaService,
                workflowExecutor
            );

            inicializarSubprocesso();
        }

        private void inicializarSubprocesso() {
            Processo processo = ProcessoFixture.processoPadrao();
            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setUnidadeSuperior(UnidadeFixture.unidadePadrao());

            unidade.setTituloTitular("123456789012");
            unidade.getUnidadeSuperior().setTituloTitular("987654321098");

            subprocesso = new Subprocesso();
            subprocesso.setCodigo(1L);
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocesso.setMapa(MapaFixture.mapaPadrao(subprocesso));

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            doNothing().when(subprocessoService).validarExistenciaAtividades(any());
            when(subprocessoService.obterAtividadesSemConhecimento(any())).thenReturn(Collections.emptyList());

            usuarioAtual = new Usuario();
            usuarioAtual.setTituloEleitoral("123456789012");
        }

        public SituacaoSubprocesso getSituacao() {
            return subprocesso.getSituacao();
        }

        public SubprocessoCadastroWorkflowService getService() {
            return service;
        }

        public Usuario getUsuarioAtual() {
            return usuarioAtual;
        }

        public void setUsuarioGestor() {
             usuarioAtual.setTituloEleitoral("987654321098");
        }

        public void setUsuarioTitular() {
             usuarioAtual.setTituloEleitoral("123456789012");
        }

        public void setUsuarioAdmin() {
            usuarioAtual.setTituloEleitoral("111111111111");
        }
    }

    static class DisponibilizarAction implements Action<Model> {
        @Override
        public boolean precondition(Model model) {
            return model.getSituacao() == MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                || model.getSituacao() == REVISAO_CADASTRO_EM_ANDAMENTO;
        }

        @Override
        public Model run(Model model) {
            model.setUsuarioTitular();
            model.getService().disponibilizarCadastro(1L, model.getUsuarioAtual());

            Assertions.assertTrue(
                model.getSituacao() == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                model.getSituacao() == REVISAO_CADASTRO_DISPONIBILIZADA,
                "Estado deve mudar para DISPONIBILIZADO"
            );
            return model;
        }

        @Override
        public String toString() {
            return "Disponibilizar Cadastro";
        }
    }

    static class DevolverAction implements Action<Model> {
        @Override
        public boolean precondition(Model model) {
            return model.getSituacao() == MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                || model.getSituacao() == REVISAO_CADASTRO_DISPONIBILIZADA;
        }

        @Override
        public Model run(Model model) {
            model.setUsuarioGestor();
            model.getService().devolverCadastro(1L, "Ajuste", model.getUsuarioAtual());

            verify(model.workflowExecutor, atLeastOnce()).registrarAnaliseETransicao(
                eq(model.subprocesso),
                argThat(s -> s == MAPEAMENTO_CADASTRO_EM_ANDAMENTO || s == REVISAO_CADASTRO_EM_ANDAMENTO),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );

            if (model.getSituacao() == MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
                model.subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            } else {
                model.subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            }

            return model;
        }

        @Override
        public String toString() {
            return "Devolver Cadastro";
        }
    }

    static class AceitarAction implements Action<Model> {
        @Override
        public boolean precondition(Model model) {
             return model.getSituacao() == MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                || model.getSituacao() == REVISAO_CADASTRO_DISPONIBILIZADA;
        }

        @Override
        public Model run(Model model) {
            model.setUsuarioGestor();
            model.getService().aceitarCadastro(1L, "Aceito", model.getUsuarioAtual());

            verify(model.workflowExecutor, atLeastOnce()).registrarAnaliseETransicao(
                eq(model.subprocesso),
                argThat(s -> s == MAPEAMENTO_CADASTRO_DISPONIBILIZADO || s == REVISAO_CADASTRO_DISPONIBILIZADA),
                any(TipoTransicao.class),
                any(), any(), any(), any(), any(), any(), any(), any()
            );

            return model;
        }

        @Override
        public String toString() {
            return "Aceitar Cadastro";
        }
    }

    static class HomologarAction implements Action<Model> {
        @Override
        public boolean precondition(Model model) {
             return model.getSituacao() == MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        }

        @Override
        public Model run(Model model) {
            model.setUsuarioAdmin();
            model.getService().homologarCadastro(1L, "Homologado", model.getUsuarioAtual());

            Assertions.assertEquals(MAPEAMENTO_CADASTRO_HOMOLOGADO, model.getSituacao());
            return model;
        }

        @Override
        public String toString() {
            return "Homologar Cadastro";
        }
    }
}
