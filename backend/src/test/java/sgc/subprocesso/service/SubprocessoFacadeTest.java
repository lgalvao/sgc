package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacade - Testes Consolidados")
class SubprocessoFacadeTest {

    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private SubprocessoFacade facade;

    private Subprocesso criarSubprocesso(Long codigo) {
        return Subprocesso.builder()
                .codigo(codigo)
                .processo(Processo.builder().codigo(1L).build())
                .unidade(Unidade.builder().codigo(1L).build())
                .situacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO)
                .build();
    }

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {

        @Test
        @DisplayName("Deve buscar subprocesso por ID")
        void deveBuscarSubprocessoPorId() {
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(criarSubprocesso(1L));
            assertThat(facade.buscarSubprocesso(1L)).isNotNull();
            verify(subprocessoService).buscarSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve buscar subprocesso com mapa")
        void deveBuscarSubprocessoComMapa() {
            when(subprocessoService.buscarSubprocessoComMapa(1L)).thenReturn(criarSubprocesso(1L));
            assertThat(facade.buscarSubprocessoComMapa(1L)).isNotNull();
            verify(subprocessoService).buscarSubprocessoComMapa(1L);
        }

        @Test
        @DisplayName("Deve listar todos os subprocessos")
        void deveListar() {
            when(subprocessoService.listarEntidades()).thenReturn(List.of(criarSubprocesso(1L)));
            assertThat(facade.listar()).hasSize(1);
            verify(subprocessoService).listarEntidades();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há subprocessos")
        void deveRetornarListaVaziaQuandoNaoHaSubprocessos() {
            when(subprocessoService.listarEntidades()).thenReturn(List.of());

            List<Subprocesso> resultado = facade.listar();

            assertThat(resultado)
                    .isNotNull()
                    .isEmpty();
            verify(subprocessoService).listarEntidades();
        }

        @Test
        @DisplayName("Deve obter por processo e unidade")
        void deveObterPorProcessoEUnidade() {
            when(subprocessoService.obterEntidadePorProcessoEUnidade(1L, 10L))
                    .thenReturn(criarSubprocesso(1L));
            Subprocesso result = facade.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
            verify(subprocessoService).obterEntidadePorProcessoEUnidade(1L, 10L);
        }

        @Test
        @DisplayName("Deve listar por processo e unidades")
        void deveListarPorProcessoEUnidades() {
            Long codProcesso = 1L;
            List<Long> unidades = List.of(2L);
            when(subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, unidades))
                    .thenReturn(List.of(criarSubprocesso(1L)));

            List<Subprocesso> resultado = facade.listarPorProcessoEUnidades(codProcesso, unidades);

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1);
            verify(subprocessoService).listarEntidadesPorProcessoEUnidades(codProcesso, unidades);
        }

        @Test
        @DisplayName("Deve retornar situação")
        void deveObterSituacao() {
            when(subprocessoService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            assertThat(facade.obterSituacao(1L)).isNotNull();
            verify(subprocessoService).obterStatus(1L);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso")
        void deveListarAtividadesSubprocesso() {
            when(subprocessoService.listarAtividadesSubprocesso(1L))
                    .thenReturn(List.of(new AtividadeDto(1L, "Atividade 1", List.of())));

            List<AtividadeDto> resultado = facade.listarAtividadesSubprocesso(1L);

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1);
            verify(subprocessoService).listarAtividadesSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por ID")
        void deveObterAtividadesSemConhecimentoPorId() {
            when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
            assertThat(facade.obterAtividadesSemConhecimento(1L)).isEmpty();
            verify(subprocessoService).obterAtividadesSemConhecimento(1L);
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por Mapa")
        void deveObterAtividadesSemConhecimentoPorMapa() {
            Mapa mapa = new Mapa();
            when(subprocessoService.obterAtividadesSemConhecimento(mapa)).thenReturn(Collections.emptyList());
            assertThat(facade.obterAtividadesSemConhecimento(mapa)).isEmpty();
            verify(subprocessoService).obterAtividadesSemConhecimento(mapa);
        }

        @Test
        @DisplayName("Deve obter contexto de edição")
        void deveObterContextoEdicao() {
            facade.obterContextoEdicao(1L);
            verify(subprocessoService).obterContextoEdicao(1L);
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(subprocessoService.obterEntidadePorCodigoMapa(100L)).thenReturn(criarSubprocesso(1L));
            assertThat(facade.obterEntidadePorCodigoMapa(100L)).isNotNull();
            verify(subprocessoService).obterEntidadePorCodigoMapa(100L);
        }

        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(subprocessoService.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(facade.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();
            verify(subprocessoService).verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L));
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(criarSubprocesso(1L)));
            assertThat(facade.listarEntidadesPorProcesso(1L)).hasSize(1);
            verify(subprocessoService).listarEntidadesPorProcesso(1L);
        }

        @Test
        @DisplayName("Deve obter sugestões")
        void deveObterSugestoes() {
            Map<String, Object> result = facade.obterSugestoes(1L);
            assertThat(result).containsKey("sugestoes");
        }

        @Test
        @DisplayName("Deve obter mapa para ajuste")
        void deveObterMapaParaAjuste() {
            facade.obterMapaParaAjuste(1L);
            verify(subprocessoService).obterMapaParaAjuste(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            CriarSubprocessoRequest request = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(1L).build();
            when(subprocessoService.criarEntidade(request)).thenReturn(criarSubprocesso(1L));
            assertThat(facade.criar(request)).isNotNull();
            verify(subprocessoService).criarEntidade(request);
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().build();
            when(subprocessoService.atualizarEntidade(1L, request)).thenReturn(criarSubprocesso(1L));
            assertThat(facade.atualizar(1L, request)).isNotNull();
            verify(subprocessoService).atualizarEntidade(1L, request);
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            facade.excluir(1L);
            verify(subprocessoService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar cadastro")
        void deveValidarCadastro() {
            ValidacaoCadastroDto val = ValidacaoCadastroDto.builder().valido(true).build();
            when(subprocessoService.validarCadastro(1L)).thenReturn(val);

            ValidacaoCadastroDto result = facade.validarCadastro(1L);
            assertThat(result.valido()).isTrue();
            verify(subprocessoService).validarCadastro(1L);
        }

        @Test
        @DisplayName("Deve validar existência de atividades")
        void deveValidarExistenciaAtividades() {
            facade.validarExistenciaAtividades(1L);
            verify(subprocessoService).validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("Deve validar associações do mapa")
        void deveValidarAssociacoesMapa() {
            facade.validarAssociacoesMapa(100L);
            verify(subprocessoService).validarAssociacoesMapa(100L);
        }
    }

    @Nested
    @DisplayName("Cenários de Workflow e Transição de Estado")
    class WorkflowTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO")
        void deveAtualizarParaEmAndamento() {
            facade.atualizarSituacaoParaEmAndamento(100L);
            verify(subprocessoService).atualizarParaEmAndamento(100L);
        }

        @Test
        @DisplayName("Deve listar subprocessos homologados")
        void deveListarSubprocessosHomologados() {
            when(subprocessoService.listarSubprocessosHomologados()).thenReturn(Collections.emptyList());
            assertThat(facade.listarSubprocessosHomologados()).isEmpty();
            verify(subprocessoService).listarSubprocessosHomologados();
        }

        @Test
        @DisplayName("Deve reabrir cadastro")
        void deveReabrirCadastro() {
            facade.reabrirCadastro(1L, "Justificativa");
            verify(subprocessoService).reabrirCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve reabrir revisão cadastro")
        void deveReabrirRevisaoCadastro() {
            facade.reabrirRevisaoCadastro(1L, "Justificativa");
            verify(subprocessoService).reabrirRevisaoCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve alterar data limite")
        void deveAlterarDataLimite() {
            LocalDate novaData = LocalDate.now();
            facade.alterarDataLimite(1L, novaData);
            verify(subprocessoService).alterarDataLimite(1L, novaData);
        }

        @Test
        @DisplayName("Deve salvar ajustes do mapa")
        void deveSalvarAjustesMapa() {
            Long codigo = 1L;
            List<CompetenciaAjusteDto> competencias = List.of(CompetenciaAjusteDto.builder().build());
            facade.salvarAjustesMapa(codigo, competencias);
            verify(subprocessoService).salvarAjustesMapa(codigo, competencias);
        }

        @Test
        @DisplayName("Deve importar atividades")
        void deveImportarAtividades() {
            facade.importarAtividades(1L, 2L);
            verify(subprocessoService).importarAtividades(1L, 2L);
        }

        @Test
        @DisplayName("Deve disponibilizar cadastro")
        void deveDisponibilizarCadastro() {
            Usuario usuario = new Usuario();
            facade.disponibilizarCadastro(1L, usuario);
            verify(subprocessoService).disponibilizarCadastro(1L, usuario);
        }

        @Test
        @DisplayName("Deve disponibilizar revisão")
        void deveDisponibilizarRevisao() {
            Usuario usuario = new Usuario();
            facade.disponibilizarRevisao(1L, usuario);
            verify(subprocessoService).disponibilizarRevisao(1L, usuario);
        }

        @Test
        @DisplayName("Deve devolver cadastro")
        void deveDevolverCadastro() {
            Usuario usuario = new Usuario();
            facade.devolverCadastro(1L, "obs", usuario);
            verify(subprocessoService).devolverCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve aceitar cadastro")
        void deveAceitarCadastro() {
            Usuario usuario = new Usuario();
            facade.aceitarCadastro(1L, "obs", usuario);
            verify(subprocessoService).aceitarCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve homologar cadastro")
        void deveHomologarCadastro() {
            Usuario usuario = new Usuario();
            facade.homologarCadastro(1L, "obs", usuario);
            verify(subprocessoService).homologarCadastro(1L, usuario, "obs");
        }

        @Test
        @DisplayName("Deve apresentar sugestões")
        void deveApresentarSugestoes() {
            Usuario usuario = new Usuario();
            facade.apresentarSugestoes(1L, "sug", usuario);
            verify(subprocessoService).apresentarSugestoes(1L, "sug", usuario);
        }

        @Test
        @DisplayName("Deve validar mapa")
        void deveValidarMapa() {
            Usuario usuario = new Usuario();
            facade.validarMapa(1L, usuario);
            verify(subprocessoService).validarMapa(1L, usuario);
        }

        @Test
        @DisplayName("Deve salvar mapa do subprocesso")
        void deveSalvarMapaSubprocesso() {
            SalvarMapaRequest request = SalvarMapaRequest.builder().build();
            when(subprocessoService.salvarMapaSubprocesso(eq(1L), any())).thenReturn(new Mapa());
            facade.salvarMapaSubprocesso(1L, request);
            verify(subprocessoService).salvarMapaSubprocesso(1L, request);
        }

        @Test
        @DisplayName("Deve adicionar competencia")
        void deveAdicionarCompetencia() {
            CompetenciaRequest req = CompetenciaRequest.builder().build();
            facade.adicionarCompetencia(1L, req);
            verify(subprocessoService).adicionarCompetencia(1L, req);
        }

        @Test
        @DisplayName("Deve atualizar competencia")
        void deveAtualizarCompetencia() {
            CompetenciaRequest req = CompetenciaRequest.builder().build();
            facade.atualizarCompetencia(1L, 100L, req);
            verify(subprocessoService).atualizarCompetencia(1L, 100L, req);
        }

        @Test
        @DisplayName("Deve remover competencia")
        void deveRemoverCompetencia() {
            facade.removerCompetencia(1L, 100L);
            verify(subprocessoService).removerCompetencia(1L, 100L);
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado")
        void deveSubmeterMapaAjustado() {
            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder().build();
            Usuario usuario = new Usuario();
            facade.submeterMapaAjustado(1L, req, usuario);
            verify(subprocessoService).submeterMapaAjustado(1L, req, usuario);
        }

        @Test
        @DisplayName("Deve registrar movimentação de lembrete")
        void deveRegistrarMovimentacaoLembrete() {
            facade.registrarMovimentacaoLembrete(1L);
            verify(subprocessoService).registrarMovimentacaoLembrete(1L);
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

            verify(subprocessoService).aceitarCadastroEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("homologarCadastroEmBloco deve delegar se houver itens")
        void homologarCadastroEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L, 60L);
            Usuario usuario = new Usuario();

            facade.homologarCadastroEmBloco(ids, usuario);

            verify(subprocessoService).homologarCadastroEmBloco(ids, usuario);
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

            verify(subprocessoService).disponibilizarMapaEmBloco(ids, req, usuario);
        }

        @Test
        @DisplayName("aceitarValidacaoEmBloco deve delegar se houver itens")
        void aceitarValidacaoEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L, 60L);
            Usuario usuario = new Usuario();

            facade.aceitarValidacaoEmBloco(ids, usuario);

            verify(subprocessoService).aceitarValidacaoEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("homologarValidacaoEmBloco deve delegar se houver itens")
        void homologarValidacaoEmBloco_DeveDelegar() {
            List<Long> ids = List.of(50L);
            Usuario usuario = new Usuario();

            facade.homologarValidacaoEmBloco(ids, usuario);

            verify(subprocessoService).homologarValidacaoEmBloco(ids, usuario);
        }

        @Test
        @DisplayName("aceitarCadastroEmBloco não deve delegar se não houver itens")
        void aceitarCadastroEmBloco_NaoDeveDelegarSeVazio() {
            facade.aceitarCadastroEmBloco(List.of(), new Usuario());
            verify(subprocessoService, never()).aceitarCadastroEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("homologarCadastroEmBloco não deve delegar se não houver itens")
        void homologarCadastroEmBloco_NaoDeveDelegarSeVazio() {
            facade.homologarCadastroEmBloco(List.of(), new Usuario());
            verify(subprocessoService, never()).homologarCadastroEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco não deve delegar se não houver itens")
        void disponibilizarMapaEmBloco_NaoDeveDelegarSeVazio() {
            facade.disponibilizarMapaEmBloco(List.of(), 1L, DisponibilizarMapaRequest.builder().build(), new Usuario());
            verify(subprocessoService, never()).disponibilizarMapaEmBloco(anyList(), any(), any());
        }

        @Test
        @DisplayName("aceitarValidacaoEmBloco não deve delegar se não houver itens")
        void aceitarValidacaoEmBloco_NaoDeveDelegarSeVazio() {
            facade.aceitarValidacaoEmBloco(List.of(), new Usuario());
            verify(subprocessoService, never()).aceitarValidacaoEmBloco(anyList(), any());
        }

        @Test
        @DisplayName("homologarValidacaoEmBloco não deve delegar se não houver itens")
        void homologarValidacaoEmBloco_NaoDeveDelegarSeVazio() {
            facade.homologarValidacaoEmBloco(List.of(), new Usuario());
            verify(subprocessoService, never()).homologarValidacaoEmBloco(anyList(), any());
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
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            facade.obterDetalhes(codigo);

            verify(subprocessoService).obterDetalhes(codigo, usuario);
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
            verify(subprocessoService).criarParaMapeamento(processo, unidades, unidadeOrigem, usuario);
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
            verify(subprocessoService).criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
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
            verify(subprocessoService).criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
        }
    }
}
