package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
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
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoContextoConsultaServiceTest {

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @InjectMocks
    private SubprocessoContextoConsultaService service;

    private Subprocesso subprocesso;
    private Unidade unidadeUsuario;
    private Unidade unidadeAlvo;

    @BeforeEach
    void setUp() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(100L);

        subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeAlvo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setProcesso(processo);

        unidadeUsuario = new Unidade();
        unidadeUsuario.setCodigo(200L);
    }

    @Test
    void shouldMontarContextoBaseSemMovimentacoes() {
        ContextoUsuarioAutenticado contextoUsuario = new ContextoUsuarioAutenticado(
                "123", 200L, Perfil.CHEFE);

        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoUsuario);
        when(unidadeService.buscarPorCodigoComSuperior(200L)).thenReturn(unidadeUsuario);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeUsuario);
        when(hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario)).thenReturn(false);

        SubprocessoContextoConsultaService.ContextoConsultaBase contexto = service.montar(subprocesso, Collections.emptyList());

        assertThat(contexto.perfil()).isEqualTo(Perfil.CHEFE);
        assertThat(contexto.localizacaoAtual()).isEqualTo(unidadeUsuario);
        assertThat(contexto.processoFinalizado()).isFalse();
        assertThat(contexto.mesmaUnidade()).isTrue();
        assertThat(contexto.mesmaUnidadeAlvo()).isFalse();
        assertThat(contexto.unidadeAlvoNaHierarquiaUsuario()).isFalse();
    }

    @Test
    void shouldMontarContextoBaseComMovimentacoes() {
        ContextoUsuarioAutenticado contextoUsuario = new ContextoUsuarioAutenticado(
                "456", 100L, Perfil.SERVIDOR);

        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoUsuario);
        when(unidadeService.buscarPorCodigoComSuperior(100L)).thenReturn(unidadeAlvo);

        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(300L);
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUnidadeDestino(unidadeDestino);

        SubprocessoContextoConsultaService.ContextoConsultaBase contexto = service.montar(subprocesso, List.of(movimentacao));

        assertThat(contexto.perfil()).isEqualTo(Perfil.SERVIDOR);
        assertThat(contexto.localizacaoAtual()).isEqualTo(unidadeDestino);
        assertThat(contexto.processoFinalizado()).isFalse();
        assertThat(contexto.mesmaUnidade()).isFalse();
        assertThat(contexto.mesmaUnidadeAlvo()).isTrue();
        assertThat(contexto.unidadeAlvoNaHierarquiaUsuario()).isTrue();
    }
}
