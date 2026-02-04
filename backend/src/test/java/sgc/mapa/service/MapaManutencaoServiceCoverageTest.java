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
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra: MapaManutencaoService")
class MapaManutencaoServiceCoverageTest {

    @Mock private AtividadeRepo atividadeRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private ConhecimentoRepo conhecimentoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private ComumRepo repo;
    @Mock private AtividadeMapper atividadeMapper;
    @Mock private ConhecimentoMapper conhecimentoMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MapaManutencaoService service;

    @Test
    void listarTodosMapas() {
        service.listarTodosMapas();
        verify(mapaRepo).findAll();
    }

    @Test
    void buscarMapaVigentePorUnidade() {
        service.buscarMapaVigentePorUnidade(1L);
        verify(mapaRepo).findMapaVigenteByUnidade(1L);
    }

    @Test
    void buscarMapaPorSubprocessoCodigo() {
        service.buscarMapaPorSubprocessoCodigo(1L);
        verify(mapaRepo).findBySubprocessoCodigo(1L);
    }

    @Test
    void salvarMapa() {
        Mapa mapa = new Mapa();
        service.salvarMapa(mapa);
        verify(mapaRepo).save(mapa);
    }

    @Test
    void mapaExiste() {
        service.mapaExiste(1L);
        verify(mapaRepo).existsById(1L);
    }

    @Test
    void excluirMapa() {
        service.excluirMapa(1L);
        verify(mapaRepo).deleteById(1L);
    }

    // --- Null Mapper Tests ---

    @Test
    @DisplayName("obterAtividadeResponse: Erro quando mapper retorna null")
    void obterAtividadeResponse_MapperNull() {
        Atividade atividade = new Atividade();
        when(repo.buscar(Atividade.class, 1L)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(null);

        assertThatThrownBy(() -> service.obterAtividadeResponse(1L))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("criarAtividade: Erro quando mapper.toEntity retorna null")
    void criarAtividade_ToEntityNull() {
        CriarAtividadeRequest req = new CriarAtividadeRequest(1L, "Desc");
        Mapa mapa = new Mapa();
        when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
        when(atividadeMapper.toEntity(req)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.criarAtividade(req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("criarAtividade: Erro quando mapper.toResponse retorna null")
    void criarAtividade_ToResponseNull() {
        CriarAtividadeRequest req = new CriarAtividadeRequest(1L, "Desc");
        Mapa mapa = new Mapa();
        Atividade atividade = new Atividade();
        when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
        when(atividadeMapper.toEntity(req)).thenReturn(atividade);
        when(atividadeRepo.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.criarAtividade(req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("atualizarAtividade: Erro quando mapper.toEntity retorna null")
    void atualizarAtividade_ToEntityNull() {
        AtualizarAtividadeRequest req = new AtualizarAtividadeRequest("Desc");
        Atividade atividade = new Atividade();
        when(repo.buscar(Atividade.class, 1L)).thenReturn(atividade);
        when(atividadeMapper.toEntity(req)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.atualizarAtividade(1L, req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("criarConhecimento: Erro quando mapper.toEntity retorna null")
    void criarConhecimento_ToEntityNull() {
        CriarConhecimentoRequest req = new CriarConhecimentoRequest(1L, "Desc");
        Atividade atividade = new Atividade();
        when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(req)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.criarConhecimento(1L, req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("criarConhecimento: Erro quando mapper.toResponse retorna null")
    void criarConhecimento_ToResponseNull() {
        CriarConhecimentoRequest req = new CriarConhecimentoRequest(1L, "Desc");
        Atividade atividade = new Atividade();
        Conhecimento conhecimento = new Conhecimento();
        
        when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(req)).thenReturn(conhecimento);
        when(conhecimentoRepo.save(conhecimento)).thenReturn(conhecimento);
        when(conhecimentoMapper.toResponse(conhecimento)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.criarConhecimento(1L, req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("atualizarConhecimento: Erro quando mapper.toEntity retorna null")
    void atualizarConhecimento_ToEntityNull() {
        AtualizarConhecimentoRequest req = new AtualizarConhecimentoRequest("Desc");
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setAtividade(atividade);

        when(conhecimentoRepo.findById(10L)).thenReturn(Optional.of(conhecimento));
        when(conhecimentoMapper.toEntity(req)).thenReturn(null); // NULL

        assertThatThrownBy(() -> service.atualizarConhecimento(1L, 10L, req))
                .isInstanceOf(ErroEstadoImpossivel.class);
    }
}
