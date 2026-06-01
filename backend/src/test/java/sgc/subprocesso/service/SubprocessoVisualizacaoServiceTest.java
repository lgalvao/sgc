package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoVisualizacaoServiceTest {
    @Mock
    private SubprocessoAcessoService acessoService;
    @Spy
    private OrganizacaoDtoMapper organizacaoDtoMapper = new OrganizacaoDtoMapper();
    @Spy
    private SubprocessoDtoMapper subprocessoDtoMapper = new SubprocessoDtoMapper(new OrganizacaoDtoMapper());

    @InjectMocks
    private SubprocessoVisualizacaoService service;

    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("SIGLA");
        unidade.setNome("Nome da Unidade");

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo");

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    void deveConstruirDetalheCadastro() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = new SubprocessoConsultaService.ContextoConsultaSubprocesso(
                subprocesso, Perfil.SERVIDOR, unidade, false, true, true, true, false
        );

        when(acessoService.resolverPermissoes(contexto)).thenReturn(null);

        SubprocessoDetalheResponse result = service.construirDetalheCadastro(contexto);

        assertThat(result.subprocesso().codigo()).isEqualTo(100L);
        assertThat(result.responsavel()).isNull();
        assertThat(result.titular()).isNull();
        assertThat(result.movimentacoes()).isEmpty();
        assertThat(result.localizacaoAtual()).isEqualTo("SIGLA");
    }
}
