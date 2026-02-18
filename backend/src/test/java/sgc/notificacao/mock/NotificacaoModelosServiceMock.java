package sgc.notificacao.mock;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoModelosService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
@Profile("test&!email-test")
@Slf4j
public class NotificacaoModelosServiceMock extends NotificacaoModelosService {
    public NotificacaoModelosServiceMock() {
        super(null);
        log.info("NotificacaoModelosServiceMock ATIVADO - Modelos de e-mail serão mockados");
    }

    @Override
    public @NonNull String criarEmailProcessoIniciado(
            @NonNull String nomeUnidade,
            @NonNull String nomeProcesso,
            @NonNull String tipoProcesso,
            @NonNull LocalDateTime dataLimite) {
        return "<html><body>Mock Email de Processo Iniciado</body></html>";
    }

    @Override
    public String criarEmailCadastroDisponibilizado(
            @NonNull String nomeUnidade, @NonNull String nomeProcesso, int quantidadeAtividades) {
        return "<html><body>Mock Email de Cadastro Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailCadastroDevolvido(
            @NonNull String nomeUnidade, @NonNull String nomeProcesso, @NonNull String motivo, @NonNull String observacoes) {
        return "<html><body>Mock Email de Cadastro Devolvido</body></html>";
    }

    @Override
    public String criarEmailMapaDisponibilizado(
            @NonNull String nomeUnidade, @NonNull String nomeProcesso, @NonNull LocalDateTime dataLimiteValidacao) {
        return "<html><body>Mock Email de Mapa Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailMapaValidado(@NonNull String nomeUnidade, @NonNull String nomeProcesso) {
        return "<html><body>Mock Email de Mapa Validado</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizado(
            @NonNull String nomeProcesso, @NonNull LocalDateTime dataFinalizacao, int quantidadeMapas) {
        return "<html><body>Mock Email de Processo Finalizado</body></html>";
    }

    @Override
    public @NonNull String criarEmailProcessoFinalizadoPorUnidade(@NonNull String siglaUnidade, @NonNull String nomeProcesso) {
        return "<html><body>Mock Email de Processo Finalizado Por Unidade</body></html>";
    }

    @Override
    public @NonNull String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            @NonNull String siglaUnidade, @NonNull String nomeProcesso, @NonNull List<String> siglasUnidadesSubordinadas) {
        return "<html><body>Mock Email de Processo Finalizado Unidades Subordinadas</body></html>";
    }
    @Override
    public String criarEmailLembretePrazo(String siglaUnidade, String nomeProcesso, LocalDateTime dataLimite) {
        return "<html><body>Mock Email de Lembrete de Prazo</body></html>";
    }
}
