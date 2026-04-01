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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaManutencaoService - Cobertura extra")
@SuppressWarnings("NullAway.Init")
class MapaManutencaoServiceCoverageExtraTest {

    @Mock private AtividadeRepo atividadeRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private ConhecimentoRepo conhecimentoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private ComumRepo repo;
    @Mock private SubprocessoSituacaoService subprocessoSituacaoService;

    @InjectMocks
    private MapaManutencaoService mapaService;

    @Test
    void atividadesMapaCodigoSemRelacionamentos_ok() {
        when(atividadeRepo.listarPorMapaSemRelacionamentos(1L)).thenReturn(List.of(new Atividade()));
        mapaService.atividadesMapaCodigoSemRels(1L);
        verify(atividadeRepo).listarPorMapaSemRelacionamentos(1L);
    }

    @Test
    void competenciasCodMapaSemRelacionamentos_ok() {
        when(competenciaRepo.listarPorMapaSemRelacionamentos(1L)).thenReturn(List.of(new Competencia()));
        mapaService.competenciasCodMapaSemRels(1L);
        verify(competenciaRepo).listarPorMapaSemRelacionamentos(1L);
    }

    @Test
    void codigosAssociacoesCompetenciaAtividade_ok() {
        Object[] row1 = new Object[]{1L, null, 2L};
        Object[] row2 = new Object[]{1L, null, 3L};
        when(competenciaRepo.listarCodigosCompetenciaEAtividadePorMapa(1L)).thenReturn(List.of(row1, row2));

        Map<Long, Set<Long>> result = mapaService.codigosAssociacoesCompetenciaAtividade(1L);

        assertEquals(1, result.size());
        assertEquals(2, result.getOrDefault(1L, Set.of()).size());
    }

    @Test
    void conhecimentosCodMapa_ok() {
        when(conhecimentoRepo.listarPorMapa(1L)).thenReturn(List.of(new Conhecimento()));
        mapaService.conhecimentosCodMapa(1L);
        verify(conhecimentoRepo).listarPorMapa(1L);
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
        when(atividadeRepo.listarPorMapaSemRelacionamentos(1L)).thenReturn(List.of(a));

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

    @Test
    void atualizarAtividadeNaoDeveValidarDescricaoDuplicadaQuandoDescricaoNaoMuda() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao("Descricao");
        atividade.setMapa(mapa);

        when(repo.buscar(Atividade.class, 1L)).thenReturn(atividade);

        mapaService.atualizarAtividade(1L, new AtualizarAtividadeRequest("descricao"));

        verify(atividadeRepo).save(atividade);
    }

    @Test
    void atualizarDescricoesAtividadeEmBlocoDeveIgnorarDescricaoNula() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao("Original");
        atividade.setMapa(mapa);

        when(atividadeRepo.findAllById(Set.of(1L))).thenReturn(List.of(atividade));

        Map<Long, String> descricoesPorCodigo = new HashMap<>();
        descricoesPorCodigo.put(1L, null);

        mapaService.atualizarDescricoesAtividadeEmBloco(descricoesPorCodigo);

        assertThat(atividade.getDescricao()).isEqualTo("Original");
        verify(atividadeRepo).saveAll(List.of(atividade));
    }

    @Test
    void atualizarConhecimentoNaoDeveValidarDescricaoDuplicadaQuandoDescricaoNaoMuda() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setMapa(mapa);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(2L);
        conhecimento.setDescricao("Conhecimento");
        conhecimento.setAtividade(atividade);

        when(repo.buscar(eq(Conhecimento.class), eq(Map.of("codigo", 2L, "atividade.codigo", 1L))))
                .thenReturn(conhecimento);

        mapaService.atualizarConhecimento(1L, 2L, new AtualizarConhecimentoRequest("conhecimento"));

        verify(conhecimentoRepo, never()).findByAtividade_Codigo(anyLong());
        verify(conhecimentoRepo).save(conhecimento);
    }
}
