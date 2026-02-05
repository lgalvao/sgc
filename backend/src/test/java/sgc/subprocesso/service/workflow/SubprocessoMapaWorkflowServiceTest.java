package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoMapaWorkflowService")
class SubprocessoMapaWorkflowServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    @Test
    @DisplayName("Deve falhar ao buscar subprocesso inexistente")
    void deveFalharSubprocessoInexistente() {
        when(crudService.buscarSubprocesso(999L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso", 999L));
        CompetenciaRequest request = CompetenciaRequest.builder().build();
        assertThatThrownBy(() -> service.adicionarCompetencia(999L, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    private Subprocesso mockSubprocesso(Long codigo, SituacaoSubprocesso situacao) {
        Subprocesso sp = mock(Subprocesso.class);
        lenient().when(sp.getCodigo()).thenReturn(codigo);
        lenient().when(subprocessoRepo.findById(codigo)).thenReturn(Optional.of(sp));
        lenient().when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
 
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        lenient().when(sp.getProcesso()).thenReturn(p);
 
        Unidade u = mock(Unidade.class);
        lenient().when(u.getUnidadeSuperior()).thenReturn(mock(Unidade.class));
        lenient().when(sp.getUnidade()).thenReturn(u);
 
        lenient().when(sp.getSituacao()).thenReturn(situacao);
        lenient().doCallRealMethod().when(sp).setSituacao(any());
 
        return sp;
    }


    @Nested
    @DisplayName("Edição de Mapa")
    class EdicaoMapa {
        @Test
        @DisplayName("Deve salvar mapa com sucesso e alterar situação se novo")
        void deveSalvarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(subprocessoRepo).save(sp);
            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Não deve mudar status ao remover competência se ficou vazio mas situação era CADASTRO_HOMOLOGADO")
        void naoDeveMudarStatusRemoverCompetenciaSeJaEraCadastroHomologado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Não deve alterar situação ao salvar mapa se já estava criado")
        void naoDeveAlterarSituacaoSalvarMapaSeJaCriado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve permitir edição em REVISAO_CADASTRO_HOMOLOGADA")
        void devePermitirEdicaoEmRevisaoCadastroHomologada() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve falhar ao editar mapa em situação inválida")
        void deveFalharEditarMapaSituacaoInvalida() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            doThrow(new ErroValidacao("Situação inválida"))
                    .when(validacaoService).validarSituacaoPermitida(eq(sp), anyString(), any(SituacaoSubprocesso[].class));

            assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, req))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve adicionar competência e mudar status")
        void deveAdicionarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            CompetenciaRequest req = CompetenciaRequest.builder()
                    .descricao("Nova Comp")
                    .atividadesIds(List.of(1L))
                    .build();

            service.adicionarCompetencia(1L, req);

            verify(subprocessoRepo).save(sp);
            verify(mapaManutencaoService).criarCompetenciaComAtividades(mapa, "Nova Comp", List.of(1L));
        }

        @Test
        @DisplayName("Não deve mudar status ao adicionar competência se status diferente de CADASTRO_HOMOLOGADO")
        void naoDeveMudarStatusAdicionarCompetenciaSeDiferente() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            CompetenciaRequest req = CompetenciaRequest.builder()
                    .descricao("Nova Comp")
                    .build();

            service.adicionarCompetencia(1L, req);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Não deve mudar status ao adicionar competência se mapa não era vazio")
        void naoDeveMudarStatusAdicionarCompetenciaSeNaoEraVazio() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            CompetenciaRequest req = CompetenciaRequest.builder()
                    .descricao("Nova Comp")
                    .build();

            service.adicionarCompetencia(1L, req);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve atualizar competência")
        void deveAtualizarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            CompetenciaRequest req = CompetenciaRequest.builder()
                    .descricao("Comp Atualizada")
                    .atividadesIds(List.of(2L))
                    .build();

            service.atualizarCompetencia(1L, 5L, req);

            verify(mapaManutencaoService).atualizarCompetencia(5L, "Comp Atualizada", List.of(2L));
        }

        @Test
        @DisplayName("Deve remover competência e voltar status se vazio")
        void deveRemoverCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L);

            verify(mapaManutencaoService).removerCompetencia(5L);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Não deve mudar status ao remover competência se não ficou vazio")
        void naoDeveMudarStatusRemoverCompetenciaSeNaoFicouVazio() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            service.removerCompetencia(1L, 5L);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve mudar status ao remover competência se ficou vazio e situação é REVISAO_MAPA_AJUSTADO")
        void deveMudarStatusRemoverCompetenciaSeRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L);

            verify(subprocessoRepo).save(sp);
            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        }
    }

    @Nested
    @DisplayName("Disponibilização de Mapa")
    class Disponibilizacao {
        @Test
        @DisplayName("Deve disponibilizar mapa com sucesso")
        void deveDisponibilizarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of());

            Unidade sedoc = new Unidade();
            sedoc.setCodigo(99L);
            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedoc);

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("Obs")
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(sp) &&
                            cmd.tipo() == TipoTransicao.MAPA_DISPONIBILIZADO &&
                            "Obs".equals(cmd.observacoes())
            ));
        }

        @Test
        @DisplayName("Deve falhar ao disponibilizar se competência sem atividade")
        void deveFalharCompetenciaSemAtividade() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>());
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .build();
            Usuario usuario = new Usuario();

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, usuario))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("pelo menos uma atividade");
        }

        @Test
        @DisplayName("Deve falhar ao disponibilizar se atividade sem competência")
        void deveFalharAtividadeSemCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            Atividade a1 = new Atividade();
            a1.setCodigo(1L);
            c.setAtividades(new HashSet<>(List.of(a1)));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

            Atividade a2 = new Atividade();
            a2.setCodigo(2L);
            a2.setDescricao("Orfã");
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a1, a2));

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .build();
            Usuario usuario = new Usuario();

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, usuario))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Orfã");
        }
    }

    @Nested
    @DisplayName("Workflow de Validação")
    class WorkflowValidacao {
        @Test
        @DisplayName("Deve apresentar sugestões")
        void deveApresentarSugestoes() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Mapa mapa = mock(Mapa.class);
            when(sp.getMapa()).thenReturn(mapa);

            service.apresentarSugestoes(1L, "Sugestao", new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(sp) &&
                            cmd.tipo() == TipoTransicao.MAPA_SUGESTOES_APRESENTADAS &&
                            "Sugestao".equals(cmd.observacoes())
            ));
        }

        @Test
        @DisplayName("Deve apresentar sugestões para tipo REVISAO")
        void deveApresentarSugestoesRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));

            service.apresentarSugestoes(1L, "Sugestoes", new Usuario());

            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        }

        @Test
        @DisplayName("Deve validar mapa")
        void deveValidarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

            service.validarMapa(1L, new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(sp) &&
                            cmd.tipo() == TipoTransicao.MAPA_VALIDADO
            ));
        }

        @Test
        @DisplayName("Deve validar mapa para tipo REVISAO")
        void deveValidarMapaRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);

            service.validarMapa(1L, new Usuario());

            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
        }

        @Test
        @DisplayName("Deve devolver validação")
        void deveDevolverValidacao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            service.devolverValidacao(1L, "Justificativa", new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO &&
                    req.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA &&
                    "Justificativa".equals(req.observacoes())));
        }

        @Test
        @DisplayName("Deve devolver validação para tipo REVISAO")
        void deveDevolverValidacaoRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);

            service.devolverValidacao(1L, "Justificativa", new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.novaSituacao() == SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO));
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar se topo da cadeia")
        void deveAceitarEHomologarSeTopo() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u = sp.getUnidade();
            Unidade superior = new Unidade();
            superior.setSigla("SUP");
            when(u.getUnidadeSuperior()).thenReturn(superior);

            service.aceitarValidacao(1L, new Usuario());

            verify(analiseFacade).criarAnalise(eq(sp), argThat(req -> req.acao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO));
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar para tipo REVISAO")
        void deveAceitarEHomologarRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Unidade u = sp.getUnidade();
            Unidade superior = new Unidade();
            superior.setSigla("SUP");
            when(u.getUnidadeSuperior()).thenReturn(superior);

            service.aceitarValidacao(1L, new Usuario());

            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("Deve aceitar validação e transitar se não é topo")
        void deveAceitarETransitarSeNaoTopo() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u = sp.getUnidade();
            Unidade superior = mock(Unidade.class);
            Unidade proxima = mock(Unidade.class);
            when(u.getUnidadeSuperior()).thenReturn(superior);
            when(superior.getUnidadeSuperior()).thenReturn(proxima);

            service.aceitarValidacao(1L, new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO &&
                    req.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_ACEITA));
        }

        @Test
        @DisplayName("Deve aceitar validação e transitar para tipo REVISAO")
        void deveAceitarETransitarRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Unidade u = sp.getUnidade();
            Unidade superior = mock(Unidade.class);
            Unidade proxima = mock(Unidade.class);
            when(u.getUnidadeSuperior()).thenReturn(superior);
            when(superior.getUnidadeSuperior()).thenReturn(proxima);

            service.aceitarValidacao(1L, new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.novaSituacao() == SituacaoSubprocesso.REVISAO_MAPA_VALIDADO));
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class EmBloco {
        @Test
        void deveDisponibilizarEmBloco() {
            Subprocesso target = mockSubprocesso(10L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(20L);
            when(target.getMapa()).thenReturn(mapa);
            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(20L)).thenReturn(List.of(c));
            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(20L)).thenReturn(List.of());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .build();

            service.disponibilizarMapaEmBloco(List.of(10L), req, new Usuario());

            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(target) &&
                            cmd.tipo() == TipoTransicao.MAPA_DISPONIBILIZADO
            ));
        }

        @Test
        void deveHomologarEmBloco() {
            Subprocesso target = mockSubprocesso(10L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

            service.homologarValidacaoEmBloco(List.of(10L), new Usuario());

            verify(subprocessoRepo).save(target);
            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(target) &&
                            cmd.tipo() == TipoTransicao.MAPA_HOMOLOGADO
            ));
        }

        @Test
        void deveAceitarEmBloco() {
            Subprocesso target = mockSubprocesso(10L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u = target.getUnidade();
            Unidade superior = mock(Unidade.class);
            when(u.getUnidadeSuperior()).thenReturn(superior);
            when(superior.getUnidadeSuperior()).thenReturn(null);

            service.aceitarValidacaoEmBloco(List.of(10L), new Usuario());

            verify(subprocessoRepo).save(target);
            verify(analiseFacade).criarAnalise(eq(target), any());
        }
    }

    @Nested
    @DisplayName("Submeter Mapa Ajustado")
    class SubmeterAjuste {
        @Test
        void deveSubmeterMapaAjustado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));

            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder()
                    .justificativa("Justificativa")
                    .dataLimiteEtapa2(LocalDateTime.now())
                    .competencias(List.of())
                    .build();

            service.submeterMapaAjustado(1L, req, new Usuario());

            verify(validacaoService).validarAssociacoesMapa(any());
            verify(subprocessoRepo).save(sp);
            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado sem nova data limite")
        void deveSubmeterAjustadoSemDataLimite() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));

            service.submeterMapaAjustado(1L, SubmeterMapaAjustadoRequest.builder()
                    .justificativa("Justificativa")
                    .dataLimiteEtapa2(null)
                    .competencias(List.of())
                    .build(), new Usuario());

            verify(sp, never()).setDataLimiteEtapa2(any());
            verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }
    }
}