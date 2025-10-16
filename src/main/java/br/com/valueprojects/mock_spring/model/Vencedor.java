package br.com.valueprojects.mock_spring.model;

public class Vencedor {

    private final Participante participante;
    private final Jogo jogo;

    public Vencedor(Participante participante, Jogo jogo) {
        this.participante = participante;
        this.jogo = jogo;
    }

    public Participante getParticipante() {
        return participante;
    }

    public Jogo getJogo() {
        return jogo;
    }
}