package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.dto.*;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.modelo.*;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapperCustom processoDetalheMapperCustom;

    @Transactional
    public ProcessoDto criar(CriarProcessoReq requisicao) {
        if (requisicao.descricao() == null || requisicao.descricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.unidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.tipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.tipo())) {
            for (Long codigoUnidade : requisicao.unidades()) {
                if (unidadeRepo.findById(codigoUnidade).isEmpty()) {
                    throw new ErroDominioNaoEncontrado("Unidade", codigoUnidade);
                }
            }
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.descricao());
        processo.setTipo(TipoProcesso.valueOf(requisicao.tipo()));
        processo.setDataLimite(requisicao.dataLimiteEtapa1());
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoCriadoEvento(this, processoSalvo.getCodigo()));
        log.info("Processo '{}' (código {}) criado com sucesso.", processoSalvo.getDescricao(), processoSalvo.getCodigo());

        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDto atualizar(Long id, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.descricao());
        processo.setTipo(TipoProcesso.valueOf(requisicao.tipo()));
        processo.setDataLimite(requisicao.dataLimiteEtapa1());

        Processo processoAtualizado = processoRepo.save(processo);
        log.info("Processo {} atualizado com sucesso.", id);

        return processoMapper.toDTO(processoAtualizado);
    }

    @Transactional
    public void apagar(Long id) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(id);
        log.info("Processo {} removido com sucesso.", id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long id) {
        return processoRepo.findById(id).map(processoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoSeguranca.checarAcesso(authentication, #idProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long idProcesso) {
        Processo processo = processoRepo.findById(idProcesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", idProcesso));

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepo.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(idProcesso);

        return processoDetalheMapperCustom.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }







}
