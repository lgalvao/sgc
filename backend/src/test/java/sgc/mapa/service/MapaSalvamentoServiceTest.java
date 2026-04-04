package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaSalvamentoService Tests")
@SuppressWarnings("NullAway.Init")
class MapaSalvamentoServiceTest {
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ComumRepo repo;

    @InjectMocks
    private MapaSalvamentoService mapaSalvamentoService;

    @Test
    @DisplayName("Deve salvar mapa com competência existente")
    void deveSalvarComCompetenciaExistente() {
        Long codMapa = 1L;
        Long codComp = 50L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(codComp)
                .descricao("Comp 1")
                .atividadesCodigos(List.of(10L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia comp = Competencia.builder().codigo(codComp).mapa(mapa).atividades(new HashSet<>()).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(comp));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(comp));

        Mapa result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isEqualTo(mapa);
        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve salvar mapa com nova competência")
    void deveSalvarComNovaCompetencia() {
        Long codMapa = 1L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L)
                .descricao("Nova comp")
                .atividadesCodigos(List.of(10L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        Mapa result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isNotNull();
        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve buscar competência no repo se não estiver no contexto atual")
    void deveBuscarNoRepoSeNaoNoContexto() {
        Long codMapa = 1L;
        Long codComp = 50L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(codComp)
                .descricao("Comp 1")
                .atividadesCodigos(List.of(10L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia comp = Competencia.builder().codigo(codComp).mapa(mapa).atividades(new HashSet<>()).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(repo.buscar(Competencia.class, codComp)).thenReturn(comp);
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(comp));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(repo).buscar(Competencia.class, codComp);
    }

    @Test
    @DisplayName("Deve sanitizar observações antes de persistir o mapa")
    void deveSanitizarObservacoesAntesDePersistirMapa() {
        Long codMapa = 1L;
        String observacaoComHtmlPerigoso = "<script>alert('xss')</script><b>Observação segura</b>";

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes(observacaoComHtmlPerigoso)
                .competencias(List.of())
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(competenciaRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(mapa.getObservacoesDisponibilizacao()).isNotNull();
        assertThat(mapa.getObservacoesDisponibilizacao())
                .doesNotContain("<script>")
                .doesNotContain("alert('xss')")
                .contains("Observação segura");
    }

    @Test
    @DisplayName("Deve manter relacionamento bidirecional entre atividade e competência ao salvar")
    void deveManterRelacionamentoBidirecionalAoSalvarAssociacoes() {
        Long codMapa = 1L;
        Long codComp = 50L;
        Long codAtividade = 10L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(codComp)
                .descricao("Comp 1")
                .atividadesCodigos(List.of(codAtividade))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia competencia = Competencia.builder()
                .codigo(codComp)
                .descricao("Comp 1")
                .mapa(mapa)
                .atividades(new HashSet<>())
                .build();
        Atividade atividade = Atividade.builder()
                .codigo(codAtividade)
                .descricao("Atividade A")
                .mapa(mapa)
                .competencias(new HashSet<>())
                .build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(competencia));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(atividade));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(atividade.getCompetencias()).containsExactly(competencia);
        assertThat(competencia.getAtividades()).contains(atividade);
    }

    @Test
    @DisplayName("Não deve remover competências quando todas ainda existem na requisição")
    void naoDeveRemoverCompetenciasQuandoTodasPermanecem() {
        Long codMapa = 1L;
        Long codComp = 50L;
        Long codAtividade = 10L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(codComp)
                .descricao("Comp mantida")
                .atividadesCodigos(List.of(codAtividade))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia compAtual = Competencia.builder()
                .codigo(codComp)
                .descricao("Comp antiga")
                .mapa(mapa)
                .atividades(new HashSet<>())
                .build();
        Atividade atividade = Atividade.builder()
                .codigo(codAtividade)
                .mapa(mapa)
                .competencias(new HashSet<>())
                .build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(compAtual));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(atividade));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(competenciaRepo, never()).deleteAll(anyCollection());
        assertThat(compAtual.getDescricao()).isEqualTo("Comp mantida");
    }

    @Test
    @DisplayName("Deve remover competência obsoleta")
    void deveRemoverObsoleta() {
        Long codMapa = 1L;
        Long codCompObsoleto = 50L;

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of())
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia compObsoleto = spy(Competencia.builder().codigo(codCompObsoleto).mapa(mapa).atividades(new HashSet<>()).build());
        Atividade ativ = spy(Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>(List.of(compObsoleto))).build());

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(compObsoleto));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(competenciaRepo).deleteAll(anyCollection());
        assertThat(ativ.getCompetencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve validar integridade com avisos quando há itens desvinculados")
    void deveValidarIntegridadeComAvisos() {
        Long codMapa = 1L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L)
                .descricao("Comp")
                .atividadesCodigos(List.of()) // Comp sem atividade
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build(); // Ativ sem comp

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(ativ.getCompetencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar erro ao associar atividade de outro mapa")
    void deveLancarErroAtividadeOutroMapa() {
        Long codMapa = 1L;

        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L)
                .descricao("Nova comp")
                .atividadesCodigos(List.of(99L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("não pertence ao mapa");
    }

    @Test
    @DisplayName("Deve parar processamento de associações se número de competências salvas for menor que o esperado")
    void devePararProcessamentoSeMenosCompetenciasSalvas() {
        Long codMapa = 1L;

        SalvarMapaRequest.CompetenciaRequest compDto1 = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L).descricao("Comp 1").atividadesCodigos(List.of(10L)).build();
        SalvarMapaRequest.CompetenciaRequest compDto2 = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L).descricao("Comp 2").atividadesCodigos(List.of(10L)).build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto1, compDto2))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));

        // Simular que apenas 1 competência foi salva apesar de 2 pedidas
        Competencia salvas = Competencia.builder().codigo(100L).mapa(mapa).atividades(new HashSet<>()).build();
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(salvas));

        Mapa result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isNotNull();
        // Apenas a primeira competência deve ter sido associada
        assertThat(ativ.getCompetencias()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar erro quando faltar estrutura de associação da atividade")
    void deveLancarErroQuandoFaltarEstruturaAssociacao() throws Exception {
        Method metodo = MapaSalvamentoService.class.getDeclaredMethod("construirMapaAssociacoes",
                Class.forName("sgc.mapa.service.MapaSalvamentoService$ContextoSalvamento"), List.class);
        metodo.setAccessible(true);

        var classeContexto = Class.forName("sgc.mapa.service.MapaSalvamentoService$ContextoSalvamento");
        Constructor<?> construtor = classeContexto.getDeclaredConstructor(
                Long.class, List.class, List.class, Set.class, Set.class, SalvarMapaRequest.class);
        construtor.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Long> codigosAtividadesDoMapa = mock(Set.class);
        when(codigosAtividadesDoMapa.iterator()).thenReturn(List.of(10L).iterator());
        when(codigosAtividadesDoMapa.contains(20L)).thenReturn(true);

        SalvarMapaRequest.CompetenciaRequest competenciaRequest = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(0L)
                .descricao("Comp")
                .atividadesCodigos(List.of(20L))
                .build();
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(competenciaRequest))
                .build();
        Object contexto = construtor.newInstance(
                1L, new ArrayList<Competencia>(), new ArrayList<Atividade>(), codigosAtividadesDoMapa, Set.of(0L), request);

        Competencia competencia = Competencia.builder().codigo(1L).descricao("Comp").atividades(new HashSet<>()).build();

        assertThatThrownBy(() -> metodo.invoke(mapaSalvamentoService, contexto, List.of(competencia)))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("Atividade 20 sem estrutura de associação preparada");
    }
}
