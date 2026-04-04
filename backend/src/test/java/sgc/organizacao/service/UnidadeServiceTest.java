package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.mapa.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeService")
@SuppressWarnings("NullAway.Init")
class UnidadeServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeService service;

    @Test
    @DisplayName("buscarPorCodigo - Sucesso")
    void buscarPorCodigo() {
        Unidade u = new Unidade();
        when(unidadeRepo.buscarPorCodigoComResponsavel(1L)).thenReturn(Optional.of(u));

        Unidade result = service.buscarPorCodigo(1L);

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarPorSigla - Sucesso")
    void buscarPorSigla() {
        Unidade u = new Unidade();
        when(unidadeRepo.buscarPorSiglaComResponsavel("U1")).thenReturn(Optional.of(u));

        Unidade result = service.buscarPorSigla("U1");

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarEntidadesPorIds - Sucesso")
    void buscarPorCodigos() {
        List<Unidade> lista = List.of(new Unidade());
        when(unidadeRepo.findAllById(any())).thenReturn(lista);

        List<Unidade> result = service.buscarPorCodigos(List.of(1L));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("buscarSiglasPorCodigos - Sucesso")
    void buscarSiglasPorCodigos() {
        List<String> lista = List.of("U1");
        when(unidadeRepo.buscarSiglasPorCodigos(any())).thenReturn(lista);

        List<String> result = service.buscarSiglasPorCodigos(List.of(1L));

        assertThat(result).containsExactly("U1");
    }

    @Test
    @DisplayName("buscarSiglaPorCodigo - Sucesso")
    void buscarSiglaPorCodigo() {
        when(unidadeRepo.buscarSiglaPorCodigo(1L)).thenReturn(Optional.of("U1"));

        String result = service.buscarSiglaPorCodigo(1L);

        assertThat(result).isEqualTo("U1");
    }

    @Test
    @DisplayName("buscarPorCodigoComSuperior - Sucesso")
    void buscarPorCodigoComSuperior() {
        Unidade unidade = new Unidade();
        when(unidadeRepo.buscarPorCodigoComSuperior(1L)).thenReturn(Optional.of(unidade));

        Unidade result = service.buscarPorCodigoComSuperior(1L);

        assertThat(result).isSameAs(unidade);
    }

    @Test
    @DisplayName("verificarMapaVigente - Sucesso")
    void temMapaVigente() {
        when(unidadeMapaRepo.existsById(1L)).thenReturn(true);
        assertThat(service.temMapaVigente(1L)).isTrue();
    }

    @Test
    @DisplayName("buscarTodosCodigosUnidadesComMapa - Sucesso")
    void buscarTodosCodigosUnidadesComMapa() {
        when(unidadeMapaRepo.listarTodosCodigosUnidade()).thenReturn(List.of(1L, 2L));
        assertThat(service.buscarTodosCodigosUnidadesComMapa()).hasSize(2);
    }

    @Test
    @DisplayName("buscarReferenciaMapaVigente - Retorna vazio quando unidade não tem mapa vigente")
    void buscarReferenciaMapaVigente_semMapaVigente() {
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());

        Optional<MapaVigenteReferenciaDto> resultado = service.buscarReferenciaMapaVigente(1L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarReferenciaMapaVigente - Retorna vazio quando mapa não tem subprocesso")
    void buscarReferenciaMapaVigente_mapaSemSubprocesso() {
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(new Mapa());
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(unidadeMapa));

        Optional<MapaVigenteReferenciaDto> resultado = service.buscarReferenciaMapaVigente(1L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarReferenciaMapaVigente - Retorna referência do processo e subprocesso")
    void buscarReferenciaMapaVigente_retornaReferencia() {
        Processo processo = Processo.builder().build();
        processo.setCodigo(10L);

        Subprocesso subprocesso = Subprocesso.builder().build();
        subprocesso.setCodigo(20L);
        subprocesso.setProcesso(processo);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);

        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(mapa);

        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(unidadeMapa));

        Optional<MapaVigenteReferenciaDto> resultado = service.buscarReferenciaMapaVigente(1L);

        assertThat(resultado)
                .isPresent()
                .get()
                .extracting(MapaVigenteReferenciaDto::codProcesso, MapaVigenteReferenciaDto::codSubprocesso)
                .containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("definirMapaVigente - Criar novo")
    void definirMapaVigente_Novo() {
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());
        Mapa mapa = new Mapa();

        service.definirMapaVigente(1L, mapa);

        verify(unidadeMapaRepo).save(argThat(um -> um.getUnidadeCodigoPersistido().equals(1L) && um.getMapaVigente().equals(mapa)));
    }

    @Test
    @DisplayName("definirMapaVigente - Atualizar existente")
    void definirMapaVigente_Existente() {
        UnidadeMapa existente = new UnidadeMapa();
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(existente));
        Mapa mapa = new Mapa();

        service.definirMapaVigente(1L, mapa);

        assertThat(existente.getMapaVigente()).isSameAs(mapa);
        verify(unidadeMapaRepo).save(existente);
    }
}
