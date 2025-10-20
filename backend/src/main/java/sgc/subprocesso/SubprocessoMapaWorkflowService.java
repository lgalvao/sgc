package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.mapa.MapaService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaWorkflowService {

    private final SubprocessoRepo repositorioSubprocesso;
    private final CompetenciaRepo repositorioCompetencia;
    private final MapaService mapaService;

    public MapaCompletoDto salvarMapaSubprocesso(Long idSubprocesso, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa do subprocesso: idSubprocesso={}, usuario={}", idSubprocesso, usuarioTituloEleitoral);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new sgc.comum.erros.ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.CADASTRO_HOMOLOGADO && situacao != SituacaoSubprocesso.MAPA_CRIADO) {
            throw new IllegalStateException("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new sgc.comum.erros.ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }

        Long idMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(idMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(idMapa, request, usuarioTituloEleitoral);

        if (eraVazio && temNovasCompetencias && situacao == SituacaoSubprocesso.CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", idSubprocesso);
        }

        return mapaDto;
    }
}