package sgc.mapper;

import sgc.dto.ProcessoDTO;
import sgc.dto.ProcessDetailDTO;
import sgc.model.Processo;
import sgc.model.UnidadeProcesso;
import sgc.model.Subprocesso;

import java.util.*;

/**
 * Mapper entre Processo e DTOs relacionados.
 */
public class ProcessoMapper {
    public static ProcessoDTO toDTO(Processo p) {
        if (p == null) return null;
        return new ProcessoDTO(
                p.getCodigo(),
                p.getDataCriacao(),
                p.getDataFinalizacao(),
                p.getDataLimite(),
                p.getDescricao(),
                p.getSituacao(),
                p.getTipo()
        );
    }

    public static Processo toEntity(ProcessoDTO dto) {
        if (dto == null) return null;
        Processo p = new Processo();
        p.setCodigo(dto.getCodigo());
        p.setDataCriacao(dto.getDataCriacao());
        p.setDataFinalizacao(dto.getDataFinalizacao());
        p.setDataLimite(dto.getDataLimite());
        p.setDescricao(dto.getDescricao());
        p.setSituacao(dto.getSituacao());
        p.setTipo(dto.getTipo());
        return p;
    }

    /**
     * Constrói um ProcessDetailDTO a partir da entidade Processo e listas auxiliares
     * de UnidadeProcesso e Subprocesso. A função tenta combinar UnidadeProcesso com
     * Subprocesso pela sigla da unidade (criada como snapshot no startMapping/startRevision).
     * <p>
     * Observação: esta implementação constrói uma lista plana de unidades (filhos vazios).
     * Se a árvore hierárquica completa for necessária, ajustar usando campo unidadeSuperiorCodigo
     * presente em UnidadeProcesso para montar a hierarquia.
     */
    public static ProcessDetailDTO toDetailDTO(Processo p,
                                              List<UnidadeProcesso> unidadesProcesso,
                                              List<Subprocesso> subprocessos) {
        if (p == null) return null;
        ProcessDetailDTO dto = new ProcessDetailDTO();
        dto.setCodigo(p.getCodigo());
        dto.setDescricao(p.getDescricao());
        dto.setTipo(p.getTipo());
        dto.setSituacao(p.getSituacao());
        dto.setDataLimite(p.getDataLimite());
        dto.setDataCriacao(p.getDataCriacao());
        dto.setDataFinalizacao(p.getDataFinalizacao());

        Map<String, ProcessDetailDTO.UnitParticipantDTO> bySigla = new HashMap<>();
        List<ProcessDetailDTO.UnitParticipantDTO> unidades = new ArrayList<>();

        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessDetailDTO.UnitParticipantDTO unit = new ProcessDetailDTO.UnitParticipantDTO();
                // tenta popular campos, se getters existirem; caso contrário ficam null
                try { unit.setUnidadeCodigo(up.getCodigo()); } catch (Exception ignored) { unit.setUnidadeCodigo(null); }
                try { unit.setNome(up.getNome()); } catch (Exception ignored) { unit.setNome(null); }
                try { unit.setSigla(up.getSigla()); } catch (Exception ignored) { unit.setSigla(null); }
                try { unit.setUnidadeSuperiorCodigo(up.getUnidadeSuperiorCodigo()); } catch (Exception ignored) { unit.setUnidadeSuperiorCodigo(null); }
                try { unit.setSituacaoSubprocesso(up.getSituacao()); } catch (Exception ignored) { /* será sobrescrito se houver subprocesso */ }
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

                ProcessDetailDTO.UnitParticipantDTO unit = sigla != null ? bySigla.get(sigla) : null;
                if (unit != null) {
                    try { unit.setSituacaoSubprocesso(sp.getSituacaoId()); } catch (Exception ignored) {}
                    try { unit.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
                } else {
                    // Se não encontrou por sigla, cria um registro mínimo baseado no subprocesso
                    ProcessDetailDTO.UnitParticipantDTO novo = new ProcessDetailDTO.UnitParticipantDTO();
                    try { novo.setNome(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
                    try { novo.setSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null); } catch (Exception ignored) {}
                    try { novo.setSituacaoSubprocesso(sp.getSituacaoId()); } catch (Exception ignored) {}
                    try { novo.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
                    unidades.add(novo);
                    if (novo.getSigla() != null) bySigla.put(novo.getSigla(), novo);
                }
            }
        }

        dto.setUnidades(unidades);

        // resumo de subprocessos: reutilizamos info mínima para cada subprocesso
        List<sgc.dto.ProcessoSummaryDTO> resumo = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                sgc.dto.ProcessoSummaryDTO s = new sgc.dto.ProcessoSummaryDTO();
                try { s.setCodigo(sp.getCodigo()); } catch (Exception ignored) {}
                try { s.setDescricao(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
                try { s.setSituacao(sp.getSituacaoId()); } catch (Exception ignored) {}
                try { s.setTipo(null); } catch (Exception ignored) {}
                try { s.setDataLimite(sp.getDataLimiteEtapa1()); } catch (Exception ignored) {}
                try { s.setDataCriacao(null); } catch (Exception ignored) {}
                try { s.setUnidadeCodigo(sp.getUnidade() != null ? sp.getUnidade().getCodigo() : null); } catch (Exception ignored) {}
                try { s.setUnidadeNome(sp.getUnidade() != null ? sp.getUnidade().getNome() : null); } catch (Exception ignored) {}
                resumo.add(s);
            }
        }
        dto.setResumoSubprocessos(resumo);

        return dto;
    }
}