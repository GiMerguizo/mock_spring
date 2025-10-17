package br.com.valueprojects.mock_spring.service;

import br.com.valueprojects.mock_spring.model.Participante;
import infra.MessageSender;

public class SMSService implements MessageSender {

    @Override
    public void send(Participante participante, String mensagem) {

        System.out.println("--------------------------------------------------");
        System.out.println("Enviando SMS para: " + participante.getNome());
        System.out.println("Mensagem: " + mensagem);
        System.out.println("--------------------------------------------------");
    }
}