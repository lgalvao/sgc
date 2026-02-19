package sgc.subprocesso.service.crud;

import net.jqwik.api.*;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.model.Subprocesso;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("PBT")
class SubprocessoValidacaoServicePbtTest {

    @Property
    void validarExistenciaAtividades_falhaSeAlgumaAtividadeSemConhecimento(@ForAll("mapaComAtividadesPotencialmenteSemConhecimento") Mapa mapa) {
        // Mock dependencies
        MapaManutencaoService mapaManutencaoService = mock(MapaManutencaoService.class);
        SubprocessoCrudService crudService = mock(SubprocessoCrudService.class);
        
        SubprocessoValidacaoService service = new SubprocessoValidacaoService(mapaManutencaoService, crudService);
        
        Subprocesso sp = Subprocesso.builder().codigo(1L).mapa(mapa).build();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo())).thenReturn(new ArrayList<>(mapa.getAtividades()));

        boolean temAtividadeSemConhecimento = mapa.getAtividades().stream().anyMatch(a -> a.getConhecimentos().isEmpty());
        boolean listaVazia = mapa.getAtividades().isEmpty();

        if (listaVazia) {
            assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        } else if (temAtividadeSemConhecimento) {
            assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        } else {
            assertThatCode(() -> service.validarExistenciaAtividades(1L)).doesNotThrowAnyException();
        }
    }

    @Property
    void validarAssociacoesMapa_falhaSeHouverDesconexao(@ForAll("mapaComCompetenciasEAtividades") Mapa mapa) {
        // Mock dependencies
        MapaManutencaoService mapaManutencaoService = mock(MapaManutencaoService.class);
        SubprocessoCrudService crudService = mock(SubprocessoCrudService.class);
        
        SubprocessoValidacaoService service = new SubprocessoValidacaoService(mapaManutencaoService, crudService);
        
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(mapa.getCodigo())).thenReturn(new ArrayList<>(mapa.getCompetencias()));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapa.getCodigo())).thenReturn(new ArrayList<>(mapa.getAtividades()));

        boolean competenciaSemAtividade = mapa.getCompetencias().stream().anyMatch(c -> c.getAtividades().isEmpty());
        boolean atividadeSemCompetencia = mapa.getAtividades().stream().anyMatch(a -> a.getCompetencias().isEmpty());

        if (competenciaSemAtividade) {
            assertThatThrownBy(() -> service.validarAssociacoesMapa(mapa.getCodigo()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Existem competências que não foram associadas a nenhuma atividade.");
        } else if (atividadeSemCompetencia) {
            assertThatThrownBy(() -> service.validarAssociacoesMapa(mapa.getCodigo()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Existem atividades que não foram associadas a nenhuma competência.");
        } else {
            assertThatCode(() -> service.validarAssociacoesMapa(mapa.getCodigo())).doesNotThrowAnyException();
        }
    }

    @Provide
    Arbitrary<Mapa> mapaComAtividadesPotencialmenteSemConhecimento() {
        return Arbitraries.longs().between(1, 100).flatMap(mapaId ->
            Arbitraries.integers().between(0, 5).list().ofMinSize(0).ofMaxSize(1).flatMap(ignore ->
                Arbitraries.of(0, 1, 2).list().ofMinSize(0).ofMaxSize(5).map(conhecimentosCounts -> {
                    Mapa m = Mapa.builder().codigo(mapaId).atividades(new LinkedHashSet<>()).build();
                    long actId = 1;
                    for (Integer count : conhecimentosCounts) {
                        Atividade a = Atividade.builder().codigo(actId++).descricao("Ativ " + actId).mapa(m).conhecimentos(new HashSet<>()).build();
                        for (int i = 0; i < count; i++) {
                            a.getConhecimentos().add(new Conhecimento());
                        }
                        m.getAtividades().add(a);
                    }
                    return m;
                })
            )
        );
    }

    @Provide
    Arbitrary<Mapa> mapaComCompetenciasEAtividades() {
         return Arbitraries.longs().between(1, 100).flatMap(mapaId ->
            Arbitraries.integers().between(1, 3).flatMap(numComp ->
                Arbitraries.integers().between(1, 3).map(numAtiv -> {
                    Mapa m = Mapa.builder().codigo(mapaId).atividades(new LinkedHashSet<>()).competencias(new LinkedHashSet<>()).build();
                    for(int i=0; i<numAtiv; i++) {
                        m.getAtividades().add(Atividade.builder().codigo((long)i+1).descricao("A"+i).competencias(new HashSet<>()).build());
                    }
                    for(int i=0; i<numComp; i++) {
                        m.getCompetencias().add(Competencia.builder().codigo((long)i+1).descricao("C"+i).atividades(new HashSet<>()).build());
                    }
                    return m;
                })
            )
         ).flatMap(m -> {
             final List<Atividade> ativList = new ArrayList<>(m.getAtividades());
             final List<Competencia> compList = new ArrayList<>(m.getCompetencias());
             return Arbitraries.integers().between(0, ativList.size() * compList.size()).map(numLinks -> {
                 for(int i=0; i<numLinks; i++) {
                     Atividade a = ativList.get(i % ativList.size());
                     Competencia c = compList.get((i / ativList.size()) % compList.size());
                     a.getCompetencias().add(c);
                     c.getAtividades().add(a);
                 }
                 return m;
             });
         });
    }
}
