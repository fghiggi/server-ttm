
import com.sun.deploy.util.SessionState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gabriel
 */
public class ClienteConectado implements Runnable{
    Socket s;
    PrintWriter out;
    BufferedReader in;
    Thread t;
    String apelido;

    static final String ENVIO_MENSAGEM_PUCLICA = "$:->mensagem";
    static final String ENVIO_MENSAGEM_PRIVADA = "$:->privado";
    static final String ENVIO_SAIR_SALA = "$:->sair";
    static final String ENVIO_LISTA = "$:->usuario";

    public ClienteConectado(Socket s) {
        this.s = s;
        setup();
        start();
        enviarMensagem("Informe seu apelido");
    }

    public void enviarMensagem(String msg){
        System.out.println(msg);
        out.println(msg);
        out.flush();
    }

    public void receberMensagem(){
        try {
            String msg = in.readLine();

            System.out.println(msg);

            if(msg.startsWith("/lista")){
                Servidor.clientes.forEach(c -> {
                    enviaListaUsuarios(c);
                });
            }

            if(msg.startsWith("/mensagem")){
                enviaMensagemTodos(msg.split(" ")[1]);
            }

            if(msg.startsWith("/privado")){
                enviaMensagemPrivada(msg);
            }

            if(msg.startsWith("/sair")){
                Servidor.clientes.remove(this);

                enviaSair();

                Servidor.clientes.forEach(c -> {
                    enviaListaUsuarios(c);
                });

                this.t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setup(){
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(){
        t = new Thread(this);
        t.start();
    }

    private boolean verificaNick(String nick){
        return Servidor.clientes.stream().anyMatch(c -> c.apelido.equals(nick));
    }

    private void enviaListaUsuarios(ClienteConectado cli){
        for(ClienteConectado c:Servidor.clientes){
            System.out.println("enviando" + c.apelido);
            cli.enviarMensagem(ENVIO_LISTA + " " + c.apelido);
        }
    }

    private void enviaMensagemPrivada(String msg){
        System.out.println("ta enviando privado");
        //validar se o cara ta na lista depois
        String x[] = msg.split(" ");

        ClienteConectado destino = Servidor.clientes.stream().filter(c -> c.apelido.equals(x[1])).findFirst().get();

        destino.enviarMensagem(ENVIO_MENSAGEM_PRIVADA + " " + destino.apelido + " " + x[2]);
    }

    private void enviaMensagemTodos(String msg){
        for(ClienteConectado c:Servidor.clientes){
            System.out.println(c.apelido);
            c.enviarMensagem(ENVIO_MENSAGEM_PUCLICA + " " + apelido + " " + msg);
        }
    }

    private void enviaSair(){
        for(ClienteConectado c:Servidor.clientes){

            c.enviarMensagem(ENVIO_SAIR_SALA + " " + this.apelido + " saiu da sala.");
        }
    }

    private void recebeApelido(){
        try {
            String nick = in.readLine();
            if(verificaNick(nick)){
                enviarMensagem("Tem gente com esse nickj");
            }else{
                this.apelido = nick;
                enviarMensagem("Bem vindo" + this.apelido);

                Servidor.clientes.add(this);

                Servidor.clientes.forEach(c -> {
                    enviaListaUsuarios(c);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            if(apelido == null)
                recebeApelido();
            else
                receberMensagem();
        }
    }
}