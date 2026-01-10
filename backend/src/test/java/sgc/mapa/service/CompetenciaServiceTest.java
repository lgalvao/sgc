package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Competência")
class CompetenciaServiceTest {

    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private CompetenciaService service;

    @Test
    @DisplayName("Deve adicionar competência com atividades")
    void deveCriarCompetenciaComAtividades() {
        Mapa mapa = new Mapa();
        String descricao = "Comp 1";
        List<Long> ativIds = List.of(1L);
        Atividade ativ = new Atividade();
        ativ.setCompetencias(new HashSet<>());

        when(atividadeRepo.findAllById(ativIds)).thenReturn(List.of(ativ));
        when(competenciaRepo.save(any())).thenAnswer(i -> {
            Competencia c = i.getArgument(0);
            c.setAtividades(new HashSet<>(List.of(ativ))); // Simula save retornando com atividades
            return c;
        });

        service.criarCompetenciaComAtividades(mapa, descricao, ativIds);

        verify(competenciaRepo).save(any());
        verify(atividadeRepo).saveAll(any());
    }

    @Test
    @DisplayName("Deve atualizar competência e suas associações")
    void deveAtualizarCompetencia() {
        Long compId = 1L;
        String novoDesc = "Nova Desc";
        List<Long> novosIds = List.of(2L);

        Competencia comp = new Competencia();
        comp.setAtividades(new HashSet<>());
        Atividade ativAntiga = new Atividade();
        ativAntiga.setCompetencias(new HashSet<>(List.of(comp)));
        comp.getAtividades().add(ativAntiga);

        Atividade ativNova = new Atividade();
        ativNova.setCompetencias(new HashSet<>());

        when(competenciaRepo.findById(compId)).thenReturn(Optional.of(comp));
        when(atividadeRepo.listarPorCompetencia(comp)).thenReturn(List.of(ativAntiga));
        when(atividadeRepo.findAllById(novosIds)).thenReturn(List.of(ativNova));
        when(competenciaRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.atualizarCompetencia(compId, novoDesc, novosIds);

        verify(atividadeRepo, times(2)).saveAll(any()); // 1 pra limpar antigas, 1 pra novas
        assertThat(comp.getDescricao()).isEqualTo(novoDesc);
        assertThat(comp.getAtividades()).contains(ativNova);
        assertThat(comp.getAtividades()).doesNotContain(ativAntiga);
    }

    @Test
    @DisplayName("Deve remover competência e limpar associações")
    void deveRemoverCompetencia() {
        Long compId = 1L;
        Competencia comp = new Competencia();
        Atividade ativ = new Atividade();
        ativ.setCompetencias(new HashSet<>(List.of(comp)));
        comp.setAtividades(new HashSet<>(List.of(ativ)));

        when(competenciaRepo.findById(compId)).thenReturn(Optional.of(comp));
        when(atividadeRepo.listarPorCompetencia(comp)).thenReturn(List.of(ativ));

        service.removerCompetencia(compId);

        verify(atividadeRepo).saveAll(anyList());
        verify(competenciaRepo).delete(comp);
        assertThat(ativ.getCompetencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void deveBuscarPorCodigo() {
        Long id = 1L;
        when(competenciaRepo.findById(id)).thenReturn(Optional.of(new Competencia()));
        assertThat(service.buscarPorCodigo(id)).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar inexistente")
    void deveLancarErroAoBuscarInexistente() {
        when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorCodigo(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
