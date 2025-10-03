package sgc.mapper;

import sgc.dto.SubprocessoDTO;
import sgc.dto.SubprocessoDetailDTO;
import sgc.dto.MovimentacaoDTO;
import sgc.dto.AtividadeDTO;
import sgc.dto.ConhecimentoDTO;
import sgc.model.Mapa;
import sgc.model.Processo;
import sgc.model.Subprocesso;
import sgc.model.Unidade;
import sgc.model.Movimentacao;
import sgc.model.Atividade;
import sgc.model.Conhecimento;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper entre Subprocesso e SubprocessoDTO / SubprocessoDetailDTO.
 */
public class SubprocessoMapper {
    public static SubprocessoDTO toDTO(Subprocesso s) {
        if (s == null) return null;
        Long processoCodigo = s.getProcesso() != null ? s.getProcesso().getCodigo() : null;
        Long unidadeCodigo = s.getUnidade() != null ? s.getUnidade().getCodigo() : null;
        Long mapaCodigo = s.getMapa() != null ? s.getMapa().getCodigo() : null;
        return new SubprocessoDTO(
                s.getCodigo(),
                processoCodigo,
                unidadeCodigo,
                mapaCodigo,
                s.getDataLimiteEtapa1(),
                s.getDataFimEtapa1(),
                s.getDataLimiteEtapa2(),
                s.getDataFimEtapa2(),
                s.getSituacaoId()
        );
    }

    public static Subprocesso toEntity(SubprocessoDTO dto) {
        if (dto == null) return null;
        Subprocesso s = new Subprocesso();
        s.setCodigo(dto.getCodigo());

        if (dto.getProcessoCodigo() != null) {
            Processo p = new Processo();
            p.setCodigo(dto.getProcessoCodigo());
            s.setProcesso(p);
        } else {
            s.setProcesso(null);
        }

        if (dto.getUnidadeCodigo() != null) {
            Unidade u = new Unidade();
            u.setCodigo(dto.getUnidadeCodigo());
            s.setUnidade(u);
        } else {
            s.setUnidade(null);
        }

        if (dto.getMapaCodigo() != null) {
            Mapa m = new Mapa();
            m.setCodigo(dto.getMapaCodigo());
            s.setMapa(m);
        } else {
            s.setMapa(null);
        }

        s.setDataLimiteEtapa1(dto.getDataLimiteEtapa1());
        s.setDataFimEtapa1(dto.getDataFimEtapa1());
        s.setDataLimiteEtapa2(dto.getDataLimiteEtapa2());
        s.setDataFimEtapa2(dto.getDataFimEtapa2());
        s.setSituacaoId(dto.getSituacaoId());

        return s;
    }

    /**
     * Constrói SubprocessoDetailDTO a partir do Subprocesso, lista de movimentações (ordenadas desc),
     * atividades do mapa e conhecimentos associados.
     */
    public static SubprocessoDetailDTO toDetailDTO(Subprocesso sp,
                                                   List<Movimentacao> movimentacoes,
                                                   List<Atividade> atividades,
                                                   List<Conhecimento> conhecimentos) {
        if (sp == null) return null;

        SubprocessoDetailDTO dto = new SubprocessoDetailDTO();

        // Unidade
        SubprocessoDetailDTO.UnidadeDTO unidadeDTO = null;
        try {
            if (sp.getUnidade() != null) {
                unidadeDTO = new SubprocessoDetailDTO.UnidadeDTO(
                        sp.getUnidade().getCodigo(),
                        sp.getUnidade().getSigla(),
                        sp.getUnidade().getNome()
                );
            }
        } catch (Exception ignored) {
        }
        dto.setUnidade(unidadeDTO);

        // Responsável (titular da unidade)
        SubprocessoDetailDTO.ResponsavelDTO responsavelDTO = getResponsavelDTO(sp);
        dto.setResponsavel(responsavelDTO);

        // Situação
        dto.setSituacao(sp.getSituacaoId());

        // Localização atual: destino da última movimentação (mais recente primeiro)
        String localizacao = null;
        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            try {
                Movimentacao m = movimentacoes.getFirst();
                if (m.getUnidadeDestino() != null) {
                    localizacao = m.getUnidadeDestino().getSigla();
                }
            } catch (Exception ignored) {
            }
        }
        dto.setLocalizacaoAtual(localizacao);

        // Prazo da etapa atual: usa dataLimiteEtapa1 se presente, senão dataLimiteEtapa2
        if (sp.getDataLimiteEtapa1() != null) {
            dto.setPrazoEtapaAtual(sp.getDataLimiteEtapa1());
        } else {
            dto.setPrazoEtapaAtual(sp.getDataLimiteEtapa2());
        }

        // Movimentações -> MovimentacaoDTO
        List<MovimentacaoDTO> movDtos = new ArrayList<>();
        if (movimentacoes != null) {
            for (Movimentacao m : movimentacoes) {
                MovimentacaoDTO md = new MovimentacaoDTO();
                md.setCodigo(m.getCodigo());
                md.setDataHora(m.getDataHora());
                if (m.getUnidadeOrigem() != null) {
                    md.setUnidadeOrigemCodigo(m.getUnidadeOrigem().getCodigo());
                    md.setUnidadeOrigemSigla(m.getUnidadeOrigem().getSigla());
                    md.setUnidadeOrigemNome(m.getUnidadeOrigem().getNome());
                }
                if (m.getUnidadeDestino() != null) {
                    md.setUnidadeDestinoCodigo(m.getUnidadeDestino().getCodigo());
                    md.setUnidadeDestinoSigla(m.getUnidadeDestino().getSigla());
                    md.setUnidadeDestinoNome(m.getUnidadeDestino().getNome());
                }
                md.setDescricao(m.getDescricao());
                movDtos.add(md);
            }
        }
        dto.setMovimentacoes(movDtos);

        // Elementos do processo: montar lista com Atividades e Conhecimentos (payloads como DTOs)
        List<SubprocessoDetailDTO.ElementoProcessoDTO> elementos = new ArrayList<>();

        if (atividades != null) {
            for (Atividade a : atividades) {
                AtividadeDTO ad = AtividadeMapper.toDTO(a);
                SubprocessoDetailDTO.ElementoProcessoDTO ep = new SubprocessoDetailDTO.ElementoProcessoDTO();
                ep.setTipo("ATIVIDADE");
                ep.setPayload(ad);
                elementos.add(ep);
            }
        }

        if (conhecimentos != null) {
            for (Conhecimento c : conhecimentos) {
                ConhecimentoDTO cd = ConhecimentoMapper.toDTO(c);
                SubprocessoDetailDTO.ElementoProcessoDTO ep = new SubprocessoDetailDTO.ElementoProcessoDTO();
                ep.setTipo("CONHECIMENTO");
                ep.setPayload(cd);
                elementos.add(ep);
            }
        }

        dto.setElementosDoProcesso(elementos);

        return dto;
    }

    private static SubprocessoDetailDTO.ResponsavelDTO getResponsavelDTO(Subprocesso sp) {
        SubprocessoDetailDTO.ResponsavelDTO responsavelDTO = null;
        try {
            if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
                var titular = sp.getUnidade().getTitular();
                responsavelDTO = new SubprocessoDetailDTO.ResponsavelDTO(
                        null, // título não é id numérico; mantemos null para id
                        titular.getNome(),
                        null,
                        titular.getRamal(),
                        titular.getEmail()
                );
            }
        } catch (Exception ignored) {
        }
        return responsavelDTO;
    }
}