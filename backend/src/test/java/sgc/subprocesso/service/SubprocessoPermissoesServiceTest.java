package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoPermissoesServiceTest {

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Test
    void devePermitirVisualizarImpactoParaAdminEmSituacaoCorreta() {
        Usuario admin = mock(Usuario.class);
        when(admin.getPerfis()).thenReturn(Set.of(Perfil.ADMIN));

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

        assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
    }

    @Test
    void naoDevePermitirVisualizarImpactoParaAdminEmSituacaoIncorreta() {
        Usuario admin = mock(Usuario.class);
        when(admin.getPerfis()).thenReturn(Set.of(Perfil.ADMIN));

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.MAPA_ELABORADO);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

        assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
    }

    @Test
    void naoDevePermitirVisualizarImpactoParaNaoAdmin() {
        Usuario gestor = mock(Usuario.class);
        when(gestor.getPerfis()).thenReturn(Set.of(Perfil.GESTOR));

        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS);

        SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

        assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
    }
}
