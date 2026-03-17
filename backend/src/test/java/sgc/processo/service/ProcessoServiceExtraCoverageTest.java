package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailModelosService;
import sgc.alerta.EmailService;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.model.AcaoProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.subprocesso.service.SubprocessoTransicaoService;
import sgc.subprocesso.service.SubprocessoValidacaoService;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProcessoService Extra Coverage Test")
class ProcessoServiceExtraCoverageTest {

    @InjectMocks
    private ProcessoService processoService;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private AlertaFacade servicoAlertas;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @Mock
    private SubprocessoTransicaoService transicaoService;

    @Nested
    @DisplayName("buscarPorCodigoComParticipantes")
    class BuscarPorCodigoComParticipantes {
        @Test
        @DisplayName("deve lancar excecao se nao encontrar")
        void deveLancarExcecao() {
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.empty());

            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> processoService.buscarPorCodigoComParticipantes(1L));
        }

        @Test
        @DisplayName("deve retornar se encontrar")
        void deveRetornar() {
            Processo p = new Processo();
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            Processo res = processoService.buscarPorCodigoComParticipantes(1L);

            assertThat(res).isEqualTo(p);
        }
    }

    @Nested
    @DisplayName("listarFinalizados")
    class ListarFinalizados {
        @Test
        @DisplayName("deve retornar listarPorSituacaoComParticipantes se admin")
        void admin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }

        @Test
        @DisplayName("deve retornar listarPorSituacaoEUnidadeCodigos se nao admin")
        void naoAdmin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uni));

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.FINALIZADO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, null, List.<Long>of());

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve lancar erro se unidade invalida")
        void unidadeInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.INTERMEDIARIA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, null, List.of(1L));

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve atualizar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.RAIZ);
            u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            when(processoRepo.saveAndFlush(p)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc2", TipoProcesso.MAPEAMENTO, null, List.of(1L));

            Processo res = processoService.atualizar(1L, req);

            assertThat(res.getDescricao()).isEqualTo("desc2");
        }
    }

    @Nested
    @DisplayName("apagar")
    class Apagar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.apagar(1L));
        }

        @Test
        @DisplayName("deve apagar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            processoService.apagar(1L);

            verify(processoRepo).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("iniciar")
    class Iniciar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se revisao sem unidades")
        void revisaoSemUnidades() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se mapeamento sem participantes")
        void mapeamentoSemParticipantes() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }
    }

    @Nested
    @DisplayName("finalizar")
    class Finalizar {
        @Test
        @DisplayName("deve lancar erro se nao em andamento")
        void naoAndamento() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }

        @Test
        @DisplayName("deve lancar erro se nao validado")
        void naoValidado() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofInvalido("Erro de validacao"));

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }
    }

    @Nested
    @DisplayName("executarAcaoEmBloco")
    class ExecutarAcaoEmBloco {
        @Test
        @DisplayName("deve lancar erro se unidades vazio")
        void vazio() {
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(), AcaoProcesso.ACEITAR, null);
            assertThrows(ErroValidacao.class, () -> processoService.executarAcaoEmBloco(1L, req));
        }

        @Test
        @DisplayName("deve lancar erro se quantidade nao bate")
        void qtdeNaoBate() {
            Usuario u = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            when(subprocessoService.listarEntidadesPorProcessoEUnidades(1L, List.of(1L, 2L))).thenReturn(List.of());

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L, 2L), AcaoProcesso.ACEITAR, null);
            assertThrows(ErroValidacao.class, () -> processoService.executarAcaoEmBloco(1L, req));
        }

        @Test
        @DisplayName("deve lancar erro de permissao ao disponibilizar sem permissao")
        void semPermissaoDisponibilizar() {
            Usuario u = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Subprocesso sp = new Subprocesso();
            sp.setUnidade(new Unidade());
            sp.getUnidade().setCodigo(1L);
            when(subprocessoService.listarEntidadesPorProcessoEUnidades(1L, List.of(1L))).thenReturn(List.of(sp));

            when(permissionEvaluator.verificarPermissao(eq(u), anyList(), eq(sgc.seguranca.AcaoPermissao.DISPONIBILIZAR_MAPA))).thenReturn(false);

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), AcaoProcesso.DISPONIBILIZAR, null);
            assertThrows(sgc.comum.erros.ErroAcessoNegado.class, () -> processoService.executarAcaoEmBloco(1L, req));
        }
    }
}
