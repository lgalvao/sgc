package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.*;
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
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final ApplicationEventPublisher publicadorEventos;
    private final SubprocessoFacade subprocessoFacade;
    private final ProcessoValidador processoValidador;

    /**
     * Inicia um processo de qualquer tipo.
     *
     * @param codigo            Código do processo
     * @param codsUnidadesParam Lista de códigos de unidades (usada apenas para REVISAO)
     * @param usuario           Usuário que está iniciando o processo
     * @return Lista de erros (vazia se sucesso)
     */
    public List<String> iniciar(Long codigo, List<Long> codsUnidadesParam, Usuario usuario) {
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
            unidadesParaProcessar = Set.of(); // Será buscado individualmente via Repo se necessário
        } else {
            // Mapeamento e Diagnóstico usam participantes do processo (snapshots)
            List<Long> codsParticipantes = processo.getCodigosParticipantes();
            if (codsParticipantes.isEmpty()) {
                throw new ErroUnidadesNaoDefinidas("Não há unidades participantes definidas para este processo.");
            }
            codigosUnidades = codsParticipantes;
            // Buscar as entidades Unidade do repositório para criar subprocessos
            unidadesParaProcessar = new HashSet<>(unidadeRepo.findAllById(codigosUnidades));
        }

        // Validar unidades (Batch)
        List<String> erros = validarUnidades(tipo, codigosUnidades);
        if (!erros.isEmpty()) {
            return erros;
        }

        // Se for REVISAO ou DIAGNOSTICO, precisamos carregar os mapas vigentes em lote para passar para a factory
        List<UnidadeMapa> unidadesMapas = List.of();
        if (tipo == TipoProcesso.REVISAO || tipo == TipoProcesso.DIAGNOSTICO) {
            unidadesMapas = unidadeMapaRepo.findAllById(codigosUnidades);
        }

        // Buscar SEDOC como unidade de origem para as movimentações iniciais
        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", "SEDOC"));

        // Criar subprocessos
        criarSubprocessos(processo, tipo, codigosUnidades, unidadesParaProcessar, unidadesMapas, sedoc, usuario);

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
            processoValidador.getMensagemErroUnidadesSemMapa(codigosUnidades).ifPresent(erros::add);
        }

        // Validar se unidades já estão em uso
        getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades).ifPresent(erros::add);

        return erros;
    }

    private void criarSubprocessos(Processo processo, TipoProcesso tipo,
                                   List<Long> codigosUnidades, Set<Unidade> unidadesParaProcessar,
                                   List<UnidadeMapa> unidadesMapas, Unidade sedoc, Usuario usuario) {

        Map<Long, UnidadeMapa> mapaUnidadeMapa = unidadesMapas.stream()
                .collect(Collectors.toMap(UnidadeMapa::getUnidadeCodigo, m -> m));

        if (tipo == TipoProcesso.MAPEAMENTO) {
            subprocessoFacade.criarParaMapeamento(processo, unidadesParaProcessar, sedoc, usuario);
        } else if (tipo == TipoProcesso.REVISAO) {
            // Batch fetch units to avoid N+1 queries
            List<Unidade> unidades = unidadeRepo.findAllById(codigosUnidades);
            Map<Long, Unidade> mapaUnidades = unidades.stream()
                    .collect(Collectors.toMap(Unidade::getCodigo, u -> u));

            for (Long codUnidade : codigosUnidades) {
                Unidade unidade = mapaUnidades.get(codUnidade);
                if (unidade == null) {
                    throw new ErroEntidadeNaoEncontrada("Unidade", codUnidade);
                }
                UnidadeMapa um = mapaUnidadeMapa.get(codUnidade);
                subprocessoFacade.criarParaRevisao(processo, unidade, um, sedoc, usuario);
            }
        } else {
            // Caso DIAGNOSTICO
            for (Unidade unidade : unidadesParaProcessar) {
                UnidadeMapa um = mapaUnidadeMapa.get(unidade.getCodigo());
                subprocessoFacade.criarParaDiagnostico(processo, unidade, um, sedoc, usuario);
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
}
