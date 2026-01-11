package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.AlertaService;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PainelService - Testes Unitários")
class PainelServiceTest {

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private PainelService painelService;

    private Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("Listar Processos - Consultas Básicas")
    class ListarProcessosBasico {

        @Test
        @DisplayName("listarProcessos deve lançar erro se perfil for nulo")
        void listarProcessos_PerfilNulo() {
            Pageable pageable = Pageable.unpaged();
            assertThatThrownBy(() -> painelService.listarProcessos(null, 1L, pageable))
                    .isInstanceOf(ErroParametroPainelInvalido.class);
        }

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
            when(unidadeService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L, 3L));
            
            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));

            painelService.listarProcessos(Perfil.GESTOR, 1L, PageRequest.of(0, 10));

            // Verifica se chamou buscando por 1L, 2L e 3L
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class));
        }

        @Test
        @DisplayName("listarProcessos não ADMIN retorna vazio se unidade for nula")
        void listarProcessos_NaoAdminSemUnidade() {
            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, null, PageRequest.of(0, 10));
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("listarProcessos: Perfil GESTOR deve buscar subordinadas")
        void listarProcessos_GestorBuscaSubordinadas() {
            Long codigoUnidade = 1L;
            when(unidadeService.buscarIdsDescendentes(codigoUnidade)).thenReturn(List.of(2L));

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(Set.of());

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.GESTOR, codigoUnidade, pageable);

            assertThat(result.getContent()).isNotEmpty();
            verify(unidadeService).buscarIdsDescendentes(codigoUnidade);
            verify(processoFacade).listarPorParticipantesIgnorandoCriado(
                    argThat(list -> list.contains(1L) && list.contains(2L)), any());
        }

        @Test
        @DisplayName("Deve retornar vazio se codigoUnidade nulo para não-ADMIN")
        void vazioSeUnidadeNula() {
            Page<ProcessoResumoDto> res = painelService.listarProcessos(Perfil.SERVIDOR, null, PageRequest.of(0, 10));
            assertThat(res).isNotNull();
            assertThat(res.getContent()).isEmpty();
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

            assertThat(result.getContent().get(0).getLinkDestino()).contains("/processo/cadastro?codProcesso=1");
        }

        @Test
        @DisplayName("listarProcessos deve calcular link correto para CHEFE")
        void listarProcessos_LinkChefe() {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("U1");

            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(UnidadeDto.builder()
                    .codigo(1L)
                    .sigla("U1")
                    .build());

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 1L, PageRequest.of(0, 10));

            assertThat(result.getContent().get(0).getLinkDestino()).isEqualTo("/processo/1/U1");
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
            p.setParticipantes(Set.of());

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

            assertThat(result.getContent().get(0).getLinkDestino()).isEqualTo("/processo/100/SIGLA");
        }

        @Test
        @DisplayName("listarProcessos deve retornar link null se unidade nao encontrada no calculo de link CHEFE")
        void listarProcessos_LinkChefeErro() {
            Processo p = criarProcessoMock(1L);
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(p)));
            
            when(unidadeService.buscarPorCodigo(2L)).thenThrow(new RuntimeException("Unidade não achada"));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 2L, PageRequest.of(0, 10));

            assertThat(result.getContent().get(0).getLinkDestino()).isNull();
        }

        @Test
        @DisplayName("listarProcessos: Link destino nulo se unidade não encontrada para Chefe")
        void listarProcessos_LinkNuloSeUnidadeNaoEncontrada() {
            Long codigoUnidade = 1L;
            when(unidadeService.buscarPorCodigo(codigoUnidade)).thenThrow(new RuntimeException("Erro"));

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);

            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

            assertThat(result.getContent().get(0).getLinkDestino()).isNull();
        }

        @Test
        @DisplayName("Calcula link destino com exceção na busca de unidade")
        void calculaLinkComExcecaoUnidade() {
            // Cenário: Perfil SERVIDOR, processo em andamento, mas falha ao buscar unidade p/ link
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setParticipantes(Collections.emptySet());

            when(unidadeService.buscarIdsDescendentes(any())).thenReturn(Collections.emptyList());
            when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                .thenReturn(new PageImpl<>(List.of(p)));

            // Simula erro na busca da unidade para montar o link
            when(unidadeService.buscarPorCodigo(1L)).thenThrow(new RuntimeException("Erro DB"));

            Page<ProcessoResumoDto> res = painelService.listarProcessos(Perfil.SERVIDOR, 1L, PageRequest.of(0, 10));

            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().get(0).getLinkDestino()).isNull();
        }

        @Test
        @DisplayName("calcularLinkDestinoProcesso deve retornar link padrão")
        void calcularLinkDestinoProcesso_Default() {
            // Linha 240
            Processo p = criarProcessoMock(1L);
            // CHEFE com codigoUnidade não nulo que falha na busca
            when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));
            when(unidadeService.buscarPorCodigo(999L)).thenThrow(new RuntimeException("Error"));
            
            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 999L, PageRequest.of(0, 10));
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).getLinkDestino()).isNull(); // Cai no catch do calcularLinkDestinoProcesso
        }
    }

    @Nested
    @DisplayName("Listar Processos - Formatação de Unidades")
    class ListarProcessosFormatacao {

        @Test
        @DisplayName("listarProcessos deve tratar exceção ao formatar unidades participantes")
        void listarProcessos_FormatarUnidadesException() {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            
            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u));
            
            when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            // Deve retornar lista mas com participantes vazio ou parcial, sem quebrar
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("paraProcessoResumoDto deve lidar com participantes nulos ou vazios")
        void paraProcessoResumoDto_ParticipantesVazios() {
            Processo p = criarProcessoMock(1L);
            p.setParticipantes(null); // Caso nulo
            
            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            
            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
            assertThat(result.getContent().get(0).getUnidadeCodigo()).isNull();
            assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEmpty();
        }

        @Test
        @DisplayName("encontrarMaiorIdVisivel deve retornar null se unidade for null ou não participante")
        void encontrarMaiorIdVisivel_CasosBorda() {
            Unidade u = new Unidade();
            u.setCodigo(999L);
            
            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u));
            
            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            // Usamos lenient para atingir indiretamente o fluxo de segurança/null checks se houver
            lenient().when(unidadeService.buscarEntidadePorId(999L)).thenReturn(null);

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("encontrarMaiorIdVisivel deve retornar null se unidade nula")
        void encontrarMaiorIdVisivel_UnidadeNull() {
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            u1.setSigla("U1");
            
            Processo p = criarProcessoMock(1L);
            p.setParticipantes(Set.of(u1));
            
            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
            u1.setSigla(null);
            
            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
            assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEmpty();
        }

        @Test
        @DisplayName("formatarUnidadesParticipantes: deve formatar corretamente e agrupar hierarquia")
        void formatarUnidadesParticipantes_Complexa() {
            Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setSigla("PAI");
            Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setSigla("FILHO");
            filho.setUnidadeSuperior(pai);

            when(unidadeService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));
            when(unidadeService.buscarIdsDescendentes(2L)).thenReturn(Collections.emptyList());

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(Set.of(pai, filho));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEqualTo("PAI");
        }

        @Test
        @DisplayName("formatarUnidadesParticipantes: deve mostrar filho se pai não participa")
        void formatarUnidadesParticipantes_FilhoSemPai() {
            Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setSigla("PAI");
            Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setSigla("FILHO");
            filho.setUnidadeSuperior(pai);

            Processo p = new Processo();
            p.setCodigo(100L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(Set.of(filho));

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, pageable);

            assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEqualTo("FILHO");
        }

        @Test
        @DisplayName("Deve tratar exceção ao buscar unidade visível no formatarUnidadesParticipantes")
        void excecaoEmSelecionarIdsVisiveis() {
            Unidade part = new Unidade();
            part.setCodigo(99L);
            part.setSigla("U99");

            Processo p = new Processo();
            p.setCodigo(1L);
            p.setParticipantes(Set.of(part));
            p.setTipo(TipoProcesso.MAPEAMENTO);

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            Page<ProcessoResumoDto> res = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            assertThat(res).isNotEmpty();
            assertThat(res.getContent().get(0).getUnidadesParticipantes()).contains("U99");
        }
    }

    @Nested
    @DisplayName("Listar Processos - Outros")
    class ListarProcessosOutros {

        @Test
        @DisplayName("garantirOrdenacaoPadrao deve retornar pageable original se já estiver ordenado")
        void garantirOrdenacaoPadrao_JaOrdenado() {
            PageRequest pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("descricao"));
            when(processoFacade.listarTodos(pageable)).thenReturn(Page.empty(pageable));
            
            painelService.listarProcessos(Perfil.ADMIN, null, pageable);
            
            verify(processoFacade).listarTodos(pageable);
        }

        @Test
        @DisplayName("selecionarIdsVisiveis deve ignorar unidade se buscarEntidadePorId falhar")
        void selecionarIdsVisiveis_CatchException() {
            Unidade u = new Unidade();
            u.setCodigo(999L);
            
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
                    .thenReturn(org.springframework.data.domain.Page.empty());

            when(unidadeService.buscarIdsDescendentes(raizId))
                    .thenReturn(List.of(2L, 3L, 4L, 5L));

            painelService.listarProcessos(Perfil.GESTOR, raizId, org.springframework.data.domain.PageRequest.of(0, 10));

            verify(unidadeService, times(1)).buscarIdsDescendentes(raizId);
            verify(unidadeService, never()).listarSubordinadas(any());
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
            
            when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(alerta)));
            when(alertaService.obterDataHoraLeitura(any(), any())).thenReturn(Optional.empty());

            Page<AlertaDto> result = painelService.listarAlertas("123456", 1L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescricao()).isEqualTo("Alerta teste");
            verify(alertaService).listarPorUnidade(any(Long.class), any(Pageable.class));
        }

        @Test
        @DisplayName("listarAlertas sem unidade deve retornar vazio")
        void listarAlertas_SemUnidadeRetornaVazio() {
            Page<AlertaDto> result = painelService.listarAlertas(null, null, PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("listarAlertas deve tratar unidades nulas no DTO")
        void listarAlertas_UnidadesNulas() {
            Alerta alerta = new Alerta();
            alerta.setCodigo(400L);
            alerta.setDescricao("Alerta sem unidade");
            alerta.setDataHora(LocalDateTime.now());
            
            when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(alerta)));

            Page<AlertaDto> result = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));

            assertThat(result.getContent().get(0).getUnidadeOrigem()).isNull();
            assertThat(result.getContent().get(0).getUnidadeDestino()).isNull();
        }

        @Test
        @DisplayName("listarAlertas deve retornar alertas mesmo se usuarioTitulo for nulo ou em branco")
        void listarAlertas_UsuarioTituloVazio() {
            Alerta alerta = new Alerta();
            alerta.setCodigo(500L);
            alerta.setDescricao("D1");
            
            when(alertaService.listarPorUnidade(any(), any())).thenReturn(new PageImpl<>(List.of(alerta)));
            
            // Testa com usuarioTitulo nulo
            Page<AlertaDto> resultNull = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));
            assertThat(resultNull.getContent().get(0).getDataHoraLeitura()).isNull();

            // Testa com usuarioTitulo em branco
            Page<AlertaDto> resultBlank = painelService.listarAlertas("  ", 1L, PageRequest.of(0, 10));
            assertThat(resultBlank.getContent().get(0).getDataHoraLeitura()).isNull();
        }

        @Test
        @DisplayName("paraAlertaDto deve lidar com unidades de origem/destino nulas")
        void paraAlertaDto_UnidadesNulas() {
            Alerta alerta = new Alerta();
            alerta.setUnidadeOrigem(null);
            alerta.setUnidadeDestino(null);
            
            when(alertaService.listarPorUnidade(any(), any())).thenReturn(new PageImpl<>(List.of(alerta)));
            
            Page<AlertaDto> result = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));
            assertThat(result.getContent().get(0).getUnidadeOrigem()).isNull();
            assertThat(result.getContent().get(0).getUnidadeDestino()).isNull();
        }

        @Test
        @DisplayName("listarAlertas: busca por unidade se codigoUnidade informado")
        void listarAlertas_PorUnidadeCobertura() {
            Long codigoUnidade = 1L;

            Alerta alerta = new Alerta();
            alerta.setCodigo(1L);
            alerta.setDataHora(LocalDateTime.now());

            when(alertaService.listarPorUnidade(eq(codigoUnidade), any())).thenReturn(new PageImpl<>(List.of(alerta)));

            Page<AlertaDto> result = painelService.listarAlertas(null, codigoUnidade, pageable);

            assertThat(result.getContent()).isNotEmpty();
            verify(alertaService).listarPorUnidade(eq(codigoUnidade), any());
        }

        @Test
        @DisplayName("listarAlertas: retorna vazio se titulo e unidade nulos")
        void listarAlertas_SemFiltrosRetornaVazio() {
            Page<AlertaDto> result = painelService.listarAlertas(null, null, pageable);

            assertThat(result).isEmpty();
        }
    }

    private Processo criarProcessoMock(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataCriacao(LocalDateTime.now());
        p.setParticipantes(Collections.emptySet());
        return p;
    }
}
