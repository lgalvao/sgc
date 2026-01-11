package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para SubprocessoPermissaoCalculator")
class SubprocessoPermissaoCalculatorTest {

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private SubprocessoPermissaoCalculator calculator;

    @Test
    @DisplayName("Deve calcular todas as permissões usando AccessControlService")
    void deveCalcularTodasPermissoes() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();

        when(accessControlService.podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso)).thenReturn(true);
        when(accessControlService.podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso)).thenReturn(true);
        when(accessControlService.podeExecutar(usuario, Acao.VISUALIZAR_MAPA, subprocesso)).thenReturn(true);
        when(accessControlService.podeExecutar(usuario, Acao.DISPONIBILIZAR_MAPA, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.DISPONIBILIZAR_CADASTRO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.DEVOLVER_CADASTRO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.ACEITAR_CADASTRO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.VISUALIZAR_DIAGNOSTICO, subprocesso)).thenReturn(true);
        when(accessControlService.podeExecutar(usuario, Acao.ALTERAR_DATA_LIMITE, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso)).thenReturn(true);
        when(accessControlService.podeExecutar(usuario, Acao.REALIZAR_AUTOAVALIACAO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.REABRIR_CADASTRO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.REABRIR_REVISAO, subprocesso)).thenReturn(false);
        when(accessControlService.podeExecutar(usuario, Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso)).thenReturn(true);

        SubprocessoPermissoesDto resultado = calculator.calcular(subprocesso, usuario);

        assertThat(resultado.isPodeVerPagina()).isTrue();
        assertThat(resultado.isPodeEditarMapa()).isTrue();
        assertThat(resultado.isPodeVisualizarMapa()).isTrue();
        assertThat(resultado.isPodeDisponibilizarMapa()).isFalse();
        assertThat(resultado.isPodeDisponibilizarCadastro()).isFalse();
        assertThat(resultado.isPodeDevolverCadastro()).isFalse();
        assertThat(resultado.isPodeAceitarCadastro()).isFalse();
        assertThat(resultado.isPodeVisualizarDiagnostico()).isTrue();
        assertThat(resultado.isPodeAlterarDataLimite()).isFalse();
        assertThat(resultado.isPodeVisualizarImpacto()).isTrue();
        assertThat(resultado.isPodeRealizarAutoavaliacao()).isFalse();
        assertThat(resultado.isPodeReabrirCadastro()).isFalse();
        assertThat(resultado.isPodeReabrirRevisao()).isFalse();
        assertThat(resultado.isPodeEnviarLembrete()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar todas as permissões como false quando usuário não tem acesso")
    void deveRetornarTodasPermissoesFalse() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();

        SubprocessoPermissoesDto resultado = calculator.calcular(subprocesso, usuario);

        assertThat(resultado.isPodeVerPagina()).isFalse();
        assertThat(resultado.isPodeEditarMapa()).isFalse();
        assertThat(resultado.isPodeVisualizarMapa()).isFalse();
        assertThat(resultado.isPodeDisponibilizarMapa()).isFalse();
        assertThat(resultado.isPodeDisponibilizarCadastro()).isFalse();
        assertThat(resultado.isPodeDevolverCadastro()).isFalse();
        assertThat(resultado.isPodeAceitarCadastro()).isFalse();
        assertThat(resultado.isPodeVisualizarDiagnostico()).isFalse();
        assertThat(resultado.isPodeAlterarDataLimite()).isFalse();
        assertThat(resultado.isPodeVisualizarImpacto()).isFalse();
        assertThat(resultado.isPodeRealizarAutoavaliacao()).isFalse();
        assertThat(resultado.isPodeReabrirCadastro()).isFalse();
        assertThat(resultado.isPodeReabrirRevisao()).isFalse();
        assertThat(resultado.isPodeEnviarLembrete()).isFalse();
    }

    @Test
    @DisplayName("Deve calcular permissões quando processo é do tipo REVISAO")
    void deveCalcularPermissoesProcessoRevisao() {
        Usuario usuario = new Usuario();
        
        sgc.processo.model.Processo processo = new sgc.processo.model.Processo();
        processo.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);

        // Verificamos se as ações de REVISAO são chamadas
        lenient().when(accessControlService.podeExecutar(eq(usuario), eq(Acao.DISPONIBILIZAR_REVISAO_CADASTRO), eq(subprocesso))).thenReturn(true);
        lenient().when(accessControlService.podeExecutar(eq(usuario), eq(Acao.DEVOLVER_REVISAO_CADASTRO), eq(subprocesso))).thenReturn(true);
        lenient().when(accessControlService.podeExecutar(eq(usuario), eq(Acao.ACEITAR_REVISAO_CADASTRO), eq(subprocesso))).thenReturn(true);

        SubprocessoPermissoesDto resultado = calculator.calcular(subprocesso, usuario);

        assertThat(resultado.isPodeDisponibilizarCadastro()).isTrue();
        assertThat(resultado.isPodeDevolverCadastro()).isTrue();
        assertThat(resultado.isPodeAceitarCadastro()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular permissões quando processo é nulo")
    void deveCalcularPermissoesProcessoNull() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(null);

        SubprocessoPermissoesDto resultado = calculator.calcular(subprocesso, usuario);
        assertThat(resultado).isNotNull();
    }
}
