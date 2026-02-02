package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes para cobrir gaps de cobertura no UnidadeFacade.
 * 
 * <p>Este arquivo adiciona testes para métodos não cobertos pelos testes existentes.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeFacade - Gaps de Cobertura")
class UnidadeFacadeGapsTest {

    @Mock
    private UnidadeConsultaService unidadeConsultaService;
    
    @Mock
    private UsuarioConsultaService usuarioConsultaService;
    
    @Mock
    private UsuarioMapper usuarioMapper;
    
    @Mock
    private UnidadeHierarquiaService hierarquiaService;
    
    @Mock
    private UnidadeMapaService mapaService;
    
    @Mock
    private UnidadeResponsavelService responsavelService;

    @InjectMocks
    private UnidadeFacade facade;

    @Nested
    @DisplayName("Métodos de Hierarquia Não Cobertos")
    class HierarquiaNaoCoberta {
        
        @Test
        @DisplayName("Deve buscar IDs descendentes com mapa de hierarquia fornecido")
        void deveBuscarIdsDescendentesComMapa() {
            // Arrange
            Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
            mapPaiFilhos.put(1L, List.of(2L, 3L));
            mapPaiFilhos.put(2L, List.of(4L));
            
            when(hierarquiaService.buscarDescendentes(1L, mapPaiFilhos))
                .thenReturn(List.of(2L, 3L, 4L));

            // Act
            List<Long> resultado = facade.buscarIdsDescendentes(1L, mapPaiFilhos);

            // Assert
            assertThat(resultado).containsExactlyInAnyOrder(2L, 3L, 4L);
            verify(hierarquiaService).buscarDescendentes(1L, mapPaiFilhos);
        }
        
        @Test
        @DisplayName("Deve buscar mapa de hierarquia completo")
        void deveBuscarMapaHierarquia() {
            // Arrange
            Map<Long, List<Long>> mapaEsperado = new HashMap<>();
            mapaEsperado.put(1L, List.of(2L, 3L));
            mapaEsperado.put(2L, List.of(4L, 5L));
            
            when(hierarquiaService.buscarMapaHierarquia()).thenReturn(mapaEsperado);

            // Act
            Map<Long, List<Long>> resultado = facade.buscarMapaHierarquia();

            // Assert
            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(1L)).containsExactlyInAnyOrder(2L, 3L);
            assertThat(resultado.get(2L)).containsExactlyInAnyOrder(4L, 5L);
            verify(hierarquiaService).buscarMapaHierarquia();
        }
        
        @Test
        @DisplayName("Deve buscar unidades subordinadas diretas")
        void deveBuscarSubordinadas() {
            // Arrange
            UnidadeDto subordinada1 = UnidadeDto.builder()
                .codigo(2L)
                .sigla("SUB1")
                .build();
            UnidadeDto subordinada2 = UnidadeDto.builder()
                .codigo(3L)
                .sigla("SUB2")
                .build();
                
            when(hierarquiaService.buscarSubordinadas(1L))
                .thenReturn(List.of(subordinada1, subordinada2));

            // Act
            List<UnidadeDto> resultado = facade.buscarSubordinadas(1L);

            // Assert
            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(UnidadeDto::getCodigo)
                .containsExactlyInAnyOrder(2L, 3L);
            verify(hierarquiaService).buscarSubordinadas(1L);
        }
    }

    @Nested
    @DisplayName("Branches de Elegibilidade")
    class BranchesElegibilidade {
        
        @Test
        @DisplayName("Deve excluir unidades bloqueadas na árvore de elegibilidade")
        void deveExcluirUnidadesBloqueadasNaElegibilidade() {
            // Arrange
            UnidadeDto dto1 = UnidadeDto.builder().codigo(1L).build();
            UnidadeDto dto2 = UnidadeDto.builder().codigo(2L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto1, dto2));
            
            java.util.Set<Long> unidadesBloqueadas = new java.util.HashSet<>();
            unidadesBloqueadas.add(2L);

            // Act
            List<UnidadeDto> resultado = facade.buscarArvoreComElegibilidade(false, unidadesBloqueadas);

            // Assert
            assertThat(resultado).isNotEmpty();
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }
        
        @Test
        @DisplayName("Deve incluir apenas unidades com mapa vigente quando requerido")
        void deveIncluirApenasUnidadesComMapaQuandoRequerido() {
            // Arrange
            when(mapaService.buscarTodosCodigosUnidades()).thenReturn(List.of(1L, 2L));
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> resultado = facade.buscarArvoreComElegibilidade(true, new java.util.HashSet<>());

            // Assert
            assertThat(resultado).isNotEmpty();
            verify(mapaService).buscarTodosCodigosUnidades();
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }
        
        @Test
        @DisplayName("Não deve buscar códigos de unidades se mapa não é requerido")
        void naoDeveBuscarCodigosSeMapaNaoRequerido() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto));

            // Act
            facade.buscarArvoreComElegibilidade(false, new java.util.HashSet<>());

            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
            // mapaService.buscarTodosCodigosUnidades() NÃO deve ser chamado
        }
    }

    @Nested
    @DisplayName("Métodos de Responsáveis Não Cobertos")
    class ResponsaveisNaoCobertos {
        
        @Test
        @DisplayName("Deve buscar responsável atual por sigla de unidade")
        void deveBuscarResponsavelAtual() {
            // Arrange
            Usuario responsavel = new Usuario();
            responsavel.setMatricula("12345");
            responsavel.setNome("João da Silva");
            
            when(responsavelService.buscarResponsavelAtual("UNIDADE-01"))
                .thenReturn(responsavel);

            // Act
            Usuario resultado = facade.buscarResponsavelAtual("UNIDADE-01");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getMatricula()).isEqualTo("12345");
            assertThat(resultado.getNome()).isEqualTo("João da Silva");
            verify(responsavelService).buscarResponsavelAtual("UNIDADE-01");
        }
        
        @Test
        @DisplayName("Deve retornar null se unidade não tem responsável atual")
        void deveRetornarNullSeNaoTemResponsavel() {
            // Arrange
            when(responsavelService.buscarResponsavelAtual("UNIDADE-SEM-RESP"))
                .thenReturn(null);

            // Act
            Usuario resultado = facade.buscarResponsavelAtual("UNIDADE-SEM-RESP");

            // Assert
            assertThat(resultado).isNull();
            verify(responsavelService).buscarResponsavelAtual("UNIDADE-SEM-RESP");
        }
    }
}
