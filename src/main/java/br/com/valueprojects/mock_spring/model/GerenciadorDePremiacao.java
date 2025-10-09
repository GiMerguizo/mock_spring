package br.com.valueprojects.mock_spring.model;

import java.util.List;
import infra.JogoDao;
import infra.SMSSender;

public class GerenciadorDePremiacao {

    private final JogoDao dao;
    private final SMSSender sms;
    private final FinalizaJogo finalizador;

    public GerenciadorDePremiacao(JogoDao dao, SMSSender sms) {
        this.dao = dao;
        this.sms = sms;

        this.finalizador = new FinalizaJogo(dao);
    }

    /**
     * Executa todo o fluxo: finaliza, salva, encontra o vencedor e notifica.
     */
    public void processaPremiacao() {
        // Passo 1: Usa a classe existente para finalizar e salvar os jogos da semana anterior.
        finalizador.finaliza();

        // Passo 2: Busca no banco de dados os jogos que acabaram de ser finalizados.
        List<Jogo> jogosFinalizados = dao.finalizados();

        // Passo 3: Itera sobre cada jogo finalizado para encontrar e notificar o vencedor.
        for (Jogo jogo : jogosFinalizados) {
            
            Participante vencedor = encontraVencedor(jogo);

            // Passo 4: Se um vencedor foi encontrado, envia o SMS.
            if (vencedor != null) {
                sms.send(vencedor);
            }
        }
    }

    private Participante encontraVencedor(Jogo jogo) {
        List<Resultado> resultados = jogo.getResultados();

        if (resultados.isEmpty()) {
            return null; // Não há vencedor se não há resultados.
        }

        Resultado melhorResultado = resultados.get(0);

        for (int i = 1; i < resultados.size(); i++) {
            Resultado resultadoAtual = resultados.get(i);
            if (resultadoAtual.getMetrica() > melhorResultado.getMetrica()) { //
                melhorResultado = resultadoAtual;
            }
        }

        return melhorResultado.getParticipante();
    }
}