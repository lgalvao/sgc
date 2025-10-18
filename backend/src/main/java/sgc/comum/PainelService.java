package sgc.comum;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
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

    public Page<ProcessoResumoDto> listarProcessos(String perfil, Long codigoUnidade, Pageable pageable) {
        if (perfil == null || perfil.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'perfil' é obrigatório");
        }

        List<Processo> processos;
        if ("ADMIN".equalsIgnoreCase(perfil)) {
            processos = processoRepo.findAll();
        } else {
            if (codigoUnidade == null) {
                return Page.empty(pageable);
            }
            List<Long> unidadeIds = getUnidadesSubordinadasIds(codigoUnidade);
            unidadeIds.add(codigoUnidade);

            List<Long> processoIds = unidadeProcessoRepo.findByUnidadeCodigoIn(unidadeIds)
                .stream()
                .map(UnidadeProcesso::getProcessoCodigo)
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
        UnidadeProcesso up = unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo()).stream().findFirst().orElse(null);
        return ProcessoResumoDto.builder()
            .codigo(processo.getCodigo())
            .descricao(processo.getDescricao())
            .situacao(processo.getSituacao())
            .tipo(processo.getTipo().name())
            .dataLimite(processo.getDataLimite())
            .dataCriacao(processo.getDataCriacao())
            .unidadeCodigo(up != null ? up.getUnidadeCodigo() : null)
            .unidadeNome(up != null ? up.getNome() : null)
            .build();
    }

    private AlertaDto mapToAlertaDto(Alerta alerta) {
        return new AlertaDto(
            alerta.getCodigo(),
            alerta.getProcesso() != null ? alerta.getProcesso().getCodigo() : null,
            alerta.getDescricao(),
            alerta.getDataHora(),
            alerta.getUnidadeOrigem() != null ? alerta.getUnidadeOrigem().getCodigo() : null,
            alerta.getUnidadeDestino() != null ? alerta.getUnidadeDestino().getCodigo() : null,
            alerta.getUsuarioDestino() != null ? String.valueOf(alerta.getUsuarioDestino().getTituloEleitoral()) : null
        );
    }
}
