package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.processo.model.Processo;
import sgc.organizacao.model.Perfil;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("PainelFacade - Gap Tests")
class PainelFacadeGapTest {

    @Test
    @DisplayName("Deve lidar com participantes null defensivamente")
    void deveLidarComParticipantesNull() {
        sgc.processo.service.ProcessoFacade processoFacade = mock(sgc.processo.service.ProcessoFacade.class);
        PainelFacade facade = new PainelFacade(processoFacade, null, null);
        
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
        p.setParticipantes(null); // Explicitamente null
        
        when(processoFacade.listarTodos(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(p)));
            
        var resultado = facade.listarProcessos(Perfil.ADMIN, 1L, Pageable.unpaged());
        
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).unidadeCodigo()).isEqualTo(1L);
        assertThat(resultado.getContent().get(0).unidadesParticipantes()).isNullOrEmpty();
    }
}
