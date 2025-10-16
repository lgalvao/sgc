package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopiaMapaServiceTest {
    @Mock
    private MapaRepo repositorioMapa;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private ConhecimentoRepo repositorioConhecimento;

    @Mock
    private UnidadeRepo repositorioUnidade;

    @InjectMocks
    private CopiaMapaService copiaMapaService;

    private Mapa mapaFonte;
    private Unidade unidadeDestino;
    private Atividade atividadeFonte;
    private Conhecimento conhecimentoFonte;

    @BeforeEach
    void setUp() {
        mapaFonte = new Mapa();
        mapaFonte.setCodigo(1L);

        unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(10L);

        atividadeFonte = new Atividade();
        atividadeFonte.setCodigo(100L);
        atividadeFonte.setDescricao("Atividade Teste");
        atividadeFonte.setMapa(mapaFonte);

        conhecimentoFonte = new Conhecimento();
        conhecimentoFonte.setCodigo(1000L);
        conhecimentoFonte.setDescricao("Conhecimento Teste");
        conhecimentoFonte.setAtividade(atividadeFonte);
    }

    @Test
    void copiarMapaParaUnidade_deveCopiarMapaCompleto() {
        // Arrange
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapaFonte));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(List.of(atividadeFonte));
        when(repositorioConhecimento.findByAtividadeCodigo(100L)).thenReturn(List.of(conhecimentoFonte));

        // Mock para saves, retornando o objeto que foi passado
        when(repositorioMapa.save(any(Mapa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(atividadeRepo.save(any(Atividade.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repositorioConhecimento.save(any(Conhecimento.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Mapa novoMapa = copiaMapaService.copiarMapaParaUnidade(1L, 10L);

        // Assert
        assertThat(novoMapa).isNotNull();
        assertThat(novoMapa.getUnidade()).isEqualTo(unidadeDestino);

        ArgumentCaptor<Atividade> atividadeCaptor = ArgumentCaptor.forClass(Atividade.class);
        verify(atividadeRepo, times(1)).save(atividadeCaptor.capture());
        Atividade atividadeSalva = atividadeCaptor.getValue();
        assertThat(atividadeSalva.getDescricao()).isEqualTo("Atividade Teste");
        assertThat(atividadeSalva.getMapa().getUnidade()).isEqualTo(novoMapa.getUnidade());

        ArgumentCaptor<Conhecimento> conhecimentoCaptor = ArgumentCaptor.forClass(Conhecimento.class);
        verify(repositorioConhecimento, times(1)).save(conhecimentoCaptor.capture());
        Conhecimento conhecimentoSalvo = conhecimentoCaptor.getValue();
        assertThat(conhecimentoSalvo.getDescricao()).isEqualTo("Conhecimento Teste");
        assertThat(conhecimentoSalvo.getAtividade().getDescricao()).isEqualTo(atividadeSalva.getDescricao());
    }

    @Test
    void copiarMapaParaUnidade_quandoMapaFonteNaoEncontrado_deveLancarExcecao() {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> copiaMapaService.copiarMapaParaUnidade(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapa fonte não encontrado: 1");
    }

    @Test
    void copiarMapaParaUnidade_quandoUnidadeDestinoNaoEncontrada_deveLancarExcecao() {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapaFonte));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> copiaMapaService.copiarMapaParaUnidade(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unidade de destino não encontrada: 10");
    }

    @Test
    void copiarMapaParaUnidade_quandoMapaVazio_deveCopiarApenasOMapa() {
        // Arrange
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapaFonte));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());
        when(repositorioMapa.save(any(Mapa.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Mapa novoMapa = copiaMapaService.copiarMapaParaUnidade(1L, 10L);

        // Assert
        assertThat(novoMapa).isNotNull();
        assertThat(novoMapa.getUnidade()).isEqualTo(unidadeDestino);
        verify(atividadeRepo, never()).save(any());
        verify(repositorioConhecimento, never()).save(any());
    }
}
