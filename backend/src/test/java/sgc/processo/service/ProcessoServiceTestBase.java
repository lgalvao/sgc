package sgc.processo.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaAplicacaoService;
import sgc.alerta.EmailModelosService;
import sgc.alerta.NotificacaoService;
import sgc.comum.model.ComumRepo;
import sgc.configuracoes.ConfiguracaoService;
import sgc.diagnostico.service.DiagnosticoFluxoService;
import sgc.diagnostico.service.DiagnosticoNotificacaoService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.ProcessoDtoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Base para testes de unidade do ProcessoService, fornecendo mocks e utilitários comuns.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway.Init")
public abstract class ProcessoServiceTestBase {
    @InjectMocks
    protected ProcessoService processoService;
    @Mock
    protected ProcessoRepo processoRepo;
    @Mock
    protected ComumRepo repo;
    @Mock
    protected UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    protected UnidadeService unidadeService;
    @Mock
    protected ResponsavelUnidadeService responsavelUnidadeService;
    @Mock
    protected SubprocessoService subprocessoService;
    @Mock
    protected SubprocessoConsultaService consultaService;
    @Mock
    protected LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    protected SubprocessoValidacaoService validacaoService;
    @Mock
    protected UsuarioAplicacaoService usuarioService;
    @Mock
    protected AlertaAplicacaoService servicoAlertas;
    @Mock
    protected NotificacaoService notificacaoService;
    @Mock
    protected EmailModelosService emailModelosService;
    @Mock
    protected SgcPermissionEvaluator permissionEvaluator;
    @Mock
    protected SubprocessoTransicaoService transicaoService;
    @Mock
    protected CadastroFluxoService cadastroFluxoService;
    @Mock
    protected ConfiguracaoService configuracaoService;
    @Mock
    protected MapaManutencaoService mapaManutencaoService;
    @Mock
    protected DiagnosticoNotificacaoService diagnosticoNotificacaoService;
    @Mock
    protected DiagnosticoFluxoService diagnosticoFluxoService;
    @Spy
    protected ProcessoDtoMapper processoDtoMapper = new ProcessoDtoMapper();

    protected void mockarResponsaveisEfetivos() {
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(true);
    }

    protected Unidade criarUnidadeValida(Long codigo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setSigla("U" + codigo);
        unidade.setNome("Unidade " + codigo);
        return unidade;
    }

    protected Processo criarProcessoTeste(TipoProcesso tipo) {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(tipo);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        return processo;
    }

    protected Subprocesso vincularProcesso(Subprocesso subprocesso, TipoProcesso tipo) {
        subprocesso.setProcesso(criarProcessoTeste(tipo));
        return subprocesso;
    }
}
