package sgc.processo.dto;

import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper manual para construir um ProcessoDetalheDto a partir da entidade Processo e listas auxiliares.
 */
@Component
public class ProcessoDetalheMapper {

    public ProcessoDetalheDto toDetailDTO(Processo p,
                                          List<UnidadeProcesso> unidadesProcesso,
                                          List<Subprocesso> subprocessos) {
        if (p == null) return null;
        ProcessoDetalheDto dto = new ProcessoDetalheDto();
        dto.setCodigo(p.getCodigo());
        dto.setDescricao(p.getDescricao());
        dto.setTipo(p.getTipo());
        dto.setSituacao(p.getSituacao());
        dto.setDataLimite(p.getDataLimite());
        dto.setDataCriacao(p.getDataCriacao());
        dto.setDataFinalizacao(p.getDataFinalizacao());

        Map<String, ProcessoDetalheDto.UnidadeParticipanteDTO> bySigla = new HashMap<>();
        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = new ArrayList<>();

        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessoDetalheDto.UnidadeParticipanteDTO unit = getUnitParticipantDTO(up);
                unidades.add(unit);
                if (unit.getSigla() != null) bySigla.put(unit.getSigla(), unit);
            }
        }

        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                String sigla;
                try {
                    sigla = sp.getUnidade() != null ? sp.getUnidade().getSigla() : null;
                } catch (Exception ignored) { sigla = null; }

                ProcessoDetalheDto.UnidadeParticipanteDTO unit = sigla != null ? bySigla.get(sigla) : null;
                if (unit != null) {
                    try { unit.setSituacaoSubprocesso(sp.getSituacaoId()); } catch (Exception ignored) {}
                    try { unit.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
                } else {
                    // Se não encontrou por sigla, cria um registro mínimo baseado no subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDTO novo = getUnitParticipantDTO(sp);
                    unidades.add(novo);
                    if (novo.getSigla() != null) bySigla.put(novo.getSigla(), novo);
                }
            }
        }

        dto.setUnidades(unidades);

        // resumo de subprocessos: reutilizamos info mínima para cada subprocesso
        List<ProcessoResumoDto> resumo = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto s = getProcessoResumoDTO(sp);
                resumo.add(s);
            }
        }
        dto.setResumoSubprocessos(resumo);

        return dto;
    }

    private ProcessoDetalheDto.UnidadeParticipanteDTO getUnitParticipantDTO(Subprocesso sp) {
        ProcessoDetalheDto.UnidadeParticipanteDTO novo = new ProcessoDetalheDto.UnidadeParticipanteDTO();
        try { novo.setNome(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
        try { novo.setSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null); } catch (Exception ignored) {}
        try { novo.setSituacaoSubprocesso(sp.getSituacaoId()); } catch (Exception ignored) {}
        try { novo.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
        return novo;
    }

    private ProcessoResumoDto getProcessoResumoDTO(Subprocesso sp) {
        ProcessoResumoDto s = new ProcessoResumoDto();
        try { s.setCodigo(sp.getCodigo()); } catch (Exception ignored) {}
        try { s.setDescricao(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
        try { s.setSituacao(sp.getSituacaoId()); } catch (Exception ignored) {}
        try { s.setTipo(null); } catch (Exception ignored) {}
        try { s.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
        try { s.setDataCriacao(null); } catch (Exception ignored) {}
        try { s.setUnidadeCodigo(sp.getUnidade() != null ? sp.getUnidade().getCodigo() : null); } catch (Exception ignored) {}
        try { s.setUnidadeNome(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
        return s;
    }

    private ProcessoDetalheDto.UnidadeParticipanteDTO getUnitParticipantDTO(UnidadeProcesso up) {
        ProcessoDetalheDto.UnidadeParticipanteDTO unit = new ProcessoDetalheDto.UnidadeParticipanteDTO();
        // tenta popular campos, se getters existirem; caso contrário ficam null
        try { unit.setUnidadeCodigo(up.getCodigo()); } catch (Exception ignored) { unit.setUnidadeCodigo(null); }
        try { unit.setNome(up.getNome()); } catch (Exception ignored) { unit.setNome(null); }
        try { unit.setSigla(up.getSigla()); } catch (Exception ignored) { unit.setSigla(null); }
        try { unit.setUnidadeSuperiorCodigo(up.getUnidadeSuperiorCodigo()); } catch (Exception ignored) { unit.setUnidadeSuperiorCodigo(null); }
        try { unit.setSituacaoSubprocesso(up.getSituacao()); } catch (Exception ignored) { /* será sobrescrito se houver subprocesso */ }
        return unit;
    }
}
