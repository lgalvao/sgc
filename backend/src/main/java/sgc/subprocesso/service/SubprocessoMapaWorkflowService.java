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
    private final SubprocessoRepo repositorioSubprocesso;
    private final CompetenciaRepo repositorioCompetencia;
    private final AtividadeRepo atividadeRepo;
    private final MapaService mapaService;
    private final CompetenciaService competenciaService;
    private final ApplicationEventPublisher publicadorDeEventos;

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, String tituloUsuario) {
        log.info("Salvando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso, tituloUsuario);

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(codMapa, request, tituloUsuario);

        if (eraVazio && temNovasCompetencias && subprocesso.getSituacao() == SituacaoSubprocesso.CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", codSubprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaReq request, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.adicionarCompetencia(subprocesso.getMapa(), request.getDescricao(),
                request.getAtividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaReq request,
            String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(codCompetencia, request.getDescricao(), request.getAtividadesIds());
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(Long codSubprocesso, Long codCompetencia, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.removerCompetencia(codCompetencia);
        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.CADASTRO_HOMOLOGADO
                && situacao != SituacaoSubprocesso.MAPA_CRIADO
                && situacao != SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Mapa só pode ser editado com cadastro homologado, mapa criado ou cadastro em andamento. Situação atual: %s"
                            .formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        log.info("Disponibilizando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso,
                usuario.getTituloEleitoral());

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        validarMapaParaDisponibilizacao(subprocesso);

        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa2(request.getDataLimite().atStartOfDay());
        repositorioSubprocesso.save(subprocesso);

        publicadorDeEventos.publishEvent(EventoSubprocessoMapaDisponibilizado.builder()
                .codSubprocesso(codSubprocesso)
                .usuario(usuario)
                .unidadeOrigem(subprocesso.getUnidade())
                .unidadeDestino(subprocesso.getUnidade())
                .build());

        log.info("Subprocesso {} atualizado para MAPA_DISPONIBILIZADO e mapa disponibilizado.", codSubprocesso);
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = repositorioCompetencia.findByMapaCodigo(codMapa);

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
            throw new ErroValidacao(
                    "Todas as atividades devem estar associadas a pelo menos uma competência. Atividades pendentes: "
                            + nomesAtividades);
        }
    }
}
