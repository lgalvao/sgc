package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sgc.model.Atividade;
import sgc.model.Conhecimento;
import sgc.model.Mapa;
import sgc.model.Unidade;
import sgc.repository.AtividadeRepository;
import sgc.repository.ConhecimentoRepository;
import sgc.repository.MapaRepository;
import sgc.repository.UnidadeRepository;
import sgc.service.impl.MapCopyServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MapCopyServiceImpl.
 */
public class MapCopyServiceTest {

    private MapaRepository mapaRepository;
    private AtividadeRepository atividadeRepository;
    private ConhecimentoRepository conhecimentoRepository;
    private UnidadeRepository unidadeRepository;

    private MapCopyServiceImpl mapCopyService;

    @BeforeEach
    public void setup() {
        mapaRepository = mock(MapaRepository.class);
        atividadeRepository = mock(AtividadeRepository.class);
        conhecimentoRepository = mock(ConhecimentoRepository.class);
        unidadeRepository = mock(UnidadeRepository.class);

        mapCopyService = new MapCopyServiceImpl(mapaRepository, atividadeRepository, conhecimentoRepository, unidadeRepository);
    }

    @Test
    public void copyMapForUnit_shouldCopyMapaAtividadesEConhecimentos_whenHappyPath() {
        Long sourceMapaId = 10L;
        Long unidadeId = 5L;

        Mapa source = new Mapa();
        source.setCodigo(sourceMapaId);
        source.setObservacoesDisponibilizacao("obs");
        source.setSugestoesApresentadas("sugestoes");

        // atividade fonte que pertence ao mapa fonte
        Atividade a1 = new Atividade();
        a1.setCodigo(100L);
        a1.setDescricao("Atividade A");
        Mapa refMapa = new Mapa();
        refMapa.setCodigo(sourceMapaId);
        a1.setMapa(refMapa);

        // conhecimento vinculado à atividade a1
        Conhecimento c1 = new Conhecimento();
        c1.setCodigo(200L);
        Atividade refAtividade = new Atividade();
        refAtividade.setCodigo(100L);
        c1.setAtividade(refAtividade);
        c1.setDescricao("Conhecimento X");

        // mocks
        when(mapaRepository.findById(sourceMapaId)).thenReturn(Optional.of(source));
        when(atividadeRepository.findAll()).thenReturn(List.of(a1));
        when(conhecimentoRepository.findAll()).thenReturn(List.of(c1));
        Unidade mockUnidade = new Unidade();
        mockUnidade.setCodigo(unidadeId);
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(mockUnidade));
        when(mapaRepository.save(any(Mapa.class))).thenAnswer(inv -> {
            Mapa m = inv.getArgument(0);
            m.setCodigo(999L);
            return m;
        });
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> {
            Atividade a = inv.getArgument(0);
            a.setCodigo(1000L);
            return a;
        });
        when(conhecimentoRepository.save(any(Conhecimento.class))).thenAnswer(inv -> {
            Conhecimento c = inv.getArgument(0);
            c.setCodigo(2000L);
            return c;
        });

        // execute
        Mapa novo = mapCopyService.copyMapForUnit(sourceMapaId, unidadeId);

        // asserts
        assertThat(novo).isNotNull();
        assertThat(novo.getCodigo()).isEqualTo(999L);

        // verify saves
        verify(mapaRepository, times(1)).save(any(Mapa.class));
        verify(atividadeRepository, times(1)).save(any(Atividade.class));
        verify(conhecimentoRepository, times(1)).save(any(Conhecimento.class));
    }

    @Test
    public void copyMapForUnit_shouldThrow_whenMapaFonteNaoEncontrado() {
        Long sourceMapaId = 777L;
        Long unidadeId = 1L;

        when(mapaRepository.findById(sourceMapaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapCopyService.copyMapForUnit(sourceMapaId, unidadeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mapa fonte não encontrado");
    }
}