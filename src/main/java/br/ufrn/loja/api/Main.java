package br.ufrn.loja.api;

import br.ufrn.loja.service.LojaService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/loja", new LojaController(new LojaService()));
        server.start();
        System.out.println("API da loja rodando em http://localhost:" + porta + "/loja");
    }
}
