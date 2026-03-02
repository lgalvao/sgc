package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaManutencaoService - Cobertura Extra")
class MapaManutencaoServiceCoverageExtraTest {

    @Mock private AtividadeRepo atividadeRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private ConhecimentoRepo conhecimentoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private ComumRepo repo;
    @Mock private SubprocessoService subprocessoService;

    @InjectMocks
    private MapaManutencaoService mapaService;

    @Test
    void atividadesMapaCodigoSemRelacionamentos_ok() {
        when(atividadeRepo.findByMapaCodigoSemFetch(1L)).thenReturn(List.of(new Atividade()));
        mapaService.atividadesMapaCodigoSemRels(1L);
        verify(atividadeRepo).findByMapaCodigoSemFetch(1L);
    }

    @Test
    void competenciasCodMapaSemRelacionamentos_ok() {
        when(competenciaRepo.findByMapaCodigoSemFetch(1L)).thenReturn(List.of(new Competencia()));
        mapaService.competenciasCodMapaSemRels(1L);
        verify(competenciaRepo).findByMapaCodigoSemFetch(1L);
    }

    @Test
    void codigosAssociacoesCompetenciaAtividade_ok() {
        Object[] row1 = new Object[]{1L, null, 2L};
        Object[] row2 = new Object[]{1L, null, 3L};
        when(competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(1L)).thenReturn(List.of(row1, row2));

        Map<Long, Set<Long>> result = mapaService.codigosAssociacoesCompetenciaAtividade(1L);

        assertEquals(1, result.size());
        assertEquals(2, result.get(1L).size());
    }

    @Test
    void conhecimentosCodMapa_ok() {
        when(conhecimentoRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Conhecimento()));
        mapaService.conhecimentosCodMapa(1L);
        verify(conhecimentoRepo).findByMapaCodigo(1L);
    }

    @Test
    void salvarCompetencia_ok() {
        Competencia c = new Competencia();
        mapaService.salvarCompetencia(c);
        verify(competenciaRepo).save(c);
    }

    @Test
    void excluirMapa_ok() {
        mapaService.excluirMapa(1L);
        verify(mapaRepo).deleteById(1L);
    }

    @Test
    void validarDescricaoAtividadeUnica_erro() {
        Atividade a = new Atividade();
        a.setDescricao("Desc");
        when(atividadeRepo.findByMapaCodigoSemFetch(1L)).thenReturn(List.of(a));

        CriarAtividadeRequest req = new CriarAtividadeRequest(1L, "desc");

        assertThrows(ErroValidacao.class, () -> mapaService.criarAtividade(req));
    }

    @Test
    void validarDescricaoConhecimentoUnica_erro() {
        Conhecimento c = new Conhecimento();
        c.setDescricao("Desc");
        when(conhecimentoRepo.findByAtividade_Codigo(1L)).thenReturn(List.of(c));

        CriarConhecimentoRequest req = new CriarConhecimentoRequest(1L, "desc");

        assertThrows(ErroValidacao.class, () -> mapaService.criarConhecimento(1L, req));
    }
}
