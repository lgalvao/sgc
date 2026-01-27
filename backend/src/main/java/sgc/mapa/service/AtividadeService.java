package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepo atividadeRepo;
    private final RepositorioComum repo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoService conhecimentoService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listar() {
        return atividadeRepo.findAllWithMapa().stream().map(atividadeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AtividadeResponse obterResponse(Long codAtividade) {
        return atividadeMapper.toResponse(obterPorCodigo(codAtividade));
    }

    @Transactional(readOnly = true)
    public Atividade obterPorCodigo(Long codAtividade) {
        return repo.buscar(Atividade.class, codAtividade);
    }
    
    @Transactional(readOnly = true)
    public List<Atividade> buscarPorCodigos(List<Long> codigos) {
        return atividadeRepo.findAllById(codigos);
    }

    @Transactional(readOnly = true)
    public int contarPorMapa(Long codMapa) {
        return (int) atividadeRepo.countByMapaCodigo(codMapa);
    }

    public AtividadeResponse criar(CriarAtividadeRequest request) {
        Mapa mapa = repo.buscar(Mapa.class, request.mapaCodigo());

        notificarAlteracaoMapa(request.mapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(request);
        entidade.setMapa(mapa);

        Atividade salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toResponse(salvo);
    }

    public void atualizar(Long codigo, AtualizarAtividadeRequest request) {
        Atividade existente = repo.buscar(Atividade.class, codigo);

        if (existente.getMapa() != null) {
            notificarAlteracaoMapa(existente.getMapa().getCodigo());
        }

        var entidadeParaAtualizar = atividadeMapper.toEntity(request);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());

        atividadeRepo.save(existente);
    }

    public List<Atividade> atualizarDescricoesEmLote(java.util.Map<Long, String> descricoesPorId) {
        List<Atividade> atividades = atividadeRepo.findAllById(descricoesPorId.keySet());
        java.util.Set<Long> mapasAfetados = new java.util.HashSet<>();

        for (Atividade atividade : atividades) {
            String novaDescricao = descricoesPorId.get(atividade.getCodigo());
            if (novaDescricao != null) {
                atividade.setDescricao(novaDescricao);
            }
            if (atividade.getMapa() != null) {
                mapasAfetados.add(atividade.getMapa().getCodigo());
            }
        }

        atividadeRepo.saveAll(atividades);

        for (Long codMapa : mapasAfetados) {
            notificarAlteracaoMapa(codMapa);
        }

        return atividades;
    }

    public void excluir(Long codAtividade) {
        Atividade atividade = repo.buscar(Atividade.class, codAtividade);

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
    public List<Atividade> buscarPorMapaCodigoSemRelacionamentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findWithConhecimentosByMapaCodigo(mapaCodigo);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
