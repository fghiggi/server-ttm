import com.feevale.protocolo.MensagemCliente;
import com.feevale.protocolo.MensagemServer;
import com.feevale.protocolo.Protocolo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;

/**
 * Created by slave00 on 26/10/16.
 */
public class ClienteConectado implements Runnable{
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Thread thread;
    private String apelido;

    public ClienteConectado(Socket s) {
        this.socket = s;
        setup();
        start();
        enviarMensagem("Informe seu apelido");
    }

    public void enviarMensagem(String msg){
        output.println(msg);
        output.flush();
    }

    public void enviarMensagemTodos(String msg){
        Servidor.clientes.forEach(c -> {
            c.enviarMensagem(msg);
        });
    }

    public void receberMensagem(){
        try {
            String msg = input.readLine();

            verificarMensagem(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void verificarMensagem(String mensagem){
        MensagemCliente tipo = Protocolo.parse(mensagem);

        switch (tipo){
            case LISTA: enviarListaUsuarios();
                break;
            case MENSAGEM: enviarMensagemPublica(mensagem);
                break;
            case PRIVADO: enviarMensagemPrivada(mensagem);
                break;
            case SAIR: enviarSair();
                break;
            case INVALIDA:
                break;
        }
    }

    private void setup(){
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verificarApelido(String apelido){
        return Servidor.clientes.stream().anyMatch(c -> c.apelido.equals(apelido));
    }

    private void enviarListaUsuarios(){
        Servidor.clientes.forEach(cli -> {
            Servidor.clientes.forEach(c -> {
                cli.enviarMensagem(String.format("%s %s",MensagemServer.ENVIO_LISTA.mensagem, c.apelido));
            });
        });
    }

    private void enviarMensagemPrivada(String msg){
        String temp[] = msg.split(" ");

        try{
            ClienteConectado destino = Servidor.clientes.stream().filter(c -> c.apelido.equals(temp[1])).findFirst().get();

            destino.enviarMensagem(String.format("%s %s %s", MensagemServer.MENSAGEM_PRIVADA.mensagem, this.apelido, temp[2]));
        } catch (NoSuchElementException ex){
            enviarMensagem(String.format("%s Usuário não conectado", MensagemServer.RECEBE_STATUS.mensagem));
        }
    }

    private void enviarMensagemPublica(String msg){
        enviarMensagemTodos(String.format("%s %s %s", MensagemServer.MENSAGEM_PUCLICA.mensagem, apelido, msg.substring(10)));
    }

    private void enviarSair(){
        Servidor.clientes.remove(this);

        enviarMensagemTodos(String.format("%s %s saiu da sala.", MensagemServer.SAIU_SALA.mensagem, this.apelido));

        enviarListaUsuarios();

        encerrar();
    }

    private void receberApelido() {
        try {
            String apelido = input.readLine();

            if (verificarApelido(apelido)) {
                enviarMensagem(String.format("Já existe um usuário com o apelido %s", apelido));
            } else {
                this.apelido = apelido;

                enviarMensagem(String.format("Bem vindo %s", this.apelido));

                enviarMensagemTodos(String.format("%s %s", MensagemServer.ENTROU_SALA.mensagem, this.apelido));

                Servidor.clientes.add(this);

                enviarListaUsuarios();
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(){
        thread = new Thread(this);
        thread.start();
    }

    private void encerrar(){
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            if(apelido == null)
                receberApelido();
            else
                receberMensagem();
        }
    }
}