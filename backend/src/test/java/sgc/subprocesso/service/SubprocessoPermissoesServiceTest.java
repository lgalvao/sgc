package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Mapa;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubprocessoPermissoesServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Test
    void deveValidarAcessoUnidadeComSucesso() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        assertDoesNotThrow(() -> service.validar(sub, 1L, "ENVIAR_REVISAO"));
    }

    @Test
    void deveLancarErroQuandoUnidadeSemAcesso() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);

        assertThatThrownBy(() -> service.validar(sub, 2L, "QUALQUER_ACAO"))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Unidade '2' sem acesso a este subprocesso (Unidade do Subprocesso: '1')");
    }

    @Test
    void deveLancarErroQuandoSituacaoInvalidaParaEnviarRevisao() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

        assertThatThrownBy(() -> service.validar(sub, 1L, "ENVIAR_REVISAO"))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Ação 'ENVIAR_REVISAO' inválida");
    }

    @Test
    void deveLancarErroQuandoSituacaoInvalidaParaAjustarMapa() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

        assertThatThrownBy(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Ação 'AJUSTAR_MAPA' inválida");
    }

    @Test
    void deveLancarErroQuandoMapaVazioAoAjustarMapaEmSituacaoRevisaoMapaAjustado() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getCodigo()).thenReturn(123L);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sub.getMapa()).thenReturn(mapa);
        
        when(atividadeRepo.countByMapaCodigo(10L)).thenReturn(0L);

        assertThatThrownBy(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("mapa do subprocesso '123' está vazio");
    }

    @Test
    void deveValidarAjusteMapaComSucesso() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sub.getMapa()).thenReturn(mapa);
        
        when(atividadeRepo.countByMapaCodigo(10L)).thenReturn(5L);

        assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
    }

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
