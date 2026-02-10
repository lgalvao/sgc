package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do Serviço de Cópia de Mapa")
class CopiaMapaServiceTest {

    @Mock
    private sgc.comum.repo.ComumRepo repo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;

    @InjectMocks
    private CopiaMapaService service;

    @Test
    @DisplayName("Deve copiar mapa com sucesso")
    @SuppressWarnings("unchecked")
    void deveCopiarMapaComSucesso() {
        Long origemId = 1L;

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
        atividadeOrigem.setConhecimentos(new ArrayList<>(List.of(conhecimentoOrigem))); // Mutable list

        Competencia competenciaOrigem = new Competencia();
        competenciaOrigem.setDescricao("Competencia 1");
        competenciaOrigem.setAtividades(Set.of(atividadeOrigem));

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapaSalvo);
        when(atividadeRepo.findWithConhecimentosByMapaCodigo(origemId)).thenReturn(List.of(atividadeOrigem));

        when(atividadeRepo.saveAll(anyList())).thenAnswer(i -> {
            List<Atividade> list = i.getArgument(0);
            list.forEach(a -> a.setCodigo(20L));
            return list;
        });

        when(competenciaRepo.findByMapaCodigo(origemId)).thenReturn(List.of(competenciaOrigem));

        Mapa resultado = service.copiarMapaParaUnidade(origemId);

        assertThat(resultado).isNotNull();
        verify(mapaRepo).save(any(Mapa.class));
        verify(atividadeRepo).saveAll(anyList());

        ArgumentCaptor<List<Competencia>> captor = ArgumentCaptor.forClass(List.class);
        verify(competenciaRepo).saveAll(captor.capture());

        List<Competencia> competenciasSalvas = captor.getValue();
        assertThat(competenciasSalvas).hasSize(1);
        Competencia competenciaSalva = competenciasSalvas.getFirst();
        assertThat(competenciaSalva.getAtividades()).hasSize(1);
        Atividade atividadeAssociada = competenciaSalva.getAtividades().iterator().next();
        assertThat(atividadeAssociada.getCodigo()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Deve lançar erro se mapa origem não existir")
    void deveLancarErroSeMapaOrigemNaoExistir() {
        when(repo.buscar(Mapa.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));
        assertThatThrownBy(() -> service.copiarMapaParaUnidade(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lidar com listas vazias de atividades e competencias")
    void deveLidarComListasVazias() {
        Long origemId = 1L;
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.findWithConhecimentosByMapaCodigo(origemId)).thenReturn(List.of()); // Empty list
        when(competenciaRepo.findByMapaCodigo(origemId)).thenReturn(List.of()); // Empty list

        Mapa resultado = service.copiarMapaParaUnidade(origemId);

        assertThat(resultado).isNotNull();
        verify(atividadeRepo, never()).saveAll(anyList());
        verify(competenciaRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lidar com atividade que tem lista de conhecimentos vazia")
    void deveLidarComConhecimentosVazio() {
        Long origemId = 1L;
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);

        Atividade atividadeOrigem = new Atividade();
        atividadeOrigem.setCodigo(10L);
        atividadeOrigem.setConhecimentos(List.of()); // Empty list

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.findWithConhecimentosByMapaCodigo(origemId)).thenReturn(List.of(atividadeOrigem));
        when(competenciaRepo.findByMapaCodigo(origemId)).thenReturn(List.of());

        service.copiarMapaParaUnidade(origemId);

        verify(atividadeRepo).saveAll(anyList());
    }
}
