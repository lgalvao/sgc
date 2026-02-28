package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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

    private Subprocesso subprocesso;
    private Competencia competencia;
    private Atividade atividade;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_AJUSTE");
        unidade.setNome("Unidade de Ajuste");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
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
