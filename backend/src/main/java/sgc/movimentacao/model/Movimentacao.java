
    package sgc.movimentacao.model;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import sgc.sgrh.model.Usuario;
    import sgc.subprocesso.model.Subprocesso;
    import sgc.unidade.model.Unidade;

    import java.time.LocalDateTime;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    public class Movimentacao {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "subprocesso_id", nullable = false)
        private Subprocesso subprocesso;

        @Column(nullable = false)
        private String descricao;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "usuario_id", nullable = false)
        private Usuario usuario;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "unidade_origem_id", nullable = false)
        private Unidade unidadeOrigem;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "unidade_destino_id", nullable = false)
        private Unidade unidadeDestino;

        @Column(nullable = false)
        private LocalDateTime dataHora;

        public Movimentacao(Subprocesso subprocesso, String descricao, Usuario usuario, Unidade unidadeOrigem, Unidade unidadeDestino) {
            this.subprocesso = subprocesso;
            this.descricao = descricao;
            this.usuario = usuario;
            this.unidadeOrigem = unidadeOrigem;
            this.unidadeDestino = unidadeDestino;
            this.dataHora = LocalDateTime.now();
        }
    }
