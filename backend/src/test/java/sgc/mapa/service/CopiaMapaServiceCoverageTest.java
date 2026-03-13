package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CopiaMapaService - Cobertura adicional")
class CopiaMapaServiceCoverageTest {

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

    @Captor
    private ArgumentCaptor<List<Atividade>> atividadesCaptor;

    @Test
    @DisplayName("copiarMapaParaUnidade deve lidar com atividade não encontrada no mapa de destino")
    void deveLidarComAtividadeNaoEncontrada() {

        Long codMapaOrigem = 1L;
        Mapa fonte = new Mapa();
        fonte.setCodigo(codMapaOrigem);

        when(repo.buscar(Mapa.class, codMapaOrigem)).thenReturn(fonte);
        when(mapaRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Atividade ativFonte = new Atividade();
        ativFonte.setCodigo(100L);
        ativFonte.setDescricao("Atividade teste");
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(codMapaOrigem)).thenReturn(List.of(ativFonte));

        Competencia compFonte = new Competencia();
        compFonte.setCodigo(200L);
        compFonte.setDescricao("Competencia teste");

        // Associada a uma atividade que NÃO existe no mapa de origem (ou ID errado no mock)
        Atividade ativFantasma = new Atividade();
        ativFantasma.setCodigo(999L);
        compFonte.setAtividades(Set.of(ativFantasma));

        when(competenciaRepo.findByMapa_Codigo(codMapaOrigem)).thenReturn(List.of(compFonte));


        service.copiarMapaParaUnidade(codMapaOrigem);


        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa deve importar atividades que não existem no destino")
    void deveImportarAtividadesNaoExistentes() {

        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;

        Atividade ativOrigem = Atividade.builder()
                .descricao("Nova atividade")
                .conhecimentos(new HashSet<>())
                .build();

        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of()); // Destino vazio
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());


        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, null);


        verify(atividadeRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa deve importar somente atividades selecionadas (codigosAtividades não nulo)")
    void deveImportarApenasAtividadesSelecionadas() {

        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;

        Atividade ativ1 = Atividade.builder().descricao("Selecionada").conhecimentos(new HashSet<>()).build();
        ativ1.setCodigo(10L);
        Atividade ativ2 = Atividade.builder().descricao("Nao selecionada").conhecimentos(new HashSet<>()).build();
        ativ2.setCodigo(20L);

        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativ1, ativ2));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of());
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());


        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, List.of(10L));


        verify(atividadeRepo).saveAll(atividadesCaptor.capture());
        assertThat(atividadesCaptor.getValue()).hasSize(1);
        assertThat(atividadesCaptor.getValue().getFirst().getDescricao()).isEqualTo("Selecionada");
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa deve ignorar IDs inexistentes sem lançar exceção")
    void deveIgnorarIdsInexistentesELogarAviso() {

        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;

        Atividade ativ1 = Atividade.builder().descricao("Existente").conhecimentos(new HashSet<>()).build();
        ativ1.setCodigo(10L);

        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativ1));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of());
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());


        // ID 999 não existe no mapa de origem — deve ser ignorado sem lançar exceção
        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, List.of(999L));


        verify(atividadeRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa não deve importar atividades com descrição já existente")
    void naoDeveImportarAtividadesExistentes() {

        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;

        Atividade ativOrigem = Atividade.builder().descricao("Existente").build();
        Atividade ativDestino = Atividade.builder().descricao("Existente").build();

        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of(ativDestino));
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());


        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, null);


        verify(atividadeRepo, never()).saveAll(anyList());
    }
}
