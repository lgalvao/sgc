package sgc.comum;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaDto;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.comum.enums.SituacaoProcesso;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Serviço responsável por compor as visões do painel (processos e alertas).
 * Aplica regras de visibilidade e realiza o mapeamento para DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PainelService {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;

    /**
     * Lista processos aplicando a regra de visibilidade por perfil e/ou unidade.
     * Regras implementadas:
     * - perfil == "ADMIN" -> somente processos com situação 'CRIADO'
     * - se codigoUnidade informado -> somente processos vinculados a essa unidade (via UNIDADE_PROCESSO)
     * <p>
     * Observação: para simplificar os testes iniciais, este método realiza a paginação em memória
     * após aplicar os filtros. Em conjuntos de dados grandes, isso deve ser otimizado com queries específicas.
     *
     * @param perfil        perfil do requisitante (obrigatório)
     * @param codigoUnidade filtro opcional por unidade
     * @param pageable      informações de paginação
     * @return página de ProcessoResumoDto
     */
    public Page<ProcessoResumoDto> listarProcessos(String perfil, Long codigoUnidade, Pageable pageable) {
        if (perfil == null || perfil.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'perfil' é obrigatório");
        }

        // Obter todos os processos (simplificação para testes)
        List<Processo> todosOsProcessos = processoRepo.findAll();

        // Aplicar filtro por perfil e unidade
        List<Processo> processosFiltrados = new ArrayList<>();
        for (Processo processo : todosOsProcessos) {
            if ("ADMIN".equalsIgnoreCase(perfil)) {
                if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
                    continue;
                }
            }
            if (codigoUnidade != null) {
                List<UnidadeProcesso> unidadesDoProcesso = unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo());
                boolean pertence = unidadesDoProcesso.stream().anyMatch(up -> Objects.equals(up.getCodigo(), codigoUnidade));
                if (!pertence) {
                    continue;
                }
            }
            processosFiltrados.add(processo);
        }

        // Mapear para DTOs
        List<ProcessoResumoDto> listaDeDtos = processosFiltrados.stream()
                .map(processo -> {
                    Long unidadeCodigo = null;
                    String unidadeNome = null;

                    // Tentar obter uma unidade vinculada (a primeira encontrada)
                    List<UnidadeProcesso> unidadesDoProcesso = unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo());
                    if (!unidadesDoProcesso.isEmpty()) {
                        UnidadeProcesso up = unidadesDoProcesso.getFirst();
                        unidadeCodigo = up.getCodigo();
                        unidadeNome = up.getNome();
                    }

                    return ProcessoResumoDto.builder()
                        .codigo(processo.getCodigo())
                        .descricao(processo.getDescricao())
                        .situacao(processo.getSituacao())
                        .tipo(processo.getTipo().name())
                        .dataLimite(processo.getDataLimite())
                        .dataCriacao(processo.getDataCriacao())
                        .unidadeCodigo(unidadeCodigo)
                        .unidadeNome(unidadeNome)
                        .build();
                })
                .collect(Collectors.toList());

        // Paginação em memória
        int total = listaDeDtos.size();
        int tamanhoPagina = pageable.getPageSize();
        int indiceInicial = (int) pageable.getOffset();
        int indiceFinal = Math.min(indiceInicial + tamanhoPagina, total);

        List<ProcessoResumoDto> conteudoDaPagina;
        if (indiceInicial >= total || indiceInicial < 0) {
            conteudoDaPagina = List.of();
        } else {
            conteudoDaPagina = listaDeDtos.subList(indiceInicial, indiceFinal);
        }

        return new PageImpl<>(conteudoDaPagina, pageable, total);
    }

    /**
     * Lista os alertas visíveis para o usuário/unidade.
     * Regras básicas:
     * - se usuarioTitulo informado -> alertas destinados ao usuário
     * - se codigoUnidade informado -> alertas destinados à unidade
     * - se ambos nulos -> retorna todos (para administração/testes)
     *
     * @param usuarioTitulo título do usuário autenticado (opcional)
     * @param codigoUnidade código da unidade autenticada (opcional)
     * @param pageable      informações de paginação
     * @return página de AlertaDto
     */
    public Page<AlertaDto> listarAlertas(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        List<Alerta> todosOsAlertas = alertaRepo.findAll();

        List<Alerta> alertasFiltrados = todosOsAlertas.stream()
                .filter(alerta -> {
                    if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
                        return alerta.getUsuarioDestino() != null && usuarioTitulo.equalsIgnoreCase(alerta.getUsuarioDestino().getTitulo());
                    }
                    if (codigoUnidade != null) {
                        return alerta.getUnidadeDestino() != null && Objects.equals(alerta.getUnidadeDestino().getCodigo(), codigoUnidade);
                    }
                    return true;
                })
                .toList();

        List<AlertaDto> listaDeDtos = alertasFiltrados.stream().map(alerta -> new AlertaDto(
            alerta.getCodigo(),
            alerta.getProcesso() != null ? alerta.getProcesso().getCodigo() : null,
            alerta.getDescricao(),
            alerta.getDataHora(),
            alerta.getUnidadeOrigem() != null ? alerta.getUnidadeOrigem().getCodigo() : null,
            alerta.getUnidadeDestino() != null ? alerta.getUnidadeDestino().getCodigo() : null,
            alerta.getUsuarioDestino() != null ? alerta.getUsuarioDestino().getTitulo() : null
        )).collect(Collectors.toList());

        int total = listaDeDtos.size();
        int tamanhoPagina = pageable.getPageSize();
        int indiceInicial = (int) pageable.getOffset();
        int indiceFinal = Math.min(indiceInicial + tamanhoPagina, total);

        List<AlertaDto> conteudoDaPagina;
        if (indiceInicial >= total || indiceInicial < 0) {
            conteudoDaPagina = List.of();
        } else {
            conteudoDaPagina = listaDeDtos.subList(indiceInicial, indiceFinal);
        }

        return new PageImpl<>(conteudoDaPagina, pageable, total);
    }
}
