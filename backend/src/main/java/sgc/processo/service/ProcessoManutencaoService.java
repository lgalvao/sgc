package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static sgc.processo.model.SituacaoProcesso.CRIADO;
import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoManutencaoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeFacade unidadeService;
    private final ProcessoValidador processoValidador;
    private final ProcessoConsultaService processoConsultaService;

    @Transactional
    public Processo criar(CriarProcessoRequest req) {
        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : req.unidades()) {
            Unidade unidade = unidadeService.buscarEntidadePorId(codigoUnidade);
            participantes.add(unidade);
        }

        TipoProcesso tipoProcesso = req.tipo();

        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(req.unidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Processo processo = new Processo()
                .setDescricao(req.descricao())
                .setTipo(tipoProcesso)
                .setDataLimite(req.dataLimiteEtapa1())
                .setSituacao(CRIADO)
                .setDataCriacao(LocalDateTime.now());

        // Adiciona participantes com snapshot
        processo.adicionarParticipantes(participantes);

        // Salva uma única vez com todos os participantes
        Processo processoSalvo = processoRepo.saveAndFlush(processo);

        log.info("Processo {} criado com {} participantes: {}. Participante IDs: {}",
                processoSalvo.getCodigo(),
                processoSalvo.getParticipantes().size(),
                processoSalvo.getCodigosParticipantes(),
                processoSalvo.getParticipantes().stream()
                        .map(up -> "(" + up.getId().getProcessoCodigo() + "," + up.getId().getUnidadeCodigo() + ")")
                        .toList());
        
        // Verify the data is in the database
        var countQuery = processoRepo.findById(processoSalvo.getCodigo());
        log.info("Verificação: Processo {} existe no repo? {}", processoSalvo.getCodigo(), countQuery.isPresent());

        return processoSalvo;
    }

    @Transactional
    public Processo atualizar(Long codigo, AtualizarProcessoRequest requisicao) {
        Processo processo = processoConsultaService.buscarProcessoCodigo(codigo);

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.descricao());
        processo.setTipo(requisicao.tipo());
        processo.setDataLimite(requisicao.dataLimiteEtapa1());

        if (requisicao.tipo() == REVISAO || requisicao.tipo() == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(requisicao.unidades()))
                    .ifPresent(msg -> {
                        throw new ErroProcesso(msg);
                    });
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.unidades()) {
            participantes.add(unidadeService.buscarEntidadePorId(codigoUnidade));
        }

        // Atualiza participantes com sincronização inteligente
        processo.sincronizarParticipantes(participantes);

        Processo processoAtualizado = processoRepo.saveAndFlush(processo);
        log.info("Processo {} atualizado.", codigo);

        return processoAtualizado;
    }

    @Transactional
    public void apagar(Long codigo) {
        Processo processo = processoConsultaService.buscarProcessoCodigo(codigo);

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }
}
