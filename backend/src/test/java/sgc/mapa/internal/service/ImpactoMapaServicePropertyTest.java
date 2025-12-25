package sgc.mapa.internal.service;

import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.atividade.api.model.Atividade;
import sgc.atividade.api.model.AtividadeRepo;
import sgc.atividade.api.model.Conhecimento;
import sgc.mapa.api.ImpactoMapaDto;
import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.CompetenciaRepo;
import sgc.mapa.api.model.Mapa;
import sgc.mapa.api.model.MapaRepo;
import sgc.sgrh.api.model.Usuario;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.Unidade;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("pbt")
class ImpactoMapaServicePropertyTest {

    @Provide
    Arbitrary<Scenario> scenarios() {
        return Arbitraries.randomValue(random -> {
            long mapaVigenteId = 100L;
            long mapaSubId = 200L;

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(mapaVigenteId);

            Mapa mapaSubprocesso = new Mapa();
            mapaSubprocesso.setCodigo(mapaSubId);

            // Generate Competencies
            int numComps = random.nextInt(1, 5);
            List<Competencia> competencias = new ArrayList<>();
            for (int i = 0; i < numComps; i++) {
                Competencia c = new Competencia();
                c.setCodigo((long) i + 1000);
                c.setDescricao("Competencia " + i);
                c.setMapa(mapaVigente);
                c.setAtividades(new HashSet<>());
                competencias.add(c);
            }

            // Generate Vigente Activities
            int numVig = random.nextInt(0, 5);
            List<Atividade> vigentes = new ArrayList<>();
            for (int i = 0; i < numVig; i++) {
                Atividade a = new Atividade();
                a.setCodigo((long) i + 2000);
                a.setDescricao("Atividade " + i);
                a.setMapa(mapaVigente);

                // Link to random competencies
                if (!competencias.isEmpty()) {
                    int numLinks = random.nextInt(1, competencias.size() + 1);
                    for (int j = 0; j < numLinks; j++) {
                        Competencia comp = competencias.get(random.nextInt(competencias.size()));
                        a.getCompetencias().add(comp);
                        comp.getAtividades().add(a); // Manual bi-directional link
                    }
                }

                // Add Knowledge
                a.setConhecimentos(new ArrayList<>());
                a.getConhecimentos().add(new Conhecimento("Conhecimento A", a));

                vigentes.add(a);
            }

            // Generate Current (Modified) Activities based on Vigente
            List<Atividade> atuais = new ArrayList<>();

            // 1. Keep some
            for (Atividade v : vigentes) {
                if (random.nextBoolean()) { // Keep 50%
                    Atividade copy = new Atividade();
                    copy.setCodigo(v.getCodigo()); // Same ID
                    copy.setDescricao(v.getDescricao()); // Same desc
                    copy.setMapa(mapaSubprocesso);

                    // Maybe modify knowledge
                    copy.setConhecimentos(new ArrayList<>());
                    if (random.nextDouble() > 0.8) {
                        copy.getConhecimentos().add(new Conhecimento("Conhecimento Modified", copy));
                    } else {
                         // Same knowledge
                         v.getConhecimentos().forEach(k ->
                             copy.getConhecimentos().add(new Conhecimento(k.getDescricao(), copy)));
                    }
                    atuais.add(copy);
                }
            }

            // 2. Add new
            int numNew = random.nextInt(0, 3);
            for (int i = 0; i < numNew; i++) {
                Atividade a = new Atividade();
                a.setCodigo((long) i + 3000); // New IDs
                a.setDescricao("Atividade Nova " + i);
                a.setMapa(mapaSubprocesso);
                a.setConhecimentos(new ArrayList<>());
                atuais.add(a);
            }

            return new Scenario(mapaVigente, mapaSubprocesso, competencias, vigentes, atuais);
        });
    }

    @Property(tries = 50)
    void verificarImpactos_Compliance(@ForAll("scenarios") Scenario scenario) {
        SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);
        AtividadeRepo atividadeRepo = mock(AtividadeRepo.class);
        CompetenciaRepo competenciaRepo = mock(CompetenciaRepo.class);

        ImpactoMapaService service = new ImpactoMapaService(
            subprocessoRepo, mapaRepo, atividadeRepo, competenciaRepo
        );

        // Mock Setup
        Usuario mockUser = mock(Usuario.class);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Mockito.doReturn(authorities).when(mockUser).getAuthorities();

        Subprocesso mockSub = new Subprocesso();
        mockSub.setCodigo(99L);
        mockSub.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade mockUnidade = new Unidade();
        mockUnidade.setCodigo(55L);
        mockSub.setUnidade(mockUnidade);

        when(subprocessoRepo.findById(99L)).thenReturn(Optional.of(mockSub));
        when(mapaRepo.findMapaVigenteByUnidade(55L)).thenReturn(Optional.of(scenario.mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(99L)).thenReturn(Optional.of(scenario.mapaSubprocesso));

        when(atividadeRepo.findByMapaCodigoWithConhecimentos(scenario.mapaSubprocesso.getCodigo()))
            .thenReturn(scenario.atuais);
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(scenario.mapaVigente.getCodigo()))
            .thenReturn(scenario.vigentes);

        when(competenciaRepo.findByMapaCodigo(scenario.mapaVigente.getCodigo()))
            .thenReturn(scenario.competencias);

        // Execute
        ImpactoMapaDto result = service.verificarImpactos(99L, mockUser);

        // Verify Inseridas
        Set<String> vigentesDesc = scenario.vigentes.stream().map(Atividade::getDescricao).collect(Collectors.toSet());
        long expectedInserted = scenario.atuais.stream()
            .filter(a -> !vigentesDesc.contains(a.getDescricao()))
            .count();
        assertThat(result.getTotalAtividadesInseridas()).isEqualTo((int) expectedInserted);

        // Verify Removidas
        Set<String> atuaisDesc = scenario.atuais.stream().map(Atividade::getDescricao).collect(Collectors.toSet());
        long expectedRemoved = scenario.vigentes.stream()
            .filter(a -> !atuaisDesc.contains(a.getDescricao()))
            .count();
        assertThat(result.getTotalAtividadesRemovidas()).isEqualTo((int) expectedRemoved);

        // Sanity Check
        if (scenario.atuais.isEmpty() && !scenario.vigentes.isEmpty()) {
             assertThat(result.isTemImpactos()).isTrue();
        }
    }

    record Scenario(
        Mapa mapaVigente,
        Mapa mapaSubprocesso,
        List<Competencia> competencias,
        List<Atividade> vigentes,
        List<Atividade> atuais
    ) {}
}
