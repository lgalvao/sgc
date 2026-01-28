package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para AtividadeService")
class AtividadeServiceCoverageTest {

    @InjectMocks
    private AtividadeService service;

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private RepositorioComum repo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoService conhecimentoService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("atualizar deve lidar com atividade sem mapa associado")
    void atualizarSemMapa() {
        Long codigo = 1L;
        AtualizarAtividadeRequest request = new AtualizarAtividadeRequest("Nova Descricao");
        Atividade atividade = new Atividade(); // mapa is null

        when(repo.buscar(Atividade.class, codigo)).thenReturn(atividade);
        when(atividadeMapper.toEntity(request)).thenReturn(new Atividade());

        service.atualizar(codigo, request);

        verify(eventPublisher, never()).publishEvent(any(EventoMapaAlterado.class));
        verify(atividadeRepo).save(atividade);
    }

    @Test
    @DisplayName("excluir deve lidar com atividade sem mapa associado")
    void excluirSemMapa() {
        Long codigo = 1L;
        Atividade atividade = new Atividade(); // mapa is null

        when(repo.buscar(Atividade.class, codigo)).thenReturn(atividade);

        service.excluir(codigo);

        verify(eventPublisher, never()).publishEvent(any(EventoMapaAlterado.class));
        verify(conhecimentoService).excluirTodosDaAtividade(atividade);
        verify(atividadeRepo).delete(atividade);
    }
}
