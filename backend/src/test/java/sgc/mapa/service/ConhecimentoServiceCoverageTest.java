package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para ConhecimentoService")
class ConhecimentoServiceCoverageTest {

    @InjectMocks
    private ConhecimentoService service;

    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("criar deve lidar com atividade sem mapa associado")
    void criarSemMapa() {
        Long codAtividade = 1L;
        CriarConhecimentoRequest request = new CriarConhecimentoRequest(codAtividade, "Descricao");
        Atividade atividade = new Atividade(); // mapa is null by default

        when(atividadeRepo.findById(eq(codAtividade))).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(any(CriarConhecimentoRequest.class))).thenReturn(new Conhecimento());
        when(conhecimentoRepo.save(any(Conhecimento.class))).thenReturn(new Conhecimento());
        when(conhecimentoMapper.toResponse(any())).thenReturn(new ConhecimentoResponse(1L, codAtividade, "Desc"));

        service.criar(codAtividade, request);

        verify(eventPublisher, never()).publishEvent(any(EventoMapaAlterado.class));
        verify(conhecimentoRepo).save(any(Conhecimento.class));
    }

    @Test
    @DisplayName("atualizar deve lidar com conhecimento de atividade sem mapa")
    void atualizarSemMapa() {
        Long codAtividade = 1L;
        Long codConhecimento = 2L;
        AtualizarConhecimentoRequest request = new AtualizarConhecimentoRequest("Nova Descricao");

        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtividade); // mapa is null

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(codConhecimento);
        conhecimento.setAtividade(atividade);

        when(conhecimentoRepo.findById(codConhecimento)).thenReturn(Optional.of(conhecimento));
        when(conhecimentoMapper.toEntity(request)).thenReturn(new Conhecimento());

        service.atualizar(codAtividade, codConhecimento, request);

        verify(eventPublisher, never()).publishEvent(any(EventoMapaAlterado.class));
        verify(conhecimentoRepo).save(conhecimento);
    }

    @Test
    @DisplayName("excluir deve lidar com conhecimento de atividade sem mapa")
    void excluirSemMapa() {
        Long codAtividade = 1L;
        Long codConhecimento = 2L;

        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtividade); // mapa is null

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(codConhecimento);
        conhecimento.setAtividade(atividade);

        when(conhecimentoRepo.findById(codConhecimento)).thenReturn(Optional.of(conhecimento));

        service.excluir(codAtividade, codConhecimento);

        verify(eventPublisher, never()).publishEvent(any(EventoMapaAlterado.class));
        verify(conhecimentoRepo).delete(conhecimento);
    }
}
