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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.SgrhMapper;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.dto.CriarAtribuicaoTemporariaReq;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeMapaRepo;
import sgc.unidade.model.UnidadeRepo;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("buscarTodasUnidades deve retornar hierarquia")
    void buscarTodasUnidades() {
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).build());

        List<UnidadeDto> result = service.buscarTodasUnidades();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade")
    void buscarArvoreComElegibilidade() {
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                .thenReturn(new ArrayList<>());
        when(sgrhMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

        List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade com mapa vigente")
    void buscarArvoreComElegibilidadeComMapa() {
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of(1L));
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(any(), any()))
                .thenReturn(new ArrayList<>());
        when(sgrhMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(UnidadeDto.builder().codigo(1L).build());

        List<UnidadeDto> result = service.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria sucesso")
    void criarAtribuicaoTemporaria() {
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario()));

        CriarAtribuicaoTemporariaReq req = new CriarAtribuicaoTemporariaReq(
                "123", java.time.LocalDate.now(), "Justificativa");

        service.criarAtribuicaoTemporaria(1L, req);

        verify(atribuicaoTemporariaRepo).save(any());
    }

    @Test
    @DisplayName("verificarMapaVigente")
    void verificarMapaVigente() {
        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(new sgc.mapa.model.Mapa()));
        assertThat(service.verificarMapaVigente(1L)).isTrue();
    }

    @Test
    @DisplayName("buscarServidoresPorUnidade")
    void buscarServidoresPorUnidade() {
        when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(List.of(new Usuario()));
        when(sgrhMapper.toUsuarioDto(any())).thenReturn(sgc.sgrh.dto.UsuarioDto.builder().build());

        assertThat(service.buscarServidoresPorUnidade(1L)).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorSigla")
    void buscarPorSigla() {
        when(unidadeRepo.findBySigla("U1")).thenReturn(Optional.of(new Unidade()));
        when(sgrhMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

        assertThat(service.buscarPorSigla("U1")).isNotNull();
    }

    @Test
    @DisplayName("buscarPorCodigo")
    void buscarPorCodigo() {
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
        when(sgrhMapper.toUnidadeDto(any(), eq(false))).thenReturn(UnidadeDto.builder().build());

        assertThat(service.buscarPorCodigo(1L)).isNotNull();
    }

    @Test
    @DisplayName("buscarArvore")
    void buscarArvore() {
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).build());

        assertThat(service.buscarArvore(1L)).isNotNull();
    }

    @Test
    @DisplayName("buscarSiglasSubordinadas")
    void buscarSiglasSubordinadas() {
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        when(sgrhMapper.toUnidadeDto(any())).thenReturn(UnidadeDto.builder().codigo(1L).sigla("U1").build());

        List<String> result = service.buscarSiglasSubordinadas("U1");
        assertThat(result).contains("U1");
    }

    @Test
    @DisplayName("buscarSiglaSuperior")
    void buscarSiglaSuperior() {
        Unidade u = new Unidade();
        Unidade pai = new Unidade();
        pai.setSigla("PAI");
        u.setUnidadeSuperior(pai);

        when(unidadeRepo.findBySigla("FILHO")).thenReturn(Optional.of(u));

        assertThat(service.buscarSiglaSuperior("FILHO")).isEqualTo("PAI");
    }
}
