package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do Serviço UnidadeFacade")
class UnidadeFacadeTest {

    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private UnidadeHierarquiaService hierarquiaService;
    @Mock
    private ResponsavelUnidadeService responsavelService;

    @InjectMocks
    private UnidadeFacade service;

    @Test
    @DisplayName("Deve testar elegibilidade com unidades que possuem pai")
    void deveTestarElegibilidadeComPai() {
        // Arrange
        UnidadeDto dto = UnidadeDto.builder().codigo(2L).build();
        when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(List.of(dto));

        // Act
        service.buscarArvoreComElegibilidade(false, Collections.emptySet());

        // Assert
        verify(hierarquiaService).buscarArvoreComElegibilidade(any());
    }

    @Nested
    @DisplayName("Busca de Unidades e Hierarquia")
    class BuscaUnidades {

        @Test
        @DisplayName("Deve retornar hierarquia ao buscar todas as unidades")
        void deveRetornarHierarquiaAoBuscarTodasUnidades() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> result = service.buscarTodasUnidades();

            // Assert
            assertThat(result).hasSize(1);
            verify(hierarquiaService).buscarArvoreHierarquica();
        }

        @Test
        @DisplayName("Deve buscar unidade por sigla")
        void deveBuscarPorSigla() {
            // Arrange
            Unidade u = criarUnidade(1L, "U1");
            when(unidadeService.buscarPorSigla("U1")).thenReturn(u);

            // Act & Assert
            assertThat(service.buscarPorSigla("U1")).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarPorCodigo() {
            // Arrange
            Unidade unidade = criarUnidade(1L, "U1");
            when(unidadeService.buscarPorId(1L)).thenReturn(unidade);

            // Act & Assert
            assertThat(service.buscarPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar árvore de unidades")
        void deveBuscarArvore() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvore(1L)).thenReturn(dto);

            // Act & Assert
            assertThat(service.buscarArvore(1L)).isNotNull();
            verify(hierarquiaService).buscarArvore(1L);
        }

        @Test
        @DisplayName("Deve buscar siglas subordinadas")
        void deveBuscarSiglasSubordinadas() {
            // Arrange
            when(hierarquiaService.buscarSiglasSubordinadas("U1")).thenReturn(List.of("U1", "U1-1"));

            // Act
            List<String> result = service.buscarSiglasSubordinadas("U1");

            // Assert
            assertThat(result).contains("U1", "U1-1");
            verify(hierarquiaService).buscarSiglasSubordinadas("U1");
        }

        @Test
        @DisplayName("Deve buscar sigla da unidade superior")
        void deveBuscarSiglaSuperior() {
            // Arrange
            when(hierarquiaService.buscarSiglaSuperior("FILHO")).thenReturn(Optional.of("PAI"));

            // Act & Assert
            assertThat(service.buscarSiglaSuperior("FILHO")).contains("PAI");
            verify(hierarquiaService).buscarSiglaSuperior("FILHO");
        }
    }

    @Nested
    @DisplayName("Elegibilidade e Mapas")
    class ElegibilidadeMapas {
        @Test
        @DisplayName("Deve buscar árvore com elegibilidade")
        void deveBuscarArvoreComElegibilidade() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(false, Collections.emptySet());

            // Assert
            assertThat(result).hasSize(1);
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }

        @Test
        @DisplayName("Deve buscar árvore com elegibilidade considerando mapa vigente")
        void deveBuscarArvoreComElegibilidadeComMapa() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(true, Collections.emptySet());

            // Assert
            assertThat(result).hasSize(1);
            verify(unidadeService).buscarTodosCodigosUnidadesComMapa();
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }

        @Test
        @DisplayName("Deve verificar se tem mapa vigente")
        void deveVerificarMapaVigente() {
            // Arrange
            when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

            // Act & Assert
            assertThat(service.verificarMapaVigente(1L)).isTrue();
            verify(unidadeService).verificarMapaVigente(1L);
        }
    }

    @Nested
    @DisplayName("Gestão de Atribuições e Usuários")
    class GestaoAtribuicoesUsuarios {
        @Test
        @DisplayName("Deve criar atribuição temporária com sucesso")
        void deveCriarAtribuicaoTemporariaComSucesso() {
            // Arrange
            CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                    "123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");

            // Act
            service.criarAtribuicaoTemporaria(1L, req);

            // Assert
            verify(responsavelService).criarAtribuicaoTemporaria(1L, req);
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade")
        void deveBuscarUsuariosPorUnidade() {
            // Arrange
            when(usuarioService.buscarPorUnidadeLotacao(1L)).thenReturn(List.of(new Usuario()));

            // Act & Assert
            assertThat(service.buscarUsuariosPorUnidade(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar todas as atribuições")
        void deveBuscarTodasAtribuicoes() {
            // Arrange
            AtribuicaoTemporariaDto dto = AtribuicaoTemporariaDto.builder().build();
            when(responsavelService.buscarTodasAtribuicoes()).thenReturn(List.of(dto));

            // Act
            List<AtribuicaoTemporariaDto> result = service.buscarTodasAtribuicoes();

            // Assert
            assertThat(result).hasSize(1);
            verify(responsavelService).buscarTodasAtribuicoes();
        }
    }

    @Nested
    @DisplayName("Outros Metodos de Cobertura")
    class OutrosMetodos {
        @Test
        @DisplayName("Buscar IDs descendentes recursivamente")
        void buscarIdsDescendentes() {
            // Arrange
            when(hierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L, 3L));

            // Act
            List<Long> result = service.buscarIdsDescendentes(1L);

            // Assert
            assertThat(result).containsExactlyInAnyOrder(2L, 3L);
            verify(hierarquiaService).buscarIdsDescendentes(1L);
        }

        @Test
        @DisplayName("Buscar entidades por IDs")
        void buscarEntidadesPorIds() {
            service.buscarEntidadesPorIds(List.of(1L));
            verify(unidadeService).buscarEntidadesPorIds(any());
        }

        @Test
        @DisplayName("Definir mapa vigente (criação/atualização)")
        void definirMapaVigente() {
            // Arrange
            Mapa mapa = new Mapa();

            // Act
            service.definirMapaVigente(1L, mapa);

            // Assert
            verify(unidadeService).definirMapaVigente(1L, mapa);
        }

        @Test
        @DisplayName("Deve falhar ao buscar entidade por ID inexistente")
        void deveFalharAoBuscarEntidadePorIdInexistente() {
            when(unidadeService.buscarPorId(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 999L));
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarEntidadePorId(999L)));
        }
    }

    @Nested
    @DisplayName("Hierarquia Avançada")
    class HierarquiaAvancada {
        
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
            List<Long> resultado = service.buscarIdsDescendentes(1L, mapPaiFilhos);

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
            
            when(hierarquiaService.buscarMapaHierarquia()).thenReturn(mapaEsperado);

            // Act
            Map<Long, List<Long>> resultado = service.buscarMapaHierarquia();

            // Assert
            assertThat(resultado).isEqualTo(mapaEsperado);
            verify(hierarquiaService).buscarMapaHierarquia();
        }
        
        @Test
        @DisplayName("Deve buscar unidades subordinadas diretas")
        void deveBuscarSubordinadas() {
            // Arrange
            UnidadeDto subordinada1 = UnidadeDto.builder()
                .codigo(2L)
                .build();
                
            when(hierarquiaService.buscarSubordinadas(1L))
                .thenReturn(List.of(subordinada1));

            // Act
            List<UnidadeDto> resultado = service.buscarSubordinadas(1L);

            // Assert
            assertThat(resultado).hasSize(1);
            verify(hierarquiaService).buscarSubordinadas(1L);
        }
    }

    @Nested
    @DisplayName("Responsáveis (Gaps)")
    class ResponsaveisAvancados {
        
        @Test
        @DisplayName("Deve buscar responsável atual por sigla de unidade")
        void deveBuscarResponsavelAtual() {
            // Arrange
            Usuario responsavel = new Usuario();
            responsavel.setMatricula("12345");
            
            when(responsavelService.buscarResponsavelAtual("UNIDADE-01"))
                .thenReturn(responsavel);

            // Act
            Usuario resultado = service.buscarResponsavelAtual("UNIDADE-01");

            // Assert
            assertThat(resultado).isEqualTo(responsavel);
            verify(responsavelService).buscarResponsavelAtual("UNIDADE-01");
        }      
    }

    // Métodos auxiliares
    private Unidade criarUnidade(Long codigo, String sigla) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome("Unidade Teste");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
    }
}
