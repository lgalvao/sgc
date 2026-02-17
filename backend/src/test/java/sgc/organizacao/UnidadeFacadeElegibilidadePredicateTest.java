package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes focados no predicado de elegibilidade do método buscarArvoreComElegibilidade.
 * 
 * <p>Este arquivo testa especificamente as condições do predicado nas linhas 56-58:
 * <ul>
 *   <li>u.getTipo() != TipoUnidade.INTERMEDIARIA</li>
 *   <li>(!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))</li>
 *   <li>!unidadesBloqueadas.contains(u.getCodigo())</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeFacade - Predicado de Elegibilidade")
class UnidadeFacadeElegibilidadePredicateTest {
    @Mock
    private UnidadeHierarquiaService hierarquiaService;
    
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeFacade facade;

    @Captor
    private ArgumentCaptor<Predicate<Unidade>> predicateCaptor;

    @Nested
    @DisplayName("Condição 1: Tipo de Unidade")
    class TipoUnidadeTestes {
        
        @Test
        @DisplayName("Deve aceitar unidade FINAL")
        void deveAceitarUnidadeFinal() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(false, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeFinal = new Unidade();
            unidadeFinal.setCodigo(1L);
            unidadeFinal.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeFinal)).isTrue();
        }
        
        @Test
        @DisplayName("Deve rejeitar unidade INTERMEDIARIA")
        void deveRejeitarUnidadeIntermediaria() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(false, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeIntermediaria = new Unidade();
            unidadeIntermediaria.setCodigo(1L);
            unidadeIntermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
            
            assertThat(predicate.test(unidadeIntermediaria)).isFalse();
        }
    }

    @Nested
    @DisplayName("Condição 2: Mapa Vigente")
    class MapaVigenteTestes {
        
        @Test
        @DisplayName("Se mapa NÃO requerido: deve aceitar unidade sem mapa")
        void semMapaRequerido_DeveAceitarUnidadeSemMapa() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(false, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeSemMapa = new Unidade();
            unidadeSemMapa.setCodigo(1L);
            unidadeSemMapa.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeSemMapa)).isTrue();
        }
        
        @Test
        @DisplayName("Se mapa REQUERIDO e unidade TEM mapa: deve aceitar")
        void comMapaRequerido_UnidadeComMapa_DeveAceitar() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L, 2L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(true, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeComMapa = new Unidade();
            unidadeComMapa.setCodigo(1L);
            unidadeComMapa.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeComMapa)).isTrue();
        }
        
        @Test
        @DisplayName("Se mapa REQUERIDO e unidade NÃO TEM mapa: deve rejeitar")
        void comMapaRequerido_UnidadeSemMapa_DeveRejeitar() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(2L, 3L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(true, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeSemMapa = new Unidade();
            unidadeSemMapa.setCodigo(1L);
            unidadeSemMapa.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeSemMapa)).isFalse();
        }
    }

    @Nested
    @DisplayName("Condição 3: Unidades Bloqueadas")
    class UnidadesBloqueadasTestes {
        
        @Test
        @DisplayName("Deve aceitar unidade NÃO bloqueada")
        void deveAceitarUnidadeNaoBloqueada() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            Set<Long> unidadesBloqueadas = new HashSet<>();
            unidadesBloqueadas.add(2L);
            unidadesBloqueadas.add(3L);
            
            // Act
            facade.buscarArvoreComElegibilidade(false, unidadesBloqueadas);
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeNaoBloqueada = new Unidade();
            unidadeNaoBloqueada.setCodigo(1L);
            unidadeNaoBloqueada.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeNaoBloqueada)).isTrue();
        }
        
        @Test
        @DisplayName("Deve rejeitar unidade bloqueada")
        void deveRejeitarUnidadeBloqueada() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            Set<Long> unidadesBloqueadas = new HashSet<>();
            unidadesBloqueadas.add(1L);
            unidadesBloqueadas.add(2L);
            
            // Act
            facade.buscarArvoreComElegibilidade(false, unidadesBloqueadas);
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidadeBloqueada = new Unidade();
            unidadeBloqueada.setCodigo(1L);
            unidadeBloqueada.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidadeBloqueada)).isFalse();
        }
    }

    @Nested
    @DisplayName("Combinações de Condições")
    class CombinacoesCondicoes {
        
        @Test
        @DisplayName("Todas condições verdadeiras: mapa requerido + tem mapa + não bloqueada + FINAL")
        void todasCondicoesVerdadeiras() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(true, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidade)).isTrue();
        }
        
        @Test
        @DisplayName("Unidade FINAL + mapa requerido + sem mapa + não bloqueada = FALSO")
        void finalComMapaRequeridoSemMapa() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(2L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(true, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidade)).isFalse();
        }
        
        @Test
        @DisplayName("Unidade FINAL + sem mapa requerido + bloqueada = FALSO")
        void finalSemMapaRequeridoMasBloqueada() {
            // Arrange
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            Set<Long> bloqueadas = new HashSet<>();
            bloqueadas.add(1L);
            
            // Act
            facade.buscarArvoreComElegibilidade(false, bloqueadas);
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            
            assertThat(predicate.test(unidade)).isFalse();
        }
        
        @Test
        @DisplayName("Unidade INTERMEDIARIA (todas outras condições OK) = FALSO")
        void intermediariaSempreRejeitada() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(Collections.emptyList());
            
            // Act
            facade.buscarArvoreComElegibilidade(true, Collections.emptySet());
            
            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(predicateCaptor.capture());
            Predicate<Unidade> predicate = predicateCaptor.getValue();
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setTipo(TipoUnidade.INTERMEDIARIA);
            
            assertThat(predicate.test(unidade)).isFalse();
        }
    }
}
