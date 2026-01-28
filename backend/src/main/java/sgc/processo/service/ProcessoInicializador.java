package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.factory.SubprocessoFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static sgc.processo.model.SituacaoProcesso.CRIADO;

/**
 * Serviço responsável pela inicialização de processos.
 * Consolidando lógica comum dos métodos iniciarProcessoMapeamento, iniciarProcessoRevisao e iniciarProcessoDiagnostico.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoInicializador {

    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    private final ApplicationEventPublisher publicadorEventos;
    private final SubprocessoFactory subprocessoFactory;

    /**
     * Inicia um processo de qualquer tipo.
     *
     * @param codigo            Código do processo
     * @param codsUnidadesParam Lista de códigos de unidades (usada apenas para REVISAO)
     * @return Lista de erros (vazia se sucesso)
     */
    public List<String> iniciar(Long codigo, List<Long> codsUnidadesParam) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        validarSituacaoProcesso(processo);

        TipoProcesso tipo = processo.getTipo();
        List<Long> codigosUnidades;
        Set<Unidade> unidadesParaProcessar;

        // Determinar unidades baseado no tipo de processo
        if (tipo == TipoProcesso.REVISAO) {
            // Revisão usa lista passada como parâmetro
            if (codsUnidadesParam.isEmpty()) {
                throw new ErroUnidadesNaoDefinidas("A lista de unidades é obrigatória para iniciar o processo de revisão.");
            }
            codigosUnidades = codsUnidadesParam;
            unidadesParaProcessar = null; // Será buscado individualmente via Repo se necessário
        } else {
            // Mapeamento e Diagnóstico usam participantes do processo
            Set<Unidade> participantes = processo.getParticipantes();
            if (participantes.isEmpty()) {
                throw new ErroUnidadesNaoDefinidas("Não há unidades participantes definidas para este processo.");
            }
            codigosUnidades = participantes.stream().map(Unidade::getCodigo).toList();
            unidadesParaProcessar = participantes;
        }

        // Validar unidades (Batch)
        List<String> erros = validarUnidades(tipo, codigosUnidades);
        if (!erros.isEmpty()) {
            return erros;
        }

        // Se for REVISAO ou DIAGNOSTICO, precisamos carregar os mapas vigentes em lote para passar para a factory
        List<sgc.organizacao.model.UnidadeMapa> unidadesMapas = List.of();
        if (tipo == TipoProcesso.REVISAO || tipo == TipoProcesso.DIAGNOSTICO) {
            unidadesMapas = unidadeMapaRepo.findAllById(codigosUnidades);
        }

        // Criar subprocessos
        criarSubprocessos(processo, tipo, codigosUnidades, unidadesParaProcessar, unidadesMapas);

        // Atualizar situação e salvar
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        // Publicar evento
        publicadorEventos.publishEvent(
                EventoProcessoIniciado.builder()
                        .codProcesso(processo.getCodigo())
                        .tipo(processo.getTipo().name())
                        .dataHoraInicio(LocalDateTime.now())
                        .codUnidades(codigosUnidades)
                        .build());

        log.info("Processo de {} {} iniciado para {} unidade(s).",
                tipo.name().toLowerCase(), codigo, codigosUnidades.size());

        return List.of();
    }

    private void validarSituacaoProcesso(Processo processo) {
        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }
    }

    private List<String> validarUnidades(TipoProcesso tipo, List<Long> codigosUnidades) {
        List<String> erros = new ArrayList<>();

        // Validar mapa vigente para revisão e diagnóstico
        if (tipo == TipoProcesso.REVISAO || tipo == TipoProcesso.DIAGNOSTICO) {
            getMensagemErroUnidadesSemMapa(codigosUnidades).ifPresent(erros::add);
        }

        // Validar se unidades já estão em uso
        getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades).ifPresent(erros::add);

        return erros;
    }

    private void criarSubprocessos(Processo processo, TipoProcesso tipo,
                                   List<Long> codigosUnidades, Set<Unidade> unidadesParaProcessar,
                                   List<sgc.organizacao.model.UnidadeMapa> unidadesMapas) {

        java.util.Map<Long, sgc.organizacao.model.UnidadeMapa> mapaUnidadeMapa = unidadesMapas.stream()
                .collect(Collectors.toMap(sgc.organizacao.model.UnidadeMapa::getUnidadeCodigo, m -> m));

        if (tipo == TipoProcesso.MAPEAMENTO) {
            subprocessoFactory.criarParaMapeamento(processo, unidadesParaProcessar);
        } else if (tipo == TipoProcesso.REVISAO) {
            // Batch fetch units to avoid N+1 queries
            List<Unidade> unidades = unidadeRepo.findAllById(codigosUnidades);
            java.util.Map<Long, Unidade> mapaUnidades = unidades.stream()
                    .collect(Collectors.toMap(Unidade::getCodigo, u -> u));

            for (Long codUnidade : codigosUnidades) {
                Unidade unidade = mapaUnidades.get(codUnidade);
                if (unidade == null) {
                    throw new ErroEntidadeNaoEncontrada("Unidade", codUnidade);
                }
                sgc.organizacao.model.UnidadeMapa um = mapaUnidadeMapa.get(codUnidade);
                subprocessoFactory.criarParaRevisao(processo, unidade, um);
            }
        } else {
            // Caso DIAGNOSTICO
            for (Unidade unidade : unidadesParaProcessar) {
                sgc.organizacao.model.UnidadeMapa um = mapaUnidadeMapa.get(unidade.getCodigo());
                subprocessoFactory.criarParaDiagnostico(processo, unidade, um);
            }
        }
    }

    private Optional<String> getMensagemErroUnidadesEmProcessosAtivos(List<Long> codsUnidades) {
        // Validation done upstream ensures list is not empty
        List<Long> unidadesBloqueadas = processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
                SituacaoProcesso.EM_ANDAMENTO, codsUnidades);

        if (!unidadesBloqueadas.isEmpty()) {
            List<String> siglasUnidadesBloqueadas = unidadeRepo.findSiglasByCodigos(unidadesBloqueadas);
            return Optional.of("As seguintes unidades já participam de outro processo ativo: %s"
                    .formatted(String.join(", ", siglasUnidadesBloqueadas)));
        }
        return Optional.empty();
    }

    private Optional<String> getMensagemErroUnidadesSemMapa(List<Long> codigosUnidades) {
        // Validation done upstream ensures list is not empty
        List<Long> codigosComMapa = unidadeMapaRepo.findAllById(codigosUnidades).stream()
                .map(sgc.organizacao.model.UnidadeMapa::getUnidadeCodigo)
                .toList();

        List<Long> unidadesSemMapa = codigosUnidades.stream()
                .filter(codigo -> !codigosComMapa.contains(codigo))
                .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeRepo.findSiglasByCodigos(unidadesSemMapa);
            return Optional.of(("As seguintes unidades não possuem mapa vigente e não podem participar"
                    + " de um processo de revisão: %s").formatted(String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }
}
