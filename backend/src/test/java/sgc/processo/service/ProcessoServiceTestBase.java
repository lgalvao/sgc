package sgc.processo.service;

import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.comum.model.*;
import sgc.configuracoes.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.service.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
}
