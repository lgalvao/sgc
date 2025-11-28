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
        log.info(">>> NotificacaoModelosServiceMock ATIVADO - Modelos de e-mail serão mockados <<<");
    }

    @Override
    public String criarEmailDeProcessoIniciado(
            String nomeUnidade,
            String nomeProcesso,
            String tipoProcesso,
            LocalDateTime dataLimite) {
        log.debug("[MOCK] Criando e-mail de processo iniciado (mock): Unidade={}, Processo={}, Tipo={}, DataLimite={}",
                nomeUnidade, nomeProcesso, tipoProcesso, dataLimite);
        return "<html><body>Mock Email de Processo Iniciado</body></html>";
    }

    @Override
    public void criarEmailCadastroDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            int quantidadeAtividades) {
        log.debug("[MOCK] Criando e-mail de cadastro disponibilizado (mock): Unidade={}, Processo={}, Atividades={}",
                nomeUnidade, nomeProcesso, quantidadeAtividades);
    }

    @Override
    public void criarEmailCadastroDevolvido(
            String nomeUnidade,
            String nomeProcesso,
            String motivo,
            String observacoes) {
        log.debug("[MOCK] Criando e-mail de cadastro devolvido (mock): Unidade={}, Processo={}, Motivo={}, Observacoes={}",
                nomeUnidade, nomeProcesso, motivo, observacoes);
    }

    @Override
    public void criarEmailMapaDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            LocalDateTime dataLimiteValidacao) {
        log.debug("[MOCK] Criando e-mail de mapa disponibilizado (mock): Unidade={}, Processo={}, DataLimiteValidacao={}",
                nomeUnidade, nomeProcesso, dataLimiteValidacao);
    }

    @Override
    public void criarEmailMapaValidado(String nomeUnidade, String nomeProcesso) {
        log.debug("[MOCK] Criando e-mail de mapa validado (mock): Unidade={}, Processo={}",
                nomeUnidade, nomeProcesso);
    }

    @Override
    public void criarEmailProcessoFinalizado(String nomeProcesso, LocalDateTime dataFinalizacao, int quantidadeMapas) {
        log.debug("[MOCK] Criando e-mail de processo finalizado (mock): Processo={}, DataFinalizacao={}, QuantidadeMapas={}",
                nomeProcesso, dataFinalizacao, quantidadeMapas);
    }

    @Override
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        log.debug("[MOCK] Criando e-mail de processo finalizado por unidade (mock): Unidade={}, Processo={}",
                siglaUnidade, nomeProcesso);
        return "<html><body>Mock Email de Processo Finalizado Por Unidade</body></html>";
    }

    @Override
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade,
            String nomeProcesso,
            List<String> siglasUnidadesSubordinadas) {
        log.debug("[MOCK] Criando e-mail de processo finalizado unidades subordinadas (mock): Unidade={}, Processo={}, Subordinadas={}",
                siglaUnidade, nomeProcesso, siglasUnidadesSubordinadas);
        return "<html><body>Mock Email de Processo Finalizado Unidades Subordinadas</body></html>";
    }
}
