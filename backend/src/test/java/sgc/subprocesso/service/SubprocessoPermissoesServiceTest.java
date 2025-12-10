package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubprocessoPermissoesServiceTest {

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Test
    void devePermitirVisualizarImpactoParaAdminEmSituacaoCorreta() {
        Usuario admin = mock(Usuario.class);
        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(
                UsuarioPerfil.builder().perfil(Perfil.ADMIN).unidade(new Unidade()).build());
        when(admin.getTodasAtribuicoes()).thenReturn(atribuicoes);

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        when(sub.getUnidade()).thenReturn(new Unidade());
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(sub.getProcesso()).thenReturn(processo);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

        assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
    }

    @Test
    void naoDevePermitirVisualizarImpactoParaAdminEmSituacaoIncorreta() {
        Usuario admin = mock(Usuario.class);
        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(
                UsuarioPerfil.builder().perfil(Perfil.ADMIN).unidade(new Unidade()).build());
        when(admin.getTodasAtribuicoes()).thenReturn(atribuicoes);

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        when(sub.getUnidade()).thenReturn(new Unidade());
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(sub.getProcesso()).thenReturn(processo);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

        assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
    }

    @Test
    void naoDevePermitirVisualizarImpactoParaNaoAdmin() {
        Usuario gestor = mock(Usuario.class);
        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(
                UsuarioPerfil.builder().perfil(Perfil.GESTOR).unidade(new Unidade()).build());
        when(gestor.getTodasAtribuicoes()).thenReturn(atribuicoes);

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        when(sub.getUnidade()).thenReturn(new Unidade());
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(sub.getProcesso()).thenReturn(processo);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

        assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
    }
}
