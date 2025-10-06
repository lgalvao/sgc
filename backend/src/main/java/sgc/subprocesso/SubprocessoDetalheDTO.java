package sgc.subprocesso;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubprocessoDetalheDTO {
    private UnidadeDTO unidade;
    private ResponsavelDTO responsavel;
    private String situacao;
    private String localizacaoAtual;
    private LocalDate prazoEtapaAtual;
    private List<MovimentacaoDTO> movimentacoes;
    private List<ElementoProcessoDTO> elementosDoProcesso;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnidadeDTO {
        private Long codigo;
        private String sigla;
        private String nome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsavelDTO {
        private Long id;
        private String nome;
        private String tipoResponsabilidade;
        private String ramal;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElementoProcessoDTO {
        /**
         * Ex.: "ATIVIDADE", "CONHECIMENTO", "MAPA", "DIAGNOSTICO", etc.
         */
        private String tipo;

        /**
         * Payload variável: pode ser AtividadeDTO, ConhecimentoDTO, MapaResumoDTO ou outro objeto.
         * Usamos Object para permitir flexibilidade no mapeamento do backend.
         */
        private Object payload;
    }
}