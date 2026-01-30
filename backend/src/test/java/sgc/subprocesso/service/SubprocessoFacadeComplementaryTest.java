package sgc.subprocesso.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.analise.AnaliseFacade;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("SubprocessoFacade - Testes Complementares")
class SubprocessoFacadeComplementaryTest {
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private UnidadeFacade unidadeFacade;
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
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private SubprocessoAjusteMapaService ajusteMapaService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private SubprocessoContextoService contextoService;
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock
    private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock
    private sgc.mapa.service.CopiaMapaService copiaMapaService;
    @Mock
    private sgc.mapa.service.MapaFacade mapaFacade;
    @Mock
    private sgc.mapa.mapper.ConhecimentoMapper conhecimentoMapper;

    @InjectMocks
    private SubprocessoFacade subprocessoFacade;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {
        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(crudService.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(subprocessoFacade.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(crudService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(new Subprocesso()));
            assertThat(subprocessoFacade.listarEntidadesPorProcesso(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso - Delegação")
        void deveListarAtividadesSubprocesso() {
              subprocessoFacade.listarAtividadesSubprocesso(1L);

            verify(atividadeService).listarAtividadesSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar situação quando subprocesso existe")
        void deveRetornarSituacaoQuandoSubprocessoExiste() {
            when(crudService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            assertThat(subprocessoFacade.obterSituacao(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(crudService.obterEntidadePorCodigoMapa(100L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.obterEntidadePorCodigoMapa(100L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades sem conhecimento")
        void deveRetornarListaVaziaSeNaoHouverAtividadesSemConhecimento() {
            when(validacaoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
            List<Atividade> result = subprocessoFacade.obterAtividadesSemConhecimento(1L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar subprocesso por ID")
        void deveBuscarSubprocessoPorId() {
            when(crudService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.buscarSubprocesso(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar subprocesso com mapa")
        void deveBuscarSubprocessoComMapa() {
            when(crudService.buscarSubprocessoComMapa(1L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.buscarSubprocessoComMapa(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por Mapa")
        void deveObterAtividadesSemConhecimentoPorMapa() {
            Mapa mapa = new Mapa();
            when(validacaoService.obterAtividadesSemConhecimento(mapa)).thenReturn(Collections.emptyList());
            assertThat(subprocessoFacade.obterAtividadesSemConhecimento(mapa)).isEmpty();
        }

        @Test
        @DisplayName("Deve listar subprocessos homologados")
        void deveListarSubprocessosHomologados() {
            when(adminWorkflowService.listarSubprocessosHomologados()).thenReturn(Collections.emptyList());
            assertThat(subprocessoFacade.listarSubprocessosHomologados()).isEmpty();
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
            assertThat(subprocessoFacade.criar(request)).isNotNull();
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(100L).build();
            SubprocessoDto response = SubprocessoDto.builder().codMapa(100L).build();
            when(crudService.atualizar(1L, request)).thenReturn(response);
            assertThat(subprocessoFacade.atualizar(1L, request)).isNotNull();
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            subprocessoFacade.excluir(1L);
            verify(crudService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar existência de atividades - Sucesso")
        void deveValidarExistenciaAtividadesSucesso() {
            subprocessoFacade.validarExistenciaAtividades(1L);
            verify(validacaoService).validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("validarCadastro sucesso")
        void validarCadastroSucesso() {
            ValidacaoCadastroDto val = ValidacaoCadastroDto.builder().valido(true).build();
            when(validacaoService.validarCadastro(1L)).thenReturn(val);

            ValidacaoCadastroDto result = subprocessoFacade.validarCadastro(1L);
            assertThat(result.valido()).isTrue();
        }

        @Test
        @DisplayName("Deve validar associações do mapa")
        void deveValidarAssociacoesMapa() {
            subprocessoFacade.validarAssociacoesMapa(100L);
            verify(validacaoService).validarAssociacoesMapa(100L);
        }
    }

    @Nested
    @DisplayName("Cenários de Transição de Estado")
    class TransicaoEstadoTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO")
        void deveAtualizarParaEmAndamentoMapeamento() {
            subprocessoFacade.atualizarSituacaoParaEmAndamento(100L);
            verify(adminWorkflowService).atualizarSituacaoParaEmAndamento(100L);
        }

        @Test
        @DisplayName("Deve alterar data limite")
        void deveAlterarDataLimite() {
            LocalDate novaData = java.time.LocalDate.now();
            subprocessoFacade.alterarDataLimite(1L, novaData);
            verify(adminWorkflowService).alterarDataLimite(1L, novaData);
        }

        @Test
        @DisplayName("Deve reabrir cadastro")
        void deveReabrirCadastro() {
            subprocessoFacade.reabrirCadastro(1L, "Justificativa");
            verify(cadastroWorkflowService).reabrirCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve reabrir revisão cadastro")
        void deveReabrirRevisaoCadastro() {
            subprocessoFacade.reabrirRevisaoCadastro(1L, "Justificativa");
            verify(cadastroWorkflowService).reabrirRevisaoCadastro(1L, "Justificativa");
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    class PermissaoDetalheTests {
        @Test
        @DisplayName("obterPermissoes - Delegação para PermissaoCalculator")
        void obterPermissoesLancaExcecaoQuandoNaoAutenticado() {
            when(usuarioService.obterUsuarioAutenticado())
                    .thenThrow(new sgc.comum.erros.ErroAccessoNegado("Nenhum usuário autenticado"));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                    subprocessoFacade.obterPermissoes(1L)
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes - Delegação para PermissaoCalculator")
        void obterPermissoesComAutenticacaoSemNome() {
            when(usuarioService.obterUsuarioAutenticado())
                    .thenThrow(new sgc.comum.erros.ErroAccessoNegado("Usuário sem nome"));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                    subprocessoFacade.obterPermissoes(1L)
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("Deve obter permissoes - Delegação")
        void deveObterPermissoes() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();

            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            subprocessoFacade.obterPermissoes(codigo);

            verify(permissaoCalculator).obterPermissoes(codigo, usuario);
        }

        @Test
        @DisplayName("Deve obter detalhes do subprocesso - Delegação")
        void deveObterDetalhes() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();

            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            subprocessoFacade.obterDetalhes(codigo, sgc.organizacao.model.Perfil.ADMIN);

            verify(contextoService).obterDetalhes(codigo, usuario);
        }


        @Test
        @DisplayName("Deve obter contexto de edição - Delegação")
        void deveObterContextoEdicao() {
            Long codigo = 1L;

            subprocessoFacade.obterContextoEdicao(codigo, sgc.organizacao.model.Perfil.ADMIN);

            verify(contextoService).obterContextoEdicao(codigo);
        }

    }

    @Nested
    @DisplayName("Cenários de DTO e Mapeamento")
    class DtoMappingTests {
        @Test
        @DisplayName("obterSugestoes - Delegação")
        void obterSugestoes() {
            Long codigo = 1L;

            subprocessoFacade.obterSugestoes(codigo);

            verify(contextoService).obterSugestoes(codigo);
        }

        @Test
        @DisplayName("listar")
        void listar() {
            when(crudService.listar()).thenReturn(List.of(new SubprocessoDto()));
            List<SubprocessoDto> result = subprocessoFacade.listar();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("obterPorProcessoEUnidade")
        void obterPorProcessoEUnidade() {
            when(crudService.obterPorProcessoEUnidade(1L, 10L)).thenReturn(SubprocessoDto.builder().codigo(1L).build());
            SubprocessoDto result = subprocessoFacade.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve obter cadastro - Delegação")
        void deveObterCadastro() {
            Long codigo = 1L;

            subprocessoFacade.obterCadastro(codigo);

            verify(contextoService).obterCadastro(codigo);
        }

        @Test
        @DisplayName("Deve obter mapa para ajuste - Delegação")
        void deveObterMapaParaAjuste() {
            Long codigo = 1L;

            subprocessoFacade.obterMapaParaAjuste(codigo);

            verify(ajusteMapaService).obterMapaParaAjuste(codigo);
        }
    }

    @Nested
    @DisplayName("Cenários Complexos")
    class SubprocessoFacadeRefactoredTest {
        @Test
        @DisplayName("Deve salvar ajustes do mapa - Delegação")
        void deveSalvarAjustesMapa() {
            Long codigo = 1L;
            List<CompetenciaAjusteDto> competencias = List.of();

            subprocessoFacade.salvarAjustesMapa(codigo, competencias);

            verify(ajusteMapaService).salvarAjustesMapa(codigo, competencias);
        }

    }
}