package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import sgc.comum.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@WithMockAdmin
@DisplayName("CDU-21: Finalizar processo de mapeamento ou revisão")
class CDU21IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    private Processo processo;
    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("CDU21-UND");
        unidade.setNome("Unidade CDU-21");
        unidade = unidadeRepo.save(unidade);

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo CDU-21");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(7));
        subprocesso = subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Não deve finalizar quando houver subprocesso não homologado")
    void naoDeveFinalizarQuandoSubprocessosNaoHomologados() throws Exception {
        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS));

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        assertThat(atualizado.getDataFinalizacao()).isNull();
        assertThat(unidadeMapaRepo.findById(unidade.getCodigo())).isEmpty();
    }

    @Test
    @DisplayName("Não deve finalizar revisão quando estiver somente com cadastro homologado")
    void naoDeveFinalizarRevisaoComCadastroHomologado() throws Exception {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS));

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        assertThat(atualizado.getDataFinalizacao()).isNull();
    }

    @Test
    @DisplayName("Deve finalizar processo e definir mapa vigente da unidade")
    void deveFinalizarProcessoEAtualizarMapaVigente() throws Exception {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();

        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(unidade.getCodigo()).orElseThrow();
        assertThat(unidadeMapa.getMapaVigente()).isNotNull();
        assertThat(unidadeMapa.getMapaVigente().getCodigo()).isEqualTo(subprocesso.getMapa().getCodigo());
    }

    @Test
    @DisplayName("Deve finalizar revisão com subprocesso em mapa homologado")
    void deveFinalizarRevisaoComMapaHomologado() throws Exception {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();
    }
}
