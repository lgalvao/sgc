package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.service.MapaService;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaReq;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoConsultaService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço UnidadeService")
class UnidadeServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private MapaService mapaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private UsuarioMapper usuarioMapper;

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
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

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
            when(usuarioMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

            // Act & Assert
            assertThat(service.buscarPorSigla("U1")).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarPorCodigo() {
            // Arrange
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
            when(usuarioMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

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
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

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
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).sigla("U1").build());

            // Act
            List<String> result = service.buscarSiglasSubordinadas("U1");

            // Assert
            assertThat(result).contains("U1");
        }

        @Test
        @DisplayName("Deve lançar exceção se unidade não encontrada ao buscar siglas subordinadas")
        void deveLancarExcecaoSeUnidadeNaoEncontradaAoBuscarSiglasSubordinadas() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            u1.setSigla("U1");
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).sigla("U1").build());

            // Act & Assert
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglasSubordinadas("U2"));
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

        @Test
        @DisplayName("Deve retornar null se unidade não tiver superior ao buscar sigla superior")
        void deveRetornarNullSeSemSuperior() {
            // Arrange
            Unidade u = new Unidade();
            u.setUnidadeSuperior(null);

            when(unidadeRepo.findBySigla("RAIZ")).thenReturn(Optional.of(u));

            // Act & Assert
            assertThat(service.buscarSiglaSuperior("RAIZ")).isNull();
        }

        @Test
        @DisplayName("Deve buscar árvore de unidade específica")
        void deveBuscarArvoreEspecifica() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            u1.setSigla("U1");
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).sigla("U1").build());

            // Act
            UnidadeDto result = service.buscarArvore(1L);

            // Assert
            assertThat(result.getCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve buscar siglas subordinadas recursivamente")
        void deveBuscarSiglasSubordinadasRecursivamente() {
            // Arrange
            UnidadeDto netoDto = UnidadeDto.builder().codigo(3L).sigla("NETO").build();
            UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(List.of(netoDto)).build();
            UnidadeDto paiDto = UnidadeDto.builder().codigo(1L).sigla("PAI").subunidades(List.of(filhoDto)).build();

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(Collections.emptyList()); // Not used because we mock more levels
            // We need to mock the full hierarchy return from buscarTodasUnidades
            // which calls buscarArvoreHierarquica -> montarHierarquia
            
            Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setSigla("PAI");
            Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setSigla("FILHO"); u2.setUnidadeSuperior(u1);
            Unidade u3 = new Unidade(); u3.setCodigo(3L); u3.setSigla("NETO"); u3.setUnidadeSuperior(u2);
            
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1, u2, u3));
            when(usuarioMapper.toUnidadeDto(any(Unidade.class), anyBoolean())).thenReturn(paiDto, filhoDto, netoDto);

            // Act
            List<String> result = service.buscarSiglasSubordinadas("PAI");

            // Assert
            assertThat(result).containsExactlyInAnyOrder("PAI", "FILHO", "NETO");
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
            when(processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(any()))
                    .thenReturn(Collections.emptySet());
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

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
            when(processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(any()))
                    .thenReturn(Collections.emptySet());
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

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
            when(processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(any()))
                    .thenReturn(Collections.emptySet());

            // Mock deve ser chamado com elegivel=false
            when(usuarioMapper.toUnidadeDto(u1, false)).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(usuarioMapper).toUnidadeDto(u1, false);
        }

        @Test
        @DisplayName("Unidade em processo ativo não deve ser elegível")
        void deveSerIneligivelSeEmProcessoAtivo() {
            // Arrange
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));

            // Unidade 1 está em processo ativo
            when(processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(any()))
                    .thenReturn(new java.util.HashSet<>(List.of(1L)));

            // Mock deve ser chamado com elegivel=false
            when(usuarioMapper.toUnidadeDto(u1, false)).thenReturn(UnidadeDto.builder().codigo(1L).build());

            // Act
            List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(usuarioMapper).toUnidadeDto(u1, false);
        }

        @Test
        @DisplayName("Deve verificar se tem mapa vigente")
        void deveVerificarMapaVigente() {
            // Arrange
            when(mapaService.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(new sgc.mapa.model.Mapa()));

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
            when(usuarioService.buscarPorId("123")).thenReturn(new Usuario());

            CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                    "123", java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(1), "Justificativa");

            // Act
            service.criarAtribuicaoTemporaria(1L, req);

            // Assert
            verify(atribuicaoTemporariaRepo).save(any());
        }

        @Test
        @DisplayName("Deve falhar ao criar atribuição se datas inválidas")
        void deveFalharCriarAtribuicaoDatasInvalidas() {
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
            when(usuarioService.buscarPorId("123")).thenReturn(new Usuario());

            CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                    "123", java.time.LocalDate.now().plusDays(1), java.time.LocalDate.now(), "Justificativa");

            assertThrows(ErroValidacao.class, () -> service.criarAtribuicaoTemporaria(1L, req));
        }

        @Test
        @DisplayName("Deve falhar ao criar atribuição se unidade não encontrada")
        void deveFalharCriarAtribuicaoSeUnidadeNaoEncontrada() {
            when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

            CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                    "123", java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(1), "Justificativa");

            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class,
                    () -> service.criarAtribuicaoTemporaria(99L, req));
        }

        @Test
        @DisplayName("Deve criar atribuição com data atual se dataInicio for nula")
        void deveCriarAtribuicaoComDataAtualSeInicioNulo() {
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
            when(usuarioService.buscarPorId("123")).thenReturn(new Usuario());

            CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                    "123", null, java.time.LocalDate.now().plusDays(1), "Justificativa");

            service.criarAtribuicaoTemporaria(1L, req);

            verify(atribuicaoTemporariaRepo).save(argThat(at -> at.getDataInicio() != null));
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade")
        void deveBuscarUsuariosPorUnidade() {
            // Arrange
            when(usuarioService.buscarUsuariosPorUnidade(1L)).thenReturn(List.of(sgc.organizacao.dto.UsuarioDto.builder().build()));

            // Act & Assert
            assertThat(service.buscarUsuariosPorUnidade(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar todas as atribuições")
        void deveBuscarTodasAtribuicoes() {
            // Arrange
            when(atribuicaoTemporariaRepo.findAll()).thenReturn(List.of(new sgc.organizacao.model.AtribuicaoTemporaria()));
            when(usuarioMapper.toAtribuicaoTemporariaDto(any())).thenReturn(sgc.organizacao.dto.AtribuicaoTemporariaDto.builder().build());

            // Act
            List<AtribuicaoTemporariaDto> result = service.buscarTodasAtribuicoes();

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Outros Metodos de Cobertura")
    class OutrosMetodos {
        @Test
        @DisplayName("Buscar IDs descendentes recursivamente")
        void buscarIdsDescendentes() {
            Unidade pai = new Unidade(); pai.setCodigo(1L);
            Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setUnidadeSuperior(pai);
            Unidade neto = new Unidade(); neto.setCodigo(3L); neto.setUnidadeSuperior(filho);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(pai, filho, neto));

            List<Long> result = service.buscarIdsDescendentes(1L);
            assertThat(result).containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        @DisplayName("Listar subordinadas")
        void listarSubordinadas() {
            service.listarSubordinadas(1L);
            verify(unidadeRepo).findByUnidadeSuperiorCodigo(1L);
        }

        @Test
        @DisplayName("Buscar entidades por IDs")
        void buscarEntidadesPorIds() {
            service.buscarEntidadesPorIds(List.of(1L));
            verify(unidadeRepo).findAllById(any());
        }

        @Test
        @DisplayName("Buscar todas entidades com hierarquia (cache)")
        void buscarTodasEntidadesComHierarquia() {
            service.buscarTodasEntidadesComHierarquia();
            verify(unidadeRepo).findAllWithHierarquia();
        }

        @Test
        @DisplayName("Buscar siglas por IDs")
        void buscarSiglasPorIds() {
            service.buscarSiglasPorIds(List.of(1L));
            verify(unidadeRepo).findSiglasByCodigos(any());
        }

        @Test
        @DisplayName("Verificar existencia mapa vigente")
        void verificarExistenciaMapaVigente() {
            service.verificarExistenciaMapaVigente(1L);
            verify(unidadeMapaRepo).existsById(1L);
        }

        @Test
        @DisplayName("Definir mapa vigente (criação)")
        void definirMapaVigenteCriacao() {
            when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());
            service.definirMapaVigente(1L, new sgc.mapa.model.Mapa());
            verify(unidadeMapaRepo).save(any());
        }

        @Test
        @DisplayName("Definir mapa vigente (atualização)")
        void definirMapaVigenteAtualizacao() {
            when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(new sgc.organizacao.model.UnidadeMapa()));
            service.definirMapaVigente(1L, new sgc.mapa.model.Mapa());
            verify(unidadeMapaRepo).save(any());
        }

        @Test
        @DisplayName("Deve falhar ao buscar entidade por ID inexistente")
        void deveFalharAoBuscarEntidadePorIdInexistente() {
            when(unidadeRepo.findById(999L)).thenReturn(Optional.empty());
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> service.buscarEntidadePorId(999L));
        }

        @Test
        @DisplayName("Deve falhar ao buscar sigla superior de unidade inexistente")
        void deveFalharAoBuscarSiglaSuperiorInexistente() {
            when(unidadeRepo.findBySigla("INVALIDO")).thenReturn(Optional.empty());
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglaSuperior("INVALIDO"));
        }

        @Test
        @DisplayName("Deve buscar na hierarquia recursivamente (não encontrado)")
        void deveBuscarNaHierarquiaRecursivamenteNaoEncontrado() {
            UnidadeDto filho = UnidadeDto.builder().codigo(2L).subunidades(new ArrayList<>()).build();
            UnidadeDto pai = UnidadeDto.builder().codigo(1L).subunidades(List.of(filho)).build();
            
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioMapper.toUnidadeDto(u1)).thenReturn(pai);
            
            assertThat(service.buscarArvore(999L)).isNull();
        }

        @Test
        @DisplayName("Deve buscar na hierarquia por sigla recursivamente (não encontrado)")
        void deveBuscarNaHierarquiaPorSiglaRecursivamenteNaoEncontrado() {
            UnidadeDto filho = UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(new ArrayList<>()).build();
            UnidadeDto pai = UnidadeDto.builder().codigo(1L).sigla("PAI").subunidades(List.of(filho)).build();
            
            Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setSigla("PAI");
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioMapper.toUnidadeDto(u1)).thenReturn(pai);
            
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglasSubordinadas("INVALIDA"));
        }
    }

    @Test
    @DisplayName("Deve testar elegibilidade com unidades que possuem pai")
    void deveTestarElegibilidadeComPai() {
        Unidade pai = new Unidade(); pai.setCodigo(1L);
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setUnidadeSuperior(pai);
        
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(pai, filho));
        when(processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(null)).thenReturn(Collections.emptySet());
        
        when(usuarioMapper.toUnidadeDto(eq(pai), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());
        when(usuarioMapper.toUnidadeDto(eq(filho), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(2L).build());
        
        service.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);
        
        verify(usuarioMapper).toUnidadeDto(eq(filho), anyBoolean());
    }

    @Test
    @DisplayName("Deve construir unidades com hierarquia (cobertura caminhos recursivos)")
    void deveTestarHierarquiaRecursiva() {

        // Arrange
        Unidade pai = new Unidade();
        pai.setCodigo(1L);
        pai.setSigla("PAI");
        
        Unidade filho = new Unidade();
        filho.setCodigo(2L);
        filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);
        
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(pai, filho));
        when(usuarioMapper.toUnidadeDto(pai)).thenReturn(UnidadeDto.builder().codigo(1L).sigla("PAI").subunidades(new ArrayList<>()).build());
        when(usuarioMapper.toUnidadeDto(filho)).thenReturn(UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(new ArrayList<>()).build());
        
        // Act
        List<UnidadeDto> res = service.buscarTodasUnidades();
        
        // Assert
        assertThat(res).hasSize(1); // Somente o pai na raiz
        UnidadeDto paiDto = res.get(0);
        assertThat(paiDto.getSigla()).isEqualTo("PAI");
        assertThat(paiDto.getSubunidades()).hasSize(1);
        assertThat(paiDto.getSubunidades().get(0).getSigla()).isEqualTo("FILHO");
    }
}
