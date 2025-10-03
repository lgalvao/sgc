package sgc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.model.*;
import sgc.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for starting a process in REVISAO mode.
 * Executa fluxo completo: cria processo, unidade, mapa vigente com atividades/conhecimentos,
 * popula UNIDADE_MAPA apontando para o mapa vigente e chama endpoint /api/processos/{id}/iniciar?tipo=REVISAO.
 * Verifica que foi criado um novo MAPA (cópia), um SUBPROCESSO vinculado ao novo mapa e movimentação inicial.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProcessoStartRevisionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private MapaRepository mapaRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private ConhecimentoRepository conhecimentoRepository;

    @Autowired
    private UnidadeMapaRepository unidadeMapaRepository;

    @Autowired
    private SubprocessoRepository subprocessoRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void startRevision_shouldCreateCopiedMapaAndSubprocesso() throws Exception {
        // criar unidade
        Unidade u = new Unidade();
        u.setNome("Unidade Int");
        u.setSigla("UI");
        unidadeRepository.save(u);

        // criar mapa fonte com uma atividade e conhecimento
        Mapa fonte = new Mapa();
        fonte.setObservacoesDisponibilizacao("origem");
        mapaRepository.save(fonte);

        Atividade a = new Atividade();
        a.setDescricao("Atividade Origem");
        a.setMapa(fonte);
        atividadeRepository.save(a);

        Conhecimento c = new Conhecimento();
        c.setDescricao("Conhecimento Origem");
        c.setAtividade(a);
        conhecimentoRepository.save(c);

        // registrar mapa vigente para a unidade
        UnidadeMapa um = new UnidadeMapa();
        UnidadeMapa.Id id = new UnidadeMapa.Id();
        id.setUnidadeCodigo(u.getCodigo());
        id.setMapaVigenteCodigo(fonte.getCodigo());
        um.setId(id);
        um.setUnidade(u);
        um.setMapaVigente(fonte);
        unidadeMapaRepository.save(um);

        // criar processo na situação CRIADO
        Processo p = new Processo();
        p.setDescricao("Processo de revisão int");
        p.setTipo("REVISAO");
        p.setSituacao("CRIADO");
        processoRepository.save(p);

        // chamar endpoint iniciar revisão
        mockMvc.perform(post("/api/processos/{id}/iniciar?tipo=REVISAO", p.getCodigo())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(u.getCodigo()))))
                .andExpect(status().isOk());

        // verificar que foi criado um subprocesso vinculado ao processo e que o mapa do subprocesso é diferente do original
        List<Subprocesso> sps = subprocessoRepository.findAll().stream()
                .filter(sp -> sp.getProcesso() != null && p.getCodigo().equals(sp.getProcesso().getCodigo()))
                .toList();

        assertThat(sps).isNotEmpty();
        Subprocesso sp = sps.getFirst();
        assertThat(sp.getMapa()).isNotNull();
        Long novoMapaId = sp.getMapa().getCodigo();
        assertThat(novoMapaId).isNotNull();
        assertThat(novoMapaId).isNotEqualTo(fonte.getCodigo());

        // verificar que atividades e conhecimentos foram copiados para o novo mapa
        List<Atividade> atividadesNovas = atividadeRepository.findAll().stream()
                .filter(at -> at.getMapa() != null && novoMapaId.equals(at.getMapa().getCodigo()))
                .toList();
        assertThat(atividadesNovas).isNotEmpty();

        List<Conhecimento> conhecimentosNovos = conhecimentoRepository.findAll().stream()
                .filter(k -> k.getAtividade() != null && atividadesNovas.stream().anyMatch(a2 -> a2.getCodigo().equals(k.getAtividade().getCodigo())))
                .toList();
        assertThat(conhecimentosNovos).isNotEmpty();

        // verificar existência de movimentação inicial vinculada ao subprocesso
        List<Movimentacao> movs = movimentacaoRepository.findAll().stream()
                .filter(mov -> mov.getSubprocesso() != null && sp.getCodigo().equals(mov.getSubprocesso().getCodigo()))
                .toList();
        assertThat(movs).isNotEmpty();
    }
}