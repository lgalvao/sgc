package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.*;
import sgc.organizacao.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Visualização de Mapa")
@SuppressWarnings("NullAway.Init")
class MapaVisualizacaoServiceTest {
    @Spy
    private MapaDtoMapper mapaDtoMapper = new MapaDtoMapper();
    @Spy
    private OrganizacaoDtoMapper organizacaoDtoMapper = new OrganizacaoDtoMapper();

    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private MapaRepo mapaRepo;
    @InjectMocks
    private MapaVisualizacaoService service;

    private Unidade criarUnidade(Long codigo, String sigla, String nome) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
    }

    @Test
    @DisplayName("Deve obter mapa para visualização")
    void deveObterMapaParaVisualizacao() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);

        Unidade unidade = criarUnidade(100L, "UND100", "Unidade 100");
        sub.setUnidade(unidade);

        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setDescricao("A1");

        Atividade ativ2 = new Atividade(); // Sem competencia
        ativ2.setCodigo(2L);
        ativ2.setDescricao("A2");

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setAtividades(Set.of(ativ1, ativ2));
        sub.setMapa(mapa);

        Competencia comp1 = new Competencia();
        comp1.setCodigo(50L);
        comp1.setDescricao("C1");
        comp1.setAtividades(Set.of(ativ1));

        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapa_Codigo(10L)).thenReturn(List.of(comp1));

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response).isNotNull();
        assertThat(response.competencias()).hasSize(1);
        assertThat(response.atividadesSemCompetencia()).hasSize(1);
        assertThat(response.atividadesSemCompetencia().getFirst().codigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve tratar caso mapa não seja encontrado para visualização, mas fase não exija mapa")
    void deveRetornarVazioCasoNaoEncontreMapaMasSituacaoOk() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setUnidade(criarUnidade(101L, "UND101", "Unidade 101"));
        sub.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.empty());

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response).isNotNull();
        assertThat(response.competencias()).isEmpty();
        assertThat(response.atividadesSemCompetencia()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar ErroInconsistenciaInterna se situação exigir mapa mas não for encontrado")
    void deveLancarErroSeNaoEncontrarMapaNaSituacaoDeMapa() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setUnidade(criarUnidade(102L, "UND102", "Unidade 102"));
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterMapaParaVisualizacao(sub))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("em etapa de mapa sem mapa vinculado para visualizacao");
    }

    @Test
    @DisplayName("Deve retornar vazio se subprocesso sem mapa e com situacao de cadastro")
    void deveTratarSubprocessoComSituacaoDeCadastro() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setUnidade(criarUnidade(103L, "UND103", "Unidade 103"));
        sub.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.empty());

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response).isNotNull();
        assertThat(response.competencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve mapear conhecimentos das atividades")
    void deveMapearConhecimentos() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setUnidade(criarUnidade(104L, "UND104", "Unidade 104"));

        Atividade ativ = new Atividade();
        ativ.setCodigo(1L);
        ativ.setDescricao("A1");

        Conhecimento k = Conhecimento.builder()
                .codigo(100L)
                .descricao("K1")
                .build();
        ativ.setConhecimentos(Set.of(k));

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setAtividades(Set.of(ativ));
        sub.setMapa(mapa);

        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapa_Codigo(10L)).thenReturn(List.of());

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response.atividadesSemCompetencia().getFirst().conhecimentos()).hasSize(1);
        assertThat(response.atividadesSemCompetencia().getFirst().conhecimentos().stream().findFirst())
                .hasValueSatisfying(conhecimento -> assertThat(conhecimento.descricao()).isEqualTo("K1"));
    }

    @Test
    @DisplayName("Deve retornar resposta vazia se mapa não encontrado fora de etapa de mapa")
    void deveRetornarVazioSeMapaNaoEncontradoForaDeEtapaDeMapa() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        Unidade u = criarUnidade(105L, "UND105", "Unidade 105");
        sub.setUnidade(u);
        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.empty());

        MapaVisualizacaoResponse res = service.obterMapaParaVisualizacao(sub);
        assertThat(res).isNotNull();
        assertThat(res.unidade()).isEqualTo(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(u));
        assertThat(res.competencias()).isEmpty();
        assertThat(res.atividadesSemCompetencia()).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar se mapa não encontrado em etapa de mapa")
    void deveFalharSeMapaNaoEncontradoEmEtapaDeMapa() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        sub.setUnidade(criarUnidade(106L, "UND106", "Unidade 106"));
        sub.setMapa(new Mapa());
        when(mapaRepo.buscarCompletoPorSubprocesso(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterMapaParaVisualizacao(sub))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("sem mapa vinculado para visualizacao");
    }

    @Test
    @DisplayName("Deve ignorar mapa pendurado no subprocesso quando o carregamento completo nao encontrar mapa")
    void deveIgnorarMapaPenduradoQuandoCarregamentoCompletoNaoEncontrarMapa() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(3L);
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        sub.setUnidade(criarUnidade(107L, "UND107", "Unidade 107"));
        Mapa mapaPendurado = new Mapa();
        mapaPendurado.setCodigo(300L);
        mapaPendurado.setAtividades(Set.of());
        sub.setMapa(mapaPendurado);
        when(mapaRepo.buscarCompletoPorSubprocesso(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterMapaParaVisualizacao(sub))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("sem mapa vinculado para visualizacao");
        verify(competenciaRepo, never()).findByMapa_Codigo(anyLong());
    }

    @Test
    @DisplayName("Deve retornar atividades sem competência vazias quando mapa não possui atividades")
    void deveRetornarAtividadesVaziasQuandoMapaSemAtividades() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(2L);
        Unidade unidade = criarUnidade(20L, "UND020", "Unidade 20");
        sub.setUnidade(unidade);

        Mapa mapa = new Mapa();
        mapa.setCodigo(200L);
        mapa.setAtividades(Set.of());
        sub.setMapa(mapa);

        when(mapaRepo.buscarCompletoPorSubprocesso(2L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapa_Codigo(200L)).thenReturn(List.of());

        MapaVisualizacaoResponse res = service.obterMapaParaVisualizacao(sub);

        assertThat(res).isNotNull();
        assertThat(res.unidade()).isEqualTo(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(unidade));
        assertThat(res.atividadesSemCompetencia()).isEmpty();
        assertThat(res.competencias()).isEmpty();
    }
}
