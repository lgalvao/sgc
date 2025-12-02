package sgc.subprocesso.service;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.ListArbitrary;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaWorkflowServicePropertiesTest {

    record ServiceAndMocks(
        SubprocessoMapaWorkflowService service,
        SubprocessoRepo subprocessoRepo,
        CompetenciaRepo competenciaRepo,
        AtividadeRepo atividadeRepo,
        MapaService mapaService,
        CompetenciaService competenciaService,
        ApplicationEventPublisher publisher
    ) {}

    private ServiceAndMocks createService() {
        SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
        CompetenciaRepo competenciaRepo = mock(CompetenciaRepo.class);
        AtividadeRepo atividadeRepo = mock(AtividadeRepo.class);
        MapaService mapaService = mock(MapaService.class);
        CompetenciaService competenciaService = mock(CompetenciaService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        SubprocessoMapaWorkflowService service = new SubprocessoMapaWorkflowService(
            subprocessoRepo,
            competenciaRepo,
            atividadeRepo,
            mapaService,
            competenciaService,
            publisher
        );
        return new ServiceAndMocks(
            service,
            subprocessoRepo,
            competenciaRepo,
            atividadeRepo,
            mapaService,
            competenciaService,
            publisher
        );
    }

    record MapaCenario(Subprocesso subprocesso, List<Atividade> atividades, List<Competencia> competencias) {
        @Override
        public String toString() {
            return String.format("Atividades: %d, Competencias: %d", atividades.size(), competencias.size());
        }
    }

    @Property
    void deveAceitarMapaCompleto(@ForAll("cenarioValido") MapaCenario cenario) {
        var mocks = createService();

        when(mocks.subprocessoRepo.findById(cenario.subprocesso.getCodigo()))
            .thenReturn(Optional.of(cenario.subprocesso));
        when(mocks.competenciaRepo.findByMapaCodigo(cenario.subprocesso.getMapa().getCodigo()))
            .thenReturn(cenario.competencias);
        when(mocks.atividadeRepo.findBySubprocessoCodigo(cenario.subprocesso.getCodigo()))
            .thenReturn(cenario.atividades);

        assertThatCode(() -> mocks.service.disponibilizarMapa(
            cenario.subprocesso.getCodigo(),
            new DisponibilizarMapaRequest(){ { setDataLimite(java.time.LocalDate.now().plusDays(1)); } },
            new Usuario()
        )).doesNotThrowAnyException();
    }

    @Property
    void deveRejeitarMapaComAtividadeFaltando(@ForAll("cenarioComAtividadeFaltando") MapaCenario cenario) {
        var mocks = createService();

        when(mocks.subprocessoRepo.findById(cenario.subprocesso.getCodigo()))
            .thenReturn(Optional.of(cenario.subprocesso));
        when(mocks.competenciaRepo.findByMapaCodigo(cenario.subprocesso.getMapa().getCodigo()))
            .thenReturn(cenario.competencias);
        when(mocks.atividadeRepo.findBySubprocessoCodigo(cenario.subprocesso.getCodigo()))
            .thenReturn(cenario.atividades);

        assertThatThrownBy(() -> mocks.service.disponibilizarMapa(
            cenario.subprocesso.getCodigo(),
            new DisponibilizarMapaRequest(){ { setDataLimite(java.time.LocalDate.now().plusDays(1)); } },
            new Usuario()
        ))
        .isInstanceOf(ErroValidacao.class)
        .hasMessageContaining("Todas as atividades devem estar associadas");
    }

    @Property
    void deveRejeitarMapaComCompetenciaVazia(@ForAll("cenarioComCompetenciaVazia") MapaCenario cenario) {
         var mocks = createService();

        when(mocks.subprocessoRepo.findById(cenario.subprocesso.getCodigo()))
            .thenReturn(Optional.of(cenario.subprocesso));
        when(mocks.competenciaRepo.findByMapaCodigo(cenario.subprocesso.getMapa().getCodigo()))
            .thenReturn(cenario.competencias);

        assertThatThrownBy(() -> mocks.service.disponibilizarMapa(
            cenario.subprocesso.getCodigo(),
            new DisponibilizarMapaRequest(),
            new Usuario()
        ))
        .isInstanceOf(ErroValidacao.class)
        .hasMessageContaining("Todas as competências devem estar associadas");
    }

    // --- Geradores ---

    @Provide
    Arbitrary<MapaCenario> cenarioValido() {
         return atividadesArb().flatMap(atividades ->
             competenciasValidasArb(atividades).map(competencias -> {
                 Subprocesso sp = criarSubprocesso();
                 return new MapaCenario(sp, atividades, competencias);
             })
         );
    }

    @Provide
    Arbitrary<MapaCenario> cenarioComAtividadeFaltando() {
        return atividadesArb().filter(l -> l.size() >= 2).flatMap(atividades -> {
            // Usa sublista para gerar competências que cobrem apenas uma parte
            List<Atividade> subLista = atividades.subList(0, atividades.size() - 1);
            return competenciasValidasArb(subLista).map(competencias -> {
                 Subprocesso sp = criarSubprocesso();
                 return new MapaCenario(sp, atividades, competencias);
            });
        });
    }

    @Provide
    Arbitrary<MapaCenario> cenarioComCompetenciaVazia() {
        return atividadesArb().flatMap(atividades ->
             competenciasValidasArb(atividades).map(competencias -> {
                 Subprocesso sp = criarSubprocesso();
                 // Adiciona uma competência vazia
                 Competencia vazia = new Competencia();
                 vazia.setCodigo(999L);
                 vazia.setDescricao("Vazia");
                 vazia.setAtividades(new HashSet<>());
                 competencias.add(vazia);
                 return new MapaCenario(sp, atividades, competencias);
             })
        );
    }

    private Arbitrary<List<Atividade>> atividadesArb() {
        return Arbitraries.longs().between(1, 1000).list().ofMinSize(1).ofMaxSize(10).uniqueElements()
            .map(ids -> ids.stream().map(id -> {
                Atividade a = new Atividade();
                a.setCodigo(id);
                a.setDescricao("Atividade " + id);
                return a;
            }).collect(Collectors.toList()));
    }

    private Arbitrary<List<Competencia>> competenciasValidasArb(List<Atividade> atividades) {
        if (atividades.isEmpty()) {
             // Se não tem atividades, não precisamos de competências para cobrir.
             // Mas o teste geralmente assume pelo menos 1 atividade para testar a lógica.
             return Arbitraries.just(Collections.emptyList());
        }

        return Arbitraries.integers().between(1, 5).flatMap(numCompetencias -> {
            // Gera atribuições de atividades para competências
            // Lista de Sets, onde cada Set contem os índices das competencias para aquela atividade
            return Arbitraries.integers().between(0, numCompetencias - 1).set().ofMinSize(1)
                .list().ofSize(atividades.size())
                .filter(assignments -> {
                     // Garante que toda competência receba pelo menos uma atividade (para não ser vazia)
                     Set<Integer> indicesUsados = assignments.stream()
                         .flatMap(Set::stream)
                         .collect(Collectors.toSet());
                     return indicesUsados.size() == numCompetencias;
                })
                .map(assignments -> {
                     List<Competencia> comps = IntStream.range(0, numCompetencias).mapToObj(i -> {
                         Competencia c = new Competencia();
                         c.setCodigo((long)i);
                         c.setDescricao("Competencia " + i);
                         c.setAtividades(new HashSet<>());
                         return c;
                     }).collect(Collectors.toList());

                     for (int i = 0; i < atividades.size(); i++) {
                         Atividade a = atividades.get(i);
                         for (Integer compIndex : assignments.get(i)) {
                             comps.get(compIndex).getAtividades().add(a);
                         }
                     }
                     return comps;
                });
        });
    }

    private Subprocesso criarSubprocesso() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPA_CRIADO); // ou CADASTRO_HOMOLOGADO, ambos válidos para edição, mas para disponibilizar deve estar válido?
        // O método getSubprocessoParaEdicao checa se é CADASTRO_HOMOLOGADO ou MAPA_CRIADO.
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());
        return sp;
    }
}
