package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.processo.eventos.EventoSubprocessoMapaDisponibilizado;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaWorkflowService {
    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaService mapaService;
    private final CompetenciaService competenciaService;
    private final ApplicationEventPublisher publicadorDeEventos;

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, String tituloUsuario) {
        log.debug("Salvando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso, tituloUsuario);

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(codMapa, request, tituloUsuario);

        if (eraVazio
                && temNovasCompetencias
                && subprocesso.getSituacao()
                == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaReq request, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();

        competenciaService.adicionarCompetencia(
                subprocesso.getMapa(), request.getDescricao(), request.getAtividadesIds()
        );

        // Alterar situação para MAPA_CRIADO se era vazio e passou a ter competências
        if (eraVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaReq request,
            String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(
                codCompetencia, request.getDescricao(), request.getAtividadesIds());

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(
            Long codSubprocesso, Long codCompetencia, String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        competenciaService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio e estava em MAPA_CRIADO, voltar para CADASTRO_HOMOLOGADO
        boolean ficouVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();
        if (ficouVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            subprocessoRepo.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.findById(codSubprocesso)
                .orElseThrow(() ->
                        new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO
                && situacao != SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO
                && situacao != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && situacao != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s"
                            .formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        log.info("Disponibilizando mapa do subprocesso: codSubprocesso={}", codSubprocesso);

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        validarMapaParaDisponibilizacao(subprocesso);

        if (request.getDataLimite() == null) {
            throw new ErroValidacao("A data limite para validação é obrigatória.");
        }

        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa2(request.getDataLimite().atStartOfDay());
        subprocessoRepo.save(subprocesso);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaDisponibilizado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(subprocesso.getUnidade())
                        .unidadeDestino(subprocesso.getUnidade())
                        .build());

        log.info("Subprocesso {} atualizado e mapa disponibilizado.", codSubprocesso);
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = competenciaRepo.findByMapaCodigo(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = atividadeRepo.findBySubprocessoCodigo(subprocesso.getCodigo());

        var atividadesAssociadas = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        var atividadesNaoAssociadas = atividadesDoSubprocesso.stream()
                .filter(a -> !atividadesAssociadas.contains(a.getCodigo()))
                .toList();

        if (!atividadesNaoAssociadas.isEmpty()) {
            String nomesAtividades = atividadesNaoAssociadas.stream()
                    .map(Atividade::getDescricao)
                    .collect(Collectors.joining(", "));

            throw new ErroValidacao("""
                    Todas as atividades devem estar associadas a pelo menos uma competência.
                    Atividades pendentes: %s""".
                    formatted(nomesAtividades)
            );
        }
    }
}
