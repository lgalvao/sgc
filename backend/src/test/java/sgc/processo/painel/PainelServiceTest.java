package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.testutils.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelFacade - Testes Unitários")
class PainelServiceTest {

    private final Pageable pageable = PageRequest.of(0, 10);
    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private OrganizacaoFacade unidadeService;
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
        return p;
    }

    @Nested
    @DisplayName("Listar Processos - Regras de Perfil")
    class ListarProcessosPerfil {

        @Test
        @DisplayName("ADMIN: deve listar todos os processos")
        void listarProcessos_Admin() {
            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(Page.empty());
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());

            painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            verify(processoFacade).listarTodos(any(Pageable.class));
            verify(processoFacade, never()).listarPorParticipantesIgnorandoCriado(any(), any());
        }

        @Test
        @DisplayName("GESTOR: deve listar processos da unidade e subordinadas")
        void listarProcessos_Gestor() {
            Long codigoUnidade = 1L;
            List<Long> subordinadas = List.of(2L, 3L);
            
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());
            when(unidadeService.buscarIdsDescendentes(eq(codigoUnidade), any())).thenReturn(subordinadas);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            painelService.listarProcessos(Perfil.GESTOR, codigoUnidade, pageable);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(captor.capture(), any());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("CHEFE: deve listar processos apenas da própria unidade")
        void listarProcessos_Chefe() {
            Long codigoUnidade = 1L;
            
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            painelService.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(captor.capture(), any());
            assertThat(captor.getValue()).containsExactly(1L);
        }
    }

    @Nested
    @DisplayName("Listar Processos - Formatação de Unidades")
    class ListarProcessosFormatacao {

        @Test
        @DisplayName("paraProcessoResumoDto deve gerar link destino correto para ADMIN em processo CRIADO")
        void linkDestino_AdminCriado() {
            Processo p = criarProcessoMock(100L);
            p.setSituacao(SituacaoProcesso.CRIADO);
            
            Unidade u = criarUnidade(1L, "SIGLA");
            p.adicionarParticipantes(Set.of(u));

            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/cadastro?codProcesso=100");
        }

        @Test
        @DisplayName("paraProcessoResumoDto deve gerar link destino correto para CHEFE")
        void linkDestino_Chefe() {
            Long codigoUnidade = 1L;
            Processo p = criarProcessoMock(100L);
            Unidade u = criarUnidade(codigoUnidade, "SIGLA");
            p.adicionarParticipantes(Set.of(u));

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());
            
            sgc.organizacao.dto.UnidadeDto dto = new sgc.organizacao.dto.UnidadeDto();
            dto.setSigla("SIGLA");
            when(unidadeService.dtoPorCodigo(codigoUnidade)).thenReturn(dto);

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

            assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/100/SIGLA");
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

            when(unidadeService.buscarMapaHierarquia()).thenReturn(Map.of(0L, List.of(1L), 1L, List.of(2L)));

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.adicionarParticipantes(Set.of(pai, filho));

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
            p.adicionarParticipantes(Set.of(filho));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Map.of(1L, List.of(2L)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("FILHO");
        }
    }

    @Nested
    @DisplayName("Listar Processos - Outros")
    class ListarProcessosOutros {
        @Test
        @DisplayName("garantirOrdenacaoPadrao deve retornar pageable original se já estiver ordenado")
        void garantirOrdenacaoPadrao_JaOrdenado() {
            Pageable sorted = PageRequest.of(0, 10, Sort.by("dataCriacao"));
            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());
            when(processoFacade.listarTodos(any())).thenReturn(Page.empty());

            painelService.listarProcessos(Perfil.ADMIN, null, sorted);
            
            verify(processoFacade).listarTodos(eq(sorted));
        }

        @Test
        @DisplayName("Deve usar busca otimizada (in-memory) ao buscar unidades subordinadas")
        void deveUsarBuscaOtimizadaDeSubordinadas() {
            Long raizId = 1L;

            when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                    .thenReturn(Page.empty());

            when(unidadeService.buscarMapaHierarquia()).thenReturn(Collections.emptyMap());
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

            Page<Alerta> result = painelService.listarAlertas("titulo", 1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(alertaService).listarPorUnidade(eq(1L), any(Pageable.class));
        }
    }
}
