package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

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

    @Captor
    private ArgumentCaptor<List<Competencia>> competenciaListCaptor;

    @Captor
    private ArgumentCaptor<List<Atividade>> atividadesCaptor;

    @Test
    @DisplayName("Deve copiar mapa com sucesso")
    void deveCopiarMapaComSucesso() {
        Long origemId = 1L;

        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);
        mapaOrigem.setObservacoesDisponibilizacao("Obs");
        Subprocesso subprocessoDestino = new Subprocesso();
        subprocessoDestino.setCodigo(99L);

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
        when(atividadeRepo.listarPorMapaComConhecimentos(origemId)).thenReturn(List.of(atividadeOrigem));

        when(atividadeRepo.saveAll(anyList())).thenAnswer(i -> {
            List<Atividade> list = i.getArgument(0);
            list.forEach(a -> a.setCodigo(20L));
            return list;
        });

        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of(competenciaOrigem));

        Mapa resultado = service.copiarMapaParaUnidade(origemId, subprocessoDestino);

        assertThat(resultado).isNotNull();
        verify(mapaRepo).save(any(Mapa.class));
        verify(atividadeRepo).saveAll(anyList());

        verify(competenciaRepo).saveAll(competenciaListCaptor.capture());

        List<Competencia> competenciasSalvas = competenciaListCaptor.getValue();
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
        Subprocesso destino = new Subprocesso();
        assertThatThrownBy(() -> service.copiarMapaParaUnidade(1L, destino))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lidar com listas vazias de atividades e competencias")
    void deveLidarComListasVazias() {
        Long origemId = 1L;
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(origemId);
        Subprocesso subprocessoDestino = new Subprocesso();
        subprocessoDestino.setCodigo(99L);

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.listarPorMapaComConhecimentos(origemId)).thenReturn(List.of()); // Empty list
        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of()); // Empty list

        Mapa resultado = service.copiarMapaParaUnidade(origemId, subprocessoDestino);

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
        Subprocesso subprocessoDestino = new Subprocesso();
        subprocessoDestino.setCodigo(99L);

        Atividade atividadeOrigem = new Atividade();
        atividadeOrigem.setCodigo(10L);
        atividadeOrigem.setConhecimentos(Set.of()); // Empty set

        when(repo.buscar(Mapa.class, origemId)).thenReturn(mapaOrigem);
        when(mapaRepo.save(any(Mapa.class))).thenReturn(new Mapa());
        when(atividadeRepo.listarPorMapaComConhecimentos(origemId)).thenReturn(List.of(atividadeOrigem));
        when(competenciaRepo.findByMapa_Codigo(origemId)).thenReturn(List.of());

        service.copiarMapaParaUnidade(origemId, subprocessoDestino);

        verify(atividadeRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("copiarMapaParaUnidade deve lidar com atividade não encontrada no mapa de destino")
    void deveLidarComAtividadeNaoEncontrada() {

        Long codMapaOrigem = 1L;
        Mapa fonte = new Mapa();
        fonte.setCodigo(codMapaOrigem);
        Subprocesso subprocessoDestino = new Subprocesso();
        subprocessoDestino.setCodigo(10L);

        when(repo.buscar(Mapa.class, codMapaOrigem)).thenReturn(fonte);
        when(mapaRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Atividade ativFonte = new Atividade();
        ativFonte.setCodigo(100L);
        ativFonte.setDescricao("Atividade teste");
        when(atividadeRepo.listarPorMapaComConhecimentos(codMapaOrigem)).thenReturn(List.of(ativFonte));

        Competencia compFonte = new Competencia();
        compFonte.setCodigo(200L);
        compFonte.setDescricao("Competencia teste");

        Atividade ativFantasma = new Atividade();
        ativFantasma.setCodigo(999L);
        compFonte.setAtividades(Set.of(ativFantasma));

        when(competenciaRepo.findByMapa_Codigo(codMapaOrigem)).thenReturn(List.of(compFonte));

        service.copiarMapaParaUnidade(codMapaOrigem, subprocessoDestino);

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

        when(atividadeRepo.listarPorMapaComConhecimentos(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of());
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());

        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, List.of());

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

        when(atividadeRepo.listarPorMapaComConhecimentos(mapaOrigemId)).thenReturn(List.of(ativ1, ativ2));
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

        when(atividadeRepo.listarPorMapaComConhecimentos(mapaOrigemId)).thenReturn(List.of(ativ1));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of());
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());

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

        when(atividadeRepo.listarPorMapaComConhecimentos(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of(ativDestino));
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());

        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, List.of());

        verify(atividadeRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa deve tratar atividade de origem sem conhecimentos")
    void deveImportarAtividadeComConhecimentosNulos() {
        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;

        Atividade ativOrigem = new Atividade();
        ativOrigem.setCodigo(10L);
        ativOrigem.setDescricao("Sem conhecimentos");
        ativOrigem.setConhecimentos(Set.of());

        when(atividadeRepo.listarPorMapaComConhecimentos(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of());
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());

        int quantidade = service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId, List.of(10L));

        assertThat(quantidade).isEqualTo(1);
        verify(atividadeRepo).saveAll(anyList());
    }
}
