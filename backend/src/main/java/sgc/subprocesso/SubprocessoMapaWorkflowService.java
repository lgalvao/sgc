package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.competencia.CompetenciaService;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.dto.CompetenciaReq;
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
    private final CompetenciaService competenciaService;

    /**
     * Salva o mapa de um subprocesso e atualiza o estado do workflow.
     * <p>
     * Este método primeiro valida se o subprocesso está em uma situação que permite
     * a edição do mapa. Em seguida, delega a operação de salvar o mapa para o
     * {@link MapaService}. Se for a primeira vez que competências estão sendo
     * adicionadas a um mapa (ou seja, o mapa estava vazio), e o subprocesso
     * estava na situação 'CADASTRO_HOMOLOGADO', o estado do subprocesso é
     * avançado para 'MAPA_CRIADO'.
     *
     * @param idSubprocesso        O ID do subprocesso.
     * @param request              O DTO com os dados completos do mapa a serem salvos.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está realizando a operação.
     * @return O {@link MapaCompletoDto} representando o estado salvo do mapa.
     * @throws sgc.comum.erros.ErroDominioNaoEncontrado se o subprocesso ou seu mapa não forem encontrados.
     * @throws IllegalStateException se o subprocesso não estiver em uma situação
     *                               válida para a edição do mapa.
     */
    public MapaCompletoDto salvarMapaSubprocesso(Long idSubprocesso, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa do subprocesso: idSubprocesso={}, usuario={}", idSubprocesso, usuarioTituloEleitoral);

        Subprocesso subprocesso = getSubprocessoParaEdicao(idSubprocesso);

        Long idMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(idMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(idMapa, request, usuarioTituloEleitoral);

        if (eraVazio && temNovasCompetencias && subprocesso.getSituacao() == SituacaoSubprocesso.CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", idSubprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long idSubprocesso, CompetenciaReq request, Long usuarioTituloEleitoral) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(idSubprocesso);
        competenciaService.adicionarCompetencia(subprocesso.getMapa(), request.descricao(), request.atividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), idSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(Long idSubprocesso, Long competenciaId, CompetenciaReq request, Long usuarioTituloEleitoral) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(idSubprocesso);
        competenciaService.atualizarCompetencia(competenciaId, request.descricao(), request.atividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), idSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(Long idSubprocesso, Long competenciaId, Long usuarioTituloEleitoral) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(idSubprocesso);
        competenciaService.removerCompetencia(competenciaId);
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), idSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long idSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new sgc.comum.erros.ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.CADASTRO_HOMOLOGADO && situacao != SituacaoSubprocesso.MAPA_CRIADO) {
            throw new IllegalStateException("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new sgc.comum.erros.ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }
}