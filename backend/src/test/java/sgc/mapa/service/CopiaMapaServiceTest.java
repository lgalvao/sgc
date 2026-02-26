package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Cópia de Mapa")
class CopiaMapaServiceTest {

    @Mock
    private ComumRepo repo;
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
        atividadeOrigem.setConhecimentos(new LinkedHashSet<>(Set.of(conhecimentoOrigem))); // Mutable set

        Competencia competenciaOrigem = new Competencia();
        competenciaOrigem.setDescricao("Competencia 1");
        competenciaOrigem.setAtividades(Set.of(atividadeOrigem));

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapaSalvo);
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(origemId)).thenReturn(List.of(atividadeOrigem));

        when(atividadeRepo.saveAll(anyList())).thenAnswer(i -> {
            List<Atividade> list = i.getArgument(0);
            list.forEach(a -> a.setCodigo(20L));
            return list;
        });

        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of(competenciaOrigem));

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
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(origemId)).thenReturn(List.of()); // Empty list
        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of()); // Empty list

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
        atividadeOrigem.setConhecimentos(Set.of()); // Empty set

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(origemId)).thenReturn(List.of(atividadeOrigem));
        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of());

        service.copiarMapaParaUnidade(origemId);

        verify(atividadeRepo).saveAll(anyList());
    }
}
