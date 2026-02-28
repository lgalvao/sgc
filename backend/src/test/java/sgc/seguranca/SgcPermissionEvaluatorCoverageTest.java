package sgc.seguranca;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SgcPermissionEvaluator - Cobertura")
class SgcPermissionEvaluatorCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private ProcessoRepo processoRepo;

    @InjectMocks
    private SgcPermissionEvaluator evaluator;

    private Usuario usuario(Perfil perfil, Long codUnidade) {
        Usuario u = new Usuario();
        u.setPerfilAtivo(perfil);
        u.setUnidadeAtivaCodigo(codUnidade);
        u.setTituloEleitoral("123");
        return u;
    }

    private Subprocesso criarSubprocesso(Long codigo, Long codUnidade) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(Unidade.builder().codigo(codUnidade).sigla("U" + codUnidade).build());
        return sp;
    }

    @Test
    @DisplayName("checkSubprocesso - Processo FINALIZADO bloqueia escrita")
    void checkSubprocesso_Finalizado_BloqueiaEscrita() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        sp.setProcesso(p);

        Usuario user = usuario(Perfil.ADMIN, 10L);

        boolean result = evaluator.checkPermission(user, sp, "EDITAR_CADASTRO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("checkSubprocesso - Processo FINALIZADO permite leitura")
    void checkSubprocesso_Finalizado_PermiteLeitura() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        sp.setProcesso(p);

        Usuario user = usuario(Perfil.ADMIN, 10L); // Admin ve tudo

        boolean result = evaluator.checkPermission(user, sp, "VISUALIZAR");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkSubprocesso - CONSULTAR_PARA_IMPORTACAO permite acesso fora da hierarquia para CHEFE")
    void checkSubprocesso_Importacao_Chefe() {
        Subprocesso sp = criarSubprocesso(1L, 20L); // Unidade diferente
        sp.setProcesso(new Processo()); // Nao finalizado

        Usuario user = usuario(Perfil.CHEFE, 10L);

        boolean result = evaluator.checkPermission(user, sp, "CONSULTAR_PARA_IMPORTACAO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkSubprocesso - VERIFICAR_IMPACTOS permite visualizacao para GESTOR")
    void checkSubprocesso_Impactos_Gestor() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        boolean result = evaluator.checkPermission(user, sp, "VERIFICAR_IMPACTOS");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkSubprocesso - Escrita falha se localizacao diferente da unidade usuario")
    void checkSubprocesso_Escrita_LocalizacaoDiferente() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(20L).build()); // Localizacao difere da unidade origem e do usuario

        Usuario user = usuario(Perfil.CHEFE, 10L); // Chefe na unidade 10

        // Acao de escrita
        boolean result = evaluator.checkPermission(user, sp, "EDITAR_CADASTRO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("checkSubprocesso - Escrita sucesso se localizacao igual unidade usuario")
    void checkSubprocesso_Escrita_LocalizacaoIgual() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Usuario user = usuario(Perfil.CHEFE, 10L); // Chefe deve editar cadastro

        boolean result = evaluator.checkPermission(user, sp, "EDITAR_CADASTRO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkProcesso - FINALIZADO bloqueia escrita")
    void checkProcesso_Finalizado_BloqueiaEscrita() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);

        Usuario user = usuario(Perfil.ADMIN, 10L);

        boolean result = evaluator.checkPermission(user, p, "HOMOLOGAR_CADASTRO_EM_BLOCO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("checkProcesso - Permite acoes em bloco para GESTOR")
    void checkProcesso_Gestor_Bloco() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Usuario user = usuario(Perfil.GESTOR, 10L);

        boolean result = evaluator.checkPermission(user, p, "HOMOLOGAR_CADASTRO_EM_BLOCO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("obterUnidadeLocalizacao - fallback para unidade subprocesso se sem movimentos")
    void obterUnidadeLocalizacao_Fallback() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        // sp.localizacaoAtual is null

        when(movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(Optional.empty());

        Usuario user = usuario(Perfil.CHEFE, 10L);
        // Deve usar unidade do sp (10L) que é igual a do usuario -> allow
        boolean result = evaluator.checkPermission(user, sp, "EDITAR_CADASTRO");

        assertThat(result).isTrue();
        assertThat(sp.getLocalizacaoAtual().getCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("checkHierarquia - GESTOR usa hierarquia service")
    void checkHierarquia_Gestor() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(hierarquiaService.isMesmaOuSubordinada(any(), any())).thenReturn(true);

        boolean result = evaluator.checkPermission(user, sp, "VISUALIZAR");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkHierarquia - GESTOR nega acesso se nao subordinada")
    void checkHierarquia_Gestor_NaoSubordinada() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(hierarquiaService.isMesmaOuSubordinada(any(), any())).thenReturn(false);

        boolean result = evaluator.checkPermission(user, sp, "VISUALIZAR");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasPermission - Collection")
    void hasPermission_Collection() {
        Subprocesso sp1 = criarSubprocesso(1L, 10L);
        sp1.setProcesso(new Processo());
        sp1.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Subprocesso sp2 = criarSubprocesso(2L, 10L);
        sp2.setProcesso(new Processo());
        sp2.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Usuario user = usuario(Perfil.CHEFE, 10L);

        // Using the interface method directly
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        boolean result = evaluator.hasPermission(auth, List.of(sp1, sp2), "EDITAR_CADASTRO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission - ById Subprocesso")
    void hasPermission_ById_Subprocesso() {
        Long spId = 1L;
        Subprocesso sp = criarSubprocesso(spId, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Usuario user = usuario(Perfil.CHEFE, 10L);

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        boolean result = evaluator.hasPermission(auth, spId, "Subprocesso", "EDITAR_CADASTRO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission - ById Processo")
    void hasPermission_ById_Processo() {
        Long procId = 1L;
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(processoRepo.findById(procId)).thenReturn(Optional.of(p));

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        boolean result = evaluator.hasPermission(auth, procId, "Processo", "HOMOLOGAR_CADASTRO_EM_BLOCO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkPerfil - ADMIN actions")
    void checkPerfil_MapActions_Admin() {
        Usuario user = usuario(Perfil.ADMIN, 10L);
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        assertThat(evaluator.checkPermission(user, sp, "EDITAR_MAPA")).isTrue();
        assertThat(evaluator.checkPermission(user, sp, "HOMOLOGAR_MAPA")).isTrue();
    }

    @Test
    @DisplayName("checkPerfil - CHEFE actions")
    void checkPerfil_MapActions_Chefe() {
        Usuario user = usuario(Perfil.CHEFE, 10L);
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        assertThat(evaluator.checkPermission(user, sp, "VALIDAR_MAPA")).isTrue();
        assertThat(evaluator.checkPermission(user, sp, "HOMOLOGAR_MAPA")).isFalse();
    }
}
