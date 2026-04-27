package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.AnaliseRepo;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoVisualizacaoServiceTest {

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoAcessoService acessoService;

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private AnaliseHistoricoService analiseHistoricoService;

    @InjectMocks
    private SubprocessoVisualizacaoService service;

    @Test
    void construirDetalheCadastro_deveRetornarDetalhesSemMovimentacoesETitular() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = mock(SubprocessoConsultaService.ContextoConsultaSubprocesso.class);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sgc.processo.model.Processo processo = new sgc.processo.model.Processo(); processo.setDescricao("P1"); sp.setProcesso(processo);
        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setSigla("U1");
        unidadeAlvo.setCodigo(2L);
        unidadeAlvo.setNome("Unidade 1");
        sp.setUnidade(unidadeAlvo);

        when(contexto.subprocesso()).thenReturn(sp);

        Unidade loc = new Unidade();
        loc.setSigla("LOC");
        when(contexto.localizacaoAtual()).thenReturn(loc);

        PermissoesSubprocessoDto permissoes = PermissoesSubprocessoDto.builder()
                .habilitarAcessoCadastro(true)
                .habilitarAcessoMapa(false)
                .build();
        when(acessoService.resolverPermissoes(any())).thenReturn(permissoes);

        SubprocessoDetalheResponse resposta = service.construirDetalheCadastro(contexto);

        assertThat(resposta.subprocesso().codigo()).isEqualTo(1L);
        assertThat(resposta.responsavel()).isNull();
        assertThat(resposta.titular()).isNull();
        assertThat(resposta.movimentacoes()).isEmpty();
        assertThat(resposta.localizacaoAtual()).isEqualTo("LOC");
        assertThat(resposta.permissoes()).isEqualTo(permissoes);
    }
}
