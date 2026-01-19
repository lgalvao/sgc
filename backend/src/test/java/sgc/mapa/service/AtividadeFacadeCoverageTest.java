package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.service.SubprocessoFacade;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeFacadeCoverageTest {

    @Mock private AtividadeService atividadeService;
    @Mock private ConhecimentoService conhecimentoService;
    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private AccessControlService accessControlService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AtividadeFacade facade;

    @Test
    @DisplayName("obterAtividadePorId - Pass Through")
    void obterAtividadePorId_PassThrough() {
        Long codAtividade = 1L;
        AtividadeResponse response = AtividadeResponse.builder().codigo(codAtividade).build();
        when(atividadeService.obterResponse(codAtividade)).thenReturn(response);

        AtividadeResponse result = facade.obterAtividadePorId(codAtividade);

        org.junit.jupiter.api.Assertions.assertEquals(response, result);
        verify(atividadeService).obterResponse(codAtividade);
    }

    @Test
    @DisplayName("listarConhecimentosPorAtividade - Pass Through")
    void listarConhecimentosPorAtividade_PassThrough() {
        Long codAtividade = 1L;
        List<ConhecimentoResponse> response = List.of(ConhecimentoResponse.builder().codigo(10L).build());
        when(conhecimentoService.listarPorAtividade(codAtividade)).thenReturn(response);

        List<ConhecimentoResponse> result = facade.listarConhecimentosPorAtividade(codAtividade);

        org.junit.jupiter.api.Assertions.assertEquals(response, result);
        verify(conhecimentoService).listarPorAtividade(codAtividade);
    }
}
