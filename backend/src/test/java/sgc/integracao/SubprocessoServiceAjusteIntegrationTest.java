package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Ajustes de Mapa")
class SubprocessoServiceAjusteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;
    private Competencia competencia;
    private Atividade atividade;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_AJUSTE");
        unidade.setNome("Unidade de Ajuste");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste Ajuste")
                .tipo(TipoProcesso.REVISAO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);

        atividade = Atividade.builder().mapa(mapa).descricao("Atividade Antiga").build();
        atividadeRepo.save(atividade);

        competencia = Competencia.builder().mapa(mapa).descricao("Competencia Antiga").atividades(Set.of(atividade)).build();
        competenciaRepo.save(competencia);
    }

    @Test
    @DisplayName("salvarAjustesMapa: deve salvar ajustes e alterar descrição de atividade e competência")
    void salvarAjustesMapa_Sucesso() {
        AtividadeAjusteDto ativAjuste = new AtividadeAjusteDto(atividade.getCodigo(), "Atividade Nova", List.of());
        CompetenciaAjusteDto compAjuste = CompetenciaAjusteDto.builder()
                .codCompetencia(competencia.getCodigo())
                .nome("Competencia Nova")
                .atividades(List.of(ativAjuste))
                .build();

        List<CompetenciaAjusteDto> ajustes = List.of(compAjuste);

        subprocessoService.salvarAjustesMapa(subprocesso.getCodigo(), ajustes);

        Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

        Competencia compAtualizada = competenciaRepo.findById(competencia.getCodigo()).orElseThrow();
        assertThat(compAtualizada.getDescricao()).isEqualTo("Competencia Nova");

        Atividade ativAtualizada = atividadeRepo.findById(atividade.getCodigo()).orElseThrow();
        assertThat(ativAtualizada.getDescricao()).isEqualTo("Atividade Nova");
    }

}
