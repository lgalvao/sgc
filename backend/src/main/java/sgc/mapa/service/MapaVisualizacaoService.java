package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.dto.visualizacao.CompetenciaDto;
import sgc.mapa.dto.visualizacao.ConhecimentoDto;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Service especializado para visualização de mapas de competências.
 * 
 * <p><b>IMPORTANTE:</b> Este serviço deve ser acessado APENAS via {@link MapaFacade}.
 * Controllers não devem injetar este serviço diretamente.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final SubprocessoFacade subprocessoFacade;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;

    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new sgc.comum.erros.ErroEstadoImpossivel(
                    "Subprocesso %d existe mas não possui Mapa associado - violação de invariante"
                    .formatted(codSubprocesso));
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = MapaVisualizacaoDto.UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .sigla(unidade.getSigla())
                .nome(unidade.getNome())
                .build();

        List<Atividade> atividadesComConhecimentos =
                atividadeRepo.findByMapaCodigoWithConhecimentos(mapa.getCodigo());

        Map<Long, AtividadeDto> atividadeDtoMap = atividadesComConhecimentos.stream()
                .collect(Collectors.toMap(Atividade::getCodigo, this::mapAtividadeToDto));

        // ⚡ Bolt: Usando projeção otimizada para evitar hidratar entidades Atividade redundantes
        // via JOIN FETCH. Recuperamos apenas (CompID, CompDesc, AtivID)
        List<Object[]> tuples = competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(mapa.getCodigo());

        // Agrupa as competências e suas atividades
        Map<Long, CompetenciaDto> compMap = new LinkedHashMap<>();
        Set<Long> atividadesComCompetenciaIds = new HashSet<>();

        for (Object[] t : tuples) {
            Long compId = (Long) t[0];
            String compDesc = (String) t[1];
            Long ativId = (Long) t[2];

            compMap.computeIfAbsent(compId, k -> CompetenciaDto.builder()
                    .codigo(compId)
                    .descricao(compDesc)
                    .atividades(new ArrayList<>())
                    .build());

            if (ativId != null) {
                atividadesComCompetenciaIds.add(ativId);
                AtividadeDto ativDto = atividadeDtoMap.get(ativId);
                // Só adiciona se a atividade existir no mapa de atividades do subprocesso
                if (ativDto != null) {
                    compMap.get(compId).getAtividades().add(ativDto);
                }
            }
        }

        List<CompetenciaDto> competenciasDto = new ArrayList<>(compMap.values());

        // Buscar atividades sem competência (órfãs)
        List<AtividadeDto> atividadesSemCompetencia =
                atividadesComConhecimentos.stream()
                        .filter(a -> !atividadesComCompetenciaIds.contains(a.getCodigo()))
                        .map(a -> atividadeDtoMap.get(a.getCodigo()))
                        .toList();

        return MapaVisualizacaoDto.builder()
                .unidade(unidadeDto)
                .competencias(competenciasDto)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(mapa.getSugestoes())
                .build();
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        List<Conhecimento> conhecimentos = atividade.getConhecimentos();

        List<ConhecimentoDto> conhecimentosDto = conhecimentos.stream()
                .map(c -> ConhecimentoDto.builder()
                        .codigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }
}
