package sgc.mapper;

import sgc.dto.ConhecimentoDTO;
import sgc.model.Conhecimento;
import sgc.model.Atividade;

/**
 * Mapper entre Conhecimento e ConhecimentoDTO.
 */
public class ConhecimentoMapper {

    public static ConhecimentoDTO toDTO(Conhecimento c) {
        if (c == null) return null;
        Long atividadeCodigo = c.getAtividade() != null ? c.getAtividade().getCodigo() : null;
        return new ConhecimentoDTO(c.getCodigo(), atividadeCodigo, c.getDescricao());
    }

    public static Conhecimento toEntity(ConhecimentoDTO dto) {
        if (dto == null) return null;
        Conhecimento c = new Conhecimento();
        c.setCodigo(dto.getCodigo());
        if (dto.getAtividadeCodigo() != null) {
            Atividade a = new Atividade();
            a.setCodigo(dto.getAtividadeCodigo());
            c.setAtividade(a);
        } else {
            c.setAtividade(null);
        }
        c.setDescricao(dto.getDescricao());
        return c;
    }
}