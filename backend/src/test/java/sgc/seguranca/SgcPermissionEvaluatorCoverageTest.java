package sgc.seguranca;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.mapa.model.*;
import sgc.subprocesso.service.*;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.seguranca.AcaoPermissao.*;

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

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;

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
    @DisplayName("verificarSubprocesso - Processo FINALIZADO bloqueia escrita")
    void verificarSubprocesso_Finalizado_BloqueiaEscrita() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        sp.setProcesso(p);

        Usuario user = usuario(Perfil.ADMIN, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, EDITAR_CADASTRO);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verificarSubprocesso - Processo FINALIZADO permite leitura")
    void verificarSubprocesso_Finalizado_PermiteLeitura() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        sp.setProcesso(p);

        Usuario user = usuario(Perfil.ADMIN, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, VISUALIZAR_SUBPROCESSO);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarSubprocesso - CONSULTAR_PARA_IMPORTACAO permite acesso fora da hierarquia para CHEFE")
    void verificarSubprocesso_Importacao_Chefe() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        sp.setProcesso(p);

        Usuario user = usuario(Perfil.CHEFE, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, CONSULTAR_PARA_IMPORTACAO);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarSubprocesso - VERIFICAR_IMPACTOS permite visualizacao para GESTOR")
    void verificarSubprocesso_Impactos_Gestor() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, VERIFICAR_IMPACTOS);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarSubprocesso - Escrita falha se localizacao diferente da unidade usuario")
    void verificarSubprocesso_Escrita_LocalizacaoDiferente() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(20L).build());

        Usuario user = usuario(Perfil.CHEFE, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, EDITAR_CADASTRO);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verificarSubprocesso - Escrita sucesso se localizacao igual unidade usuario")
    void verificarSubprocesso_Escrita_LocalizacaoIgual() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Usuario user = usuario(Perfil.CHEFE, 10L);

        boolean result = evaluator.verificarPermissao(user, sp, EDITAR_CADASTRO);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarProcesso - acoes em bloco para ADMIN independem da situacao do processo")
    void verificarProcesso_Admin_Bloco_IndependeDaSituacao() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);

        Usuario user = usuario(Perfil.ADMIN, 10L);

        boolean result = evaluator.verificarPermissao(user, p, HOMOLOGAR_CADASTRO_EM_BLOCO);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarProcesso - GESTOR nao homologa cadastro em bloco")
    void verificarProcesso_Gestor_NaoHomologaCadastroEmBloco() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Usuario user = usuario(Perfil.GESTOR, 10L);

        boolean result = evaluator.verificarPermissao(user, p, HOMOLOGAR_CADASTRO_EM_BLOCO);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("obterUnidadeLocalizacao - fallback para unidade subprocesso se sem movimentos")
    void obterUnidadeLocalizacao_Fallback() {
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());

        when(movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(Optional.empty());

        Usuario user = usuario(Perfil.CHEFE, 10L);
        boolean result = evaluator.verificarPermissao(user, sp, EDITAR_CADASTRO);

        assertThat(result).isTrue();
        assertThat(sp.getLocalizacaoAtual().getCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("verificarHierarquia - GESTOR usa hierarquia service")
    void verificarHierarquia_Gestor() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);

        boolean result = evaluator.verificarPermissao(user, sp, VISUALIZAR_SUBPROCESSO);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verificarHierarquia - GESTOR nega acesso se nao subordinada")
    void verificarHierarquia_Gestor_NaoSubordinada() {
        Subprocesso sp = criarSubprocesso(1L, 20L);
        sp.setProcesso(new Processo());

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(false);

        boolean result = evaluator.verificarPermissao(user, sp, VISUALIZAR_SUBPROCESSO);
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

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(spId)).thenReturn(Optional.of(sp));

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        boolean result = evaluator.hasPermission(auth, spId, "Subprocesso", "EDITAR_CADASTRO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission - ById Processo nega homologacao em bloco para GESTOR")
    void hasPermission_ById_Processo_GestorNaoHomologaEmBloco() {
        Long procId = 1L;
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Usuario user = usuario(Perfil.GESTOR, 10L);

        when(processoRepo.buscarPorCodigoComParticipantes(procId)).thenReturn(Optional.of(p));

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        boolean result = evaluator.hasPermission(auth, procId, "Processo", "HOMOLOGAR_CADASTRO_EM_BLOCO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verificarPerfil - Ações de ADMIN para mapa")
    void verificarPerfil_MapActions_Admin() {
        Usuario user = usuario(Perfil.ADMIN, 10L);
        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        assertThat(evaluator.verificarPermissao(user, sp, EDITAR_MAPA)).isTrue();
        assertThat(evaluator.verificarPermissao(user, sp, HOMOLOGAR_MAPA)).isTrue();
    }

    @Test
    @DisplayName("hasPermission - non-Usuario principal")
    void hasPermission_NonUsuario() {
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-a-usuario");

        assertThat(evaluator.hasPermission(auth, new Processo(), "VISUALIZAR_PROCESSO")).isFalse();
        assertThat(evaluator.hasPermission(auth, 1L, "Processo", "VISUALIZAR_PROCESSO")).isFalse();
    }

    @Test
    @DisplayName("hasPermission - Coleção de alvos")
    void hasPermission_ColecaoAlvos() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);

        Processo p1 = new Processo();
        Processo p2 = new Processo();

        assertThat(evaluator.hasPermission(auth, List.of(p1, p2), "VISUALIZAR_PROCESSO")).isTrue();
    }

    @Test
    @DisplayName("hasPermission - Coleção de IDs")
    void hasPermission_ColecaoIds() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);

        Processo p1 = new Processo();
        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p1));
        when(processoRepo.buscarPorCodigoComParticipantes(2L)).thenReturn(Optional.of(p1));

        assertThat(evaluator.hasPermission(auth, (Serializable) List.of(1L, 2L), "Processo", "VISUALIZAR_PROCESSO")).isTrue();
    }

    @Test
    @DisplayName("hasPermission - Mapa e Atividade")
    void hasPermission_MapaEAtividade() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);

        Subprocesso sp = criarSubprocesso(1L, 10L);
        sp.setProcesso(new Processo());
        sp.setLocalizacaoAtual(Unidade.builder().codigo(10L).build());

        Mapa m = new Mapa();
        m.setCodigo(100L);
        m.setSubprocesso(sp);
        when(mapaRepo.findById(100L)).thenReturn(Optional.of(m));

        Atividade a = new Atividade();
        a.setCodigo(200L);
        a.setMapa(m);
        when(atividadeRepo.findById(200L)).thenReturn(Optional.of(a));

        assertThat(evaluator.hasPermission(auth, m, "EDITAR_MAPA")).isTrue();
        assertThat(evaluator.hasPermission(auth, a, "EDITAR_MAPA")).isTrue();
        assertThat(evaluator.hasPermission(auth, 100L, "Mapa", "EDITAR_MAPA")).isTrue();
        assertThat(evaluator.hasPermission(auth, 200L, "Atividade", "EDITAR_MAPA")).isTrue();
    }

    @Test
    @DisplayName("verificarPermissao - Casos nulo e default")
    void verificarPermissao_NuloEDefault() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        assertThat(evaluator.verificarPermissao(null, new Processo(), VISUALIZAR_PROCESSO)).isFalse();
        assertThat(evaluator.verificarPermissao(u, null, VISUALIZAR_PROCESSO)).isFalse();
        assertThat(evaluator.verificarPermissao(u, "string-nao-mapeada", VISUALIZAR_PROCESSO)).isFalse();
        assertThat(evaluator.verificarPermissao(u, List.of(new Processo()), VISUALIZAR_PROCESSO)).isTrue();
    }

    @Test
    @DisplayName("resolverAcao - erro para acao desconhecida")
    void resolverAcao_Erro() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);

        assertThatThrownBy(() -> evaluator.hasPermission(auth, new Processo(), "ACAO_INEXISTENTE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ação de permissão desconhecida");
    }

    @Test
    @DisplayName("hasPermission - default case switch")
    void hasPermission_DefaultCase() {
        Usuario u = usuario(Perfil.ADMIN, 10L);
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);

        assertThat(evaluator.hasPermission(auth, "alvo-desconhecido", "VISUALIZAR_PROCESSO")).isFalse();
        assertThat(evaluator.hasPermission(auth, 1L, "TipoDesconhecido", "VISUALIZAR_PROCESSO")).isFalse();
    }
}
