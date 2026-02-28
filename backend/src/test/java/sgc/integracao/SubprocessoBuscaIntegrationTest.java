package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("Busca de Subprocesso Integration")
class SubprocessoBuscaIntegrationTest extends BaseIntegrationTest {

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;
    @Autowired
    private sgc.organizacao.model.UsuarioRepo usuarioRepo;

    @BeforeEach
    void setUp() {

        Unidade raiz = unidadeRepo.findById(1L).orElseThrow();
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_BUSCA");
        unidade.setNome("Unidade de Busca");
        unidade.setUnidadeSuperior(raiz);
        unidade = unidadeRepo.save(unidade);


        processo = Processo.builder()
                .descricao("Processo Busca")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);


        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .processo(processo)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade - Deve encontrar subprocesso existente no banco")
    void buscarPorProcessoEUnidade_Sucesso() throws Exception {
        // Autenticar como ADMIN para ter visão global
        sgc.organizacao.model.Usuario admin = usuarioRepo.findById("111111111111").orElseThrow();
        admin.setPerfilAtivo(sgc.organizacao.model.Perfil.ADMIN);
        admin.setUnidadeAtivaCodigo(1L);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())
        );

        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", processo.getCodigo().toString())
                        .param("siglaUnidade", unidade.getSigla()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(subprocesso.getCodigo()));
    }
}
