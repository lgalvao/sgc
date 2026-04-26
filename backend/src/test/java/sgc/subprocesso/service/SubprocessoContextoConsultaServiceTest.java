package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoContextoConsultaService - Cobertura de Testes")
class SubprocessoContextoConsultaServiceTest {

    @InjectMocks
    private SubprocessoContextoConsultaService target;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @Test
    @DisplayName("montar deve preencher contexto corretamente quando usuario for da mesma unidade alvo, nao finalizado e com mapa vigente")
    void montar_UsuarioMesmaUnidadeNaoFinalizadoComMapa() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidadeAlvo);

        ContextoUsuarioAutenticado contextoUsuario = new ContextoUsuarioAutenticado("tit", 10L, Perfil.ADMIN);
        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoUsuario);

        Unidade unidadeUsuario = new Unidade();
        unidadeUsuario.setCodigo(10L);
        when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(unidadeUsuario);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeAlvo);
        when(unidadeService.temMapaVigente(10L)).thenReturn(true);

        SubprocessoContextoConsultaService.ContextoConsultaBase result = target.montar(subprocesso, List.of());

        assertThat(result.perfil()).isEqualTo(Perfil.ADMIN);
        assertThat(result.localizacaoAtual()).isEqualTo(unidadeAlvo);
        assertThat(result.processoFinalizado()).isFalse();
        assertThat(result.mesmaUnidade()).isTrue();
        assertThat(result.mesmaUnidadeAlvo()).isTrue();
        assertThat(result.unidadeAlvoNaHierarquiaUsuario()).isTrue();
        assertThat(result.temMapaVigente()).isTrue();
    }

    @Test
    @DisplayName("montar deve usar unidade destino da ultima movimentacao como localizacao atual")
    void montar_ComMovimentacao() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidadeAlvo);

        Unidade unidadeMovimentacao = new Unidade();
        unidadeMovimentacao.setCodigo(20L);
        Movimentacao mov = new Movimentacao();
        mov.setUnidadeDestino(unidadeMovimentacao);

        ContextoUsuarioAutenticado contextoUsuario = new ContextoUsuarioAutenticado("tit", 30L, Perfil.GESTOR);
        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoUsuario);

        Unidade unidadeUsuario = new Unidade();
        unidadeUsuario.setCodigo(30L);
        when(unidadeService.buscarPorCodigoComSuperior(30L)).thenReturn(unidadeUsuario);
        when(hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario)).thenReturn(false);
        when(unidadeService.temMapaVigente(10L)).thenReturn(false);

        SubprocessoContextoConsultaService.ContextoConsultaBase result = target.montar(subprocesso, List.of(mov));

        assertThat(result.localizacaoAtual()).isEqualTo(unidadeMovimentacao);
        assertThat(result.mesmaUnidade()).isFalse();
        assertThat(result.mesmaUnidadeAlvo()).isFalse();
        assertThat(result.unidadeAlvoNaHierarquiaUsuario()).isFalse();
        assertThat(result.temMapaVigente()).isFalse();
    }

    @Test
    @DisplayName("montar deve retornar campos ajustados quando processo estiver finalizado")
    void montar_ProcessoFinalizado() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.FINALIZADO);

        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidadeAlvo);

        ContextoUsuarioAutenticado contextoUsuario = new ContextoUsuarioAutenticado("tit", 10L, Perfil.ADMIN);
        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoUsuario);

        Unidade unidadeUsuario = new Unidade();
        unidadeUsuario.setCodigo(10L);
        when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(unidadeUsuario);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeAlvo);

        SubprocessoContextoConsultaService.ContextoConsultaBase result = target.montar(subprocesso, List.of());

        assertThat(result.processoFinalizado()).isTrue();
        assertThat(result.mesmaUnidade()).isFalse(); // !processoFinalizado && ... => falso
        assertThat(result.temMapaVigente()).isFalse(); // !processoFinalizado && ... => falso
    }
}
