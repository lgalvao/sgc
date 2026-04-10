package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.processo.model.AcaoProcesso;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcaoEmBlocoRequestTest {

    @Test
    void deveRetornarDisponibilizarMapaEmBlocoCommandParaAcaoDisponibilizar() {
        var dataLimite = LocalDate.of(2023, 1, 1);
        var request = new AcaoEmBlocoRequest(List.of(1L, 2L), AcaoProcesso.DISPONIBILIZAR, dataLimite);

        var command = request.paraCommand();

        assertThat(command).isInstanceOf(DisponibilizarMapaEmBlocoCommand.class);
        var disponibilizarCommand = (DisponibilizarMapaEmBlocoCommand) command;
        assertThat(disponibilizarCommand.unidadeCodigos()).containsExactly(1L, 2L);
        assertThat(disponibilizarCommand.dataLimite()).isEqualTo(dataLimite);
    }

    @Test
    void deveLancarErroValidacaoSeDataLimiteNaoInformadaAoDisponibilizar() {
        var request = new AcaoEmBlocoRequest(List.of(1L), AcaoProcesso.DISPONIBILIZAR, null);

        assertThatThrownBy(request::paraCommand)
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DATA_LIMITE_OBRIGATORIA);
    }

    @Test
    void deveRetornarProcessarAnaliseEmBlocoCommandParaOutrasAcoes() {
        var requestAceitar = new AcaoEmBlocoRequest(List.of(1L), AcaoProcesso.ACEITAR, null);
        var commandAceitar = requestAceitar.paraCommand();

        assertThat(commandAceitar).isInstanceOf(ProcessarAnaliseEmBlocoCommand.class);
        var processarCommand = (ProcessarAnaliseEmBlocoCommand) commandAceitar;
        assertThat(processarCommand.unidadeCodigos()).containsExactly(1L);
        assertThat(processarCommand.acao()).isEqualTo(AcaoProcesso.ACEITAR);

        var requestHomologar = new AcaoEmBlocoRequest(List.of(2L), AcaoProcesso.HOMOLOGAR, null);
        var commandHomologar = requestHomologar.paraCommand();

        assertThat(commandHomologar).isInstanceOf(ProcessarAnaliseEmBlocoCommand.class);
        var processarCommand2 = (ProcessarAnaliseEmBlocoCommand) commandHomologar;
        assertThat(processarCommand2.unidadeCodigos()).containsExactly(2L);
        assertThat(processarCommand2.acao()).isEqualTo(AcaoProcesso.HOMOLOGAR);
    }
}
