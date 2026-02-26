package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoManutencaoService {
    private final ProcessoRepo processoRepo;
    private final OrganizacaoFacade organizacaoFacade;
    private final ProcessoValidador processoValidador;
    private final ProcessoConsultaService processoConsultaService;

    @Transactional
    public Processo criar(CriarProcessoRequest req) {
        Set<Unidade> participantes = req.unidades().stream()
                .map(organizacaoFacade::unidadePorCodigo)
                .collect(Collectors.toSet());

        processoValidador.validarTiposUnidades(new ArrayList<>(participantes)).ifPresent(msg -> {
            throw new ErroProcesso(msg);
        });

        TipoProcesso tipoProcesso = req.tipo();
        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(req.unidades())).ifPresent(msg -> {
                throw new ErroProcesso(msg);
            });
        }

        Processo processo = new Processo()
                .setDescricao(req.descricao())
                .setTipo(tipoProcesso)
                .setDataLimite(req.dataLimiteEtapa1())
                .setSituacao(CRIADO)
                .setDataCriacao(LocalDateTime.now());

        processo.adicionarParticipantes(participantes);
        Processo processoSalvo = processoRepo.saveAndFlush(processo);

        log.info("Processo {} criado com {} unidades participantes",
                processoSalvo.getCodigo(),
                processoSalvo.getParticipantes().size());

        return processoSalvo;
    }

    @Transactional
    public Processo atualizar(Long codigo, AtualizarProcessoRequest req) {
        Processo processo = processoConsultaService.buscarProcessoCodigo(codigo);
        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        TipoProcesso tipoProcesso = req.tipo();
        processo.setDescricao(req.descricao());
        processo.setTipo(tipoProcesso);
        processo.setDataLimite(req.dataLimiteEtapa1());

        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            processoValidador.getMensagemErroUnidadesSemMapa(new ArrayList<>(req.unidades())).ifPresent(msg -> {
                throw new ErroProcesso(msg);
            });
        }

        Set<Unidade> participantes = req.unidades().stream().map(organizacaoFacade::unidadePorCodigo).collect(Collectors.toSet());
        processoValidador.validarTiposUnidades(new ArrayList<>(participantes)).ifPresent(msg -> {
            throw new ErroProcesso(msg);
        });

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
