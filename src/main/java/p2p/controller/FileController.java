package p2p.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.server.reactive.HttpHandler;
import p2p.service.FileSharer;

import java.io.ByteArrayOutputStream;
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
             // if method is post ... fetch content type

            Headers requestHeaders = httpExchange.getRequestHeaders();
            String contentType = requestHeaders.getFirst("Content-Type");
            if(contentType == null || !contentType.startsWith("multipart/form-data")) {
                String response = "Bad request : Content-Type must be multipart/form-data";
                httpExchange.sendResponseHeaders(400,response.getBytes().length);
                try(OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes());
                }
            }

            // proceed if the method is post and content-Type is valid

            try {

                String boundary = contentType.substring(contentType.indexOf("boundary=")+9);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                IOUtils.copy(httpExchange.getRequestBody() , byteArrayOutputStream);
                byte[] requestdata = byteArrayOutputStream.toByteArray();

                MultiParser multiParser = new MultiParser(requestdata , boundary);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static class Multiparser{

        public final byte[] data;
        public final String boundary;


        private Multiparser(byte[] data, String boundary) {
            this.data = data;
            this.boundary = boundary;
        }

        public ParseResult parse() {
            try{
                String dataAsString = new String(data); // pdf json csv txt
                String filenameMarker = "filename=\"";
                int filenameStart = dataAsString.indexOf(filenameMarker);
                if(filenameStart==-1){
                    return null; // no filename found;
                }

                int filenameEnd = dataAsString.indexOf("\"" , filenameStart);
                String filename = dataAsString.substring(filenameStart , filenameEnd);

                String contentTypeMarker = "Content-Type: ";
                int contentTypeStart = dataAsString.indexOf(contentTypeMarker,filenameEnd);
                String contentType = "application/octet-stream";
                if(contentTypeStart !=-1) {
                    contentTypeStart += contentTypeMarker.length();
                    int contentTypeEnd = dataAsString.indexOf("\r\n" , contentTypeStart);
                    contentType = dataAsString.substring(contentTypeStart,contentTypeEnd);
                }

                //data retrieval
                String headerEndMarker = "\r\n\r\n";
                int headerEnd = dataAsString.indexOf(headerEndMarker);
                if(headerEnd == -1) { // no empty space found after content type
                    return null;
                }
                int contentStart = headerEnd + headerEndMarker.length();

                byte[] boundaryBytes = ("\r\n--"+boundary+"--").getBytes();
                int contentEnd = findSequence(data,boundaryBytes);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private int findSequence(byte[] data , byte[] seqeunce , int startPos) {
            outer :
            for(int i= startPos ;i <= data.length - seqeunce.length;i++) {
                for(int j=0;j<seqeunce.length;j++) {
                    if(data[i+j] != seqeunce[j]) {
                        continue outer;
                    }
                }
                return i;
            }
            return -1;
        }

    }

    public static class ParseResult{
        private final String fileName;
        private final byte[] fileContent;

        public ParseResult(String fileName, byte[] fileContent) {
            this.fileName = fileName;
            this.fileContent = fileContent;
        }
    }





}
