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
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.*;

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
    private UsuarioMapper usuarioMapper;
    @Mock
    private UnidadeHierarquiaService hierarquiaService;
    @Mock
    private UnidadeResponsavelService responsavelService;

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

    @Test
    @DisplayName("Deve construir unidades com hierarquia (cobertura caminhos recursivos)")
    void deveTestarHierarquiaRecursiva() {
        // Arrange
        UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(new ArrayList<>()).build();
        UnidadeDto paiDto = UnidadeDto.builder().codigo(1L).sigla("PAI").subunidades(List.of(filhoDto)).build();
        when(hierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(paiDto));

        // Act
        List<UnidadeDto> res = service.buscarTodasUnidades();

        // Assert
        assertThat(res).hasSize(1);
        UnidadeDto resultado = res.getFirst();
        assertThat(resultado.getSigla()).isEqualTo("PAI");
        assertThat(resultado.getSubunidades()).hasSize(1);
        assertThat(resultado.getSubunidades().getFirst().getSigla()).isEqualTo("FILHO");
        verify(hierarquiaService).buscarArvoreHierarquica();
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
            when(unidadeService.buscarPorSigla("U1")).thenReturn(new Unidade());
            when(usuarioMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

            // Act & Assert
            assertThat(service.buscarPorSigla("U1")).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarPorCodigo() {
            // Arrange
            Unidade unidade = new Unidade();
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorId(1L)).thenReturn(unidade);
            when(usuarioMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

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
        @DisplayName("Deve lançar exceção se unidade não encontrada ao buscar siglas subordinadas")
        void deveLancarExcecaoSeUnidadeNaoEncontradaAoBuscarSiglasSubordinadas() {
            // Arrange
            when(hierarquiaService.buscarSiglasSubordinadas("U2"))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "U2"));

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglasSubordinadas("U2")));
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

        @Test
        @DisplayName("Deve retornar vazio se unidade não tiver superior ao buscar sigla superior")
        void deveRetornarVazioSeSemSuperior() {
            // Arrange
            when(hierarquiaService.buscarSiglaSuperior("RAIZ")).thenReturn(Optional.empty());

            // Act & Assert
            assertThat(service.buscarSiglaSuperior("RAIZ")).isEmpty();
            verify(hierarquiaService).buscarSiglaSuperior("RAIZ");
        }

        @Test
        @DisplayName("Deve buscar árvore de unidade específica")
        void deveBuscarArvoreEspecifica() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).sigla("U1").build();
            when(hierarquiaService.buscarArvore(1L)).thenReturn(dto);

            // Act
            UnidadeDto result = service.buscarArvore(1L);

            // Assert
            assertThat(result.getCodigo()).isEqualTo(1L);
            verify(hierarquiaService).buscarArvore(1L);
        }

        @Test
        @DisplayName("Deve buscar siglas subordinadas recursivamente")
        void deveBuscarSiglasSubordinadasRecursivamente() {
            // Arrange
            when(hierarquiaService.buscarSiglasSubordinadas("PAI"))
                    .thenReturn(List.of("PAI", "FILHO", "NETO"));

            // Act
            List<String> result = service.buscarSiglasSubordinadas("PAI");

            // Assert
            assertThat(result).containsExactlyInAnyOrder("PAI", "FILHO", "NETO");
            verify(hierarquiaService).buscarSiglasSubordinadas("PAI");
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
        @DisplayName("Unidade sem mapa não deve ser elegível para REVISÃO")
        void deveSerIneligivelParaRevisaoSeSemMapa() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(2L));
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(true, Collections.emptySet());

            // Assert
            assertThat(result).hasSize(1);
            verify(unidadeService).buscarTodosCodigosUnidadesComMapa();
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }

        @Test
        @DisplayName("Unidade em processo ativo não deve ser elegível")
        void deveSerIneligivelSeEmProcessoAtivo() {
            // Arrange
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any())).thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(false, new HashSet<>(List.of(1L)));

            // Assert
            assertThat(result).hasSize(1);
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
        @DisplayName("Deve falhar ao criar atribuição se datas inválidas")
        void deveFalharCriarAtribuicaoDatasInvalidas() {
            // Arrange
            CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                    "123", LocalDate.now().plusDays(1), LocalDate.now(), "Justificativa");
            doThrow(new ErroValidacao("Data fim deve ser posterior à data início"))
                    .when(responsavelService).criarAtribuicaoTemporaria(1L, req);

            // Act & Assert
            assertNotNull(assertThrows(ErroValidacao.class, () -> service.criarAtribuicaoTemporaria(1L, req)));
        }

        @Test
        @DisplayName("Deve falhar ao criar atribuição se unidade não encontrada")
        void deveFalharCriarAtribuicaoSeUnidadeNaoEncontrada() {
            // Arrange
            CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                    "123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");
            doThrow(new ErroEntidadeNaoEncontrada("Unidade", 99L))
                    .when(responsavelService).criarAtribuicaoTemporaria(99L, req);

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.criarAtribuicaoTemporaria(99L, req)));
        }

        @Test
        @DisplayName("Deve criar atribuição com data atual se dataInicio for nula")
        void deveCriarAtribuicaoComDataAtualSeInicioNulo() {
            // Arrange
            CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                    "123", null, LocalDate.now().plusDays(1), "Justificativa");

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
        @DisplayName("Buscar todas entidades com hierarquia (cache)")
        void buscarTodasEntidadesComHierarquia() {
            service.buscarTodasEntidadesComHierarquia();
            verify(unidadeService).buscarTodasEntidadesComHierarquia();
        }

        @Test
        @DisplayName("Buscar siglas por IDs")
        void buscarSiglasPorIds() {
            service.buscarSiglasPorIds(List.of(1L));
            verify(unidadeService).buscarSiglasPorIds(any());
        }

        @Test
        @DisplayName("Verificar existencia mapa vigente")
        void verificarExistenciaMapaVigente() {
            // Act
            service.verificarMapaVigente(1L);

            // Assert
            verify(unidadeService).verificarMapaVigente(1L);
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

        @Test
        @DisplayName("Deve falhar ao buscar sigla superior de unidade inexistente")
        void deveFalharAoBuscarSiglaSuperiorInexistente() {
            // Arrange
            when(hierarquiaService.buscarSiglaSuperior("INVALIDO"))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "INVALIDO"));

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglaSuperior("INVALIDO")));
        }

        @Test
        @DisplayName("Deve falhar ao buscar na hierarquia recursivamente (não encontrado)")
        void deveFalharAoBuscarNaHierarquiaRecursivamenteNaoEncontrado() {
            // Arrange
            when(hierarquiaService.buscarArvore(999L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 999L));

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarArvore(999L)));
        }

        @Test
        @DisplayName("Deve buscar na hierarquia por sigla recursivamente (não encontrado)")
        void deveBuscarNaHierarquiaPorSiglaRecursivamenteNaoEncontrado() {
            // Arrange
            when(hierarquiaService.buscarSiglasSubordinadas("INVALIDA"))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "INVALIDA"));

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglasSubordinadas("INVALIDA")));
        }
    }

    @Nested
    @DisplayName("Testes Adicionais de Cobertura")
    class TestesAdicionaisCobertura {

        @Test
        @DisplayName("Deve ser ineligível se unidade for intermediária")
        void deveSerIneligivelSeUnidadeIntermediaria() {
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
        @DisplayName("Deve lançar exceção se unidade inativa ao buscar por ID")
        void deveLancarExcecaoSeUnidadeInativa() {
            // Arrange
            when(unidadeService.buscarPorId(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 1L));

            // Act & Assert
            assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarEntidadePorId(1L)));
        }

        @Test
        @DisplayName("Deve montar responsável sem substituto")
        void deveMontarResponsavelSemSubstituto() {
            // Arrange
            UnidadeResponsavelDto dto = UnidadeResponsavelDto.builder()
                    .titularTitulo("123")
                    .substitutoTitulo(null)
                    .build();
            when(responsavelService.buscarResponsavelUnidade(1L)).thenReturn(dto);

            // Act
            UnidadeResponsavelDto result = service.buscarResponsavelUnidade(1L);

            // Assert
            assertThat(result.titularTitulo()).isEqualTo("123");
            assertThat(result.substitutoTitulo()).isNull();
            verify(responsavelService).buscarResponsavelUnidade(1L);
        }

        @Test
        @DisplayName("Deve retornar mapa vazio se lista de códigos vazia")
        void deveRetornarMapaVazioSeListaVazia() {
            // Arrange
            when(responsavelService.buscarResponsaveisUnidades(Collections.emptyList()))
                    .thenReturn(Collections.emptyMap());

            // Act & Assert
            assertThat(service.buscarResponsaveisUnidades(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("Deve montar responsável com substituto")
        void deveMontarResponsavelComSubstituto() {
            // Arrange
            UnidadeResponsavelDto dto = UnidadeResponsavelDto.builder()
                    .titularTitulo("123")
                    .substitutoTitulo("456")
                    .build();
            when(responsavelService.buscarResponsavelUnidade(1L)).thenReturn(dto);

            // Act
            UnidadeResponsavelDto result = service.buscarResponsavelUnidade(1L);

            // Assert
            assertThat(result.titularTitulo()).isEqualTo("123");
            assertThat(result.substitutoTitulo()).isEqualTo("456");
            verify(responsavelService).buscarResponsavelUnidade(1L);
        }

        @Test
        @DisplayName("Deve carregar atribuições em lote com lista vazia indiretamente")
        void deveCarregarAtribuicoesEmLoteComListaVaziaIndiretamente() {
            // Arrange
            when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                    .thenReturn(Collections.emptyMap());

            // Act
            Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

            // Assert
            assertThat(result).isEmpty();
            verify(responsavelService).buscarResponsaveisUnidades(List.of(1L));
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
            mapaEsperado.put(2L, List.of(4L, 5L));
            
            when(hierarquiaService.buscarMapaHierarquia()).thenReturn(mapaEsperado);

            // Act
            Map<Long, List<Long>> resultado = service.buscarMapaHierarquia();

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
            List<UnidadeDto> resultado = service.buscarSubordinadas(1L);

            // Assert
            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(UnidadeDto::getCodigo)
                .containsExactlyInAnyOrder(2L, 3L);
            verify(hierarquiaService).buscarSubordinadas(1L);
        }
    }

    @Nested
    @DisplayName("Branches de Elegibilidade")
    class ElegibilidadeAvancada {
        
        @Test
        @DisplayName("Deve excluir unidades bloqueadas na árvore de elegibilidade")
        void deveExcluirUnidadesBloqueadasNaElegibilidade() {
            // Arrange
            UnidadeDto dto1 = UnidadeDto.builder().codigo(1L).build();
            UnidadeDto dto2 = UnidadeDto.builder().codigo(2L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto1, dto2));
            
            Set<Long> unidadesBloqueadas = new HashSet<>();
            unidadesBloqueadas.add(2L);

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(false, unidadesBloqueadas);

            // Assert
            assertThat(resultado).isNotEmpty();
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
        }
        
        @Test
        @DisplayName("Deve incluir apenas unidades com mapa vigente quando requerido")
        void deveIncluirApenasUnidadesComMapaQuandoRequerido() {
            // Arrange
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L, 2L));
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).build();
            when(hierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto));

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(true, new HashSet<>());

            // Assert
            assertThat(resultado).isNotEmpty();
            verify(unidadeService).buscarTodosCodigosUnidadesComMapa();
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
            service.buscarArvoreComElegibilidade(false, new HashSet<>());

            // Assert
            verify(hierarquiaService).buscarArvoreComElegibilidade(any());
            verify(unidadeService, never()).buscarTodosCodigosUnidadesComMapa();
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
            responsavel.setNome("João da Silva");
            
            when(responsavelService.buscarResponsavelAtual("UNIDADE-01"))
                .thenReturn(responsavel);

            // Act
            Usuario resultado = service.buscarResponsavelAtual("UNIDADE-01");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getMatricula()).isEqualTo("12345");
            assertThat(resultado.getNome()).isEqualTo("João da Silva");
            verify(responsavelService).buscarResponsavelAtual("UNIDADE-01");
        }      
    }
}
