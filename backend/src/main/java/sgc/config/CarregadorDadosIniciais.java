package sgc.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sgc.model.*;
import sgc.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carrega dados iniciais na base para facilitar testes manuais.
 * Executado ao iniciar a aplicação (CommandLineRunner).
 * <p>
 * Observações:
 * - Os nomes das classes, variáveis e comentários estão em português conforme
 * solicitado.
 * - O carregador tenta inserir exemplos básicos de Processo, Unidade, Usuario,
 * Mapa,
 * Atividade, Competencia, Conhecimento, Subprocesso e vínculos
 * CompetenciaAtividade.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class CarregadorDadosIniciais implements CommandLineRunner {
    private final ProcessoRepository processoRepository;
    private final UnidadeRepository unidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MapaRepository mapaRepository;
    private final AtividadeRepository atividadeRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ConhecimentoRepository conhecimentoRepository;
    private final SubprocessoRepository subprocessoRepository;
    private final CompetenciaAtividadeRepository competenciaAtividadeRepository;

    @Override
    public void run(String... args) {
        // Não duplicar dados se já existirem
        if (processoRepository.count() > 0) return;

        // Criar Processo
        Processo p1 = new Processo();
        p1.setDataCriacao(LocalDateTime.now());
        p1.setDescricao("Processo de Mapeamento Exemplo");
        p1.setSituacao("CRIADO");
        p1.setTipo("MAPEAMENTO");
        p1 = processoRepository.save(p1);

        // Criar Unidades
        Unidade u1 = new Unidade();
        u1.setNome("Diretoria de Exemplo");
        u1.setSigla("DEX");
        u1.setTipo("OPERACIONAL");
        u1.setSituacao("ATIVA");
        u1 = unidadeRepository.save(u1);

        Unidade u2 = new Unidade();
        u2.setNome("Seção de Testes");
        u2.setSigla("ST");
        u2.setTipo("OPERACIONAL");
        u2.setSituacao("ATIVA");
        u2 = unidadeRepository.save(u2);

        // Criar Usuários
        Usuario user1 = new Usuario();
        user1.setTitulo("USR001");
        user1.setNome("João Exemplo");
        user1.setEmail("joao.exemplo@exemplo.gov.br");
        user1.setRamal("1234");
        user1.setUnidade(u1);
        usuarioRepository.save(user1);

        Usuario user2 = new Usuario();
        user2.setTitulo("USR002");
        user2.setNome("Maria Teste");
        user2.setEmail("maria.teste@exemplo.gov.br");
        user2.setRamal("5678");
        user2.setUnidade(u2);
        usuarioRepository.save(user2);

        // Criar Mapa
        Mapa mapa = new Mapa();
        mapa.setDataHoraDisponibilizado(LocalDateTime.now());
        mapa.setObservacoesDisponibilizacao("Mapa inicial de exemplo");
        mapa = mapaRepository.save(mapa);

        // Criar Atividades
        Atividade at1 = new Atividade();
        at1.setMapa(mapa);
        at1.setDescricao("Atividade de exemplo 1");
        at1 = atividadeRepository.save(at1);

        Atividade at2 = new Atividade();
        at2.setMapa(mapa);
        at2.setDescricao("Atividade de exemplo 2");
        at2 = atividadeRepository.save(at2);

        // Criar Competências
        Competencia c1 = new Competencia();
        c1.setMapa(mapa);
        c1.setDescricao("Competência A");
        c1 = competenciaRepository.save(c1);

        Competencia c2 = new Competencia();
        c2.setMapa(mapa);
        c2.setDescricao("Competência B");
        c2 = competenciaRepository.save(c2);

        // Vincular Competência-Atividade (N-N)
        CompetenciaAtividade.Id id1 = new CompetenciaAtividade.Id();
        id1.setAtividadeCodigo(at1.getCodigo());
        id1.setCompetenciaCodigo(c1.getCodigo());
        CompetenciaAtividade ca1 = new CompetenciaAtividade();
        ca1.setId(id1);
        ca1.setAtividade(at1);
        ca1.setCompetencia(c1);
        competenciaAtividadeRepository.save(ca1);

        CompetenciaAtividade.Id id2 = new CompetenciaAtividade.Id();
        id2.setAtividadeCodigo(at2.getCodigo());
        id2.setCompetenciaCodigo(c2.getCodigo());
        CompetenciaAtividade ca2 = new CompetenciaAtividade();
        ca2.setId(id2);
        ca2.setAtividade(at2);
        ca2.setCompetencia(c2);
        competenciaAtividadeRepository.save(ca2);

        // Criar Conhecimentos
        Conhecimento k1 = new Conhecimento();
        k1.setAtividade(at1);
        k1.setDescricao("Conhecimento X necessário");
        conhecimentoRepository.save(k1);

        Conhecimento k2 = new Conhecimento();
        k2.setAtividade(at2);
        k2.setDescricao("Conhecimento Y necessário");
        conhecimentoRepository.save(k2);

        // Criar Subprocesso
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p1);
        sp.setUnidade(u1);
        sp.setMapa(mapa);
        sp.setDataLimiteEtapa1(LocalDate.now().plusDays(7));
        sp.setSituacaoId("CRIADO");
        subprocessoRepository.save(sp);

        String msg = """
                Carregador de dados iniciais: inseridos exemplos de
                Processo, Unidade, Usuário, Mapa, Atividade,
                Competência, Conhecimento, Subprocesso e vínculos.""";
        log.info(msg);
    }
}