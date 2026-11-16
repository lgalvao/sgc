package sesel.sgc.atribuicao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.processo.Processo;
import sesel.sgc.processo.ProcessoRepository;
import sesel.sgc.processo.ProcessoService;
import sesel.sgc.seguranca.Perfil;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.servidor.ServidorRepository;
import sesel.sgc.servidor.ServidorService;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;
import sesel.sgc.unidade.UnidadeService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AtribuicaoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private ServidorRepository servidorRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private AtribuicaoRepository atribuicaoRepository;

    @MockitoBean
    private AtribuicaoService atribuicaoService;

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private ServidorService servidorService;

    @MockitoBean
    private ProcessoService processoService;

    @WithMockUser(username = "admin", roles = { "SEDOC" })
    @Transactional
    @Test
    void deveRetornarOkNaListagemDeAtribuicoesPorUnidade() throws Exception {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste " + UUID.randomUUID());
        unidade.setDescricao("Descricao Teste");
        unidade = unidadeRepository.save(unidade);

        Servidor servidor = new Servidor();
        servidor.setNome("Servidor Teste");
        servidor.setEmail("servidor%s@teste.com".formatted(UUID.randomUUID()));
        servidor.setSenha("123456");
        servidor.setCargo("Analista");
        servidor.setUnidade(unidade);
        servidor.setPerfis(List.of(Perfil.SERVIDOR));
        servidor = servidorRepository.save(servidor);

        Processo processo = new Processo();
        processo.setNome("Processo Teste %s".formatted(UUID.randomUUID()));
        processoRepository.save(processo);

        Atribuicao atribuicao = new Atribuicao();
        atribuicao.setUnidade(unidade);
        atribuicao.setServidor(servidor);
        atribuicao.setProcesso(processo);
        atribuicao.setDataInicio(LocalDate.now());
        atribuicao.setDataTermino(LocalDate.now().plusDays(5));
        atribuicao.setJustificativa("Teste");
        atribuicaoRepository.save(atribuicao);

        mockMvc.perform(get("/atribuicoes/unidade/%s".formatted(unidade.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("atribuicao/atribuicao-lista"));
    }

    @WithMockUser
    @Test
    void deveCriarAtribuicaoERedirecionar() throws Exception {
        // Arrange
        UUID unidadeId = UUID.randomUUID();
        UUID servidorId = UUID.randomUUID();
        String processoId = UUID.randomUUID().toString();
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataTermino = LocalDate.now().plusDays(1);
        String justificativa = "Teste";

        Unidade unidade = new Unidade();
        unidade.setId(unidadeId);
        Servidor servidor = new Servidor();
        servidor.setId(servidorId);
        Processo processo = new Processo();
        processo.setId(UUID.fromString(processoId));

        when(unidadeService.buscarPorId(unidadeId)).thenReturn(unidade);
        when(servidorService.listarPorUnidade(unidadeId)).thenReturn(java.util.List.of(servidor));
        when(processoService.buscarPorId(UUID.fromString(processoId))).thenReturn(processo);

        // Act & Assert
        mockMvc.perform(post("/atribuicoes")
                .param("unidadeId", unidadeId.toString())
                .param("servidorId", servidorId.toString())
                .param("processoId", processoId)
                .param("dataInicio", dataInicio.toString())
                .param("dataTermino", dataTermino.toString())
                .param("justificativa", justificativa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/atribuicoes/unidade/" + unidadeId));
    }

    @WithMockUser
    @Test
    void deveBuscarAtribuicoesPorUnidade() throws Exception {
        // Arrange
        UUID unidadeId = UUID.randomUUID();
        when(atribuicaoService.buscarAtribuicoesPorUnidade(unidadeId)).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/atribuicoes/unidade/" + unidadeId))
                .andExpect(status().isOk())
                .andExpect(view().name("atribuicao/atribuicao-lista"));
    }

    @WithMockUser
    @Test
    void deveBuscarAtribuicoesPorServidor() throws Exception {
        // Arrange
        UUID servidorId = UUID.randomUUID();
        when(atribuicaoService.buscarAtribuicoesPorServidor(servidorId)).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/atribuicoes/servidor/" + servidorId))
                .andExpect(status().isOk())
                .andExpect(view().name("atribuicao/atribuicao-lista"));
    }

    @WithMockUser
    @Test
    void deveRemoverAtribuicaoERedirecionar() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(post("/atribuicoes/remover/" + id)
                .param("unidadeId", unidadeId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/atribuicoes/unidade/" + unidadeId));
    }
}