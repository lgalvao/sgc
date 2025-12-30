package sgc.notificacao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
@Profile("test")
@Slf4j
public class NotificacaoModelosServiceMock extends NotificacaoModelosService {
    public NotificacaoModelosServiceMock() {
        super(null); // SpringTemplateEngine will be null, but methods are overridden
        log.info("NotificacaoModelosServiceMock ATIVADO - Modelos de e-mail serão mockados");
    }

    @Override
    public String criarEmailDeProcessoIniciado(
            String nomeUnidade,
            String nomeProcesso,
            String tipoProcesso,
            LocalDateTime dataLimite) {
        log.debug(
                "[MOCK] Criando e-mail de processo iniciado (mock): Unidade={}, Processo={},"
                        + " Tipo={}, DataLimite={}",
                nomeUnidade,
                nomeProcesso,
                tipoProcesso,
                dataLimite);
        return "<html><body>Mock Email de Processo Iniciado</body></html>";
    }

    @Override
    public String criarEmailCadastroDisponibilizado(
            String nomeUnidade, String nomeProcesso, int quantidadeAtividades) {
        log.debug(
                "[MOCK] Criando e-mail de cadastro disponibilizado (mock): Unidade={}, Processo={},"
                        + " Atividades={}",
                nomeUnidade,
                nomeProcesso,
                quantidadeAtividades);
        return "<html><body>Mock Email de Cadastro Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailCadastroDevolvido(
            String nomeUnidade, String nomeProcesso, String motivo, String observacoes) {
        log.debug(
                "[MOCK] Criando e-mail de cadastro devolvido (mock): Unidade={}, Processo={},"
                        + " Motivo={}, Observacoes={}",
                nomeUnidade,
                nomeProcesso,
                motivo,
                observacoes);
        return "<html><body>Mock Email de Cadastro Devolvido</body></html>";
    }

    @Override
    public String criarEmailMapaDisponibilizado(
            String nomeUnidade, String nomeProcesso, LocalDateTime dataLimiteValidacao) {
        log.debug(
                "[MOCK] Criando e-mail de mapa disponibilizado (mock): Unidade={}, Processo={},"
                        + " DataLimiteValidacao={}",
                nomeUnidade,
                nomeProcesso,
                dataLimiteValidacao);
        return "<html><body>Mock Email de Mapa Disponibilizado</body></html>";
    }

    @Override
    public String criarEmailMapaValidado(String nomeUnidade, String nomeProcesso) {
        log.debug(
                "[MOCK] Criando e-mail de mapa validado (mock): Unidade={}, Processo={}",
                nomeUnidade,
                nomeProcesso);
        return "<html><body>Mock Email de Mapa Validado</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizado(
            String nomeProcesso, LocalDateTime dataFinalizacao, int quantidadeMapas) {
        log.debug(
                "[MOCK] Criando e-mail de processo finalizado (mock): Processo={},"
                        + " DataFinalizacao={}, QuantidadeMapas={}",
                nomeProcesso,
                dataFinalizacao,
                quantidadeMapas);
        return "<html><body>Mock Email de Processo Finalizado</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        log.debug(
                "[MOCK] Criando e-mail de processo finalizado por unidade (mock): Unidade={},"
                        + " Processo={}",
                siglaUnidade,
                nomeProcesso);
        return "<html><body>Mock Email de Processo Finalizado Por Unidade</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade, String nomeProcesso, List<String> siglasUnidadesSubordinadas) {
        log.debug(
                "[MOCK] Criando e-mail de processo finalizado unidades subordinadas (mock):"
                        + " Unidade={}, Processo={}, Subordinadas={}",
                siglaUnidade,
                nomeProcesso,
                siglasUnidadesSubordinadas);
        return "<html><body>Mock Email de Processo Finalizado Unidades Subordinadas</body></html>";
    }
}
