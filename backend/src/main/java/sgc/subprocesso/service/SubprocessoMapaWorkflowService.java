package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroNegocio;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.competencia.CompetenciaService;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.mapa.service.MapaService;

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
     * Valida se o subprocesso está em uma situação que permite
     * a edição do mapa. Em seguida, delega a operação de salvar o mapa para o
     * {@link MapaService}. Se for a primeira vez que competências estão sendo
     * adicionadas a um mapa (ou seja, o mapa estava vazio), e o subprocesso
     * estava na situação 'CADASTRO_HOMOLOGADO', o estado do subprocesso é
     * avançado para 'MAPA_CRIADO'.
     *
     * @param codSubprocesso        O código do subprocesso.
     * @param request              O DTO com os dados completos do mapa a serem salvos.
     * @param tituloUsuario O título de eleitor do usuário que está realizando a operação.
     * @return O {@link MapaCompletoDto} representando o estado salvo do mapa.
     * @throws sgc.comum.erros.ErroDominioNaoEncontrado se o subprocesso ou seu mapa não forem encontrados.
     * @throws IllegalStateException se o subprocesso não estiver em uma situação
     *                               válida para a edição do mapa.
     */
    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, Long tituloUsuario) {
        log.info("Salvando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso, tituloUsuario);

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(codMapa, request, tituloUsuario);

        if (eraVazio && temNovasCompetencias && subprocesso.getSituacao() == SituacaoSubprocesso.CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", codSubprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaReq request, Long tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.adicionarCompetencia(subprocesso.getMapa(), request.descricao(), request.atividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaReq request, Long tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(Long codSubprocesso, Long codCompetencia, Long tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.removerCompetencia(codCompetencia);
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso.findById(codSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.CADASTRO_HOMOLOGADO && situacao != SituacaoSubprocesso.MAPA_CRIADO) {
            throw new ErroNegocio("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }
}