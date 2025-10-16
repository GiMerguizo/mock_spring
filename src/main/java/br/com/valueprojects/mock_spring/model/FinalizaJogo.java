package br.com.valueprojects.mock_spring.model;

import java.util.Calendar;
import java.util.List;
import infra.JogoDao;
import infra.MessageSender;
import infra.VencedorDao;

public class FinalizaJogo {

    private int total = 0;
    private final JogoDao jogoDao;
    private final VencedorDao vencedorDao;
    private final MessageSender messageSender;

    /**
     * Construtor que recebe todas as dependências (DAOs e services)
     * necessárias para executar a regra de negócio.
     */
    public FinalizaJogo(JogoDao jogoDao, VencedorDao vencedorDao, MessageSender messageSender) {
        this.jogoDao = jogoDao;
        this.vencedorDao = vencedorDao;
        this.messageSender = messageSender;
    }

    /**
     * Executa a principal regra de negócio:
     * Finaliza jogos da semana anterior, salva o vencedor e envia uma notificação.
     */
    public void finaliza() {
        List<Jogo> todosJogosEmAndamento = jogoDao.emAndamento();

        for (Jogo jogo : todosJogosEmAndamento) {
            if (iniciouSemanaAnterior(jogo)) {
                jogo.finaliza();
                total++;
                jogoDao.atualiza(jogo); // 1. Atualiza o status do jogo

                if (!jogo.getResultados().isEmpty()) {
                    Participante vencedorParticipante = encontraVencedor(jogo);
                    
                    Vencedor vencedor = new Vencedor(vencedorParticipante, jogo);
                    vencedorDao.salva(vencedor); // 2. Salva o vencedor

                    String mensagem = "Parabéns, " + vencedorParticipante.getNome() + "! Você venceu o jogo '" + jogo.getDescricao() + "'!";
                    messageSender.send(vencedorParticipante, mensagem); // 3. Envia a notificação
                }
            }
        }
    }

    /**
     * Encontra o participante com a maior métrica no jogo.
     */
    private Participante encontraVencedor(Jogo jogo) {
        Resultado maiorResultado = null;
        for(Resultado r : jogo.getResultados()) {
            if(maiorResultado == null || r.getMetrica() > maiorResultado.getMetrica()) {
                maiorResultado = r;
            }
        }
        return maiorResultado.getParticipante();
    }

    /**
     * Verifica se o jogo começou há 7 dias ou mais.
     */
    private boolean iniciouSemanaAnterior(Jogo jogo) {
        return diasEntre(jogo.getData(), Calendar.getInstance()) >= 7;
    }

    private int diasEntre(Calendar inicio, Calendar fim) {
        Calendar data = (Calendar) inicio.clone();
        int diasNoIntervalo = 0;
        while (data.before(fim)) {
            data.add(Calendar.DAY_OF_MONTH, 1);
            diasNoIntervalo++;
        }
        return diasNoIntervalo;
    }

    public int getTotalFinalizados() {
        return total;
    }
}