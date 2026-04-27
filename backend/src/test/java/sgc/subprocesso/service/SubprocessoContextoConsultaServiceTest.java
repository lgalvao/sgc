package sgc.subprocesso.service;

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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Test
    void montar_ProcessoFinalizado() {
        ContextoUsuarioAutenticado contextoAuth = mock(ContextoUsuarioAutenticado.class);
        when(contextoAuth.unidadeAtivaCodigo()).thenReturn(1L);
        when(contextoAuth.perfil()).thenReturn(Perfil.ADMIN);

        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoAuth);

        Unidade unidadeUsuario = mock(Unidade.class);
        when(unidadeService.buscarPorCodigoComSuperior(1L)).thenReturn(unidadeUsuario);

        Subprocesso sp = mock(Subprocesso.class);
        Processo proc = mock(Processo.class);
        when(sp.getProcesso()).thenReturn(proc);
        when(proc.getSituacao()).thenReturn(SituacaoProcesso.FINALIZADO);

        Unidade unidadeAlvo = mock(Unidade.class);
        when(unidadeAlvo.getCodigo()).thenReturn(2L);
        when(sp.getUnidade()).thenReturn(unidadeAlvo);

        Unidade localizacao = mock(Unidade.class);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(localizacao);


        SubprocessoContextoConsultaService.ContextoConsultaBase contexto = service.montar(sp, Collections.emptyList());

        assertThat(contexto.perfil()).isEqualTo(Perfil.ADMIN);
        assertThat(contexto.localizacaoAtual()).isEqualTo(localizacao);
        assertThat(contexto.processoFinalizado()).isTrue();
        assertThat(contexto.mesmaUnidade()).isFalse(); // finalizado é false
        assertThat(contexto.mesmaUnidadeAlvo()).isFalse(); // 1 != 2
        assertThat(contexto.temMapaVigente()).isFalse(); // finalizado é false
    }

    @Test
    void montar_ProcessoAndamento_ComMovimentacao() {
        ContextoUsuarioAutenticado contextoAuth = mock(ContextoUsuarioAutenticado.class);
        when(contextoAuth.unidadeAtivaCodigo()).thenReturn(1L);
        when(contextoAuth.perfil()).thenReturn(Perfil.CHEFE);

        when(usuarioFacade.contextoAutenticado()).thenReturn(contextoAuth);

        Unidade unidadeUsuario = mock(Unidade.class);
        when(unidadeService.buscarPorCodigoComSuperior(1L)).thenReturn(unidadeUsuario);

        Subprocesso sp = mock(Subprocesso.class);
        Processo proc = mock(Processo.class);
        when(sp.getProcesso()).thenReturn(proc);
        when(proc.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);

        Unidade unidadeAlvo = mock(Unidade.class);
        when(unidadeAlvo.getCodigo()).thenReturn(1L);
        when(sp.getUnidade()).thenReturn(unidadeAlvo);

        Movimentacao mov = mock(Movimentacao.class);
        Unidade unidadeDestino = mock(Unidade.class);
        when(unidadeDestino.getCodigo()).thenReturn(1L);
        when(mov.getUnidadeDestino()).thenReturn(unidadeDestino);

        when(unidadeService.temMapaVigente(1L)).thenReturn(true);

        SubprocessoContextoConsultaService.ContextoConsultaBase contexto = service.montar(sp, List.of(mov));

        assertThat(contexto.perfil()).isEqualTo(Perfil.CHEFE);
        assertThat(contexto.localizacaoAtual()).isEqualTo(unidadeDestino);
        assertThat(contexto.processoFinalizado()).isFalse();
        assertThat(contexto.mesmaUnidade()).isTrue(); // ativa = 1, loc = 1
        assertThat(contexto.mesmaUnidadeAlvo()).isTrue(); // ativa = 1, alvo = 1
        assertThat(contexto.unidadeAlvoNaHierarquiaUsuario()).isTrue(); // mesma unidade
        assertThat(contexto.temMapaVigente()).isTrue();
    }
}
