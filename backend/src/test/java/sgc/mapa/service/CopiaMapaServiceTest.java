package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Cópia de Mapa")
class CopiaMapaServiceTest {

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;

    @InjectMocks
    private CopiaMapaService service;

    @Test
    @DisplayName("Deve copiar mapa com sucesso")
    void deveCopiarMapaComSucesso() {
        Long origemId = 1L;
        Long destinoId = 2L;

        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);
        mapaOrigem.setObservacoesDisponibilizacao("Obs");

        Mapa mapaSalvo = new Mapa();
        mapaSalvo.setCodigo(3L);

        Atividade atividadeOrigem = new Atividade();
        atividadeOrigem.setCodigo(10L);
        atividadeOrigem.setDescricao("Atividade 1");
        Conhecimento conhecimentoOrigem = new Conhecimento();
        conhecimentoOrigem.setDescricao("Conhecimento 1");
        atividadeOrigem.setConhecimentos(new java.util.ArrayList<>(List.of(conhecimentoOrigem))); // Mutable list

        Atividade atividadeSalva = new Atividade();
        atividadeSalva.setCodigo(20L);
        atividadeSalva.setDescricao("Atividade 1");
        atividadeSalva.setConhecimentos(new java.util.ArrayList<>());

        Competencia competenciaOrigem = new Competencia();
        competenciaOrigem.setDescricao("Competencia 1");
        competenciaOrigem.setAtividades(Set.of(atividadeOrigem));

        when(mapaRepo.findById(origemId)).thenReturn(Optional.of(mapaOrigem));
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapaSalvo);
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(origemId)).thenReturn(List.of(atividadeOrigem));
        when(atividadeRepo.save(any(Atividade.class))).thenReturn(atividadeSalva);
        when(competenciaRepo.findByMapaCodigo(origemId)).thenReturn(List.of(competenciaOrigem));
        when(competenciaRepo.save(any(Competencia.class))).thenAnswer(i -> i.getArgument(0));

        Mapa resultado = service.copiarMapaParaUnidade(origemId, destinoId);

        assertThat(resultado).isNotNull();
        verify(mapaRepo).save(any(Mapa.class));
        verify(atividadeRepo).save(any(Atividade.class));
        verify(conhecimentoRepo).saveAll(anyList());

        ArgumentCaptor<Competencia> captor = ArgumentCaptor.forClass(Competencia.class);
        verify(competenciaRepo).save(captor.capture());

        Competencia competenciaSalva = captor.getValue();
        assertThat(competenciaSalva.getAtividades()).hasSize(1);
        Atividade atividadeAssociada = competenciaSalva.getAtividades().iterator().next();
        assertThat(atividadeAssociada.getCodigo()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Deve lançar erro se mapa origem não existir")
    void deveLancarErroSeMapaOrigemNaoExistir() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.copiarMapaParaUnidade(1L, 2L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lidar com listas vazias ou nulas de atividades e competencias")
    void deveLidarComListasVazias() {
        Long origemId = 1L;
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);

        when(mapaRepo.findById(origemId)).thenReturn(Optional.of(mapaOrigem));
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(origemId)).thenReturn(null); // Return null
        when(competenciaRepo.findByMapaCodigo(origemId)).thenReturn(Collections.emptyList()); // Empty list

        Mapa resultado = service.copiarMapaParaUnidade(origemId, 2L);

        assertThat(resultado).isNotNull();
        verify(atividadeRepo, never()).save(any(Atividade.class));
        verify(competenciaRepo, never()).save(any(Competencia.class));
    }
}
