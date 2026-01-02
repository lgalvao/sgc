package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoServiceCoverageTest {

    @InjectMocks
    private SubprocessoService service;

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private AtividadeService atividadeService;

    @Test
    @DisplayName("alterarDataLimite: deve capturar exceção do AlertaService e não falhar")
    void alterarDataLimite_DeveCapturarExceptionAlerta() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(repositorioSubprocesso.findById(codSubprocesso)).thenReturn(Optional.of(sp));
        doThrow(new RuntimeException("Erro envio alerta")).when(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), anyInt());

        assertThatCode(() -> service.alterarDataLimite(codSubprocesso, LocalDate.now()))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("alterarDataLimite: deve atualizar data etapa 2 quando situacao contem MAPA")
    void alterarDataLimite_DeveAtualizarEtapa2() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setUnidade(new Unidade());

        when(repositorioSubprocesso.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        service.alterarDataLimite(codSubprocesso, LocalDate.now());

        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("validarPermissaoEdicaoMapa: deve lançar erro se titular for nulo")
    void validarPermissaoEdicaoMapa_ErroTitularNulo() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade(); // titular null
        sp.setUnidade(u);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456");

        when(repositorioSubprocesso.findByMapaCodigo(codMapa)).thenReturn(Optional.of(sp));
        when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(usuario);

        assertThatThrownBy(() -> service.validarPermissaoEdicaoMapa(codMapa, "user"))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("validarAssociacoesMapa: deve lançar erro se competência não tem atividade")
    void validarAssociacoesMapa_ErroCompetenciaSemAtividade() {
        Long mapaId = 1L;
        Competencia comp = new Competencia();
        comp.setDescricao("Comp 1");
        // atividades empty

        when(competenciaService.buscarPorMapa(mapaId)).thenReturn(List.of(comp));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(mapaId))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("competências que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: deve lançar erro se atividade não tem competência")
    void validarAssociacoesMapa_ErroAtividadeSemCompetencia() {
        Long mapaId = 1L;
        Atividade ativ = new Atividade();
        ativ.setDescricao("Ativ 1");
        // competencias empty

        when(competenciaService.buscarPorMapa(mapaId)).thenReturn(Collections.emptyList());
        when(atividadeService.buscarPorMapaCodigo(mapaId)).thenReturn(List.of(ativ));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(mapaId))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades que não foram associadas");
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento: deve lançar erro se processo for nulo")
    void atualizarSituacaoParaEmAndamento_ErroProcessoNulo() {
        Long mapaCodigo = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(10L);
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setProcesso(null);

        when(repositorioSubprocesso.findByMapaCodigo(mapaCodigo)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.atualizarSituacaoParaEmAndamento(mapaCodigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Processo não associado");
    }

    @Test
    @DisplayName("reabrirCadastro: deve falhar se tipo processo incorreto")
    void reabrirCadastro_ErroTipoProcesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.reabrirCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("apenas para processos de Mapeamento");
    }

    @Test
    @DisplayName("reabrirCadastro: deve falhar se situação inicial")
    void reabrirCadastro_ErroSituacaoInicial() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.reabrirCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ainda está em fase de cadastro");
    }

    @Test
    @DisplayName("reabrirCadastro: sucesso com captura de erro na notificação")
    void reabrirCadastro_SucessoComErroNotificacao() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setUnidade(new Unidade());

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());
        doThrow(new RuntimeException("Erro alerta")).when(alertaService).criarAlertaReaberturaCadastro(any(), any(), any());

        assertThatCode(() -> service.reabrirCadastro(codigo, "justif"))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
        verify(repositorioMovimentacao).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: deve falhar se tipo processo incorreto")
    void reabrirRevisaoCadastro_ErroTipoProcesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("apenas para processos de Revisão");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: deve falhar se situação inicial")
    void reabrirRevisaoCadastro_ErroSituacaoInicial() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ainda está em fase de revisão");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: sucesso com captura de erro na notificação")
    void reabrirRevisaoCadastro_SucessoComErroNotificacao() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setUnidade(new Unidade());

        when(repositorioSubprocesso.findById(codigo)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());
        doThrow(new RuntimeException("Erro alerta")).when(alertaService).criarAlertaReaberturaRevisao(any(), any(), any());

        assertThatCode(() -> service.reabrirRevisaoCadastro(codigo, "justif"))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
        verify(repositorioMovimentacao).save(any(Movimentacao.class));
    }
}
