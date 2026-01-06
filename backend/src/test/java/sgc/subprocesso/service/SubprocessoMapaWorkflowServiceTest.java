package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseService;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoMapaWorkflowService")
class SubprocessoMapaWorkflowServiceTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private CompetenciaService competenciaService;
    @Mock private AtividadeService atividadeService;
    @Mock private MapaService mapaService;
    @Mock private SubprocessoTransicaoService transicaoService;
    @Mock private AnaliseService analiseService;
    @Mock private UnidadeService unidadeService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private SubprocessoWorkflowExecutor workflowExecutor;

    @InjectMocks private SubprocessoMapaWorkflowService service;

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

            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of()); // Era vazio

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of(new CompetenciaMapaDto())); // Tem novas

            service.salvarMapaSubprocesso(1L, req, "user");

            verify(subprocessoRepo).save(sp);
            verify(mapaService).salvarMapaCompleto(10L, req, "user");
        }

        @Test
        @DisplayName("Deve falhar ao editar mapa em situação inválida")
        void deveFalharEditarMapaSituacaoInvalida() {
            mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of(new CompetenciaMapaDto()));

            assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, req, "user"))
                .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
        }

        @Test
        @DisplayName("Deve adicionar competência e mudar status")
        void deveAdicionarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of());

            CompetenciaReq req = new CompetenciaReq();
            req.setDescricao("Nova Comp");
            req.setAtividadesIds(List.of(1L));

            service.adicionarCompetencia(1L, req, "user");

            verify(subprocessoRepo).save(sp);
            verify(competenciaService).adicionarCompetencia(mapa, "Nova Comp", List.of(1L));
        }

        @Test
        @DisplayName("Deve atualizar competência")
        void deveAtualizarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            CompetenciaReq req = new CompetenciaReq();
            req.setDescricao("Comp Atualizada");
            req.setAtividadesIds(List.of(2L));

            service.atualizarCompetencia(1L, 5L, req, "user");

            verify(competenciaService).atualizarCompetencia(5L, "Comp Atualizada", List.of(2L));
        }

        @Test
        @DisplayName("Deve remover competência e voltar status se vazio")
        void deveRemoverCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Simular que ficou vazio após remover
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L, "user");

            verify(competenciaService).removerCompetencia(5L);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve falhar ao buscar subprocesso sem mapa")
        void deveFalharSubprocessoSemMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            when(sp.getMapa()).thenReturn(null);
            
            SalvarMapaRequest req = new SalvarMapaRequest();
            assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, req, "user"))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("mapa associado");
        }

        @Test
        @DisplayName("Deve salvar mapa sem alterar situação se mapa não era vazio")
        void deveSalvarMapaSemAlterarSituacaoSeNaoVazio() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Mapa já tinha competências (não era vazio)
            Competencia c = new Competencia();
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of(new CompetenciaMapaDto()));

            service.salvarMapaSubprocesso(1L, req, "user");

            // Não deve salvar porque não mudou situação
            verify(subprocessoRepo, never()).save(sp);
            verify(mapaService).salvarMapaCompleto(10L, req, "user");
        }

        @Test
        @DisplayName("Deve salvar mapa sem alterar situação se requisição sem novas competências")
        void deveSalvarMapaSemAlterarSituacaoSeSemNovasCompetencias() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of()); // Era vazio

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of()); // Sem novas competências

            service.salvarMapaSubprocesso(1L, req, "user");

            // Não deve salvar porque não tem novas competências
            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve adicionar competência sem alterar status se já tinha mapa")
        void deveAdicionarCompetenciaSemAlterarStatusSeJaTinhaMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            // Mapa não era vazio
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(new Competencia()));

            CompetenciaReq req = new CompetenciaReq();
            req.setDescricao("Nova Comp");
            req.setAtividadesIds(List.of(1L));

            service.adicionarCompetencia(1L, req, "user");

            // Não deve salvar porque o mapa já não era vazio
            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve remover competência sem alterar status se ainda tem competências")
        void deveRemoverCompetenciaSemAlterarStatusSeAindaTemCompetencias() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Simular que ainda tem competências após remover
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(new Competencia()));

            service.removerCompetencia(1L, 5L, "user");

            verify(competenciaService).removerCompetencia(5L);
            // Não deve salvar porque ainda tem competências
            verify(subprocessoRepo, never()).save(sp);
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

            // Validacao mocks
            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));
            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of()); // Nenhuma extra

            Unidade sedoc = new Unidade();
            sedoc.setCodigo(99L);
            when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(UnidadeDto.builder().codigo(99L).build());
            when(unidadeService.buscarEntidadePorId(99L)).thenReturn(sedoc);

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
            req.setDataLimite(LocalDate.now());
            req.setObservacoes("Obs");

            service.disponibilizarMapa(1L, req, new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(eq(sp), eq(TipoTransicao.MAPA_DISPONIBILIZADO), any(), any(), any(), eq("Obs"));
        }

        @Test
        @DisplayName("Deve falhar ao disponibilizar se competência sem atividade")
        void deveFalharCompetenciaSemAtividade() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>()); // Vazia
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
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
            Atividade a1 = new Atividade(); a1.setCodigo(1L);
            c.setAtividades(new HashSet<>(List.of(a1)));
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));

            Atividade a2 = new Atividade(); a2.setCodigo(2L); a2.setDescricao("Orfã");
            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of(a1, a2));

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
            Usuario usuario = new Usuario();

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, usuario))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Orfã");
        }

        @Test
        @DisplayName("Deve falhar se data limite for nula ao disponibilizar")
        void deveFalharDataLimiteNula() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class); when(mapa.getCodigo()).thenReturn(10L); when(sp.getMapa()).thenReturn(mapa);
            
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(new Competencia() {{ 
                setAtividades(new HashSet<>(List.of(new Atividade()))); 
            }}));
            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
            req.setDataLimite(null);

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, new Usuario()))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("obrigatória");
        }

        @Test
        @DisplayName("Deve disponibilizar mapa sem definir sugestões quando observações null")
        void deveDisponibilizarMapaSemObservacoes() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));
            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of());

            when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(UnidadeDto.builder().codigo(99L).build());
            when(unidadeService.buscarEntidadePorId(99L)).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
            req.setDataLimite(LocalDate.now());
            req.setObservacoes(null); // Observações null

            service.disponibilizarMapa(1L, req, new Usuario());

            // Verifica que sugestões não foi setado (por ser null)
            verify(mapa).setSugestoes(null);
            verify(mapa, never()).setSugestoes(argThat(s -> s != null && !s.isBlank())); 
        }

        @Test
        @DisplayName("Deve disponibilizar mapa sem definir sugestões quando observações blank")
        void deveDisponibilizarMapaComObservacoesBlank() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));
            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of());

            when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(UnidadeDto.builder().codigo(99L).build());
            when(unidadeService.buscarEntidadePorId(99L)).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
            req.setDataLimite(LocalDate.now());
            req.setObservacoes("   "); // Observações blank

            service.disponibilizarMapa(1L, req, new Usuario());

            // Verifica que sugestões não foi setado (por ser blank)
            verify(mapa).setSugestoes(null);
        }

        @Test
        @DisplayName("Deve editar mapa em situação REVISAO_CADASTRO_HOMOLOGADA")
        void deveEditarMapaEmRevisaoCadastroHomologada() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of());

            service.salvarMapaSubprocesso(1L, req, "user");

            verify(mapaService).salvarMapaCompleto(10L, req, "user");
        }

        @Test
        @DisplayName("Deve editar mapa em situação REVISAO_MAPA_AJUSTADO")
        void deveEditarMapaEmRevisaoMapaAjustado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of());

            service.salvarMapaSubprocesso(1L, req, "user");

            verify(mapaService).salvarMapaCompleto(10L, req, "user");
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
            verify(transicaoService).registrar(eq(sp), eq(TipoTransicao.MAPA_SUGESTOES_APRESENTADAS), any(), any(), any(), eq("Sugestao"));
        }

        @Test
        @DisplayName("Deve validar mapa")
        void deveValidarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

            service.validarMapa(1L, new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(eq(sp), eq(TipoTransicao.MAPA_VALIDADO), any(), any(), any());
        }

        @Test
        @DisplayName("Deve devolver validação")
        void deveDevolverValidacao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            service.devolverValidacao(1L, "Justificativa", new Usuario());

            verify(workflowExecutor).registrarAnaliseETransicao(
                eq(sp),
                eq(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO),
                eq(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA),
                eq(TipoAnalise.VALIDACAO),
                eq(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO),
                any(), any(), any(), any(), eq("Justificativa"), eq("Justificativa")
            );
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar se topo da cadeia")
        void deveAceitarEHomologarSeTopo() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            // Unidade superior null ou sem superior -> topo
            Unidade u = sp.getUnidade();
            Unidade superior = new Unidade();
            superior.setSigla("SUP");
            when(u.getUnidadeSuperior()).thenReturn(superior);
            // superior.getUnidadeSuperior() is null implicitly

            service.aceitarValidacao(1L, new Usuario());

            verify(analiseService).criarAnalise(eq(sp), argThat(req -> req.getAcao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO));
            verify(subprocessoRepo).save(sp);
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

            verify(workflowExecutor).registrarAnaliseETransicao(
                eq(sp),
                eq(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO),
                eq(TipoTransicao.MAPA_VALIDACAO_ACEITA),
                any(), any(), any(), any(), any(), any(), any(), any()
            );
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class EmBloco {
         @Test
         void deveDisponibilizarEmBloco() {
             Subprocesso base = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
             base.getProcesso().setCodigo(100L);

             Subprocesso target = mockSubprocesso(2L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
             when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(target));

             // Mock validations for target
             Mapa mapa = mock(Mapa.class); when(mapa.getCodigo()).thenReturn(20L); when(target.getMapa()).thenReturn(mapa);
             Competencia c = new Competencia(); c.setAtividades(new HashSet<>(List.of(new Atividade())));
             when(competenciaService.buscarPorMapa(20L)).thenReturn(List.of(c));
             when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(UnidadeDto.builder().codigo(99L).build());
             when(unidadeService.buscarEntidadePorId(99L)).thenReturn(new Unidade());

             DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
             req.setDataLimite(LocalDate.now());

             service.disponibilizarMapaEmBloco(List.of(10L), 1L, req, new Usuario());

             verify(transicaoService).registrar(eq(target), eq(TipoTransicao.MAPA_DISPONIBILIZADO), any(), any(), any(), any());
         }

         @Test
         void deveHomologarEmBloco() {
             Subprocesso base = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
             base.getProcesso().setCodigo(100L);

             Subprocesso target = mockSubprocesso(2L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
             when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(target));

             when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(UnidadeDto.builder().codigo(99L).build());
             when(unidadeService.buscarEntidadePorId(99L)).thenReturn(new Unidade());

             service.homologarValidacaoEmBloco(List.of(10L), 1L, new Usuario());

             verify(subprocessoRepo).save(target);
             verify(transicaoService).registrar(eq(target), eq(TipoTransicao.MAPA_HOMOLOGADO), any(), any(), any());
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
         void deveAceitarEmBloco() {
             Subprocesso base = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
             base.getProcesso().setCodigo(100L);

             Subprocesso target = mockSubprocesso(2L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
             when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(100L, 10L)).thenReturn(Optional.of(target));

             // Topo da cadeia para simplificar
             Unidade u = target.getUnidade();
             Unidade superior = mock(Unidade.class);
             when(u.getUnidadeSuperior()).thenReturn(superior);
             when(superior.getUnidadeSuperior()).thenReturn(null);

             service.aceitarValidacaoEmBloco(List.of(10L), 1L, new Usuario());

             verify(subprocessoRepo).save(target);
             verify(analiseService).criarAnalise(eq(target), any());
         }
    }

    @Nested
    @DisplayName("Submeter Mapa Ajustado")
    class SubmeterAjuste {
        @Test
        void deveSubmeterMapaAjustado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO); // Revisão
            Mapa m = mock(Mapa.class); when(sp.getMapa()).thenReturn(m);

            SubmeterMapaAjustadoReq req = new SubmeterMapaAjustadoReq();
            req.setDataLimiteEtapa2(java.time.LocalDateTime.now());

            service.submeterMapaAjustado(1L, req, new Usuario());

            verify(subprocessoService).validarAssociacoesMapa(any());
            verify(subprocessoRepo).save(sp);
            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado para tipo MAPEAMENTO")
        void deveSubmeterAjustadoMapeamento() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));

            service.submeterMapaAjustado(1L, new SubmeterMapaAjustadoReq(), new Usuario());

            verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }
    }
    
    @Test
    @DisplayName("Deve falhar ao buscar subprocesso inexistente")
    void deveFalharSubprocessoInexistente() {
        when(subprocessoRepo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.adicionarCompetencia(999L, new CompetenciaReq(), "user"))
            .isInstanceOf(sgc.comum.erros.ErroEntidadeNaoEncontrada.class);
    }

    private Subprocesso mockSubprocesso(Long codigo, SituacaoSubprocesso situacao) {
        Subprocesso sp = mock(Subprocesso.class);
        lenient().when(sp.getCodigo()).thenReturn(codigo);
        lenient().when(subprocessoRepo.findById(codigo)).thenReturn(Optional.of(sp));

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        lenient().when(sp.getProcesso()).thenReturn(p);

        Unidade u = mock(Unidade.class);
        lenient().when(sp.getUnidade()).thenReturn(u);

        // Lenient for situations where we set it
        lenient().when(sp.getSituacao()).thenReturn(situacao);
        lenient().doCallRealMethod().when(sp).setSituacao(any());

        return sp;
    }
}
