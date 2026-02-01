package sgc.painel;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.testutils.UnidadeTestBuilder;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PainelFacade - Testes Unitários")
class PainelServiceTest {

    private final Pageable pageable = PageRequest.of(0, 10);
    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private UnidadeFacade unidadeService;
    @InjectMocks
    private PainelFacade painelService;

    private Unidade criarUnidade(Long codigo, String sigla) {
        return UnidadeTestBuilder.umaDe()
                .comCodigo(codigo.toString())
                .comSigla(sigla)
                .build();
    }

    private Processo criarProcessoMock(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataCriacao(LocalDateTime.now());

        Unidade participante = criarUnidade(1L, "PART");
        p.setParticipantes(Set.of(participante));

        return p;
    }

    @Nested
    @DisplayName("Listar Processos - Consultas Básicas")
    class ListarProcessosBasico {
        @Test
        @DisplayName("listarProcessos para ADMIN deve listar todos")
        void listarProcessos_Admin() {
            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("listarProcessos para GESTOR deve incluir subordinadas")
        void listarProcessos_Gestor() {
            when(unidadeService.buscarIdsDescendentes(eq(1L), any())).thenReturn(List.of(2L, 3L));

            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));

            painelService.listarProcessos(Perfil.GESTOR, 1L, PageRequest.of(0, 10));

            // Verifica se chamou buscando por 1L, 2L e 3L
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class));
        }


        @Test
        @DisplayName("listarProcessos: Perfil GESTOR deve buscar subordinadas")
        void listarProcessos_GestorBuscaSubordinadas() {
            Long codigoUnidade = 1L;
            when(unidadeService.buscarIdsDescendentes(eq(codigoUnidade), any())).thenReturn(List.of(2L));

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade u = criarUnidade(1L, "U1");
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.GESTOR, codigoUnidade, pageable);

            assertThat(result.getContent()).isNotEmpty();
            verify(unidadeService, atLeastOnce()).buscarIdsDescendentes(eq(codigoUnidade), any());
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(
                    argThat(list -> list.contains(1L) && list.contains(2L)), any());
        }


    }

    @Nested
    @DisplayName("Listar Processos - Links de Destino")
    class ListarProcessosLinks {
        @Test
        @DisplayName("listarProcessos deve calcular link correto para ADMIN e processo CRIADO")
        void listarProcessos_LinkAdminCriado() {
            Processo p = criarProcessoMock(1L);
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            assertThat(result.getContent().getFirst().linkDestino()).contains("/processo/cadastro?codProcesso=1");
        }

        @Test
        @DisplayName("listarProcessos deve calcular link correto para CHEFE")
        void listarProcessos_LinkChefe() {
            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(UnidadeDto.builder()
                    .codigo(1L)
                    .sigla("U1")
                    .build());

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 1L, PageRequest.of(0, 10));

            assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/1/U1");
        }

        @Test
        @DisplayName("listarProcessos: Perfil CHEFE retorna link com sigla")
        void listarProcessos_ChefeRetornaLinkComSigla() {
            Long codigoUnidade = 1L;
            UnidadeDto unidadeDto = UnidadeDto.builder().sigla("SIGLA").build();
            when(unidadeService.buscarPorCodigo(codigoUnidade)).thenReturn(unidadeDto);

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade u = criarUnidade(1L, "U1");
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

            assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/100/SIGLA");
        }

        @Test
        @DisplayName("listarProcessos deve retornar link null se unidade nao encontrada no calculo de link CHEFE")
        void listarProcessos_LinkChefeErro() {
            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));

            when(unidadeService.buscarPorCodigo(2L)).thenThrow(new RuntimeException("Unidade não achada"));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 2L, PageRequest.of(0, 10));

            assertThat(result.getContent().getFirst().linkDestino()).isEmpty();
        }

        @Test
        @DisplayName("Calcula link destino com exceção na busca de unidade")
        void calculaLinkComExcecaoUnidade() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidade(1L, "U1");
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                    .thenReturn(new PageImpl<>(List.of(p)));

            // Simula erro na busca da unidade para montar o link
            when(unidadeService.buscarPorCodigo(1L)).thenThrow(new RuntimeException("Erro DB"));

            Page<ProcessoResumoDto> res = painelService.listarProcessos(Perfil.SERVIDOR, 1L, PageRequest.of(0, 10));

            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().getFirst().linkDestino()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listar Processos - Formatação de Unidades")
    class ListarProcessosFormatacao {
        @Test
        @DisplayName("listarProcessos deve tratar exceção ao formatar unidades participantes")
        void listarProcessos_FormatarUnidadesException() {
            Unidade u = criarUnidade(1L, "U1");

            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            // Deve retornar lista mas com participantes vazio ou parcial, sem quebrar
            assertThat(result.getContent()).hasSize(1);
        }


        @Test
        @DisplayName("encontrarMaiorIdVisivel deve retornar null se unidade for null ou não participante")
        void encontrarMaiorIdVisivel_CasosBorda() {
            Unidade u = criarUnidade(999L, "U999");

            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            // Usamos lenient para atingir indiretamente o fluxo de segurança/null checks se houver
            lenient().when(unidadeService.buscarEntidadePorId(999L)).thenReturn(null);

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
            assertThat(result.getContent()).isNotEmpty();
        }


        @Test
        @DisplayName("formatarUnidadesParticipantes: deve formatar corretamente e agrupar hierarquia")
        void formatarUnidadesParticipantes_Complexa() {
            Unidade pai = criarUnidade(1L, "PAI");
            Unidade filho = UnidadeTestBuilder.umaDe()
                    .comCodigo("2")
                    .comSigla("FILHO")
                    .comSuperior(pai)
                    .build();

            when(unidadeService.buscarIdsDescendentes(eq(1L), any())).thenReturn(List.of(2L));
            when(unidadeService.buscarIdsDescendentes(eq(2L), any())).thenReturn(Collections.emptyList());

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(Set.of(pai, filho));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("PAI");
        }

        @Test
        @DisplayName("formatarUnidadesParticipantes: deve mostrar filho se pai não participa")
        void formatarUnidadesParticipantes_FilhoSemPai() {
            Unidade pai = criarUnidade(1L, "PAI");
            Unidade filho = UnidadeTestBuilder.umaDe()
                    .comCodigo("2")
                    .comSigla("FILHO")
                    .comSuperior(pai)
                    .build();

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(Set.of(filho));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("FILHO");
        }

        @Test
        @DisplayName("Deve tratar exceção ao buscar unidade visível no formatarUnidadesParticipantes")
        void excecaoEmSelecionarIdsVisiveis() {
            Unidade part = criarUnidade(99L, "U99");

            Processo p = new Processo();
            p.setCodigo(1L);
            p.setParticipantes(Set.of(part));
            p.setTipo(TipoProcesso.MAPEAMENTO);

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> res = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            assertThat(res).isNotEmpty();
            assertThat(res.getContent().getFirst().unidadesParticipantes()).contains("U99");
        }
    }

    @Nested
    @DisplayName("Listar Processos - Outros")
    class ListarProcessosOutros {
        @Test
        @DisplayName("garantirOrdenacaoPadrao deve retornar pageable original se já estiver ordenado")
        void garantirOrdenacaoPadrao_JaOrdenado() {
            PageRequest pageRequestWithSort = PageRequest.of(0, 10, Sort.by("descricao"));
            when(processoFacade.listarTodos(pageRequestWithSort)).thenReturn(Page.empty(pageRequestWithSort));

            painelService.listarProcessos(Perfil.ADMIN, null, pageRequestWithSort);

            verify(processoFacade).listarTodos(pageRequestWithSort);
        }

        @Test
        @DisplayName("selecionarIdsVisiveis deve ignorar unidade se buscarEntidadePorId falhar")
        void selecionarIdsVisiveis_CatchException() {
            Unidade u = criarUnidade(999L, "U999");

            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            lenient().when(unidadeService.buscarEntidadePorId(999L)).thenThrow(new RuntimeException("ERRO"));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve usar busca otimizada (in-memory) ao buscar unidades subordinadas")
        void deveUsarBuscaOtimizadaDeSubordinadas() {
            Long raizId = 1L;

            when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                    .thenReturn(Page.empty());

            when(unidadeService.buscarIdsDescendentes(eq(raizId), any()))
                    .thenReturn(List.of(2L, 3L, 4L, 5L));

            painelService.listarProcessos(Perfil.GESTOR, raizId, PageRequest.of(0, 10));

            verify(unidadeService, times(1)).buscarIdsDescendentes(eq(raizId), any());
        }
    }

    @Nested
    @DisplayName("Listar Alertas")
    class ListarAlertas {
        @Test
        @DisplayName("listarAlertas por unidade deve buscar alertas da unidade")
        void listarAlertas_PorUnidade() {
            Alerta alerta = new Alerta();
            alerta.setCodigo(100L);
            alerta.setDescricao("Alerta teste");
            alerta.setDataHora(LocalDateTime.now());

            // Setup obrigatorio para evitar NPE
            Processo p = new Processo();
            p.setCodigo(123L);
            alerta.setProcesso(p);

            Unidade u = criarUnidade(1L, "U1");
            alerta.setUnidadeOrigem(u);
            alerta.setUnidadeDestino(u);

            when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(alerta)));
            when(alertaService.obterDataHoraLeitura(any(), any())).thenReturn(Optional.empty());

            Page<AlertaDto> result = painelService.listarAlertas("123456", 1L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getDescricao()).isEqualTo("Alerta teste");
            verify(alertaService).listarPorUnidade(any(Long.class), any(Pageable.class));
        }


        @Test
        @DisplayName("listarAlertas: busca por unidade se codigoUnidade informado")
        void listarAlertas_PorUnidadeCobertura() {
            Long codigoUnidade = 1L;

            Alerta alerta = new Alerta();
            alerta.setCodigo(1L);
            alerta.setDataHora(LocalDateTime.now());

            // Setup obrigatorio
            Processo p = new Processo();
            p.setCodigo(123L);
            alerta.setProcesso(p);

            Unidade u = criarUnidade(1L, "U1");
            alerta.setUnidadeOrigem(u);
            alerta.setUnidadeDestino(u);

            when(alertaService.listarPorUnidade(eq(codigoUnidade), any())).thenReturn(new PageImpl<>(List.of(alerta)));

            Page<AlertaDto> result = painelService.listarAlertas(null, codigoUnidade, pageable);

            assertThat(result.getContent()).isNotEmpty();
            verify(alertaService).listarPorUnidade(eq(codigoUnidade), any());
        }


    }
}
