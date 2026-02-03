package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.*;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Cobertura - MapaManutencaoService")
class MapaManutencaoServiceCoverageTest {

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MapaManutencaoService service;

    @Test
    @DisplayName("obterAtividadeResponse: deve lançar ErroEstadoImpossivel se mapper retornar null")
    void obterAtividadeResponse_DeveLancarErroSeMapperRetornarNull() {
        Long id = 1L;
        Atividade atividade = new Atividade();
        when(repo.buscar(Atividade.class, id)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(null);

        assertThatThrownBy(() -> service.obterAtividadeResponse(id))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter atividade para resposta.");
    }

    @Test
    @DisplayName("criarAtividade: deve lançar ErroEstadoImpossivel se mapper(toEntity) retornar null")
    void criarAtividade_DeveLancarErroSeToEntityRetornarNull() {
        CriarAtividadeRequest request = CriarAtividadeRequest.builder().mapaCodigo(1L).build();
        Mapa mapa = new Mapa();

        when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
        when(atividadeMapper.toEntity(request)).thenReturn(null);

        assertThatThrownBy(() -> service.criarAtividade(request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter requisição para entidade atividade.");
    }

    @Test
    @DisplayName("criarAtividade: deve lançar ErroEstadoImpossivel se mapper(toResponse) retornar null")
    void criarAtividade_DeveLancarErroSeToResponseRetornarNull() {
        CriarAtividadeRequest request = CriarAtividadeRequest.builder().mapaCodigo(1L).build();
        Mapa mapa = new Mapa();
        Atividade atividade = new Atividade();
        Atividade salvo = new Atividade();

        when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
        when(atividadeMapper.toEntity(request)).thenReturn(atividade);
        when(atividadeRepo.save(atividade)).thenReturn(salvo);
        when(atividadeMapper.toResponse(salvo)).thenReturn(null);

        assertThatThrownBy(() -> service.criarAtividade(request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter atividade salva para resposta.");
    }

    @Test
    @DisplayName("atualizarAtividade: deve lançar ErroEstadoImpossivel se mapper(toEntity) retornar null")
    void atualizarAtividade_DeveLancarErroSeToEntityRetornarNull() {
        Long id = 1L;
        AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().build();
        Atividade existente = new Atividade();

        when(repo.buscar(Atividade.class, id)).thenReturn(existente);
        when(atividadeMapper.toEntity(request)).thenReturn(null);

        assertThatThrownBy(() -> service.atualizarAtividade(id, request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter requisição para entidade atividade.");
    }

    @Test
    @DisplayName("criarConhecimento: deve lançar ErroEstadoImpossivel se mapper(toEntity) retornar null")
    void criarConhecimento_DeveLancarErroSeToEntityRetornarNull() {
        Long codAtividade = 1L;
        CriarConhecimentoRequest request = CriarConhecimentoRequest.builder().build();
        Atividade atividade = new Atividade();

        when(atividadeRepo.findById(codAtividade)).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(request)).thenReturn(null);

        assertThatThrownBy(() -> service.criarConhecimento(codAtividade, request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter requisição para entidade conhecimento.");
    }

    @Test
    @DisplayName("criarConhecimento: deve lançar ErroEstadoImpossivel se mapper(toResponse) retornar null")
    void criarConhecimento_DeveLancarErroSeToResponseRetornarNull() {
        Long codAtividade = 1L;
        CriarConhecimentoRequest request = CriarConhecimentoRequest.builder().build();
        Atividade atividade = new Atividade();
        Conhecimento conhecimento = new Conhecimento();
        Conhecimento salvo = new Conhecimento();

        when(atividadeRepo.findById(codAtividade)).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(request)).thenReturn(conhecimento);
        when(conhecimentoRepo.save(conhecimento)).thenReturn(salvo);
        when(conhecimentoMapper.toResponse(salvo)).thenReturn(null);

        assertThatThrownBy(() -> service.criarConhecimento(codAtividade, request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter conhecimento salvo para resposta.");
    }

    @Test
    @DisplayName("atualizarConhecimento: deve lançar ErroEstadoImpossivel se mapper(toEntity) retornar null")
    void atualizarConhecimento_DeveLancarErroSeToEntityRetornarNull() {
        Long codAtividade = 1L;
        Long codConhecimento = 2L;
        AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder().build();
        Conhecimento existente = new Conhecimento();
        // Precisamos do mapa para o evento, ou mockar atividade.mapa como null
        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtividade);
        existente.setAtividade(atividade);

        when(conhecimentoRepo.findById(codConhecimento)).thenReturn(Optional.of(existente));
        when(conhecimentoMapper.toEntity(request)).thenReturn(null);

        assertThatThrownBy(() -> service.atualizarConhecimento(codAtividade, codConhecimento, request))
                .isInstanceOf(ErroEstadoImpossivel.class)
                .hasMessage("Falha ao converter requisição para entidade conhecimento.");
    }
}
