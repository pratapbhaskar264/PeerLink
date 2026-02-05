package p2p.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.springframework.http.server.reactive.HttpHandler;
import p2p.service.FileSharer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
    private final FileSharer fileSharer;
    public final HttpServer httpServer;
    //temp dir which will store files on a temp basis
    private final String uploadDir;
    private final ExecutorService excecutorService;

    public FileController( int port) throws IOException {
        this.fileSharer = new FileSharer();
        this.httpServer = HttpServer.create(new InetSocketAddress(port) , 0);

        this.uploadDir = System.getProperty("java.io.tempdir") + File.separator + "peerlink-uploads";
        this.excecutorService = Executors.newFixedThreadPool(10); //10 threads allowed

        File uploadDirFile = new File(uploadDir);
        if(!uploadDirFile.exists()) {
            uploadDirFile.mkdir();
        }

        httpServer.createContext("/upload" , new UploadHandler());
        httpServer.createContext("/download" , new DownloadHandler());
        httpServer.createContext("/" , new CORSHandler());
        httpServer.setExecutor(excecutorService);
    }
    public void start() {
        httpServer.start();
        System.out.println("server started at port : " + httpServer.getAddress().getPort());
    }

    public void stop() {
        httpServer.stop(0);
        excecutorService.shutdown();
        System.out.println("API server stopped");
    }

    //CORS handler
    public class CORSHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin" , "*");
            headers.add("Access-Control-Allow-Methods" , "GET , POST , OPTIONS");
            headers.add("Access-Control-Allow-Headers" , "Content-Type,Authorization");

            if( httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                httpExchange.sendResponseHeaders(204,-1);
            }
            String response = "NOT FOUND";
;
            httpExchange.sendResponseHeaders(404,response.getBytes().length);

            try(OutputStream outputStream = httpExchange.getResponseBody()) { // auto close
                outputStream.write(response.getBytes());
            }
        }

    }

    //UPLOAD HANDLER
    private class UploadHandler implements HttpHandler {

        @Override
        public void handle (HttpExchange httpExchange) throws IOException {

            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin" , "*");
//            headers.add("Access-Control-Allow-Methods" , "POST"); //Only post Allowed
//            headers.add("Access-Control-Allow-Headers" , "Content-Type,Authorization");

            if( httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String response = "METHOD NOT ALLOWED";
                httpExchange.sendResponseHeaders(405,response.getBytes().length);
                try(OutputStream outputStream = httpExchange.getResponseBody()) { // auto close
                    outputStream.write(response.getBytes());
                }
                return;
            }
             // if method is post



        }

    }





}
