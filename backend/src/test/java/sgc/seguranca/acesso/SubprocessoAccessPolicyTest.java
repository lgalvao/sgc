package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoAccessPolicyTest {

    @InjectMocks
    private SubprocessoAccessPolicy policy;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    private void mockLocalizacao(Subprocesso sp, Long codUnidadeLocalizacao) {
        if (codUnidadeLocalizacao == null) {
            lenient().when(movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo()))
                    .thenReturn(Optional.empty());
            return;
        }
        Unidade dest = new Unidade();
        dest.setCodigo(codUnidadeLocalizacao);
        Movimentacao m = Movimentacao.builder().unidadeDestino(dest).build();
        lenient().when(movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo()))
                .thenReturn(Optional.of(m));
    }
    
    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Chefe Mesma Unidade")
    void canExecute_VerificarImpactos_ChefeMesmaUnidade() {
        Usuario u = criarUsuario(Perfil.CHEFE, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        mockLocalizacao(sp, 1L); // Localizado na unidade do Chefe

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Chefe Outra Unidade")
    void canExecute_VerificarImpactos_ChefeOutraUnidade() {
        Usuario u = criarUsuario(Perfil.CHEFE, 2L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        mockLocalizacao(sp, 1L); // Localizado na unidade 1, mas usuário é da 2

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Gestor Disponibilizada (Na Unidade)")
    void canExecute_VerificarImpactos_GestorDisponibilizada() {
        Usuario u = criarUsuario(Perfil.GESTOR, 2L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        mockLocalizacao(sp, 2L); // Subiu para a unidade 2 (onde o Gestor está)

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Gestor Disponibilizada (Ainda na Subordinada)")
    void canExecute_VerificarImpactos_GestorAindaNaSubordinada() {
        Usuario u = criarUsuario(Perfil.GESTOR, 2L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        mockLocalizacao(sp, 1L); // Ainda está na unidade 1, Gestor da 2 não pode agir

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Admin Homologada (Na Unidade)")
    void canExecute_VerificarImpactos_AdminHomologada() {
        Usuario u = criarUsuario(Perfil.ADMIN, 99L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, 1L);
        mockLocalizacao(sp, 99L); // Localizado no Admin

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia MesmaOuSubordinada - OK")
    void canExecute_HierarquiaMesmaOuSubordinada_OK() {
        Usuario u = criarUsuario(Perfil.SERVIDOR, 1L); // Unit 1 (Superior)
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L); // Unit 2 (Subordinate)

        when(hierarquiaService.isSubordinada(any(), any())).thenAnswer(inv -> {
            Unidade sub = inv.getArgument(0);
            Unidade sup = inv.getArgument(1);
            return sub.getCodigo().equals(2L) && sup.getCodigo().equals(1L);
        });

        // VISUALIZAR_SUBPROCESSO é leitura -> valida contra sp.getUnidade()
        assertTrue(policy.canExecute(u, Acao.VISUALIZAR_SUBPROCESSO, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia Titular - OK")
    void canExecute_HierarquiaTitular_OK() {
        Usuario u = criarUsuario(Perfil.CHEFE, 1L);
        u.setTituloEleitoral("123");
        
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);
        sp.getUnidade().setTituloTitular("123");

        // DISPONIBILIZAR_CADASTRO é escrita -> valida contra localização
        mockLocalizacao(sp, 1L);

        when(hierarquiaService.isResponsavel(sp.getUnidade(), u)).thenReturn(true);
 
        assertTrue(policy.canExecute(u, Acao.DISPONIBILIZAR_CADASTRO, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia MesmaUnidade - OK")
    void canExecute_HierarquiaMesmaUnidade_OK() {
        Usuario u = criarUsuario(Perfil.CHEFE, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);
        mockLocalizacao(sp, 1L);

        assertTrue(policy.canExecute(u, Acao.EDITAR_CADASTRO, sp));
    }

    @Test
    @DisplayName("canExecute - Acao sem Regras - False")
    void canExecute_AcaoSemRegras() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        assertFalse(policy.canExecute(u, Acao.CRIAR_PROCESSO, sp));
    }

    @Test
    @DisplayName("canExecute - Admin Via Hierarquia (Leitura)")
    void canExecute_AdminViaHierarquia_Leitura() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L); // Unidade RAIZ
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L); // Outra unidade

        // VISUALIZAR_SUBPROCESSO é leitura -> ADMIN bypassa hierarquia
        assertTrue(policy.canExecute(u, Acao.VISUALIZAR_SUBPROCESSO, sp));
    }

    @Test
    @DisplayName("canExecute - Admin Localização (Escrita) - Falha")
    void canExecute_AdminLocalizacao_Escrita_Falha() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L);

        // EDITAR_MAPA é escrita -> Deve estar na unidade 1 do Admin
        mockLocalizacao(sp, 2L); // Está na 2
        assertFalse(policy.canExecute(u, Acao.EDITAR_MAPA, sp));
    }

    @Test
    @DisplayName("canExecute - Admin Localização (Escrita) - Sucesso")
    void canExecute_AdminLocalizacao_Escrita_Sucesso() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L);

        // EDITAR_MAPA é escrita -> Deve estar na unidade 1 do Admin
        mockLocalizacao(sp, 1L); // Está na 1
        assertTrue(policy.canExecute(u, Acao.EDITAR_MAPA, sp));
    }

    @Test
    @DisplayName("canExecute - VerificarImpactos - Complex Logic - Admin")
    void canExecute_VerificarImpactos_Complex_Admin() {
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        Usuario admin = criarUsuario(Perfil.ADMIN, 99L);
        mockLocalizacao(sp, 99L);
        assertTrue(policy.canExecute(admin, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VerificarImpactos - Complex Logic - Gestor")
    void canExecute_VerificarImpactos_Complex_Gestor() {
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        Usuario gestor = criarUsuario(Perfil.GESTOR, 88L);
        mockLocalizacao(sp, 88L);
        assertTrue(policy.canExecute(gestor, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VerificarImpactos - Complex Logic - Chefe Fail")
    void canExecute_VerificarImpactos_Complex_Chefe_Fail() {
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        Usuario chefe = criarUsuario(Perfil.CHEFE, 1L);
        mockLocalizacao(sp, 1L);
        assertFalse(policy.canExecute(chefe, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VerificarImpactos - Complex Logic - Chefe Success")
    void canExecute_VerificarImpactos_Complex_Chefe_Success() {
        Subprocesso spChefe = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        spChefe.setCodigo(100L);
        Usuario chefe = criarUsuario(Perfil.CHEFE, 1L);
        mockLocalizacao(spChefe, 1L);
        assertTrue(policy.canExecute(chefe, Acao.VERIFICAR_IMPACTOS, spChefe));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Combinacoes e Falhas")
    void canExecute_VerificarImpactos_Combinacoes() {
        // 1. CHEFE (Mesma Unidade) + REVISAO_CADASTRO_EM_ANDAMENTO -> True
        Usuario uChefe = criarUsuario(Perfil.CHEFE, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        mockLocalizacao(sp, 1L);
        assertTrue(policy.canExecute(uChefe, Acao.VERIFICAR_IMPACTOS, sp));

        // 2. CHEFE (Outra Unidade/Localização) -> False
        // Use a new object to avoid mock interference
        Subprocesso sp2 = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        mockLocalizacao(sp2, 2L);
        assertFalse(policy.canExecute(uChefe, Acao.VERIFICAR_IMPACTOS, sp2));

        // 3. GESTOR + REVISAO_CADASTRO_DISPONIBILIZADA + Na Unidade -> True
        Usuario uGestor = criarUsuario(Perfil.GESTOR, 88L);
        Subprocesso spRevDisp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        mockLocalizacao(spRevDisp, 88L);
        assertTrue(policy.canExecute(uGestor, Acao.VERIFICAR_IMPACTOS, spRevDisp));

        // 4. ADMIN + REVISAO_CADASTRO_HOMOLOGADA + Na Unidade -> True
        Usuario uAdmin = criarUsuario(Perfil.ADMIN, 99L);
        Subprocesso spRevHom = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, 1L);
        mockLocalizacao(spRevHom, 99L);
        assertTrue(policy.canExecute(uAdmin, Acao.VERIFICAR_IMPACTOS, spRevHom));
    }

    @Test
    @DisplayName("canExecute - Admin Devolver Cadastro - Deve Respeitar Hierarquia")
    void canExecute_AdminDevolverCadastro_DeveRespeitarHierarquia() {
        Usuario u = criarUsuario(Perfil.ADMIN, 99L); // Admin na unidade 99
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, 1L); // SP na unidade 1

        mockLocalizacao(sp, 1L); // SP está fisicamente na unidade 1

        // Admin (99) tentando Devolver Cadastro (localizado em 1) -> DEVE SER FALSE
        assertFalse(policy.canExecute(u, Acao.DEVOLVER_CADASTRO, sp));

        // Admin (1) tentando Devolver Cadastro (localizado em 1) -> DEVE SER TRUE
        Usuario uAdmin1 = criarUsuario(Perfil.ADMIN, 1L);
        assertTrue(policy.canExecute(uAdmin1, Acao.DEVOLVER_CADASTRO, sp));
    }

    private int contadorUsuarios = 0;
    
    private Usuario criarUsuario(Perfil perfil, Long codUnidade) {
        String titulo = "titulo-" + (++contadorUsuarios);
        Usuario u = new Usuario();
        u.setTituloEleitoral(titulo);
        u.setPerfilAtivo(perfil);
        u.setUnidadeAtivaCodigo(codUnidade);
        
        Unidade un = new Unidade();
        un.setCodigo(codUnidade);
        u.setUnidadeLotacao(un);

        return u;
    }

    private Subprocesso criarSubprocesso(SituacaoSubprocesso situacao, Long codUnidade) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo((long) (++contadorUsuarios)); // Reutiliza contador para ID
        sp.setSituacaoForcada(situacao);
        Unidade un = new Unidade();
        un.setCodigo(codUnidade);
        sp.setUnidade(un);

        sgc.processo.model.Processo p = new sgc.processo.model.Processo();
        p.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        sp.setProcesso(p);

        return sp;
    }
}
