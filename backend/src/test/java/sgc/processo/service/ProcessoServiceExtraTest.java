package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.*;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.DisponibilizarMapaEmBlocoCommand;
import sgc.processo.model.*;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.subprocesso.dto.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService - Testes Extras de Cobertura")
class ProcessoServiceExtraTest {

    @InjectMocks
    private ProcessoService processoService;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private EmailModelosService emailModelosService;
    @Mock
    private ResponsavelUnidadeService responsavelUnidadeService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private ComumRepo repo;
    @Mock
    private AlertaFacade servicoAlertas;

    @Test
    @DisplayName("executarAcaoEmBloco - DisponibilizarMapaEmBlocoCommand")
    void executarAcaoEmBloco_Disponibilizar() {
        Usuario usuario = new Usuario();
        usuario.setUnidadeAtivaCodigo(10L);
        usuario.setPerfilAtivo(Perfil.CHEFE);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(processo);
        sp.setUnidade(unidade);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        when(consultaService.listarEntidadesPorProcessoEUnidades(anyLong(), anyList()))
                .thenReturn(List.of(sp));
        when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

        DisponibilizarMapaEmBlocoCommand cmd = new DisponibilizarMapaEmBlocoCommand(List.of(10L), LocalDate.now().plusDays(1));
        
        processoService.executarAcaoEmBloco(1L, cmd);

        verify(transicaoService).disponibilizarMapaEmBloco(eq(List.of(sp)), any(), eq(usuario));
    }

    @Test
    @DisplayName("criarNotificacoesFinalizacaoProcesso via Reflexao")
    void criarNotificacoesFinalizacaoProcesso_Reflexao() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo F");
        
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);
        u1.setSigla("OP1");
        
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setTipo(TipoUnidade.INTERMEDIARIA);
        u2.setSigla("INT1");
        
        processo.adicionarParticipantes(Set.of(u1, u2));

        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(u1, u2));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(anyLong())).thenReturn(List.of());
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString())).thenReturn("Corpo");
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(anyString(), anyString(), anyList())).thenReturn("Corpo");
        
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(processoService, "criarNotificacoesFinalizacaoProcesso", processo);
        
        verify(notificacaoService, atLeastOnce()).enfileirar(any());
    }

    @Test
    @DisplayName("validarSelecaoBloco - Erro de tamanho")
    void validarSelecaoBloco_Erro() {
        List<Long> codigos = List.of(1L, 2L);
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        Subprocesso s1 = new Subprocesso();
        s1.setUnidade(u1);
        List<Subprocesso> list = List.of(s1);
        
        assertThrows(ErroValidacao.class, () -> 
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(processoService, "validarSelecaoBloco", codigos, list)
        );
    }
}
