package sgc.painel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.*;
import sgc.sgrh.modelo.Perfil;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PainelService {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final UnidadeRepo unidadeRepo;

    /**
     * Lista processos com base no perfil e na unidade do usuário.
     * <p>
     * - Se o perfil for 'ADMIN', todos os processos são retornados.
     * - Para outros perfis, os processos são filtrados pela unidade do usuário e
     * suas subordinadas. Processos no estado 'CRIADO' são omitidos.
     *
     * @param perfil        O perfil do usuário (obrigatório).
     * @param codigoUnidade O código da unidade do usuário (necessário para perfis não-ADMIN).
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link ProcessoResumoDto}.
     * @throws IllegalArgumentException se o perfil for nulo or em branco.
     */
    public Page<ProcessoResumoDto> listarProcessos(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        if (perfil == null) {
            throw new IllegalArgumentException("O parâmetro 'perfil' é obrigatório");
        }

        List<Processo> processos;
        if (perfil == Perfil.ADMIN) {
            processos = processoRepo.findAll();
        } else {
            if (codigoUnidade == null) {
                return Page.empty(pageable);
            }
            List<Long> unidadeIds = getUnidadesSubordinadasIds(codigoUnidade);
            unidadeIds.add(codigoUnidade);

            List<Long> processoIds = unidadeProcessoRepo.findByCodUnidadeIn(unidadeIds)
                    .stream()
                    .map(UnidadeProcesso::getCodProcesso)
                    .distinct()
                    .collect(Collectors.toList());

            processos = processoRepo.findAllById(processoIds).stream()
                    .filter(p -> p.getSituacao() != SituacaoProcesso.CRIADO)
                    .collect(Collectors.toList());
        }

        List<ProcessoResumoDto> dtos = processos.stream()
                .map(this::mapToProcessoResumoDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    /**
     * Lista alertas com base no usuário ou na unidade.
     * <p>
     * A busca prioriza o título do usuário. Se não for fornecido, busca pela
     * unidade e suas subordinadas. Se nenhum dos dois for fornecido, retorna
     * todos os alertas.
     *
     * @param usuarioTitulo Título de eleitor do usuário.
     * @param codigoUnidade Código da unidade.
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link AlertaDto}.
     */
    public Page<AlertaDto> listarAlertas(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
            return alertaRepo.findByUsuarioDestino_TituloEleitoral(Long.parseLong(usuarioTitulo), pageable)
                    .map(this::mapToAlertaDto);
        }
        if (codigoUnidade != null) {
            List<Long> unidadeIds = getUnidadesSubordinadasIds(codigoUnidade);
            unidadeIds.add(codigoUnidade);
            return alertaRepo.findByUnidadeDestino_CodigoIn(unidadeIds, pageable)
                    .map(this::mapToAlertaDto);
        }
        return alertaRepo.findAll(pageable).map(this::mapToAlertaDto);
    }

    private List<Long> getUnidadesSubordinadasIds(Long unidadeId) {
        List<Unidade> subordinadas = unidadeRepo.findByUnidadeSuperiorCodigo(unidadeId);
        List<Long> ids = new ArrayList<>();
        for (Unidade u : subordinadas) {
            ids.add(u.getCodigo());
            ids.addAll(getUnidadesSubordinadasIds(u.getCodigo()));
        }
        return ids;
    }

    private ProcessoResumoDto mapToProcessoResumoDto(Processo processo) {
        UnidadeProcesso up = unidadeProcessoRepo.findByCodProcesso((processo.getCodigo())).stream().findFirst().orElse(null);
        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadeCodigo(up != null ? up.getCodUnidade() : null)
                .unidadeNome(up != null ? up.getNome() : null)
                .build();
    }

    private AlertaDto mapToAlertaDto(Alerta alerta) {
        return AlertaDto.builder()
            .codigo(alerta.getCodigo())
            .codProcesso(alerta.getProcesso() != null ? alerta.getProcesso().getCodigo() : null)
            .descricao(alerta.getDescricao())
            .dataHora(alerta.getDataHora())
            .codUnidadeOrigem(alerta.getUnidadeOrigem() != null ? alerta.getUnidadeOrigem().getCodigo() : null)
            .codUunidadeDestino(alerta.getUnidadeDestino() != null ? alerta.getUnidadeDestino().getCodigo() : null)
            .tituloUsuarioDestino(alerta.getUsuarioDestino() != null ? String.valueOf(alerta.getUsuarioDestino().getTituloEleitoral()) : null)
            .build();
    }
}
