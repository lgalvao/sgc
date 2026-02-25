package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.model.ComumRepo;
import sgc.subprocesso.dto.MapaAjusteMapper;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.AnaliseRepo;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Contexto")
class SubprocessoServiceContextoTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MovimentacaoRepo movimentacaoRepo;
    @Mock private ComumRepo repo;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private AlertaFacade alertaService;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private UsuarioFacade usuarioFacade; // Renamed to match field usage in SubprocessoService (was usuarioService)
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private CopiaMapaService copiaMapaService;
    @Mock private EmailService emailService;
    @Mock private TemplateEngine templateEngine;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private MapaSalvamentoService mapaSalvamentoService;
    @Mock private MapaAjusteMapper mapaAjusteMapper;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        service.setCopiaMapaService(copiaMapaService);
    }

    private Subprocesso criarSubprocesso(Long codigo) {
        return Subprocesso.builder()
                .codigo(codigo)
                .processo(Processo.builder().codigo(1L).build())
                .unidade(Unidade.builder().codigo(1L).sigla("U1").tituloTitular("T1").build())
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();
    }

    @Test
    @DisplayName("obterDetalhes por ID - Sucesso")
    void obterDetalhesPorId() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = criarSubprocesso(id);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(usuarioFacade.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioFacade.buscarPorLogin("T1")).thenReturn(new Usuario());
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());

        SubprocessoDetalheResponse result = service.obterDetalhes(id, user);

        assertThat(result).isNotNull();
        verify(subprocessoRepo).findByIdWithMapaAndAtividades(id);
    }

    @Test
    @DisplayName("obterDetalhes - Tratar erro ao buscar titular")
    void obterDetalhes_ErroTitular() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = criarSubprocesso(id);

        when(usuarioFacade.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioFacade.buscarPorLogin("T1")).thenThrow(new RuntimeException("Erro"));
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());

        SubprocessoDetalheResponse result = service.obterDetalhes(sp, user);

        assertThat(result).isNotNull();
        assertThat(result.titular()).isNull();
    }

    @Test
    @DisplayName("obterContextoEdicao - Sucesso")
    void obterContextoEdicao() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = criarSubprocesso(id);
        Mapa mapa = Mapa.builder().codigo(10L).build();
        sp.setMapa(mapa);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(usuarioFacade.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioFacade.buscarPorLogin("T1")).thenReturn(new Usuario());
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());

        // Mocking underlying service for internal call to listarAtividadesSubprocesso
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of());

        // Mocking underlying service for mapaPorCodigo (replaced MapaFacade)
        when(mapaManutencaoService.buscarMapaPorCodigo(10L)).thenReturn(new Mapa());

        ContextoEdicaoResponse result = service.obterContextoEdicao(id);

        assertThat(result).isNotNull();
        assertThat(result.unidade().getSigla()).isEqualTo("U1");
    }
}
