
    package sgc.movimentacao.service;

    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import sgc.movimentacao.model.Movimentacao;
    import sgc.movimentacao.model.MovimentacaoRepo;
    import sgc.sgrh.model.Usuario;
    import sgc.subprocesso.model.Subprocesso;

    @Service
    @RequiredArgsConstructor
    public class MovimentacaoService {
        private final MovimentacaoRepo movimentacaoRepo;

        public void registrarMovimentacao(Subprocesso subprocesso, String descricao, Usuario usuario) {
            Movimentacao movimentacao = new Movimentacao(
                subprocesso,
                descricao,
                usuario,
                subprocesso.getUnidade(),
                subprocesso.getUnidade()
            );
            movimentacaoRepo.save(movimentacao);
        }
    }
