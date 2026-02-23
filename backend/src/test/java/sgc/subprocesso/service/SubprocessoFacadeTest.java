package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.AtividadeDto;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
            when(crudService.listarEntidades()).thenReturn(List.of(new Subprocesso()));
            assertThat(facade.listar()).hasSize(1);
            verify(crudService).listarEntidades();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há subprocessos")
        void deveRetornarListaVaziaQuandoNaoHaSubprocessos() {
            when(crudService.listarEntidades()).thenReturn(List.of());

            List<Subprocesso> resultado = facade.listar();

            assertThat(resultado)
                    .isNotNull()
                    .isEmpty();
            verify(crudService).listarEntidades();
        }

        @Test
        @DisplayName("Deve obter por processo e unidade")
        void deveObterPorProcessoEUnidade() {
            when(crudService.obterEntidadePorProcessoEUnidade(1L, 10L))
                    .thenReturn(Subprocesso.builder().codigo(1L).build());
            Subprocesso result = facade.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
            verify(crudService).obterEntidadePorProcessoEUnidade(1L, 10L);
        }

        @Test
        @DisplayName("Deve listar por processo e unidades")
        void deveListarPorProcessoEUnidades() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(2L);
            when(crudService.listarEntidadesPorProcessoEUnidades(codProcesso, unidades))
                    .thenReturn(List.of(new Subprocesso()));

            List<Subprocesso> resultado = facade.listarPorProcessoEUnidades(codProcesso, unidades);

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1);
            verify(crudService).listarEntidadesPorProcessoEUnidades(codProcesso, unidades);
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
            when(atividadeService.listarAtividadesSubprocesso(1L))
                    .thenReturn(List.of(new AtividadeDto(1L, "Atividade 1", List.of())));

            List<AtividadeDto> resultado = facade.listarAtividadesSubprocesso(1L);

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1);
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
            Map<String, Object> expected = Map.of("sugestoes", "");
            Map<String, Object> result = facade.obterSugestoes(1L);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Deve obter mapa para ajuste")
        void deveObterMapaParaAjuste() {
            facade.obterMapaParaAjuste(1L);
            verify(ajusteMapaService).obterMapaParaAjuste(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            CriarSubprocessoRequest request = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(1L).build();
            when(crudService.criarEntidade(request)).thenReturn(new Subprocesso());
            assertThat(facade.criar(request)).isNotNull();
            verify(crudService).criarEntidade(request);
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().build();
            when(crudService.atualizarEntidade(1L, request)).thenReturn(new Subprocesso());
            assertThat(facade.atualizar(1L, request)).isNotNull();
            verify(crudService).atualizarEntidade(1L, request);
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
            verify(adminWorkflowService).atualizarParaEmAndamento(100L);
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

        @Test
        @DisplayName("Deve salvar mapa do subprocesso")
        void deveSalvarMapaSubprocesso() {
            SalvarMapaRequest request = SalvarMapaRequest.builder().build();
            when(mapaWorkflowService.salvarMapaSubprocesso(eq(1L), any())).thenReturn(new Mapa());
            facade.salvarMapaSubprocesso(1L, request);
            verify(mapaWorkflowService).salvarMapaSubprocesso(1L, request);
        }

        @Test
        @DisplayName("Deve adicionar competencia")
        void deveAdicionarCompetencia() {
            CompetenciaRequest req = CompetenciaRequest.builder().build();
            facade.adicionarCompetencia(1L, req);
            verify(mapaWorkflowService).adicionarCompetencia(1L, req);
        }

        @Test
        @DisplayName("Deve atualizar competencia")
        void deveAtualizarCompetencia() {
            CompetenciaRequest req = CompetenciaRequest.builder().build();
            facade.atualizarCompetencia(1L, 100L, req);
            verify(mapaWorkflowService).atualizarCompetencia(1L, 100L, req);
        }

        @Test
        @DisplayName("Deve remover competencia")
        void deveRemoverCompetencia() {
            facade.removerCompetencia(1L, 100L);
            verify(mapaWorkflowService).removerCompetencia(1L, 100L);
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado")
        void deveSubmeterMapaAjustado() {
            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder().build();
            Usuario usuario = new Usuario();
            facade.submeterMapaAjustado(1L, req, usuario);
            verify(mapaWorkflowService).submeterMapaAjustado(1L, req, usuario);
        }

        @Test
        @DisplayName("Deve registrar movimentação de lembrete")
        void deveRegistrarMovimentacaoLembrete() {
            facade.registrarMovimentacaoLembrete(1L);
            verify(adminWorkflowService).registrarMovimentacaoLembrete(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Operações em Bloco")
    class OperacoesEmBlocoTests {
        @Test
        @DisplayName("aceitarCadastroEmBloco deve delegar se houver itens")
        void aceitarCadastroEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L);
            Usuario usuario = new Usuario();

            facade.aceitarCadastroEmBloco(ids, usuario);

            verify(cadastroWorkflowService).aceitarCadastroEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("homologarCadastroEmBloco deve delegar se houver itens")
        void homologarCadastroEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L, 60L);
            Usuario usuario = new Usuario();

            facade.homologarCadastroEmBloco(ids, usuario);

            verify(cadastroWorkflowService).homologarCadastroEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco deve delegar se houver itens")
        void disponibilizarMapaEmBloco_DeveDelegar() {
            Long codProcesso = 1L;
            List<Long> ids = List.of(50L);
            Usuario usuario = new Usuario();
            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now().plusDays(1))
                    .build();

            facade.disponibilizarMapaEmBloco(ids, codProcesso, req, usuario);

            verify(mapaWorkflowService).disponibilizarMapaEmBloco(ids, req, usuario);
        }

        @Test
        @DisplayName("aceitarValidacaoEmBloco deve delegar se houver itens")
        void aceitarValidacaoEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L, 60L);
            Usuario usuario = new Usuario();

            facade.aceitarValidacaoEmBloco(ids, usuario);

            verify(mapaWorkflowService).aceitarValidacaoEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("homologarValidacaoEmBloco deve delegar se houver itens")
        void homologarValidacaoEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L);
            Usuario usuario = new Usuario();

            facade.homologarValidacaoEmBloco(ids, usuario);

            verify(mapaWorkflowService).homologarValidacaoEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("aceitarCadastroEmBloco não deve delegar se não houver itens")
        void aceitarCadastroEmBloco_NaoDeveDelegarSeVazio() {
            facade.aceitarCadastroEmBloco(List.of(), new Usuario());
            verify(cadastroWorkflowService, never()).aceitarCadastroEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("homologarCadastroEmBloco não deve delegar se não houver itens")
        void homologarCadastroEmBloco_NaoDeveDelegarSeVazio() {
            facade.homologarCadastroEmBloco(List.of(), new Usuario());
            verify(cadastroWorkflowService, never()).homologarCadastroEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco não deve delegar se não houver itens")
        void disponibilizarMapaEmBloco_NaoDeveDelegarSeVazio() {
            facade.disponibilizarMapaEmBloco(List.of(), 1L, DisponibilizarMapaRequest.builder().build(), new Usuario());
            verify(mapaWorkflowService, never()).disponibilizarMapaEmBloco(anyList(), any(), any());
        }

        @Test
        @DisplayName("aceitarValidacaoEmBloco não deve delegar se não houver itens")
        void aceitarValidacaoEmBloco_NaoDeveDelegarSeVazio() {
            facade.aceitarValidacaoEmBloco(List.of(), new Usuario());
            verify(mapaWorkflowService, never()).aceitarValidacaoEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("homologarValidacaoEmBloco não deve delegar se não houver itens")
        void homologarValidacaoEmBloco_NaoDeveDelegarSeVazio() {
            facade.homologarValidacaoEmBloco(List.of(), new Usuario());
            verify(mapaWorkflowService, never()).homologarValidacaoEmBloco(anyList(), any());
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    class PermissaoDetalheTests {
        @Test
        @DisplayName("Deve obter detalhes do subprocesso")
        void deveObterDetalhes() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            facade.obterDetalhes(codigo);

            verify(contextoService).obterDetalhes(codigo, usuario);
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
