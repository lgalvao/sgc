package sgc.fixture;

import sgc.alerta.internal.model.Alerta;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

public class AlertaFixture {

    public static Alerta alertaPadrao(Processo processo) {
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setProcesso(processo);
        alerta.setDescricao("Alerta de Teste");
        alerta.setDataHora(LocalDateTime.now());
        return alerta;
    }

    public static Alerta alertaParaUsuario(Processo processo, Usuario usuario) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUsuarioDestino(usuario);
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
