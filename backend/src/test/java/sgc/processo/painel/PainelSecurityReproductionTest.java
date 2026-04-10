package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import sgc.alerta.model.*;
import sgc.integracao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PainelController - Reprodução de Vulnerabilidade de Segurança")
class PainelSecurityReproductionTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    private Usuario servidor;
    private Alerta alertaUnidadeAlheia;

    @BeforeEach
    void setUp() {
        // Usuário '1' é SERVIDOR na Unidade 10 (SESEL) no data.sql
        servidor = usuarioRepo.findById("1").orElseThrow();

        Unidade unidadeOrigem = unidadeRepo.findById(10L).orElseThrow();
        Unidade unidadeDestinoAlheia = unidadeRepo.findById(8L).orElseThrow();
        Processo processo = processoRepo.findAll().stream().findFirst().orElseThrow();

        Alerta alerta = new Alerta();
        alerta.setProcesso(processo);
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(unidadeOrigem);
        alerta.setUnidadeDestino(unidadeDestinoAlheia);
        alerta.setDescricao("Alerta fixture de acesso indevido entre unidades");
        alerta.setUsuarioDestinoTitulo(null);
        alertaUnidadeAlheia = alertaRepo.saveAndFlush(alerta);
    }

    private void autenticar(Usuario usuario) {
        usuario.setPerfilAtivo(Perfil.SERVIDOR);
        usuario.setUnidadeAtivaCodigo(10L);
        usuario.setAuthorities(Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + Perfil.SERVIDOR.name())));
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("CENÁRIO POSITIVO: SERVIDOR vê processos da sua unidade")
    void listarProcessos_Sucesso() throws Exception {
        autenticar(servidor);

        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "SERVIDOR")
                        .param("unidade", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].unidadeCodigo", everyItem(is(10))));
    }

    @Test
    @DisplayName("SEGURANÇA: Sistema ignora perfil malicioso na URL e usa o Perfil do Token")
    void listarProcessos_BypassPerfil_DeveEstarCorrigido() throws Exception {
        // Usuário está autenticado como SERVIDOR na Unidade 10
        autenticar(servidor);

        // Atacante tenta passar ADMIN e Unidade 1 na URL
        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "ADMIN")
                        .param("unidade", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].unidadeCodigo", everyItem(is(10))));
    }

    @Test
    @DisplayName("SEGURANÇA: Sistema ignora unidade maliciosa na URL e não expõe alerta de fixture de outra unidade")
    void listarAlertas_AcessoUnidadeAlheia_DeveEstarCorrigido() throws Exception {
        // Usuário está autenticado na Unidade 10
        autenticar(servidor);

        // Atacante tenta listar alertas da Unidade 8 pela URL
        mockMvc.perform(get("/api/painel/alertas")
                        .param("unidade", "8")
                        .param("usuarioTitulo", "TITULO_ALHEIO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].codigo", not(hasItem(alertaUnidadeAlheia.getCodigo().intValue()))));
    }

    @Test
    @DisplayName("SEGURANÇA: Servidor não deve receber alerta coletivo de unidade diferente")
    void listarAlertas_NaoRetornaAlertaColetivoDeOutraUnidade() throws Exception {
        autenticar(servidor);

        mockMvc.perform(get("/api/painel/alertas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].codigo", not(hasItem(alertaUnidadeAlheia.getCodigo().intValue()))));
    }

    @Test
    @DisplayName("SEGURANÇA: Endpoint mantém isolamento mesmo com parâmetros conflitantes")
    void listarAlertas_ParamConflitanteMantemIsolamento() throws Exception {
        autenticar(servidor);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("perfil", "ADMIN")
                        .param("unidade", "1")
                        .param("usuarioTitulo", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].codigo", not(hasItem(alertaUnidadeAlheia.getCodigo().intValue()))));
    }
}
