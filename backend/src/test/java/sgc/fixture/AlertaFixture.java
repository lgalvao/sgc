package sgc.fixture;

import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;

public class AlertaFixture {

    public static Alerta alertaPadrao(Processo processo) {
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setProcesso(processo);
        alerta.setDescricao("Alerta de Teste");
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(UnidadeFixture.unidadePadrao());
        alerta.setUnidadeDestino(UnidadeFixture.unidadePadrao());
        return alerta;
    }

    public static Alerta alertaParaUsuario(Processo processo, Usuario usuario) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUnidadeDestino(usuario.getUnidadeLotacao());
        alerta.setDescricao("Alerta para Usuario " + usuario.getNome());
        return alerta;
    }

    public static Alerta alertaParaUnidade(Processo processo, Unidade unidade) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUnidadeDestino(unidade);
        alerta.setDescricao("Alerta para Unidade " + unidade.getNome());
        return alerta;
    }
}
