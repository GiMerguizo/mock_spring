package br.com.valueprojects.mock_spring.model;

/**
 * Classe de dados para representar o vencedor de um jogo.
 */
public class Vencedor {

    private final Jogo jogo;
    private final Participante participante;

    public Vencedor(Jogo jogo, Participante participante) {
        this.jogo = jogo;
        this.participante = participante;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public Participante getParticipante() {
        return participante;
    }
}