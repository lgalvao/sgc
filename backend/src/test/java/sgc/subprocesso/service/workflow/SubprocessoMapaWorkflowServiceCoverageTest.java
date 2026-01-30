package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@SuppressWarnings("unused")
class SubprocessoMapaWorkflowServiceCoverageTest {
    @InjectMocks
    private SubprocessoMapaWorkflowService service;
    @Mock
    private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private AnaliseFacade analiseFacade;

    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private sgc.subprocesso.service.crud.SubprocessoCrudService crudService;
    @Mock
    private sgc.organizacao.UnidadeFacade unidadeService;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private sgc.subprocesso.service.crud.SubprocessoValidacaoService validacaoService;

    // --- SALVAR MAPA ---

    @Test
    @DisplayName("salvarMapaSubprocesso: erro se situacao invalida")
    void salvarMapaSubprocesso_ErroSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Invalido

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        SalvarMapaRequest request = SalvarMapaRequest.builder().build();
        assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, request))
                .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
    }


    @Test
    @DisplayName("salvarMapaSubprocesso: muda situacao se era vazio e adicionou competencias")
    void salvarMapaSubprocesso_MudaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(Collections.emptyList());

        SalvarMapaRequest req = SalvarMapaRequest.builder()
                .competencias(List.of(CompetenciaMapaDto.builder().build())) // Tem competencia
                .build();

        when(mapaFacade.salvarMapaCompleto(any(), any())).thenReturn(MapaCompletoDto.builder().build());

        service.salvarMapaSubprocesso(1L, req);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- ADICIONAR COMPETENCIA ---

    @Test
    @DisplayName("adicionarCompetencia: muda situacao se era vazio")
    void adicionarCompetencia_MudaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(Collections.emptyList());

        CompetenciaRequest req = CompetenciaRequest.builder()
                .descricao("Nova")
                .build();

        when(mapaFacade.obterMapaCompleto(any(), any())).thenReturn(MapaCompletoDto.builder().build());

        service.adicionarCompetencia(1L, req);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- REMOVER COMPETENCIA ---

    @Test
    @DisplayName("removerCompetencia: volta situacao se ficou vazio")
    void removerCompetencia_VoltaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        // Simula que ficou vazio apos remover
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(Collections.emptyList());

        when(mapaFacade.obterMapaCompleto(any(), any())).thenReturn(MapaCompletoDto.builder().build());

        service.removerCompetencia(1L, 100L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- DISPONIBILIZAR MAPA ---

    @Test
    @DisplayName("disponibilizarMapa: erro se data limite null")
    void disponibilizarMapa_ErroData() {


        DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().build();
        Usuario user = new Usuario();

        assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, user))
                .isInstanceOf(ErroValidacao.class)
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("validarMapaParaDisponibilizacao: erro se competencia sem atividade")
    void validarMapa_ErroCompetenciaSemAtividade() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        sgc.mapa.model.Competencia comp = new sgc.mapa.model.Competencia();
        comp.setAtividades(Collections.emptySet()); // Sem atividade

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(comp));

        DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                .dataLimite(java.time.LocalDate.now())
                .build();
        Usuario user = new Usuario();
        assertThatThrownBy(() -> service.disponibilizarMapa(1L, request, user))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("pelo menos uma atividade");
    }

    @Test
    @DisplayName("validarMapaParaDisponibilizacao: erro se atividade sem competencia")
    void validarMapa_ErroAtividadeSemCompetencia() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        // Competencia com atividade 1
        sgc.mapa.model.Competencia comp = new sgc.mapa.model.Competencia();
        sgc.mapa.model.Atividade a1 = new sgc.mapa.model.Atividade();
        a1.setCodigo(100L);
        comp.setAtividades(java.util.Set.of(a1));

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(comp));

        // Atividades do mapa: 1 e 2. A 2 nao esta associada
        sgc.mapa.model.Atividade a2 = new sgc.mapa.model.Atividade();
        a2.setCodigo(200L);
        a2.setDescricao("A2");
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a1, a2));

        DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                .dataLimite(java.time.LocalDate.now())
                .build();
        Usuario user = new Usuario();
        assertThatThrownBy(() -> service.disponibilizarMapa(1L, request, user))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Atividades pendentes: A2");
    }

    // --- OUTROS FLUXOS DE WORKFLOW ---

    @Test
    @DisplayName("apresentarSugestoes: sucesso")
    void apresentarSugestoes() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setMapa(new Mapa());
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        Unidade u = new Unidade();
        u.setUnidadeSuperior(new Unidade());
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        service.apresentarSugestoes(1L, "Obs", new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
    }

    @Test
    @DisplayName("aceitarValidacao: fim da cadeia (homologacao implicita)")
    void aceitarValidacao_FimCadeia() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Unidade pai = new Unidade();
        pai.setSigla("PAI");
        Unidade u = new Unidade();
        u.setUnidadeSuperior(pai);
        sp.setUnidade(u);
        // pai.unidadeSuperior é null -> fim da cadeia

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");

        service.aceitarValidacao(1L, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(analiseFacade).criarAnalise(any(), any());
        verify(subprocessoRepo).save(sp);
    }
}