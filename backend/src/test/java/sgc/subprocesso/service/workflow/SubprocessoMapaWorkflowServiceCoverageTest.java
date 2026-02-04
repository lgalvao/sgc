package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra: SubprocessoMapaWorkflowService")
class SubprocessoMapaWorkflowServiceCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private SubprocessoTransicaoService transicaoService;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    private Subprocesso mockSubprocesso(Long codigo, SituacaoSubprocesso situacao, TipoProcesso tipoProcesso) {
        Subprocesso sp = mock(Subprocesso.class);
        lenient().when(sp.getCodigo()).thenReturn(codigo);
        
        Processo p = new Processo();
        p.setTipo(tipoProcesso);
        lenient().when(sp.getProcesso()).thenReturn(p);
        
        lenient().when(sp.getSituacao()).thenReturn(situacao);
        lenient().doCallRealMethod().when(sp).setSituacao(any());
        
        lenient().when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        
        return sp;
    }

    @Test
    @DisplayName("SalvarMapa: Deve mudar para REVISAO_MAPA_AJUSTADO se era vazio e situação REVISAO_CADASTRO_HOMOLOGADA")
    void salvarMapa_RevisaoCadastroHomologada_Para_RevisaoMapaAjustado() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, TipoProcesso.REVISAO);
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sp.getMapa()).thenReturn(mapa);
        
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio
        
        SalvarMapaRequest req = SalvarMapaRequest.builder()
                .competencias(List.of(CompetenciaMapaDto.builder().build())) // Tem novas
                .build();
                
        service.salvarMapaSubprocesso(1L, req);
        
        verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("AdicionarCompetencia: Deve mudar para MAPEAMENTO_MAPA_CRIADO se era vazio e situação MAPEAMENTO_CADASTRO_HOMOLOGADO")
    void adicionarCompetencia_MapeamentoCadastroHomologado_Para_MapeamentoMapaCriado() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sp.getMapa()).thenReturn(mapa);
        
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio
        
        CompetenciaRequest req = CompetenciaRequest.builder()
                .descricao("Nova")
                .atividadesIds(List.of(1L))
                .build();
                
        service.adicionarCompetencia(1L, req);
        
        verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("AdicionarCompetencia: Deve mudar para REVISAO_MAPA_AJUSTADO se era vazio e situação REVISAO_CADASTRO_HOMOLOGADA")
    void adicionarCompetencia_RevisaoCadastroHomologada_Para_RevisaoMapaAjustado() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, TipoProcesso.REVISAO);
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sp.getMapa()).thenReturn(mapa);
        
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio
        
        CompetenciaRequest req = CompetenciaRequest.builder()
                .descricao("Nova")
                .atividadesIds(List.of(1L))
                .build();
                
        service.adicionarCompetencia(1L, req);
        
        verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("RemoverCompetencia: Deve voltar para MAPEAMENTO_CADASTRO_HOMOLOGADO se ficou vazio")
    void removerCompetencia_FicouVazio_VoltaParaCadastroHomologado() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, TipoProcesso.MAPEAMENTO);
        Mapa mapa = mock(Mapa.class);
        when(mapa.getCodigo()).thenReturn(10L);
        when(sp.getMapa()).thenReturn(mapa);
        
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Ficou vazio
        
        service.removerCompetencia(1L, 55L);
        
        verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("ApresentarSugestoes: Sem unidade superior, destino é a própria unidade")
    void apresentarSugestoes_SemUnidadeSuperior() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, TipoProcesso.MAPEAMENTO);
        Mapa mapa = mock(Mapa.class);
        lenient().when(sp.getMapa()).thenReturn(mapa);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        // Unidade superior null
        lenient().when(sp.getUnidade()).thenReturn(unidade);
        
        Usuario usuario = new Usuario();
        
        service.apresentarSugestoes(1L, "Sugestoes", usuario);
        
        verify(transicaoService).registrar(argThat(cmd -> 
            cmd.destino().equals(unidade) && // Destino é a própria unidade
            cmd.tipo() == TipoTransicao.MAPA_SUGESTOES_APRESENTADAS
        ));
    }

    @Test
    @DisplayName("ValidarMapa: Sem unidade superior, destino é a própria unidade")
    void validarMapa_SemUnidadeSuperior() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, TipoProcesso.MAPEAMENTO);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        // Unidade superior null
        lenient().when(sp.getUnidade()).thenReturn(unidade);
        
        Usuario usuario = new Usuario();
        
        service.validarMapa(1L, usuario);
        
        verify(transicaoService).registrar(argThat(cmd -> 
            cmd.destino().equals(unidade) && // Destino é a própria unidade
            cmd.tipo() == TipoTransicao.MAPA_VALIDADO
        ));
    }

    @Test
    @DisplayName("DevolverValidacao: Sem unidade superior, usa própria unidade")
    void devolverValidacao_SemUnidadeSuperior() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, TipoProcesso.MAPEAMENTO);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        // Unidade superior null
        lenient().when(sp.getUnidade()).thenReturn(unidade);
        
        Usuario usuario = new Usuario();
        
        service.devolverValidacao(1L, "Justificativa", usuario);
        
        verify(transicaoService).registrarAnaliseETransicao(argThat(cmd -> 
            cmd.unidadeAnalise().equals(unidade) && 
            cmd.unidadeOrigemTransicao().equals(unidade) &&
            cmd.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA
        ));
    }
    
    @Test
    @DisplayName("AceitarValidacao: Proxima unidade null (topo da cadeia), deve homologar")
    void aceitarValidacao_ProximaUnidadeNull_Homologa() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, TipoProcesso.MAPEAMENTO);
        
        Unidade unidade = mock(Unidade.class);
        Unidade superior = mock(Unidade.class);
        
        when(sp.getUnidade()).thenReturn(unidade);
        when(unidade.getUnidadeSuperior()).thenReturn(superior);
        when(superior.getUnidadeSuperior()).thenReturn(null); // Proxima é null
        
        when(superior.getSigla()).thenReturn("SUP");
        
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        
        service.aceitarValidacao(1L, usuario);
        
        verify(analiseFacade).criarAnalise(eq(sp), argThat(cmd -> 
            cmd.acao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO &&
            "SUP".equals(cmd.siglaUnidade())
        ));
        
        verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("AceitarValidacao: Unidade superior null (topo imediato), deve homologar")
    void aceitarValidacao_UnidadeSuperiorNull_Homologa() {
        Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, TipoProcesso.MAPEAMENTO);
        
        Unidade unidade = mock(Unidade.class);
        
        when(sp.getUnidade()).thenReturn(unidade);
        when(unidade.getUnidadeSuperior()).thenReturn(null); // Superior imediato é null
        
        when(unidade.getSigla()).thenReturn("UNI");
        
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        
        service.aceitarValidacao(1L, usuario);
        
        verify(analiseFacade).criarAnalise(eq(sp), argThat(cmd -> 
            cmd.acao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO &&
            "UNI".equals(cmd.siglaUnidade())
        ));
        
        verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }
}
