package sgc.unidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.api.SgrhMapper;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.unidade.api.CriarAtribuicaoTemporariaReq;
import sgc.unidade.internal.model.AtribuicaoTemporariaRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeMapaRepo;
import sgc.unidade.internal.model.UnidadeRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço UnidadeService")
class UnidadeServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private SgrhMapper sgrhMapper;

    @InjectMocks
    private UnidadeService service;

    @Nested
    @DisplayName("Busca de Unidades e Hierarquia")
    class BuscaUnidades {

        @Test
        @DisplayName("Deve retornar hierarquia ao buscar todas as unidades")
        void deveRetornarHierarquiaAoBuscarTodasUnidades() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarTodasUnidades();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar unidade por sigla")
        void deveBuscarPorSigla() {
            // Arrange
            when(unidadeRepo.findBySigla("U1")).thenReturn(Optional.of(new Unidade()));
            when(sgrhMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

            // Act & Assert
            assertThat(service.buscarPorSigla("U1")).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarPorCodigo() {
            // Arrange
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
            when(sgrhMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

            // Act & Assert
            assertThat(service.buscarPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar árvore de unidades")
        void deveBuscarArvore() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act & Assert
            assertThat(service.buscarArvore(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar siglas subordinadas")
        void deveBuscarSiglasSubordinadas() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            u1.setSigla("U1");
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).sigla("U1").build());

            // Act
            List<String> result = service.buscarSiglasSubordinadas("U1");

            // Assert
            assertThat(result).contains("U1");
        }

        @Test
        @DisplayName("Deve buscar sigla da unidade superior")
        void deveBuscarSiglaSuperior() {
            // Arrange
            Unidade u = new Unidade();
            Unidade pai = new Unidade();
            pai.setSigla("PAI");
            u.setUnidadeSuperior(pai);

            when(unidadeRepo.findBySigla("FILHO")).thenReturn(Optional.of(u));

            // Act & Assert
            assertThat(service.buscarSiglaSuperior("FILHO")).isEqualTo("PAI");
        }
    }

    @Nested
    @DisplayName("Elegibilidade e Mapas")
    class ElegibilidadeMapas {

        @Test
        @DisplayName("Deve buscar árvore com elegibilidade")
        void deveBuscarArvoreComElegibilidade() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                    .thenReturn(new ArrayList<>());
            when(sgrhMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar árvore com elegibilidade considerando mapa vigente")
        void deveBuscarArvoreComElegibilidadeComMapa() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of(1L));
            when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                    .thenReturn(new ArrayList<>());
            when(sgrhMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Unidade sem mapa não deve ser elegível para REVISÃO")
        void deveSerIneligivelParaRevisaoSeSemMapa() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));

            // Unidade 1 não está na lista de mapas vigentes
            when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of(2L));
            when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                    .thenReturn(new ArrayList<>());

            // Mock deve ser chamado com elegivel=false
            when(sgrhMapper.toUnidadeDto(u1, false)).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(sgrhMapper).toUnidadeDto(u1, false);
        }

        @Test
        @DisplayName("Unidade em processo ativo não deve ser elegível")
        void deveSerIneligivelSeEmProcessoAtivo() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));

            // Unidade 1 está em processo ativo
            when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                    .thenReturn(new ArrayList<>(List.of(1L)));

            // Mock deve ser chamado com elegivel=false
            when(sgrhMapper.toUnidadeDto(u1, false)).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(sgrhMapper).toUnidadeDto(u1, false);
        }

        @Test
        @DisplayName("Deve verificar se tem mapa vigente")
        void deveVerificarMapaVigente() {
            // Arrange
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(new sgc.mapa.model.Mapa()));

            // Act & Assert
            assertThat(service.verificarMapaVigente(1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("Gestão de Atribuições e Usuários")
    class GestaoAtribuicoesUsuarios {

        @Test
        @DisplayName("Deve criar atribuição temporária com sucesso")
        void deveCriarAtribuicaoTemporariaComSucesso() {
            // Arrange
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario()));

            CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                    "123", java.time.LocalDate.now(), "Justificativa");

            // Act
            service.criarAtribuicaoTemporaria(1L, req);

            // Assert
            verify(atribuicaoTemporariaRepo).save(any());
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade")
        void deveBuscarUsuariosPorUnidade() {
            // Arrange
            when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(List.of(new Usuario()));
            when(sgrhMapper.toUsuarioDto(any())).thenReturn(sgc.sgrh.api.UsuarioDto.builder().build());

            // Act & Assert
            assertThat(service.buscarUsuariosPorUnidade(1L)).hasSize(1);
        }
    }
}
