package sgc.seguranca;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.Authentication;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SgcPermissionEvaluator - Cobertura adicional")
class SgcPermissionEvaluatorCoverageTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private ProcessoRepo processoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private HierarquiaService hierarquiaService;
    @Mock private Authentication authentication;

    @InjectMocks
    private SgcPermissionEvaluator evaluator;

    @Test
    @DisplayName("hasPermission: Deve retornar false se principal não for Usuario (via Serializable)")
    void principalNaoUsuarioSerializable() {
        when(authentication.getPrincipal()).thenReturn("not-a-user");
        boolean result = evaluator.hasPermission(authentication, 1L, "Subprocesso", "VISUALIZAR_SUBPROCESSO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasPermission: Deve tratar coleções como alvo")
    void alvoColecao() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.ADMIN)
                .build();
        when(authentication.getPrincipal()).thenReturn(usuario);
        
        Subprocesso sp1 = mock(Subprocesso.class);
        Subprocesso sp2 = mock(Subprocesso.class);
        // ADMINISTRADOR: verificarSubprocesso chama sp.getProcesso() e depois retorna true para leitura
        when(sp1.getProcesso()).thenReturn(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        when(sp2.getProcesso()).thenReturn(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        
        boolean result = evaluator.hasPermission(authentication, java.util.List.of(sp1, sp2), "VISUALIZAR_SUBPROCESSO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission: Deve tratar coleções como código alvo")
    void codigoAlvoColecao() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.ADMIN)
                .build();
        when(authentication.getPrincipal()).thenReturn(usuario);
        
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(Unidade.builder().codigo(1L).build());
        
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(anyLong())).thenReturn(java.util.Optional.of(sp));

        boolean result = evaluator.hasPermission(authentication, (java.io.Serializable) java.util.List.of(1L, 2L), "Subprocesso", "VISUALIZAR_SUBPROCESSO");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission: Deve cobrir diferentes tipos de alvos (Processo, Mapa, Atividade)")
    void diferentesAlvos() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.ADMIN)
                .build();
        when(authentication.getPrincipal()).thenReturn(usuario);

        Processo p = Processo.builder().codigo(1L).situacao(SituacaoProcesso.EM_ANDAMENTO).build();
        assertThat(evaluator.hasPermission(authentication, p, "VISUALIZAR_PROCESSO")).isTrue();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(Unidade.builder().codigo(1L).build());

        Mapa m = new Mapa();
        m.setSubprocesso(sp);
        assertThat(evaluator.hasPermission(authentication, m, "VISUALIZAR_SUBPROCESSO")).isTrue();

        Atividade a = new Atividade();
        a.setMapa(m);
        assertThat(evaluator.hasPermission(authentication, a, "VISUALIZAR_SUBPROCESSO")).isTrue();
    }

    @Test
    @DisplayName("hasPermission: Deve cobrir busca por código para diferentes tipos (Processo, Mapa, Atividade)")
    void buscaPorCodigoDiferentesTipos() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.ADMIN)
                .build();
        when(authentication.getPrincipal()).thenReturn(usuario);

        Processo p = Processo.builder().codigo(1L).situacao(SituacaoProcesso.EM_ANDAMENTO).build();
        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(java.util.Optional.of(p));
        assertThat(evaluator.hasPermission(authentication, 1L, "Processo", "VISUALIZAR_PROCESSO")).isTrue();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(Unidade.builder().codigo(1L).build());
        
        Mapa m = new Mapa();
        m.setSubprocesso(sp);
        when(mapaRepo.findById(1L)).thenReturn(java.util.Optional.of(m));
        assertThat(evaluator.hasPermission(authentication, 1L, "Mapa", "VISUALIZAR_SUBPROCESSO")).isTrue();

        Atividade a = new Atividade();
        a.setMapa(m);
        when(atividadeRepo.findById(1L)).thenReturn(java.util.Optional.of(a));
        assertThat(evaluator.hasPermission(authentication, 1L, "Atividade", "VISUALIZAR_SUBPROCESSO")).isTrue();
        
        // Alvos não encontrados
        when(processoRepo.buscarPorCodigoComParticipantes(999L)).thenReturn(java.util.Optional.empty());
        assertThat(evaluator.hasPermission(authentication, 999L, "Processo", "VISUALIZAR_PROCESSO")).isFalse();
    }

    @Test
    @DisplayName("verificarSubprocesso: Casos especiais - Importação")
    void verificarSubprocessoImportacao() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.CHEFE)
                .build();
        
        Processo p = Processo.builder().situacao(SituacaoProcesso.FINALIZADO).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(Unidade.builder().codigo(2L).build());

        // CONSULTAR_PARA_IMPORTACAO + CHEFE + FINALIZADO -> retorna true logo de cara
        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).isTrue();
    }

    @Test
    @DisplayName("verificarSubprocesso: Casos especiais - Finalizado nega escrita")
    void verificarSubprocessoFinalizadoEscrita() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.CHEFE)
                .build();
        
        Processo p = Processo.builder().situacao(SituacaoProcesso.FINALIZADO).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(Unidade.builder().codigo(2L).build());

        // FINALIZADO + ESCRITA (deve negar) -> retorna !acao.dependeLocalizacao()
        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.EDITAR_CADASTRO)).isFalse();
    }

    @Test
    @DisplayName("verificarSubprocesso: Casos especiais - Verificar impactos")
    void verificarSubprocessoImpactos() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.CHEFE)
                .build();
        
        Processo p = Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(Unidade.builder().codigo(2L).build());

        // VERIFICAR_IMPACTOS (deve permitir)
        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VERIFICAR_IMPACTOS)).isTrue();
    }

    @Test
    @DisplayName("verificarHierarquia: Gestor e subordinadas")
    void hierarquiaGestor() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.GESTOR)
                .unidadeAtivaCodigo(1L)
                .build();
        
        Unidade unidadeAlvo = Unidade.builder().codigo(2L).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(unidadeAlvo);

        when(hierarquiaService.ehMesmaOuSubordinada(eq(unidadeAlvo), any())).thenReturn(true);
        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VISUALIZAR_SUBPROCESSO)).isTrue();
    }

    @Test
    @DisplayName("verificarHierarquia: Gestor nega se não subordinada")
    void hierarquiaGestorNega() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.GESTOR)
                .unidadeAtivaCodigo(1L)
                .tituloEleitoral("1111111111")
                .build();
        
        Unidade unidadeAlvo = Unidade.builder().codigo(2L).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(unidadeAlvo);

        when(hierarquiaService.ehMesmaOuSubordinada(eq(unidadeAlvo), any())).thenReturn(false);
        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VISUALIZAR_SUBPROCESSO)).isFalse();
    }

    @Test
    @DisplayName("verificarProcesso: Finalizar processo")
    void finalizarProcesso() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.ADMIN)
                .build();
        
        Processo p = Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build();
        assertThat(evaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).isTrue();
        
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        assertThat(evaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).isFalse();
        
        usuario.setPerfilAtivo(Perfil.GESTOR);
        assertThat(evaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).isFalse();
    }

    @Test
    @DisplayName("resolverAcao: Deve lançar exceção para ação inválida")
    void acaoInvalida() {
        Usuario usuario = new Usuario();
        when(authentication.getPrincipal()).thenReturn(usuario);
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> 
            evaluator.hasPermission(authentication, 1L, "Subprocesso", "ACAO_INEXISTENTE")
        );
    }

    @Test
    @DisplayName("mascarar: Deve cobrir strings curtas")
    void mascararCurto() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.CHEFE)
                .unidadeAtivaCodigo(1L)
                .tituloEleitoral("12")
                .build();
        
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(Unidade.builder().codigo(2L).build());

        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VISUALIZAR_SUBPROCESSO)).isFalse();
    }

    @Test
    @DisplayName("mascarar: Deve cobrir nulo")
    void mascararNulo() {
        Usuario usuario = Usuario.builder()
                .perfilAtivo(Perfil.CHEFE)
                .unidadeAtivaCodigo(1L)
                .tituloEleitoral(null)
                .build();
        
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(Unidade.builder().codigo(2L).build());

        assertThat(evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VISUALIZAR_SUBPROCESSO)).isFalse();
    }
}
