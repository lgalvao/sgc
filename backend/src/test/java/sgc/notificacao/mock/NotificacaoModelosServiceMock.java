package sgc.notificacao.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoModelosService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
@Profile("test")
@Slf4j
public class NotificacaoModelosServiceMock extends NotificacaoModelosService {
    public NotificacaoModelosServiceMock() {
        super(null);
        log.info("NotificacaoModelosServiceMock ATIVADO - Modelos de e-mail serão mockados");
    }

    @Override
    public String criarEmailProcessoIniciado(
            String nomeUnidade,
            String nomeProcesso,
            String tipoProcesso,
            LocalDateTime dataLimite) {
        return "<html><body>Mock Email de Processo Iniciado</body></html>";
    }

    @Override
    public String criarEmailCadastroDisponibilizado(
            String nomeUnidade, String nomeProcesso, int quantidadeAtividades) {
        return "<html><body>Mock Email de Cadastro Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailCadastroDevolvido(
            String nomeUnidade, String nomeProcesso, String motivo, String observacoes) {
        return "<html><body>Mock Email de Cadastro Devolvido</body></html>";
    }

    @Override
    public String criarEmailMapaDisponibilizado(
            String nomeUnidade, String nomeProcesso, LocalDateTime dataLimiteValidacao) {
        return "<html><body>Mock Email de Mapa Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailMapaValidado(String nomeUnidade, String nomeProcesso) {
        return "<html><body>Mock Email de Mapa Validado</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizado(
            String nomeProcesso, LocalDateTime dataFinalizacao, int quantidadeMapas) {
        return "<html><body>Mock Email de Processo Finalizado</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        return "<html><body>Mock Email de Processo Finalizado Por Unidade</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade, String nomeProcesso, List<String> siglasUnidadesSubordinadas) {
        return "<html><body>Mock Email de Processo Finalizado Unidades Subordinadas</body></html>";
    }
}
