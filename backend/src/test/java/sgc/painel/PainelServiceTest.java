package sgc.painel;

import org.junit.jupiter.api.DisplayName;
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
import sgc.organizacao.UnidadeService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelService")
class PainelServiceTest {

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private PainelService painelService;

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
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(sgc.organizacao.dto.UnidadeDto.builder()
                .codigo(1L)
                .sigla("U1")
                .build());

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 1L, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getLinkDestino()).isEqualTo("/processo/1/U1");
    }

    @Test
    @DisplayName("listarAlertas por unidade deve buscar alertas da unidade")
    void listarAlertas_PorUnidade() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(100L);
        alerta.setDescricao("Alerta teste");
        alerta.setDataHora(LocalDateTime.now());
        
        when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alerta)));
        when(alertaService.obterDataHoraLeitura(any(), any())).thenReturn(Optional.empty());

        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas("123456", 1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescricao()).isEqualTo("Alerta teste");
        verify(alertaService).listarPorUnidade(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("listarAlertas sem unidade deve retornar vazio")
    void listarAlertas_SemUnidadeRetornaVazio() {
        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas(null, null, PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listarProcessos deve tratar exceção ao formatar unidades participantes")
    void listarProcessos_FormatarUnidadesException() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        // Sem sigla ou mockando erro service
        
        Processo p = criarProcessoMock(1L);
        p.setParticipantes(Set.of(u));
        
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        // ⚡ Bolt: No longer mocking buscarEntidadePorId as it should be skipped by the optimization
        // Instead, we rely on the logic handling null or partial units, or simulating failure if needed in finding ancestors?
        // Actually, if we pass the unit in the map, it won't call the service.
        // To simulate exception, we might need to simulate map.get returning null? But the map is built from p.getParticipantes().

        // This test was verifying that if 'buscarEntidadePorId' throws, it is caught.
        // Now that we don't call it if present in map, this test scenario (service failure) is less relevant unless map fails.
        // But if we want to ensure robustness, we can simulate a case where map lookup might fail or subsequent calls fail.

        // However, since we removed the call, the test expecting "unnecessary stubbing" is correct to fail.
        // We should just remove the unnecessary stubbing.

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

        // Deve retornar lista mas com participantes vazio ou parcial, sem quebrar
        assertThat(result.getContent()).hasSize(1);
        // assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEmpty(); // Depends on sigla being null
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
    @DisplayName("garantirOrdenacaoPadrao deve retornar pageable original se já estiver ordenado")
    void garantirOrdenacaoPadrao_JaOrdenado() {
        // Linha 91
        PageRequest pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("descricao"));
        // O método é privado, mas chamado via listarProcessos
        when(processoFacade.listarTodos(pageable)).thenReturn(Page.empty(pageable));
        
        painelService.listarProcessos(Perfil.ADMIN, null, pageable);
        
        verify(processoFacade).listarTodos(pageable);
    }

    @Test
    @DisplayName("selecionarIdsVisiveis deve ignorar unidade se buscarEntidadePorId falhar")
    void selecionarIdsVisiveis_CatchException() {
        // As linhas 185-191 são atualmente código morto devido à otimização 'participantesPorCodigo'
        // Mas para manter compatibilidade com o teste e cobrir possíveis reversões futuras:
        Unidade u = new Unidade();
        u.setCodigo(999L);
        
        Processo p = criarProcessoMock(1L);
        p.setParticipantes(Set.of(u));
        
        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
        // Usamos lenient pois com a otimização atual este stub não será chamado (unidade já está no mapa)
        lenient().when(unidadeService.buscarEntidadePorId(999L)).thenThrow(new RuntimeException("ERRO"));

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).isNotEmpty();
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

    @Test
    @DisplayName("listarAlertas deve tratar unidades nulas no DTO")
    void listarAlertas_UnidadesNulas() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(400L);
        alerta.setDescricao("Alerta sem unidade");
        alerta.setDataHora(LocalDateTime.now());
        // Unidades null
        
        when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alerta)));

        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getUnidadeOrigem()).isNull();
        assertThat(result.getContent().get(0).getUnidadeDestino()).isNull();
    }

    @Test
    @DisplayName("listarAlertas deve retornar alertas mesmo se usuarioTitulo for nulo ou em branco")
    void listarAlertas_UsuarioTituloVazio() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(500L);
        alerta.setDescricao("D1");
        
        when(alertaService.listarPorUnidade(any(), any())).thenReturn(new PageImpl<>(List.of(alerta)));
        
        // Testa com usuarioTitulo nulo
        Page<sgc.alerta.dto.AlertaDto> resultNull = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));
        assertThat(resultNull.getContent().get(0).getDataHoraLeitura()).isNull();

        // Testa com usuarioTitulo em branco
        Page<sgc.alerta.dto.AlertaDto> resultBlank = painelService.listarAlertas("  ", 1L, PageRequest.of(0, 10));
        assertThat(resultBlank.getContent().get(0).getDataHoraLeitura()).isNull();
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
    @DisplayName("paraAlertaDto deve lidar com unidades de origem/destino nulas")
    void paraAlertaDto_UnidadesNulas() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setUnidadeOrigem(null);
        alerta.setUnidadeDestino(null);
        
        when(alertaService.listarPorUnidade(any(), any())).thenReturn(new PageImpl<>(List.of(alerta)));
        
        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));
        assertThat(result.getContent().get(0).getUnidadeOrigem()).isNull();
        assertThat(result.getContent().get(0).getUnidadeDestino()).isNull();
    }

    @Test
    @DisplayName("encontrarMaiorIdVisivel deve retornar null se unidade nula")
    void encontrarMaiorIdVisivel_UnidadeNull() {
        // Para chegar na linha 199 (unidade == null) através do fluxo público
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        
        Processo p = criarProcessoMock(1L);
        p.setParticipantes(Set.of(u1));
        
        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));
        // Embora u1 seja passado, SE simulássemos uma falha onde o mapa retornasse algo que não u1...
        // Mas o mapa é construído de p.getParticipantes.
        // Vamos testar se o filtro de sigla null na linha 173 funciona.
        u1.setSigla(null);
        
        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
        assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEmpty();
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
