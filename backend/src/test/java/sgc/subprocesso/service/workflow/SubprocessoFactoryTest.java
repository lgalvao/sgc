package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoWorkflowService")
class SubprocessoWorkflowFactoryTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;


    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoWorkflowService factory;

    @BeforeEach
    void setup() {
        factory.setSubprocessoRepo(subprocessoRepo);
        factory.setMovimentacaoRepo(movimentacaoRepo);
        factory.setServicoDeCopiaDeMapa(servicoDeCopiaDeMapa);
        factory.setMapaManutencaoService(mapaManutencaoService);
    }

    @Nested
    @DisplayName("criar")
    class CriarTests {
        
        @Test
        @DisplayName("deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            // Arrange
            LocalDateTime dataLimite1 = LocalDateTime.now().plusDays(10);
            LocalDateTime dataLimite2 = LocalDateTime.now().plusDays(20);
            
            CriarSubprocessoRequest request = new CriarSubprocessoRequest(
                    1L,  // codProcesso
                    100L, // codUnidade
                    null, // codMapa
                    dataLimite1,
                    dataLimite2
            );
            
            Subprocesso subprocessoSalvo = Subprocesso.builder()
                    .codigo(1L)
                    .build();
            
            Mapa mapaSalvo = Mapa.builder()
                    .codigo(10L)
                    .build();
            
            when(subprocessoRepo.save(any(Subprocesso.class)))
                    .thenReturn(subprocessoSalvo)
                    .thenAnswer(i -> i.getArgument(0));
            when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenReturn(mapaSalvo);
            
            // Act
            Subprocesso result = factory.criarEntidade(request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(subprocessoRepo, times(2)).save(any(Subprocesso.class));
            verify(mapaManutencaoService).salvarMapa(any(Mapa.class));
        }
    }

    @Nested
    @DisplayName("criarParaMapeamento")
    class CriarParaMapeamentoTests {
        
        @Test
        @DisplayName("deve criar com sucesso para unidade OPERACIONAL")
        void deveCriarComSucessoParaUnidadeOperacional() {
            Processo processo = new Processo();
            processo.setDataLimite(LocalDateTime.now().plusDays(10));
            Unidade unidade = new Unidade();
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            unidade.setSigla("U1");

            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaMapeamento(processo, List.of(unidade), unidadeOrigem, usuario);

            verify(subprocessoRepo, times(1)).saveAll(anyList());
            verify(mapaManutencaoService).salvarMapas(anyList());
            verify(movimentacaoRepo).saveAll(anyList());
        }
        
        @Test
        @DisplayName("deve criar com sucesso para unidade INTEROPERACIONAL")
        void deveCriarComSucessoParaUnidadeInteroperacional() {
            Processo processo = new Processo();
            processo.setDataLimite(LocalDateTime.now().plusDays(10));
            Unidade unidade = new Unidade();
            unidade.setTipo(TipoUnidade.INTEROPERACIONAL);
            unidade.setSigla("U2");

            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaMapeamento(processo, List.of(unidade), unidadeOrigem, usuario);

            verify(subprocessoRepo).saveAll(anyList());
            verify(mapaManutencaoService).salvarMapas(anyList());
            verify(movimentacaoRepo).saveAll(anyList());
        }
        
        @Test
        @DisplayName("deve ignorar unidade nao elegivel")
        void deveIgnorarUnidadeNaoElegivel() {
            Processo processo = new Processo();
            Unidade unidade = new Unidade();
            unidade.setTipo(TipoUnidade.INTERMEDIARIA);

            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaMapeamento(processo, List.of(unidade), unidadeOrigem, usuario);

            verifyNoInteractions(subprocessoRepo);
        }
        
        @Test
        @DisplayName("deve processar lista vazia de unidades")
        void deveProcessarListaVaziaDeUnidades() {
            Processo processo = new Processo();

            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaMapeamento(processo, Collections.emptyList(), unidadeOrigem, usuario);

            verifyNoInteractions(subprocessoRepo);
        }
        
        @Test
        @DisplayName("deve criar para múltiplas unidades elegíveis")
        void deveCriarParaMultiplasUnidadesElegiveis() {
            Processo processo = new Processo();
            processo.setDataLimite(LocalDateTime.now().plusDays(10));
            
            Unidade unidade1 = new Unidade();
            unidade1.setTipo(TipoUnidade.OPERACIONAL);
            unidade1.setSigla("U1");
            
            Unidade unidade2 = new Unidade();
            unidade2.setTipo(TipoUnidade.INTEROPERACIONAL);
            unidade2.setSigla("U2");
            
            Unidade unidade3 = new Unidade();
            unidade3.setTipo(TipoUnidade.INTERMEDIARIA);
            unidade3.setSigla("U3");

            Unidade unidadeOrigem = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaMapeamento(processo, List.of(unidade1, unidade2, unidade3), unidadeOrigem, usuario);

            verify(subprocessoRepo).saveAll(argThat(list -> ((List<?>)list).size() == 2)); // Apenas 2 elegiveis
            verify(mapaManutencaoService).salvarMapas(anyList());
            verify(movimentacaoRepo).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("criarParaRevisao")
    class CriarParaRevisaoTests {
        
        @Test
        @DisplayName("deve criar com sucesso")
        void deveCriarComSucesso() {
            Processo processo = new Processo();
            processo.setDataLimite(LocalDateTime.now().plusDays(10));
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("U1");

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(100L);
            UnidadeMapa unidadeMapa = new UnidadeMapa();
            unidadeMapa.setMapaVigente(mapaVigente);

            when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

            Mapa mapaCopiado = new Mapa();
            mapaCopiado.setCodigo(200L);
            when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
            when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

            Unidade unidadeOrigemMock = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigemMock, usuario);

            verify(subprocessoRepo, times(1)).save(any(Subprocesso.class));
            verify(mapaManutencaoService).salvarMapa(any(Mapa.class));
            verify(movimentacaoRepo).save(any(Movimentacao.class));
            verify(servicoDeCopiaDeMapa).copiarMapaParaUnidade(100L);
        }
    }

    @Nested
    @DisplayName("criarParaDiagnostico")
    class CriarParaDiagnosticoTests {
        
        @Test
        @DisplayName("deve criar com sucesso com situacao correta")
        void deveCriarComSucessoComSituacaoCorreta() {
            Processo processo = new Processo();
            processo.setDataLimite(LocalDateTime.now().plusDays(10));
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("U1");

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(100L);
            UnidadeMapa unidadeMapa = new UnidadeMapa();
            unidadeMapa.setMapaVigente(mapaVigente);

            when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

            Mapa mapaCopiado = new Mapa();
            mapaCopiado.setCodigo(200L);
            when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
            when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

            Unidade unidadeOrigemMock = new Unidade();
            Usuario usuario = new Usuario();

            factory.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigemMock, usuario);

            // Verifica situacao inicial
            verify(subprocessoRepo, atLeastOnce()).save(argThat(s -> s.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO));
            verify(movimentacaoRepo).save(any(Movimentacao.class));
            verify(servicoDeCopiaDeMapa).copiarMapaParaUnidade(100L);
            verify(mapaManutencaoService).salvarMapa(any(Mapa.class));
        }
    }
}
