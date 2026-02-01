package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SubprocessoContextoService.
 * Foca em cobrir branches e cenários de erro não cobertos.
 */
@Tag("unit")
@DisplayName("SubprocessoContextoService")
@ExtendWith(MockitoExtension.class)
class SubprocessoContextoServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    
    @Mock
    private UsuarioFacade usuarioService;
    
    @Mock
    private UnidadeFacade unidadeFacade;
    
    @Mock
    private MapaFacade mapaFacade;
    
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    
    @Mock
    private AccessControlService accessControlService;
    
    @Mock
    private SubprocessoAtividadeService atividadeService;
    
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;

    @InjectMocks
    private SubprocessoContextoService service;

    @Nested
    @DisplayName("obterDetalhes")
    class ObterDetalhesTests {

        @Test
        @DisplayName("deve obter detalhes com sucesso quando titular encontrado")
        void deveObterDetalhesComSucessoQuandoTitularEncontrado() {
            // Arrange
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            unidade.setTituloTitular("titular.login");
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setUnidade(unidade);
            
            Usuario responsavel = Usuario.builder()
                    .matricula("12345")
                    .nome("Responsavel")
                    .build();
            
            Usuario titular = Usuario.builder()
                    .tituloEleitoral("titular.login")
                    .nome("Titular")
                    .build();
            
            List<Movimentacao> movimentacoes = new ArrayList<>();
            SubprocessoPermissoesDto permissoes = SubprocessoPermissoesDto.builder().build();
            SubprocessoDetalheDto expected = SubprocessoDetalheDto.builder().build();
            
            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(responsavel);
            when(usuarioService.buscarPorLogin("titular.login")).thenReturn(titular);
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(movimentacoes);
            when(permissaoCalculator.calcularPermissoes(sp, usuario)).thenReturn(permissoes);
            when(subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes)).thenReturn(expected);
            
            // Act
            SubprocessoDetalheDto result = service.obterDetalhes(codigo, usuario);
            
            // Assert
            assertThat(result).isEqualTo(expected);
            verify(accessControlService).verificarPermissao(usuario, Acao.VISUALIZAR_SUBPROCESSO, sp);
            verify(usuarioService).buscarPorLogin("titular.login");
        }

        @Test
        @DisplayName("deve obter detalhes mesmo quando erro ao buscar titular")
        void deveObterDetalhesMesmoQuandoErroAoBuscarTitular() {
            // Arrange
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            unidade.setTituloTitular("titular.invalido");
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setUnidade(unidade);
            
            Usuario responsavel = Usuario.builder()
                    .matricula("12345")
                    .nome("Responsavel")
                    .build();
            
            List<Movimentacao> movimentacoes = new ArrayList<>();
            SubprocessoPermissoesDto permissoes = SubprocessoPermissoesDto.builder().build();
            SubprocessoDetalheDto expected = SubprocessoDetalheDto.builder().build();
            
            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(responsavel);
            when(usuarioService.buscarPorLogin("titular.invalido")).thenThrow(new RuntimeException("Titular não encontrado"));
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(movimentacoes);
            when(permissaoCalculator.calcularPermissoes(sp, usuario)).thenReturn(permissoes);
            when(subprocessoDetalheMapper.toDto(sp, responsavel, null, movimentacoes, permissoes)).thenReturn(expected);
            
            // Act
            SubprocessoDetalheDto result = service.obterDetalhes(codigo, usuario);
            
            // Assert
            assertThat(result).isEqualTo(expected);
            verify(subprocessoDetalheMapper).toDto(sp, responsavel, null, movimentacoes, permissoes);
        }

        @Test
        @DisplayName("deve obter detalhes passando entidade Subprocesso diretamente")
        void deveObterDetalhesPassandoEntidadeSubprocesso() {
            // Arrange
            Usuario usuario = new Usuario();
            
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            unidade.setTituloTitular("titular.login");
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(unidade);
            
            Usuario responsavel = new Usuario();
            Usuario titular = new Usuario();
            
            List<Movimentacao> movimentacoes = Collections.emptyList();
            SubprocessoPermissoesDto permissoes = SubprocessoPermissoesDto.builder().build();
            SubprocessoDetalheDto expected = SubprocessoDetalheDto.builder().build();
            
            when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(responsavel);
            when(usuarioService.buscarPorLogin("titular.login")).thenReturn(titular);
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(movimentacoes);
            when(permissaoCalculator.calcularPermissoes(sp, usuario)).thenReturn(permissoes);
            when(subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes)).thenReturn(expected);
            
            // Act
            SubprocessoDetalheDto result = service.obterDetalhes(sp, usuario);
            
            // Assert
            assertThat(result).isEqualTo(expected);
            verify(accessControlService).verificarPermissao(usuario, Acao.VISUALIZAR_SUBPROCESSO, sp);
        }
    }

    @Nested
    @DisplayName("obterCadastro")
    class ObterCadastroTests {

        @Test
        @DisplayName("deve obter cadastro sem atividades")
        void deveObterCadastroSemAtividades() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 100L;
            
            Usuario usuario = new Usuario();
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            
            Mapa mapa = new Mapa();
            mapa.setCodigo(codMapa);
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codSubprocesso);
            sp.setUnidade(unidade);
            sp.setMapa(mapa);
            
            when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(codMapa)).thenReturn(Collections.emptyList());
            
            // Act
            SubprocessoCadastroDto result = service.obterCadastro(codSubprocesso);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSubprocessoCodigo()).isEqualTo(codSubprocesso);
            assertThat(result.getUnidadeSigla()).isEqualTo("SIGLA");
            assertThat(result.getAtividades()).isEmpty();
            verify(accessControlService).verificarPermissao(usuario, Acao.VISUALIZAR_SUBPROCESSO, sp);
        }

        @Test
        @DisplayName("deve obter cadastro com atividades e conhecimentos")
        void deveObterCadastroComAtividadesEConhecimentos() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 100L;
            
            Usuario usuario = new Usuario();
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            
            Mapa mapa = new Mapa();
            mapa.setCodigo(codMapa);
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codSubprocesso);
            sp.setUnidade(unidade);
            sp.setMapa(mapa);
            
            Conhecimento conhecimento1 = Conhecimento.builder()
                    .codigo(1L)
                    .descricao("Conhecimento 1")
                    .build();
            
            Conhecimento conhecimento2 = Conhecimento.builder()
                    .codigo(2L)
                    .descricao("Conhecimento 2")
                    .build();
            
            Atividade atividade1 = Atividade.builder()
                    .codigo(1L)
                    .descricao("Atividade 1")
                    .conhecimentos(List.of(conhecimento1, conhecimento2))
                    .build();
            
            Atividade atividade2 = Atividade.builder()
                    .codigo(2L)
                    .descricao("Atividade 2")
                    .conhecimentos(Collections.emptyList())
                    .build();
            
            List<Atividade> atividades = List.of(atividade1, atividade2);
            
            ConhecimentoResponse conhecimentoResp1 = new ConhecimentoResponse(1L, 1L, "Conhecimento 1");
            ConhecimentoResponse conhecimentoResp2 = new ConhecimentoResponse(2L, 1L, "Conhecimento 2");
            
            when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(codMapa)).thenReturn(atividades);
            when(conhecimentoMapper.toResponse(conhecimento1)).thenReturn(conhecimentoResp1);
            when(conhecimentoMapper.toResponse(conhecimento2)).thenReturn(conhecimentoResp2);
            
            // Act
            SubprocessoCadastroDto result = service.obterCadastro(codSubprocesso);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAtividades()).hasSize(2);
            
            SubprocessoCadastroDto.AtividadeCadastroDto atividadeDto1 = result.getAtividades().get(0);
            assertThat(atividadeDto1.getCodigo()).isEqualTo(1L);
            assertThat(atividadeDto1.getDescricao()).isEqualTo("Atividade 1");
            assertThat(atividadeDto1.getConhecimentos()).hasSize(2);
            
            SubprocessoCadastroDto.AtividadeCadastroDto atividadeDto2 = result.getAtividades().get(1);
            assertThat(atividadeDto2.getCodigo()).isEqualTo(2L);
            assertThat(atividadeDto2.getConhecimentos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("obterSugestoes")
    class ObterSugestoesTests {

        @Test
        @DisplayName("deve retornar sugestões vazias")
        void deveRetornarSugestoesVazias() {
            // Arrange
            Long codSubprocesso = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codSubprocesso);
            
            when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
            
            // Act
            SugestoesDto result = service.obterSugestoes(codSubprocesso);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.sugestoes()).isEmpty();
            assertThat(result.dataHora()).isNotNull();
            verify(crudService).buscarSubprocesso(codSubprocesso);
        }
    }

    @Nested
    @DisplayName("obterContextoEdicao")
    class ObterContextoEdicaoTests {

        @Test
        @DisplayName("deve obter contexto de edição completo")
        void deveObterContextoEdicaoCompleto() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 100L;
            
            Usuario usuario = new Usuario();
            
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            
            Mapa mapa = new Mapa();
            mapa.setCodigo(codMapa);
            
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(codSubprocesso);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(mapa);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            
            SubprocessoDetalheDto.UnidadeDto unidadeDtoInterno = SubprocessoDetalheDto.UnidadeDto.builder()
                    .codigo(1L)
                    .sigla("SIGLA")
                    .nome("Nome")
                    .build();
            
            SubprocessoDetalheDto subprocessoDto = SubprocessoDetalheDto.builder()
                    .unidade(unidadeDtoInterno)
                    .build();
            
            UnidadeDto unidadeDto = new UnidadeDto();
            unidadeDto.setSigla("SIGLA");
            
            MapaCompletoDto mapaDto = MapaCompletoDto.builder().build();
            
            List<AtividadeDto> atividades = List.of(
                    new AtividadeDto(1L, "Atividade 1", null),
                    new AtividadeDto(2L, "Atividade 2", null)
            );
            
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
            when(usuarioService.buscarResponsavelAtual(anyString())).thenReturn(usuario);
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso)).thenReturn(Collections.emptyList());
            when(permissaoCalculator.calcularPermissoes(subprocesso, usuario)).thenReturn(SubprocessoPermissoesDto.builder().build());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any())).thenReturn(subprocessoDto);
            when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
            when(mapaFacade.obterMapaCompleto(codMapa, codSubprocesso)).thenReturn(mapaDto);
            when(atividadeService.listarAtividadesSubprocesso(codSubprocesso)).thenReturn(atividades);
            
            // Act
            ContextoEdicaoDto result = service.obterContextoEdicao(codSubprocesso);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.unidade()).isEqualTo(unidadeDto);
            assertThat(result.subprocesso()).isEqualTo(subprocessoDto);
            assertThat(result.mapa()).isEqualTo(mapaDto);
            assertThat(result.atividadesDisponiveis()).hasSize(2);
            
            verify(usuarioService).obterUsuarioAutenticado();
            verify(crudService).buscarSubprocesso(codSubprocesso);
            verify(unidadeFacade).buscarPorSigla("SIGLA");
            verify(mapaFacade).obterMapaCompleto(codMapa, codSubprocesso);
            verify(atividadeService).listarAtividadesSubprocesso(codSubprocesso);
        }
    }
}
