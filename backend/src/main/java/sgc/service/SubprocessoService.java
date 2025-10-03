package sgc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.dto.SubprocessoDetailDTO;
import sgc.exception.DomainAccessDeniedException;
import sgc.exception.DomainNotFoundException;
import sgc.mapper.SubprocessoMapper;
import sgc.model.Atividade;
import sgc.model.Conhecimento;
import sgc.model.Movimentacao;
import sgc.model.Subprocesso;
import sgc.repository.AtividadeRepository;
import sgc.repository.MapaRepository;
import sgc.repository.MovimentacaoRepository;
import sgc.repository.SubprocessoRepository;
import sgc.repository.ConhecimentoRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável por recuperar os detalhes de um Subprocesso (CDU-07).
 * - monta SubprocessoDetailDTO com unidade, responsável, situação, localização atual,
 *   prazo da etapa atual, movimentações (ordenadas) e elementos do processo (atividades/conhecimentos).
 * - valida permissão básica por perfil/unidade (ADMIN | GESTOR).
 */
@Service
@RequiredArgsConstructor
public class SubprocessoService {

    private final SubprocessoRepository subprocessoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final MapaRepository mapaRepository;
    private final AtividadeRepository atividadeRepository;
    private final ConhecimentoRepository conhecimentoRepository;

    /**
     * Recupera os detalhes do subprocesso.
     *
     * @param id             id do subprocesso
     * @param perfil         perfil do usuário (ADMIN | GESTOR)
     * @param unidadeUsuario código da unidade do usuário (pode ser null para ADMIN)
     * @return SubprocessoDetailDTO montado
     * @throws DomainNotFoundException      se subprocesso não existir
     * @throws DomainAccessDeniedException  se usuário não tiver permissão para acessar
     */
    @Transactional(readOnly = true)
    public SubprocessoDetailDTO getDetails(Long id, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new DomainAccessDeniedException("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = subprocessoRepository.findById(id)
                .orElseThrow(() -> new DomainNotFoundException("Subprocesso não encontrado: " + id));

        // autorização: ADMIN tem acesso; GESTOR apenas se sua unidade for a unidade do subprocesso
        if ("GESTOR".equalsIgnoreCase(perfil)) {
            if (sp.getUnidade() == null || unidadeUsuario == null || !unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new DomainAccessDeniedException("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            throw new DomainAccessDeniedException("Perfil sem permissão.");
        }

        // movimentações ordenadas (mais recente primeiro)
        List<Movimentacao> movimentacoes = movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        // atividades do mapa do subprocesso (se existir mapa)
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepository.findByMapaCodigo(sp.getMapa().getCodigo())
                : Collections.emptyList();
 
        // conhecimentos vinculados às atividades — construir conjunto de ids para filtrar eficientemente
        final Set<Long> atividadeIds = atividades.stream()
                .map(Atividade::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
 
        List<Conhecimento> conhecimentos = conhecimentoRepository.findAll()
                .stream()
                .filter(c -> c.getAtividade() != null && atividadeIds.contains(c.getAtividade().getCodigo()))
                .collect(Collectors.toList());

        // montar DTO final delegando parte da conversão ao mapper
        return SubprocessoMapper.toDetailDTO(sp, movimentacoes, atividades, conhecimentos);
    }
}