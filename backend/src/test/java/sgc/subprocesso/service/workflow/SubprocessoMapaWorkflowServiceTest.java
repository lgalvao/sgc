package sgc.subprocesso.service.workflow;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.analise.AnaliseFacade;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.comum.erros.ErroValidacao;

import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import java.time.LocalDateTime;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.service.ImpactoMapaService;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

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
        assertThatThrownBy(() -> service.adicionarCompetencia(999L, CompetenciaRequest.builder().build()))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    private Subprocesso mockSubprocesso(Long codigo, SituacaoSubprocesso situacao) {
        Subprocesso sp = mock(Subprocesso.class);
        lenient().when(sp.getCodigo()).thenReturn(codigo);
        // Suporte para ambos os padrões enquanto migramos os testes
        lenient().when(subprocessoRepo.findById(codigo)).thenReturn(Optional.of(sp));
        lenient().when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
 
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        lenient().when(sp.getProcesso()).thenReturn(p);
 
        Unidade u = mock(Unidade.class);
        lenient().when(u.getUnidadeSuperior()).thenReturn(mock(Unidade.class));
        lenient().when(sp.getUnidade()).thenReturn(u);
 
        // Lenient for situations where we set it
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

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(subprocessoRepo).save(sp);
            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Não deve alterar situação ao salvar mapa se já estava criado")
        void naoDeveAlterarSituacaoSalvarMapaSeJaCriado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio (mas status já avançado)

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            // Não deve alterar situação se já estava correta
            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve permitir edição em REVISAO_CADASTRO_HOMOLOGADA")
        void devePermitirEdicaoEmRevisaoCadastroHomologada() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Mock to prevent NPE on req.competencias().isEmpty()
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
                    .when(validacaoService).validarSituacaoPermitida(eq(sp), anyString(), any());

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

            // Já tem uma competência
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

            // Simular que ficou vazio após remover
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

            // Ainda tem competências
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            service.removerCompetencia(1L, 5L);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Não deve mudar status ao remover competência se ficou vazio mas situação é REVISAO_MAPA_AJUSTADO")
        void naoDeveMudarStatusRemoverCompetenciaSeRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Ficou vazio
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L);

            verify(subprocessoRepo, never()).save(sp);
        }

        @Test
        @DisplayName("Deve permitir edição em REVISAO_MAPA_AJUSTADO")
        void devePermitirEdicaoEmRevisaoMapaAjustado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
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
        @DisplayName("Deve salvar mapa sem alterar situação se mapa não era vazio")
        void deveSalvarMapaSemAlterarSituacaoSeNaoVazio() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            // Mapa já tinha competências (não era vazio)
            Competencia c = new Competencia();
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(CompetenciaMapaDto.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            // Não deve salvar porque não mudou situação
            verify(subprocessoRepo, never()).save(sp);
            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve salvar mapa sem alterar situação se requisição sem novas competências")
        void deveSalvarMapaSemAlterarSituacaoSeSemNovasCompetencias() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of()); // Era vazio

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();

            service.salvarMapaSubprocesso(1L, req);

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
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            CompetenciaRequest req = CompetenciaRequest.builder()
                    .descricao("Nova Comp")
                    .atividadesIds(List.of(1L))
                    .build();

            service.adicionarCompetencia(1L, req);

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
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            service.removerCompetencia(1L, 5L);

            verify(mapaManutencaoService).removerCompetencia(5L);
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
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of()); // Nenhuma extra

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
        @DisplayName("Deve disponibilizar mapa com observações vazias")
        void deveDisponibilizarMapaObsVazias() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of());
            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("")
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

            verify(mapa, never()).setSugestoes(anyString());
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



        @Test
        @DisplayName("Deve disponibilizar mapa sem definir sugestões quando observações null")
        void deveDisponibilizarMapaSemObservacoes() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of());

            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes(null)
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

            // Verifica que sugestões não foi setado (por ser null)
            verify(mapa).setSugestoes(null);
            verify(mapa, never()).setSugestoes(argThat(s -> s != null && !s.isBlank()));
        }

        @Test
        @DisplayName("Deve disponibilizar mapa com observações blank")
        void deveDisponibilizarMapaComObservacoesBlank() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>(List.of(new Atividade())));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of());

            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("   ")
                    .build();

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
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve editar mapa em situação REVISAO_MAPA_AJUSTADO")
        void deveEditarMapaEmRevisaoMapaAjustado() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp.getProcesso().setTipo(TipoProcesso.REVISAO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(mapaFacade).salvarMapaCompleto(10L, req);
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

            verify(analiseFacade).criarAnalise(eq(sp), argThat(req -> req.acao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO));
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar se unidade sem superior")
        void deveAceitarEHomologarSeSemSuperior() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u = sp.getUnidade();
            // Unidade superior é null
            when(u.getUnidadeSuperior()).thenReturn(null);

            service.aceitarValidacao(1L, new Usuario());

            verify(analiseFacade).criarAnalise(eq(sp), argThat(req -> req.acao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO));
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

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO &&
                    req.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_ACEITA));
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class EmBloco {


        @Test
        void deveDisponibilizarEmBloco() {
            Subprocesso base = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            base.getProcesso().setCodigo(100L);

            Subprocesso target = mockSubprocesso(10L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

            // Mock validations for target
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
            Subprocesso base = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            base.getProcesso().setCodigo(100L);

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

            Subprocesso target = mockSubprocesso(10L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            // Topo da cadeia para simplificar
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
            sp.getProcesso().setTipo(TipoProcesso.REVISAO); // Revisão
            Mapa m = mock(Mapa.class);
            when(sp.getMapa()).thenReturn(m);

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
        @DisplayName("Deve submeter mapa ajustado para tipo MAPEAMENTO")
        void deveSubmeterAjustadoMapeamento() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));

            service.submeterMapaAjustado(1L, SubmeterMapaAjustadoRequest.builder()
                    .justificativa("Justificativa")
                    .dataLimiteEtapa2(LocalDateTime.now())
                    .competencias(List.of())
                    .build(), new Usuario());

            verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }
    }
}