package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoTransicaoService Extra Coverage Test")
class SubprocessoTransicaoServiceExtraCoverageTest {
    @InjectMocks
    private SubprocessoTransicaoService service;

    private static final String ADMIN = "ADMIN";
    private static final String SITUACAO = "situacao";
    private static final String OBTER_ULTIMA_DATA_LIMITE = "obterUltimaDataLimite";

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private AlertaFacade alertaService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private SubprocessoNotificacaoService notificacaoService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;

    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private UsuarioFacade usuarioFacade;

    @Test
    @DisplayName("obterSituacaoObrigatoria - deve lançar IllegalStateException para situação não configurada")
    void obterSituacaoObrigatoria_Inexistente() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        
        Map<TipoProcesso, SituacaoSubprocesso> situacoes = Map.of(TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        
        assertThatThrownBy(() -> invokeMethod(service, "obterSituacaoObrigatoria", situacoes, sp, "contexto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem situação configurada");
    }

    @Test
    @DisplayName("apresentarSugestoes - envia para unidade superior")
    void apresentarSugestoes_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla(ADMIN);
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, SITUACAO, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        sp.setMapa(new sgc.mapa.model.Mapa());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.apresentarSugestoes(100L, "sugestoes");

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), admin)));
    }

    @Test
    @DisplayName("validarMapa - envia para unidade superior")
    void validarMapa_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla(ADMIN);
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, SITUACAO, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.validarMapa(100L);

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), admin)));
    }

    @Test
    @DisplayName("aceitarValidacao - encaminha para unidade superior")
    void aceitarValidacao_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla(ADMIN);
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, SITUACAO, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.aceitarValidacao(100L, "obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        verify(analiseRepo).save(any());
        verify(notificacaoService).registrarComunicacoesTransicao(argThat(cmd -> Objects.equals(cmd.unidadeDestino(), admin)));
    }

    @Test
    @DisplayName("executarDisponibilizacaoMapa - valida data limite igual")
    void disponibilizarMapa_DataIgual() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); 
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO); sp.setProcesso(p);
        sp.setMapa(new sgc.mapa.model.Mapa()); sp.getMapa().setCodigo(100L);
        sp.setDataLimiteEtapa1(LocalDateTime.of(2026, 1, 1, 0, 0));
        
        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        sgc.subprocesso.dto.DisponibilizarMapaRequest req = new sgc.subprocesso.dto.DisponibilizarMapaRequest(java.time.LocalDate.of(2026, 1, 1), "Obs");
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.disponibilizarMapa(1L, req);

        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("obterUltimaDataLimite - retorna nulo quando etapa1 e etapa2 ausentes")
    void obterUltimaDataLimite_semDatas() {
        Subprocesso sp = new Subprocesso();

        LocalDate resultado = invokeMethod(service, OBTER_ULTIMA_DATA_LIMITE, sp);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("obterUltimaDataLimite - lança erro quando etapa2 existe sem etapa1")
    void obterUltimaDataLimite_etapa2SemEtapa1() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(123L);
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> invokeMethod(service, OBTER_ULTIMA_DATA_LIMITE, sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite da etapa 1");
    }

    @Test
    @DisplayName("obterUltimaDataLimite - lança erro quando etapa1 é posterior à etapa2")
    void obterUltimaDataLimite_etapa1PosteriorEtapa2() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(456L);
        sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> invokeMethod(service, OBTER_ULTIMA_DATA_LIMITE, sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("etapa 1 posterior à etapa 2");
    }

    @Test
    @DisplayName("disponibilizarMapaEmBloco - deve aceitar lista de subprocessos")
    void disponibilizarMapaEmBloco_ComListaDeSubprocessos() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build());
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.setDataLimiteEtapa1(LocalDateTime.now());
        
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().plusDays(1), "Obs");
        
        service.disponibilizarMapaEmBloco(List.of(sp), req, new Usuario());
        
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
    }

}
