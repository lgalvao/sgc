package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelService")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PainelServiceUpdateTest {

    @Mock
    private ProcessoFacade processoFacade;

    @Mock
    private AlertaService alertaService;

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private PainelService service;

    @Nested
    @DisplayName("listarProcessos: Exceções e Edge Cases")
    class ListarProcessos {

        @Test
        @DisplayName("Deve lançar erro se perfil nulo")
        void erroPerfilNulo() {
            Pageable p = PageRequest.of(0, 10);
            assertThrows(ErroParametroPainelInvalido.class,
                () -> service.listarProcessos(null, 1L, p));
        }

        @Test
        @DisplayName("Deve retornar vazio se codigoUnidade nulo para não-ADMIN")
        void vazioSeUnidadeNula() {
            Page<ProcessoResumoDto> res = service.listarProcessos(Perfil.SERVIDOR, null, PageRequest.of(0, 10));
            assertThat(res).isNotNull();
            assertThat(res.getContent()).isEmpty();
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

            Page<ProcessoResumoDto> res = service.listarProcessos(Perfil.SERVIDOR, 1L, PageRequest.of(0, 10));

            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().get(0).getLinkDestino()).isNull();
        }

        @Test
        @DisplayName("Deve tratar exceção ao buscar unidade visível no formatarUnidadesParticipantes")
        void excecaoEmSelecionarIdsVisiveis() {
            // Cria processo com participante ID 99
            Unidade part = new Unidade();
            part.setCodigo(99L);
            part.setSigla("U99");

            Processo p = new Processo();
            p.setCodigo(1L);
            p.setParticipantes(Set.of(part));
            p.setTipo(TipoProcesso.MAPEAMENTO);

            when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

            // Simula erro ao tentar buscar a entidade completa dentro do loop de visibilidade
            when(unidadeService.buscarEntidadePorId(99L)).thenThrow(new RuntimeException("Erro"));

            Page<ProcessoResumoDto> res = service.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

            // Não deve quebrar, apenas ignora a unidade na lista formatada
            assertThat(res).isNotEmpty();
            assertThat(res.getContent().get(0).getUnidadesParticipantes()).isEmpty();
        }
    }
}
