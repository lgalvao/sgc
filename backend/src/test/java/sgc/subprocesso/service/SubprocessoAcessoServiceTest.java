package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.model.Perfil;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoAcessoServiceTest {

    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoAcessoService acessoService;

    @Test
    void resolverPermissoes_ProcessoFinalizado() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = mockContexto(true, Perfil.ADMIN, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, true, true);

        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(contexto);

        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
        assertThat(permissoes.habilitarAcessoMapa()).isTrue();
        assertThat(permissoes.habilitarEditarCadastro()).isFalse();
    }

    @Test
    void resolverPermissoes_ChefeMesmaUnidade() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = mockContexto(false, Perfil.CHEFE, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, true, true);

        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(contexto);

        assertThat(permissoes.podeEditarCadastro()).isTrue();
        assertThat(permissoes.podeDisponibilizarCadastro()).isTrue();
        assertThat(permissoes.habilitarEditarCadastro()).isTrue();
    }

    @Test
    void resolverPermissoes_AdminOutraUnidade() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = mockContexto(false, Perfil.ADMIN, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, false, false);

        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(contexto);

        assertThat(permissoes.podeHomologarCadastro()).isTrue();
        assertThat(permissoes.habilitarHomologarCadastro()).isFalse();
    }

    private SubprocessoConsultaService.ContextoConsultaSubprocesso mockContexto(
            boolean processoFinalizado,
            Perfil perfil,
            SituacaoSubprocesso situacao,
            boolean mesmaUnidade,
            boolean mesmaUnidadeAlvo
    ) {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = mock(SubprocessoConsultaService.ContextoConsultaSubprocesso.class);
        when(contexto.processoFinalizado()).thenReturn(processoFinalizado);

        if (!processoFinalizado) {
            when(contexto.situacao()).thenReturn(situacao);
            when(contexto.isChefe()).thenReturn(perfil == Perfil.CHEFE);
            when(contexto.isGestorOuAdmin()).thenReturn(perfil == Perfil.GESTOR || perfil == Perfil.ADMIN);
            when(contexto.isGestor()).thenReturn(perfil == Perfil.GESTOR);
            when(contexto.isAdmin()).thenReturn(perfil == Perfil.ADMIN);
            when(contexto.perfil()).thenReturn(perfil);
            when(contexto.mesmaUnidade()).thenReturn(mesmaUnidade);

            when(contexto.isMesmaUnidadeAlvo()).thenReturn(mesmaUnidadeAlvo);
            when(contexto.isUnidadeAlvoNaHierarquiaUsuario()).thenReturn(mesmaUnidadeAlvo);
        } else {
            when(contexto.situacao()).thenReturn(situacao);
            when(contexto.perfil()).thenReturn(perfil);
            when(contexto.mesmaUnidade()).thenReturn(mesmaUnidade);
            when(contexto.isChefe()).thenReturn(perfil == Perfil.CHEFE);
            when(contexto.isMesmaUnidadeAlvo()).thenReturn(mesmaUnidadeAlvo);
        }

        return contexto;
    }
}
