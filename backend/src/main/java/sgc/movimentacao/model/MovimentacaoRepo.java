
    package sgc.movimentacao.model;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface MovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    }
