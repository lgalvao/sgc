package sgc.subprocesso.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.factory.SubprocessoFactory;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("SubprocessoFacade - Testes Consolidados")
class SubprocessoFacadeTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoCadastroWorkflowService cadastroWorkflowService;
    @Mock
    private SubprocessoMapaWorkflowService mapaWorkflowService;
    @Mock
    private SubprocessoAdminWorkflowService adminWorkflowService;
    @Mock
    private SubprocessoAjusteMapaService ajusteMapaService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private SubprocessoContextoService contextoService;
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock
    private SubprocessoFactory subprocessoFactory;
    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private SubprocessoFacade facade;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {

        @Test
        @DisplayName("Deve buscar subprocesso por ID")
        void deveBuscarSubprocessoPorId() {
            when(crudService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            assertThat(facade.buscarSubprocesso(1L)).isNotNull();
            verify(crudService).buscarSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve buscar subprocesso com mapa")
        void deveBuscarSubprocessoComMapa() {
            when(crudService.buscarSubprocessoComMapa(1L)).thenReturn(new Subprocesso());
            assertThat(facade.buscarSubprocessoComMapa(1L)).isNotNull();
            verify(crudService).buscarSubprocessoComMapa(1L);
        }

        @Test
        @DisplayName("Deve listar todos os subprocessos")
        void deveListar() {
            when(crudService.listar()).thenReturn(List.of(new SubprocessoDto()));
            assertThat(facade.listar()).hasSize(1);
            verify(crudService).listar();
        }

        @Test
        @DisplayName("Deve obter por processo e unidade")
        void deveObterPorProcessoEUnidade() {
            when(crudService.obterPorProcessoEUnidade(1L, 10L))
                    .thenReturn(SubprocessoDto.builder().codigo(1L).build());
            SubprocessoDto result = facade.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
            verify(crudService).obterPorProcessoEUnidade(1L, 10L);
        }

        @Test
        @DisplayName("Deve listar por processo e unidades")
        void deveListarPorProcessoEUnidades() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(2L);
            facade.listarPorProcessoEUnidades(codProcesso, unidades);
            verify(crudService).listarPorProcessoEUnidades(codProcesso, unidades);
        }

        @Test
        @DisplayName("Deve retornar situação")
        void deveObterSituacao() {
            when(crudService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            assertThat(facade.obterSituacao(1L)).isNotNull();
            verify(crudService).obterStatus(1L);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso")
        void deveListarAtividadesSubprocesso() {
            facade.listarAtividadesSubprocesso(1L);
            verify(atividadeService).listarAtividadesSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por ID")
        void deveObterAtividadesSemConhecimentoPorId() {
            when(validacaoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
            assertThat(facade.obterAtividadesSemConhecimento(1L)).isEmpty();
            verify(validacaoService).obterAtividadesSemConhecimento(1L);
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por Mapa")
        void deveObterAtividadesSemConhecimentoPorMapa() {
            Mapa mapa = new Mapa();
            when(validacaoService.obterAtividadesSemConhecimento(mapa)).thenReturn(Collections.emptyList());
            assertThat(facade.obterAtividadesSemConhecimento(mapa)).isEmpty();
            verify(validacaoService).obterAtividadesSemConhecimento(mapa);
        }

        @Test
        @DisplayName("Deve obter contexto de edição")
        void deveObterContextoEdicao() {
            facade.obterContextoEdicao(1L);
            verify(contextoService).obterContextoEdicao(1L);
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(crudService.obterEntidadePorCodigoMapa(100L)).thenReturn(new Subprocesso());
            assertThat(facade.obterEntidadePorCodigoMapa(100L)).isNotNull();
            verify(crudService).obterEntidadePorCodigoMapa(100L);
        }

        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(crudService.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(facade.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();
            verify(crudService).verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L));
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(crudService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(new Subprocesso()));
            assertThat(facade.listarEntidadesPorProcesso(1L)).hasSize(1);
            verify(crudService).listarEntidadesPorProcesso(1L);
        }

        @Test
        @DisplayName("Deve obter sugestões")
        void deveObterSugestoes() {
            facade.obterSugestoes(1L);
            verify(contextoService).obterSugestoes(1L);
        }

        @Test
        @DisplayName("Deve obter mapa para ajuste")
        void deveObterMapaParaAjuste() {
            facade.obterMapaParaAjuste(1L);
            verify(ajusteMapaService).obterMapaParaAjuste(1L);
        }

        @Test
        @DisplayName("Deve obter cadastro")
        void deveObterCadastro() {
            facade.obterCadastro(1L);
            verify(contextoService).obterCadastro(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            CriarSubprocessoRequest request = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(1L).build();
            SubprocessoDto response = SubprocessoDto.builder().build();
            when(crudService.criar(request)).thenReturn(response);
            assertThat(facade.criar(request)).isNotNull();
            verify(crudService).criar(request);
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(100L).build();
            SubprocessoDto response = SubprocessoDto.builder().codMapa(100L).build();
            when(crudService.atualizar(1L, request)).thenReturn(response);
            assertThat(facade.atualizar(1L, request)).isNotNull();
            verify(crudService).atualizar(1L, request);
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            facade.excluir(1L);
            verify(crudService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar cadastro")
        void deveValidarCadastro() {
            ValidacaoCadastroDto val = ValidacaoCadastroDto.builder().valido(true).build();
            when(validacaoService.validarCadastro(1L)).thenReturn(val);

            ValidacaoCadastroDto result = facade.validarCadastro(1L);
            assertThat(result.valido()).isTrue();
            verify(validacaoService).validarCadastro(1L);
        }

        @Test
        @DisplayName("Deve validar existência de atividades")
        void deveValidarExistenciaAtividades() {
            facade.validarExistenciaAtividades(1L);
            verify(validacaoService).validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("Deve validar associações do mapa")
        void deveValidarAssociacoesMapa() {
            facade.validarAssociacoesMapa(100L);
            verify(validacaoService).validarAssociacoesMapa(100L);
        }
    }

    @Nested
    @DisplayName("Cenários de Workflow e Transição de Estado")
    class WorkflowTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO")
        void deveAtualizarParaEmAndamento() {
            facade.atualizarSituacaoParaEmAndamento(100L);
            verify(adminWorkflowService).atualizarSituacaoParaEmAndamento(100L);
        }

        @Test
        @DisplayName("Deve listar subprocessos homologados")
        void deveListarSubprocessosHomologados() {
            when(adminWorkflowService.listarSubprocessosHomologados()).thenReturn(Collections.emptyList());
            assertThat(facade.listarSubprocessosHomologados()).isEmpty();
            verify(adminWorkflowService).listarSubprocessosHomologados();
        }

        @Test
        @DisplayName("Deve reabrir cadastro")
        void deveReabrirCadastro() {
            facade.reabrirCadastro(1L, "Justificativa");
            verify(cadastroWorkflowService).reabrirCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve reabrir revisão cadastro")
        void deveReabrirRevisaoCadastro() {
            facade.reabrirRevisaoCadastro(1L, "Justificativa");
            verify(cadastroWorkflowService).reabrirRevisaoCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve alterar data limite")
        void deveAlterarDataLimite() {
            LocalDate novaData = LocalDate.now();
            facade.alterarDataLimite(1L, novaData);
            verify(adminWorkflowService).alterarDataLimite(1L, novaData);
        }

        @Test
        @DisplayName("Deve salvar ajustes do mapa")
        void deveSalvarAjustesMapa() {
            Long codigo = 1L;
            List<CompetenciaAjusteDto> competencias = List.of(CompetenciaAjusteDto.builder().build());
            facade.salvarAjustesMapa(codigo, competencias);
            verify(ajusteMapaService).salvarAjustesMapa(codigo, competencias);
        }

        @Test
        @DisplayName("Deve importar atividades")
        void deveImportarAtividades() {
            facade.importarAtividades(1L, 2L);
            verify(atividadeService).importarAtividades(1L, 2L);
        }

        @Test
        @DisplayName("Deve disponibilizar cadastro")
        void deveDisponibilizarCadastro() {
            Usuario usuario = new Usuario();
            facade.disponibilizarCadastro(1L, usuario);
            verify(cadastroWorkflowService).disponibilizarCadastro(1L, usuario);
        }

        @Test
        @DisplayName("Deve disponibilizar revisão")
        void deveDisponibilizarRevisao() {
            Usuario usuario = new Usuario();
            facade.disponibilizarRevisao(1L, usuario);
            verify(cadastroWorkflowService).disponibilizarRevisao(1L, usuario);
        }

        @Test
        @DisplayName("Deve devolver cadastro")
        void deveDevolverCadastro() {
            Usuario usuario = new Usuario();
            facade.devolverCadastro(1L, "obs", usuario);
            verify(cadastroWorkflowService).devolverCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve aceitar cadastro")
        void deveAceitarCadastro() {
            Usuario usuario = new Usuario();
            facade.aceitarCadastro(1L, "obs", usuario);
            verify(cadastroWorkflowService).aceitarCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve homologar cadastro")
        void deveHomologarCadastro() {
            Usuario usuario = new Usuario();
            facade.homologarCadastro(1L, "obs", usuario);
            verify(cadastroWorkflowService).homologarCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve apresentar sugestões")
        void deveApresentarSugestoes() {
            Usuario usuario = new Usuario();
            facade.apresentarSugestoes(1L, "sug", usuario);
            verify(mapaWorkflowService).apresentarSugestoes(1L, "sug", usuario);
        }

        @Test
        @DisplayName("Deve validar mapa")
        void deveValidarMapa() {
            Usuario usuario = new Usuario();
            facade.validarMapa(1L, usuario);
            verify(mapaWorkflowService).validarMapa(1L, usuario);
        }
    }

    @Nested
    @DisplayName("Cenários de Operações em Bloco")
    class OperacoesEmBlocoTests {
        @Test
        @DisplayName("aceitarCadastroEmBloco deve delegar se houver itens")
        void aceitarCadastroEmBloco_DeveDelegar() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades))
                    .thenReturn(List.of(SubprocessoDto.builder().codigo(50L).build()));

            facade.aceitarCadastroEmBloco(unidades, codProcesso, usuario);

            verify(cadastroWorkflowService).aceitarCadastroEmBloco(List.of(50L), usuario);
        }

        @Test
        @DisplayName("aceitarCadastroEmBloco não faz nada se lista ids vazia")
        void aceitarCadastroEmBloco_Empty() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

            facade.aceitarCadastroEmBloco(unidades, codProcesso, usuario);

            verify(cadastroWorkflowService, never()).aceitarCadastroEmBloco(any(), any());
        }

        @Test
        @DisplayName("homologarCadastroEmBloco não faz nada se lista ids vazia")
        void homologarCadastroEmBloco_Empty() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

            facade.homologarCadastroEmBloco(unidades, codProcesso, usuario);

            verify(cadastroWorkflowService, never()).homologarCadastroEmBloco(any(), any());
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco não faz nada se lista ids vazia")
        void disponibilizarMapaEmBloco_Empty() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.now(), "Obs");
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

            facade.disponibilizarMapaEmBloco(unidades, codProcesso, req, usuario);

            verify(mapaWorkflowService, never()).disponibilizarMapaEmBloco(any(), any(), any());
        }

        @Test
        @DisplayName("aceitarValidacaoEmBloco não faz nada se lista ids vazia")
        void aceitarValidacaoEmBloco_Empty() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

            facade.aceitarValidacaoEmBloco(unidades, codProcesso, usuario);

            verify(mapaWorkflowService, never()).aceitarValidacaoEmBloco(any(), any());
        }

        @Test
        @DisplayName("homologarValidacaoEmBloco não faz nada se lista ids vazia")
        void homologarValidacaoEmBloco_Empty() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(10L);
            Usuario usuario = new Usuario();
            when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

            facade.homologarValidacaoEmBloco(unidades, codProcesso, usuario);

            verify(mapaWorkflowService, never()).homologarValidacaoEmBloco(any(), any());
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    class PermissaoDetalheTests {
        @Test
        @DisplayName("Deve disparar erro se não autenticado ao obter permissões")
        void deveDispararErroSeNaoAutenticado() {
            when(usuarioService.obterUsuarioAutenticado())
                    .thenThrow(new ErroAcessoNegado("Nenhum usuário autenticado"));

            Assertions.assertThrows(ErroAcessoNegado.class, () -> facade.obterPermissoes(1L));
        }

        @Test
        @DisplayName("Deve obter detalhes do subprocesso")
        void deveObterDetalhes() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            facade.obterDetalhes(codigo);

            verify(contextoService).obterDetalhes(codigo, usuario);
        }

        @Test
        @DisplayName("Deve obter permissões")
        void deveObterPermissoes() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            facade.obterPermissoes(codigo);

            verify(permissaoCalculator).obterPermissoes(codigo, usuario);
        }
    }

    @Nested
    @DisplayName("Cenários de Factory")
    class FactoryTests {
        @Test
        @DisplayName("Deve criar para mapeamento")
        void deveCriarParaMapeamento() {
            Processo processo = new Processo();
            List<Unidade> unidades = List.of(new Unidade());
            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            facade.criarParaMapeamento(processo, unidades, unidadeOrigem, usuario);
            verify(subprocessoFactory).criarParaMapeamento(processo, unidades, unidadeOrigem, usuario);
        }

        @Test
        @DisplayName("Deve criar para revisão")
        void deveCriarParaRevisao() {
            Processo processo = new Processo();
            Unidade unidade = new Unidade();
            UnidadeMapa unidadeMapa = new UnidadeMapa();
            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            facade.criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
            verify(subprocessoFactory).criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
        }

        @Test
        @DisplayName("Deve criar para diagnóstico")
        void deveCriarParaDiagnostico() {
            Processo processo = new Processo();
            Unidade unidade = new Unidade();
            UnidadeMapa unidadeMapa = new UnidadeMapa();
            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            facade.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
            verify(subprocessoFactory).criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
        }
    }
}
