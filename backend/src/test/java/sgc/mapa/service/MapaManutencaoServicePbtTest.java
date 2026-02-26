package sgc.mapa.service;

import net.jqwik.api.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("PBT")
class MapaManutencaoServicePbtTest {

    @Property
    void criarAtividade_deveFalharSeDescricaoDuplicadaNoMapa(@ForAll("descricoesIguais") String[] descricoes) {
        // Mock dependencies
        AtividadeRepo atividadeRepo = mock(AtividadeRepo.class);
        CompetenciaRepo competenciaRepo = mock(CompetenciaRepo.class);
        ConhecimentoRepo conhecimentoRepo = mock(ConhecimentoRepo.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);
        ComumRepo repo = mock(ComumRepo.class);
        AtividadeMapper atividadeMapper = mock(AtividadeMapper.class);
        ConhecimentoMapper conhecimentoMapper = mock(ConhecimentoMapper.class);
        SubprocessoService subprocessoService = mock(SubprocessoService.class);

        MapaManutencaoService service = new MapaManutencaoService(
                atividadeRepo, competenciaRepo, conhecimentoRepo, mapaRepo, repo,
                atividadeMapper, conhecimentoMapper, subprocessoService
        );

        Long mapaCodigo = 1L;
        Mapa mapa = Mapa.builder().codigo(mapaCodigo).build();
        when(repo.buscar(Mapa.class, mapaCodigo)).thenReturn(mapa);
        
        Atividade novaAtiv = new Atividade();
        novaAtiv.setDescricao(descricoes[1]);
        when(atividadeMapper.toEntity(any(CriarAtividadeRequest.class))).thenReturn(novaAtiv);

        // Simular que já existe uma atividade com a mesma descrição
        Atividade existente = new Atividade();
        existente.setDescricao(descricoes[0]);
        when(atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo)).thenReturn(List.of(existente));

        CriarAtividadeRequest request = new CriarAtividadeRequest(mapaCodigo, descricoes[1]);
        
        assertThatThrownBy(() -> service.criarAtividade(request))
                .isInstanceOf(ErroValidacao.class);
    }

    @Property
    void criarConhecimento_deveFalharSeDescricaoDuplicadaNaAtividade(@ForAll("descricoesIguais") String[] descricoes) {
        AtividadeRepo atividadeRepo = mock(AtividadeRepo.class);
        CompetenciaRepo competenciaRepo = mock(CompetenciaRepo.class);
        ConhecimentoRepo conhecimentoRepo = mock(ConhecimentoRepo.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);
        ComumRepo repo = mock(ComumRepo.class);
        AtividadeMapper atividadeMapper = mock(AtividadeMapper.class);
        ConhecimentoMapper conhecimentoMapper = mock(ConhecimentoMapper.class);
        SubprocessoService subprocessoService = mock(SubprocessoService.class);

        MapaManutencaoService service = new MapaManutencaoService(
                atividadeRepo, competenciaRepo, conhecimentoRepo, mapaRepo, repo,
                atividadeMapper, conhecimentoMapper, subprocessoService
        );

        Long ativCodigo = 10L;
        Atividade atividade = new Atividade();
        atividade.setCodigo(ativCodigo);
        atividade.setMapa(Mapa.builder().codigo(1L).build());
        when(repo.buscar(Atividade.class, ativCodigo)).thenReturn(atividade);

        Conhecimento existente = new Conhecimento();
        existente.setDescricao(descricoes[0]);
        when(conhecimentoRepo.findByAtividade_Codigo(ativCodigo)).thenReturn(List.of(existente));

        CriarConhecimentoRequest request = new CriarConhecimentoRequest(ativCodigo, descricoes[1]);
        
        assertThatThrownBy(() -> service.criarConhecimento(ativCodigo, request))
                .isInstanceOf(ErroValidacao.class);
    }

    @Provide
    Arbitrary<String[]> descricoesIguais() {
        return Arbitraries.strings().alpha().ofMinLength(3).flatMap(s ->
            Arbitraries.of(
                new String[]{s.toLowerCase(), s.toUpperCase()},
                new String[]{s, s},
                new String[]{s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase(), s.toLowerCase()}
            )
        );
    }
}
