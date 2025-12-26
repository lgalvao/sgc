package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioPerfil;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoPermissoesServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Test
    @DisplayName("Deve validar acesso da unidade com sucesso")
    void deveValidarAcessoUnidadeComSucesso() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        assertDoesNotThrow(() -> service.validar(sub, 1L, "ENVIAR_REVISAO"));
    }

    @Test
    @DisplayName("Deve lançar erro quando unidade sem acesso")
    void deveLancarErroQuandoUnidadeSemAcesso() {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);

        assertThatThrownBy(() -> service.validar(sub, 2L, "QUALQUER_ACAO"))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Unidade '2' sem acesso a este subprocesso (Unidade do Subprocesso: '1')")
                .hasNoCause();
    }

    @ParameterizedTest
    @CsvSource({
        "ENVIAR_REVISAO",
        "AJUSTAR_MAPA"
    })
    @DisplayName("Deve lançar erro quando situação inválida para ação específica")
    void deveLancarErroQuandoSituacaoInvalidaParaAcao(String acao) {
        Subprocesso sub = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(1L);
        when(sub.getUnidade()).thenReturn(unidade);
        when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

        assertThatThrownBy(() -> service.validar(sub, 1L, acao))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Ação '" + acao + "' inválida")
                .hasNoCause();
    }

    @Test
    @DisplayName("Deve lançar erro quando mapa vazio ao ajustar mapa em situação revisão mapa ajustado")
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
                .hasMessageContaining("mapa do subprocesso '123' está vazio")
                .hasNoCause();
    }

    @Test
    @DisplayName("Deve validar ajuste de mapa com sucesso")
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
