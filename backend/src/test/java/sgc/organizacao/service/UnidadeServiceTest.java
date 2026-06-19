package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.MapaVigenteReferenciaDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeService")
class UnidadeServiceTest {
    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private CacheOrganizacaoService cacheOrganizacaoService;

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
        when(unidadeRepo.buscarPorSiglaComSuperior("U1")).thenReturn(Optional.of(u));

        Unidade result = service.buscarPorSigla("U1");

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarPorSiglaComResponsavel - Sucesso")
    void buscarPorSiglaComResponsavel() {
        Unidade u = new Unidade();
        when(unidadeRepo.buscarPorSiglaComResponsavel("U1")).thenReturn(Optional.of(u));

        Unidade result = service.buscarPorSiglaComResponsavel("U1");

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarCodigoPorSigla - Sucesso")
    void buscarCodigoPorSigla() {
        when(unidadeRepo.buscarCodigoAtivoPorSigla("U1")).thenReturn(Optional.of(10L));

        Long result = service.buscarCodigoPorSigla("U1");

        assertThat(result).isEqualTo(10L);
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
        when(unidadeMapaRepo.buscarMapaVigenteComProcesso(1L)).thenReturn(Optional.of(new UnidadeMapa()));
        assertThat(service.temMapaVigente(1L)).isTrue();
    }

    @Test
    @DisplayName("buscarTodosCodigosUnidadesComMapa - Sucesso")
    void buscarTodosCodigosUnidadesComMapa() {
        when(unidadeMapaRepo.listarTodosCodigosUnidadeComMapaVigente()).thenReturn(List.of(1L, 2L));
        assertThat(service.buscarTodosCodigosUnidadesComMapa()).hasSize(2);
    }

    @Test
    @DisplayName("buscarReferenciaMapaVigente - Retorna vazio quando unidade não tem mapa vigente")
    void buscarReferenciaMapaVigente_semMapaVigente() {
        when(unidadeMapaRepo.buscarMapaVigenteComProcesso(1L)).thenReturn(Optional.empty());

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

        when(unidadeMapaRepo.buscarMapaVigenteComProcesso(1L)).thenReturn(Optional.of(unidadeMapa));

        Optional<MapaVigenteReferenciaDto> resultado = service.buscarReferenciaMapaVigente(1L);

        assertThat(resultado)
                .isPresent()
                .get()
                .extracting(MapaVigenteReferenciaDto::codProcesso, MapaVigenteReferenciaDto::codSubprocesso)
                .containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("buscarMapaVigente - Retorna mapa vigente da unidade")
    void buscarMapaVigente() {
        Mapa mapa = new Mapa();
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(mapa);
        when(unidadeMapaRepo.buscarMapaVigenteComProcesso(1L)).thenReturn(Optional.of(unidadeMapa));

        Optional<Mapa> resultado = service.buscarMapaVigente(1L);

        assertThat(resultado).containsSame(mapa);
    }

    @Test
    @DisplayName("buscarMapasPorUnidades - Usa consulta de mapas vigentes")
    void buscarMapasPorUnidades() {
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        when(unidadeMapaRepo.listarMapasVigentesPorUnidades(List.of(1L, 2L))).thenReturn(List.of(unidadeMapa));

        List<UnidadeMapa> resultado = service.buscarMapasPorUnidades(List.of(1L, 2L));

        assertThat(resultado).containsExactly(unidadeMapa);
    }

    @Test
    @DisplayName("definirMapaVigente - Criar novo")
    void definirMapaVigente_Novo() {
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());
        Mapa mapa = new Mapa();

        service.definirMapaVigente(1L, mapa);

        verify(unidadeMapaRepo).save(argThat(um ->
                Objects.equals(um.getUnidadeCodigoPersistido(), 1L) && Objects.equals(um.getMapaVigente(), mapa)));
        verify(cacheOrganizacaoService).invalidarAposCommit();
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
        verify(cacheOrganizacaoService).invalidarAposCommit();
    }

    @Test
    @DisplayName("buscarMapasPorUnidades - Retorna vazio se a lista de unidades for vazia")
    void buscarMapasPorUnidades_ListaVazia() {
        List<UnidadeMapa> resultado = service.buscarMapasPorUnidades(List.of());
        assertThat(resultado).isEmpty();
        verifyNoInteractions(unidadeMapaRepo);
    }

    @Test
    @DisplayName("definirMapasVigentesEmBloco - Retorna imediatamente se o mapa de parâmetros for vazio")
    void definirMapasVigentesEmBloco_MapaVazio() {
        service.definirMapasVigentesEmBloco(Map.of());
        verifyNoInteractions(unidadeMapaRepo, cacheOrganizacaoService);
    }

    @Test
    @DisplayName("definirMapasVigentesEmBloco - Salva os mapas vigentes informados em bloco e invalida cache")
    void definirMapasVigentesEmBloco_SalvaEmBloco() {
        Mapa mapa1 = new Mapa();
        Mapa mapa2 = new Mapa();
        Map<Long, Mapa> novosMapas = Map.of(10L, mapa1, 20L, mapa2);

        UnidadeMapa existente = new UnidadeMapa();
        existente.setUnidadeCodigo(10L);

        when(unidadeMapaRepo.findAllById(novosMapas.keySet())).thenReturn(List.of(existente));

        service.definirMapasVigentesEmBloco(novosMapas);

        verify(unidadeMapaRepo).saveAll(argThat(lista -> {
            List<UnidadeMapa> l = (List<UnidadeMapa>) lista;
            return l.size() == 2 &&
                    l.stream().anyMatch(um -> Objects.equals(um.getUnidadeCodigoPersistido(), 10L) && um.getMapaVigente() == mapa1) &&
                    l.stream().anyMatch(um -> Objects.equals(um.getUnidadeCodigoPersistido(), 20L) && um.getMapaVigente() == mapa2);
        }));
        verify(cacheOrganizacaoService).invalidarAposCommit();
    }

    @Test
    @DisplayName("buscarPorCodigo - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarPorCodigo_NaoEncontrada() {
        when(unidadeRepo.buscarPorCodigoComResponsavel(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorCodigo(999L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("buscarPorSigla - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarPorSigla_NaoEncontrada() {
        when(unidadeRepo.buscarPorSiglaComSuperior("XXX")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorSigla("XXX"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarPorSiglaComResponsavel - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarPorSiglaComResponsavel_NaoEncontrada() {
        when(unidadeRepo.buscarPorSiglaComResponsavel("XXX")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorSiglaComResponsavel("XXX"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarCodigoPorSigla - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarCodigoPorSigla_NaoEncontrada() {
        when(unidadeRepo.buscarCodigoAtivoPorSigla("XXX")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarCodigoPorSigla("XXX"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarSiglaPorCodigo - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarSiglaPorCodigo_NaoEncontrada() {
        when(unidadeRepo.buscarSiglaPorCodigo(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarSiglaPorCodigo(999L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarPorCodigoComSuperior - Lança ErroEntidadeNaoEncontrada se unidade não encontrada")
    void buscarPorCodigoComSuperior_NaoEncontrada() {
        when(unidadeRepo.buscarPorCodigoComSuperior(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorCodigoComSuperior(999L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
