package sgc.painel;

import org.junit.jupiter.api.DisplayName;
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
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para PainelFacade")
class PainelFacadeCoverageTest {

    @InjectMocks
    private PainelFacade painelFacade;

    @Mock private AlertaFacade alertaService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeService;

    @Test
    @DisplayName("Deve mapear AlertaDto corretamente")
    void deveMapearAlertaDtoCorretamente() {
        sgc.processo.model.Processo p = new sgc.processo.model.Processo();
        p.setCodigo(55L);
        
        sgc.organizacao.model.Unidade origem = new sgc.organizacao.model.Unidade();
        origem.setSigla("ORG");
        
        sgc.organizacao.model.Unidade destino = new sgc.organizacao.model.Unidade();
        destino.setSigla("DST");

        Alerta a = new Alerta();
        a.setCodigo(100L);
        a.setDescricao("D");
        a.setUnidadeOrigem(origem);
        a.setUnidadeDestino(destino);
        a.setProcesso(p);

        Pageable pageable = PageRequest.of(0, 10);
        when(alertaService.listarPorUnidade(eq(1L), any())).thenReturn(new PageImpl<>(List.of(a)));
        when(alertaService.obterDataHoraLeitura(anyLong(), anyString())).thenReturn(Optional.empty());

        Page<AlertaDto> result = painelFacade.listarAlertas("T", 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUnidadeOrigem()).isEqualTo("ORG");
        assertThat(result.getContent().getFirst().getUnidadeDestino()).isEqualTo("DST");
        assertThat(result.getContent().getFirst().getCodProcesso()).isEqualTo(55L);
    }

    @Test
    @DisplayName("Deve calcular link para ADMIN e processo CRIADO")
    void deveCalcularLinkAdminCriado() {
        Processo p = new Processo();
        p.setCodigo(10L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO); // Inicializando tipo para evitar NPE se mapper usar
        
        sgc.organizacao.model.Unidade u = new sgc.organizacao.model.Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        u.setNome("Unidade 1");
        p.getParticipantes().add(u);
        
        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/cadastro?codProcesso=10");
    }
}
