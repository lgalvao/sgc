package sgc.comum;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.Alerta;
import sgc.alerta.AlertaDTO;
import sgc.alerta.AlertaRepository;
import sgc.processo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service responsável por compor as visões do painel (processos e alertas)
 * Aplica regras de visibilidade e realiza o mapeamento para DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PainelService {
    private final ProcessoRepository processoRepository;
    private final AlertaRepository alertaRepository;
    private final UnidadeProcessoRepository unidadeProcessoRepository;

    /**
     * Lista processos aplicando regra de visibilidade por perfil e/ou unidade.
     * Regras implementadas:
     * - perfil == "ADMIN" -> somente processos com situacao 'CRIADO'
     * - se unidadeCodigo informado -> somente processos vinculados a essa unidade (via UNIDADE_PROCESSO)
     * <p>
     * Observação: para simplificar os testes iniciais este método realiza paginação em memória
     * após aplicar os filtros. Em datasets grandes isso deve ser otimizado com queries específicas.
     *
     * @param perfil        perfil requisitante (obrigatório)
     * @param unidadeCodigo filtro opcional por unidade
     * @param pageable      paginação
     * @return página de ProcessoResumoDTO
     */
    public Page<ProcessoResumoDTO> listarProcessos(String perfil, Long unidadeCodigo, Pageable pageable) {
        if (perfil == null || perfil.isBlank()) {
            throw new IllegalArgumentException("Parâmetro 'perfil' é obrigatório");
        }

        // obter todos os processos (simplificação para testes)
        List<Processo> todos = processoRepository.findAll();

        // aplicar filtro por perfil
        List<Processo> filtrados = new ArrayList<>();
        for (Processo p : todos) {
            if ("ADMIN".equalsIgnoreCase(perfil)) {
                if (!"CRIADO".equalsIgnoreCase(nullableUpper(p.getSituacao()))) {
                    continue;
                }
            }
            // filtro por unidade: checar se existe UnidadeProcesso vinculada ao processo com unidadeCodigo
            if (unidadeCodigo != null) {
                List<UnidadeProcesso> ups = unidadeProcessoRepository.findByProcessoCodigo(p.getCodigo());
                boolean pertence = ups.stream().anyMatch(u -> Objects.equals(u.getCodigo(), unidadeCodigo));
                if (!pertence) {
                    continue;
                }
            }
            filtrados.add(p);
        }

        // mapear para DTOs
        List<ProcessoResumoDTO> dtos = filtrados.stream()
                .map(p -> {
                    ProcessoResumoDTO dto = new ProcessoResumoDTO();
                    dto.setCodigo(p.getCodigo());
                    dto.setDescricao(p.getDescricao());
                    dto.setSituacao(p.getSituacao());
                    dto.setTipo(p.getTipo());
                    dto.setDataLimite(p.getDataLimite());
                    dto.setDataCriacao(p.getDataCriacao());

                    // tentar obter uma unidade vinculada (primeira encontrada)
                    List<UnidadeProcesso> ups = unidadeProcessoRepository.findByProcessoCodigo(p.getCodigo());
                    if (!ups.isEmpty()) {
                        UnidadeProcesso up = ups.getFirst();
                        dto.setUnidadeCodigo(up.getCodigo());
                        dto.setUnidadeNome(up.getNome());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // paginação em memória
        int total = dtos.size();
        int pageSize = pageable.getPageSize();
        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ProcessoResumoDTO> pageContent;
        if (fromIndex >= total || fromIndex < 0) {
            pageContent = List.of();
        } else {
            pageContent = dtos.subList(fromIndex, toIndex);
        }

        return new PageImpl<>(pageContent, pageable, total);
    }

    /**
     * Lista alertas visíveis para o usuário/unidade autenticada.
     * Regras básicas:
     * - se usuarioTitulo informado -> alertas destinados ao usuário
     * - se unidadeCodigo informado -> alertas destinados à unidade
     * - se ambos nulos -> retorna todos (para administração/testes)
     *
     * @param usuarioTitulo título do usuário autenticado (opcional)
     * @param unidadeCodigo código da unidade autenticada (opcional)
     * @param pageable      paginação
     * @return página de AlertaDTO
     */
    public Page<AlertaDTO> listarAlertas(String usuarioTitulo, Long unidadeCodigo, Pageable pageable) {
        List<Alerta> todos = alertaRepository.findAll();

        List<Alerta> filtrados = todos.stream()
                .filter(a -> {
                    if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
                        return a.getUsuarioDestino() != null && usuarioTitulo.equalsIgnoreCase(a.getUsuarioDestino().getTitulo());
                    }
                    if (unidadeCodigo != null) {
                        return a.getUnidadeDestino() != null && Objects.equals(a.getUnidadeDestino().getCodigo(), unidadeCodigo);
                    }
                    return true;
                })
                .toList();

        List<AlertaDTO> dtos = filtrados.stream().map(a -> {
            AlertaDTO dto = new AlertaDTO();
            dto.setCodigo(a.getCodigo());
            dto.setDescricao(a.getDescricao());
            dto.setDataHora(a.getDataHora());
            dto.setProcessoCodigo(a.getProcesso() != null ? a.getProcesso().getCodigo() : null);
            dto.setUnidadeOrigemCodigo(a.getUnidadeOrigem() != null ? a.getUnidadeOrigem().getCodigo() : null);
            dto.setUnidadeDestinoCodigo(a.getUnidadeDestino() != null ? a.getUnidadeDestino().getCodigo() : null);
            dto.setUsuarioDestinoTitulo(a.getUsuarioDestino() != null ? a.getUsuarioDestino().getTitulo() : null);
            return dto;
        }).collect(Collectors.toList());

        int total = dtos.size();
        int pageSize = pageable.getPageSize();
        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<AlertaDTO> pageContent;
        if (fromIndex >= total || fromIndex < 0) {
            pageContent = List.of();
        } else {
            pageContent = dtos.subList(fromIndex, toIndex);
        }

        return new PageImpl<>(pageContent, pageable, total);
    }

    private String nullableUpper(String s) {
        return s == null ? null : s.toUpperCase();
    }
}