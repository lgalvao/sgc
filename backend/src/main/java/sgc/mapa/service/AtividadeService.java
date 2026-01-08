package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.*;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AtividadeService {
    private static final String ENTIDADE_ATIVIDADE = "Atividade";
    private static final String ENTIDADE_MAPA = "Mapa";

    private final AtividadeRepo atividadeRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoService conhecimentoService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AtividadeDto> listar() {
        return atividadeRepo.findAll().stream().map(atividadeMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AtividadeDto obterDto(Long codAtividade) {
        return atividadeRepo.findById(codAtividade)
                .map(atividadeMapper::toDto)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade));
    }

    @Transactional(readOnly = true)
    public Atividade obterPorCodigo(Long codAtividade) {
        return atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade));
    }

    public AtividadeDto criar(AtividadeDto atividadeDto) {
        Mapa mapa = mapaRepo.findById(atividadeDto.getMapaCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_MAPA, atividadeDto.getMapaCodigo()));

        notificarAlteracaoMapa(atividadeDto.getMapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(atividadeDto);
        entidade.setMapa(mapa);

        Atividade salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toDto(salvo);
    }

    public void atualizar(Long codigo, AtividadeDto atividadeDto) {
        Atividade existente = atividadeRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codigo));

        atualizarAtividadeExistente(atividadeDto, existente);
    }

    private void atualizarAtividadeExistente(AtividadeDto atividadeDto, Atividade existente) {
        if (existente.getMapa() != null) {
            notificarAlteracaoMapa(existente.getMapa().getCodigo());
        }

        var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());

        atividadeRepo.save(existente);
    }

    public void excluir(Long codAtividade) {
        Atividade atividade = atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade));

        excluirAtividadeEConhecimentos(atividade);
    }

    private void excluirAtividadeEConhecimentos(Atividade atividade) {
        notificarAlteracaoMapa(atividade.getMapa().getCodigo());
        conhecimentoService.excluirTodosDaAtividade(atividade);
        atividadeRepo.delete(atividade);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigo(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoWithConhecimentos(mapaCodigo);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
