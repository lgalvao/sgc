package sgc.subprocesso.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.decomposed.SubprocessoCrudService;
import sgc.subprocesso.service.decomposed.SubprocessoDetalheService;
import sgc.subprocesso.service.decomposed.SubprocessoValidacaoService;
import sgc.subprocesso.service.decomposed.SubprocessoWorkflowService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para SubprocessoService")
class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaService mapaService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private SubprocessoPermissoesService subprocessoPermissoesService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private sgc.subprocesso.mapper.MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private sgc.analise.AnaliseService analiseService;

    // Decomposed services mocks
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoDetalheService detalheService;
    @Mock
    private SubprocessoWorkflowService workflowService;

    @InjectMocks
    private SubprocessoService service;

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {

        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(crudService.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(service.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(crudService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(new Subprocesso()));
            assertThat(service.listarEntidadesPorProcesso(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso convertidas para DTO")
        void deveListarAtividadesSubprocesso() {
            AtividadeVisualizacaoDto dto = new AtividadeVisualizacaoDto();
            dto.setDescricao("Atividade Teste");
            when(detalheService.listarAtividadesSubprocesso(1L)).thenReturn(List.of(dto));

            List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDescricao()).isEqualTo("Atividade Teste");
        }

        @Test
        @DisplayName("Deve retornar situação quando subprocesso existe")
        void deveRetornarSituacaoQuandoSubprocessoExiste() {
            when(crudService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            assertThat(service.obterStatus(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(crudService.obterEntidadePorCodigoMapa(100L)).thenReturn(new Subprocesso());
            assertThat(service.obterEntidadePorCodigoMapa(100L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades sem conhecimento")
        void deveRetornarListaVaziaSeNaoHouverAtividadesSemConhecimento() {
            when(validacaoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
            List<Atividade> result = service.obterAtividadesSemConhecimento(1L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar subprocesso por ID")
        void deveBuscarSubprocessoPorId() {
            when(crudService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            assertThat(service.buscarSubprocesso(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar subprocesso com mapa")
        void deveBuscarSubprocessoComMapa() {
            when(crudService.buscarSubprocessoComMapa(1L)).thenReturn(new Subprocesso());
            assertThat(service.buscarSubprocessoComMapa(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por Mapa")
        void deveObterAtividadesSemConhecimentoPorMapa() {
            Mapa mapa = new Mapa();
            when(validacaoService.obterAtividadesSemConhecimento(mapa)).thenReturn(Collections.emptyList());
            assertThat(service.obterAtividadesSemConhecimento(mapa)).isEmpty();
        }

        @Test
        @DisplayName("Deve listar subprocessos homologados")
        void deveListarSubprocessosHomologados() {
            when(workflowService.listarSubprocessosHomologados()).thenReturn(Collections.emptyList());
            assertThat(service.listarSubprocessosHomologados()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().build();
            when(crudService.criar(dto)).thenReturn(dto);
            assertThat(service.criar(dto)).isNotNull();
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().codMapa(100L).build();
            when(crudService.atualizar(1L, dto)).thenReturn(dto);
            assertThat(service.atualizar(1L, dto)).isNotNull();
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            service.excluir(1L);
            verify(crudService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar permissão de edição do mapa - Sucesso")
        void deveValidarPermissaoEdicaoMapaSucesso() {
            service.validarPermissaoEdicaoMapa(100L, "user");
            verify(validacaoService).validarPermissaoEdicaoMapa(100L, "user");
        }

        @Test
        @DisplayName("Deve validar existência de atividades - Sucesso")
        void deveValidarExistenciaAtividadesSucesso() {
            service.validarExistenciaAtividades(1L);
            verify(validacaoService).validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("validarCadastro sucesso")
        void validarCadastroSucesso() {
            ValidacaoCadastroDto val = ValidacaoCadastroDto.builder().valido(true).build();
            when(validacaoService.validarCadastro(1L)).thenReturn(val);

            ValidacaoCadastroDto result = service.validarCadastro(1L);
            assertThat(result.getValido()).isTrue();
        }

        @Test
        @DisplayName("Deve validar associações do mapa")
        void deveValidarAssociacoesMapa() {
            service.validarAssociacoesMapa(100L);
            verify(validacaoService).validarAssociacoesMapa(100L);
        }
    }

    @Nested
    @DisplayName("Cenários de Transição de Estado")
    class TransicaoEstadoTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO")
        void deveAtualizarParaEmAndamentoMapeamento() {
            service.atualizarSituacaoParaEmAndamento(100L);
            verify(workflowService).atualizarSituacaoParaEmAndamento(100L);
        }

        @Test
        @DisplayName("Deve alterar data limite")
        void deveAlterarDataLimite() {
            java.time.LocalDate novaData = java.time.LocalDate.now();
            service.alterarDataLimite(1L, novaData);
            verify(workflowService).alterarDataLimite(1L, novaData);
        }

        @Test
        @DisplayName("Deve reabrir cadastro")
        void deveReabrirCadastro() {
            service.reabrirCadastro(1L, "Justificativa");
            verify(workflowService).reabrirCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve reabrir revisão cadastro")
        void deveReabrirRevisaoCadastro() {
            service.reabrirRevisaoCadastro(1L, "Justificativa");
            verify(workflowService).reabrirRevisaoCadastro(1L, "Justificativa");
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    class PermissaoDetalheTests {

        @Test
        @DisplayName("obterDetalhes sucesso")
        void obterDetalhesSucesso() {
            mockSecurityContext("admin");
            Usuario admin = new Usuario();
            when(usuarioService.buscarUsuarioPorLogin("admin")).thenReturn(admin);

            when(detalheService.obterDetalhes(1L, Perfil.ADMIN, null, admin))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes")
        void obterPermissoes() {
            mockSecurityContext("user");
            Usuario user = new Usuario();
            when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(user);

            when(detalheService.obterPermissoes(1L, user))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());

            SubprocessoPermissoesDto result = service.obterPermissoes(1L);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes lança exceção quando não autenticado")
        void obterPermissoesSemAutenticacao() {
            SecurityContextHolder.clearContext();
            org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                service.obterPermissoes(1L)
            );
        }

        @Test
        @DisplayName("obterPermissoes lança exceção quando autenticação não tem nome")
        void obterPermissoesComAutenticacaoSemNome() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(null);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                service.obterPermissoes(1L)
            );
        }
    }

    @Nested
    @DisplayName("Cenários de DTO e Mapeamento")
    class DtoMappingTests {

        @Test
        @DisplayName("obterCadastro")
        void obterCadastro() {
            when(detalheService.obterCadastro(1L)).thenReturn(SubprocessoCadastroDto.builder().subprocessoCodigo(1L).build());
            SubprocessoCadastroDto result = service.obterCadastro(1L);
            assertThat(result).isNotNull();
            assertThat(result.getSubprocessoCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("obterSugestoes")
        void obterSugestoes() {
            when(detalheService.obterSugestoes(1L)).thenReturn(SugestoesDto.builder().sugestoes("S").build());
            SugestoesDto result = service.obterSugestoes(1L);
            assertThat(result.getSugestoes()).isEqualTo("S");
        }

        @Test
        @DisplayName("obterMapaParaAjuste")
        void obterMapaParaAjuste() {
            when(detalheService.obterMapaParaAjuste(1L)).thenReturn(MapaAjusteDto.builder().build());
            MapaAjusteDto result = service.obterMapaParaAjuste(1L);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("listar")
        void listar() {
            when(crudService.listar()).thenReturn(List.of(new SubprocessoDto()));
            List<SubprocessoDto> result = service.listar();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("obterPorProcessoEUnidade")
        void obterPorProcessoEUnidade() {
            when(crudService.obterPorProcessoEUnidade(1L, 10L)).thenReturn(SubprocessoDto.builder().codigo(1L).build());
            SubprocessoDto result = service.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
        }
    }
}
