package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class SecurityCdu29Test extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Unidade raiz;
    private Unidade u1;
    private Unidade u11;
    private Unidade u2;
    
    private Processo pRaizFinalizado;
    private Processo pU1Finalizado;
    private Processo pU2Finalizado;

    @BeforeEach
    void setUp() {
        // 1. Hierarquia de Unidades
        raiz = unidadeRepo.findById(1L).orElseThrow();
        
        u1 = UnidadeFixture.unidadePadrao();
        u1.setCodigo(null);
        u1.setSigla("U1");
        u1.setUnidadeSuperior(raiz);
        u1 = unidadeRepo.save(u1);

        u11 = UnidadeFixture.unidadePadrao();
        u11.setCodigo(null);
        u11.setSigla("U1.1");
        u11.setUnidadeSuperior(u1);
        u11 = unidadeRepo.save(u11);

        u2 = UnidadeFixture.unidadePadrao();
        u2.setCodigo(null);
        u2.setSigla("U2");
        u2.setUnidadeSuperior(raiz);
        u2 = unidadeRepo.save(u2);

        // 2. Processos Finalizados
        pRaizFinalizado = criarProcesso("Processo Raiz", Set.of(raiz));
        pU1Finalizado = criarProcesso("Processo U1", Set.of(u1));
        pU2Finalizado = criarProcesso("Processo U2", Set.of(u2));
    }

    private Processo criarProcesso(String desc, Set<Unidade> participantes) {
        Processo p = Processo.builder()
                .descricao(desc)
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.FINALIZADO)
                .dataLimite(LocalDateTime.now().minusDays(10))
                .dataFinalizacao(LocalDateTime.now().minusDays(5))
                .build();
        p = processoRepo.save(p);
        p.adicionarParticipantes(participantes);
        return processoRepo.save(p);
    }

    private Usuario criarUsuario(String t, Unidade u, Perfil p) {
        Usuario user = UsuarioFixture.usuarioPadrao();
        user.setTituloEleitoral(t);
        user.setUnidadeLotacao(u);
        user = usuarioRepo.save(user);

        UsuarioPerfil up = UsuarioPerfil.builder()
                .usuario(user)
                .usuarioTitulo(user.getTituloEleitoral())
                .unidade(u)
                .unidadeCodigo(u.getCodigo())
                .perfil(p)
                .build();
        usuarioPerfilRepo.save(up);

        user.setPerfilAtivo(p);
        user.setUnidadeAtivaCodigo(u.getCodigo());
        user.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_" + p.name())));
        return user;
    }

    @Test
    @DisplayName("ADMIN deve ver todos os processos finalizados")
    void adminVeTodos() throws Exception {
        Usuario admin = criarUsuario("111111111111", raiz, Perfil.ADMIN);

        mockMvc.perform(get("/api/processos/finalizados")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Processo Raiz");
                    assertThat(content).contains("Processo U1");
                    assertThat(content).contains("Processo U2");
                });
    }

    @Test
    @DisplayName("CHEFE da U1 deve ver apenas processos da U1 e subordinadas")
    void chefeU1VeApenasSuaHierarquia() throws Exception {
        Usuario chefeU1 = criarUsuario("222222222222", u1, Perfil.CHEFE);

        mockMvc.perform(get("/api/processos/finalizados")
                        .with(user(chefeU1)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Processo U1");
                    assertThat(content).doesNotContain("Processo Raiz");
                    assertThat(content).doesNotContain("Processo U2");
                });
    }

    @Test
    @DisplayName("CHEFE da U1 não deve listar subprocessos de processo da U2")
    void chefeU1NaoListaSubprocessosDeU2() throws Exception {
        Usuario chefeU1 = criarUsuario("222222222222", u1, Perfil.CHEFE);

        mockMvc.perform(get("/api/processos/{id}/subprocessos", pU2Finalizado.getCodigo())
                        .with(user(chefeU1)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CHEFE da U1 deve listar subprocessos de seu próprio processo")
    void chefeU1ListaSeusSubprocessos() throws Exception {
        Usuario chefeU1 = criarUsuario("222222222222", u1, Perfil.CHEFE);

        mockMvc.perform(get("/api/processos/{id}/subprocessos", pU1Finalizado.getCodigo())
                        .with(user(chefeU1)))
                .andExpect(status().isOk());
    }
}
