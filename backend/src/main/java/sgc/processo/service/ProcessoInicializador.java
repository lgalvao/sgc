package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;
import java.util.stream.*;

import static sgc.processo.model.SituacaoProcesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoInicializador {
    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final ProcessoNotificacaoService notificacaoService;
    private final SubprocessoService subprocessoService;
    private final ProcessoValidador processoValidador;

    /**
     * Inicia um processo de qualquer tipo.
     *
     * @param codsUnidadesParam Lista de códigos de unidades (usada apenas para REVISAO)
     * @return Lista de erros (vazia se sucesso)
     */
    public List<String> iniciar(Long codigo, List<Long> codsUnidadesParam, Usuario usuario) {
        Processo processo = repo.buscar(Processo.class, codigo);

        validarSituacaoProcesso(processo);

        TipoProcesso tipo = processo.getTipo();
        List<Long> codigosUnidades;
        Set<Unidade> unidadesParaProcessar;

        // Determinar unidades baseado no tipo de processo
        if (tipo == TipoProcesso.REVISAO) {
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

        Unidade admin = repo.buscarPorSigla(Unidade.class, "ADMIN");
        criarSubprocessos(processo, tipo, codigosUnidades, unidadesParaProcessar, unidadesMapas, admin, usuario);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);
        notificacaoService.emailInicioProcesso(processo.getCodigo());

        int contagemUnidades = codigosUnidades.size();
        log.info("Processo de {} {} iniciado para {} unidade(s).", tipo.name().toLowerCase(), codigo, contagemUnidades);
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
                                   List<UnidadeMapa> unidadesMapas, Unidade admin, Usuario usuario) {

        Map<Long, UnidadeMapa> mapaUnidadeMapa = unidadesMapas.stream()
                .collect(Collectors.toMap(UnidadeMapa::getUnidadeCodigo, m -> m));

        switch (tipo) {
            case TipoProcesso.MAPEAMENTO ->
                    subprocessoService.criarParaMapeamento(processo, unidadesParaProcessar, admin, usuario);

            case TipoProcesso.REVISAO -> {
                for (Long codUnidade : codigosUnidades) {
                    Unidade unidade = repo.buscar(Unidade.class, codUnidade);
                    UnidadeMapa um = mapaUnidadeMapa.get(codUnidade);
                    subprocessoService.criarParaRevisao(processo, unidade, um, admin, usuario);
                }
            }

            case TipoProcesso.DIAGNOSTICO -> {
                for (Unidade unidade : unidadesParaProcessar) {
                    UnidadeMapa um = mapaUnidadeMapa.get(unidade.getCodigo());
                    subprocessoService.criarParaDiagnostico(processo, unidade, um, admin, usuario);
                }
            }
        }
    }

    private Optional<String> getMensagemErroUnidadesEmProcessosAtivos(List<Long> codsUnidades) {
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
