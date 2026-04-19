//package p2p.controller;
//
//import com.sun.net.httpserver.Headers;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;
//import org.apache.commons.io.IOUtils;
//import p2p.service.FileSharer;
//
//import java.io.*;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class FileController {
//    private final FileSharer fileSharer;
//    public final HttpServer httpServer;
//    //temp dir which will store files on a temp basis
//    private final String uploadDir;
//    private final ExecutorService excecutorService;
//
//    public FileController( int port) throws IOException {
//        this.fileSharer = new FileSharer();
//        this.httpServer = HttpServer.create(new InetSocketAddress(port) , 0);
//        this.uploadDir = System.getProperty("java.io.tempdir") + File.separator + "peerlink-uploads";
//        this.excecutorService = Executors.newFixedThreadPool(10); //10 threads allowed
//
//        File uploadDirFile = new File(uploadDir);
//        if(!uploadDirFile.exists()) {
//            uploadDirFile.mkdirs();
//        }
//        httpServer.createContext("/upload" , new UploadHandler());
//        httpServer.createContext("/download" , new DownloadHandler());
//        httpServer.createContext("/" , new CORSHandler());
//        httpServer.setExecutor(excecutorService);
//    }
//    public void start() {
//        httpServer.start();
//        System.out.println("server started at port : " + httpServer.getAddress().getPort());
//    }
//
//    public void stop() {
//        httpServer.stop(0);
//        excecutorService.shutdown();
//        System.out.println("API server stopped");
//    }
//
//    //CORS handler
//    private class CORSHandler implements HttpHandler {
//
//        @Override
//        public void handle(HttpExchange httpExchange) throws IOException {
//            Headers headers = httpExchange.getResponseHeaders();
//            headers.add("Access-Control-Allow-Origin" , "*");
//            headers.add("Access-Control-Allow-Methods" , "GET , POST , OPTIONS");
//            headers.add("Access-Control-Allow-Headers" , "Content-Type,Authorization");
//
//            if( httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
//                httpExchange.sendResponseHeaders(204,-1);
//            }
//
//            String response = "NOT FOUND";
//            httpExchange.sendResponseHeaders(404,response.getBytes().length);
//            try(OutputStream outputStream = httpExchange.getResponseBody()) { // auto close
//                outputStream.write(response.getBytes());
//            }
//        }
//
//    }
//
//    //UPLOAD HANDLER
//    private class UploadHandler implements HttpHandler {
//
//        @Override
//        public void handle (HttpExchange httpExchange) throws IOException {
//
//            Headers headers = httpExchange.getResponseHeaders();
//            headers.add("Access-Control-Allow-Origin" , "*");
////            headers.add("Access-Control-Allow-Methods" , "POST"); //Only post Allowed
////            headers.add("Access-Control-Allow-Headers" , "Content-Type,Authorization");
//
//            if( !httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
//                String response = "METHOD NOT ALLOWED";
//                httpExchange.sendResponseHeaders(405,response.getBytes().length);
//                try(OutputStream outputStream = httpExchange.getResponseBody()) { // auto close
//                    outputStream.write(response.getBytes());
//                }
//                return;
//            }
//             // if method is post ... fetch content type
//
//            Headers requestHeaders = httpExchange.getRequestHeaders();
//            String contentType = requestHeaders.getFirst("Content-Type");
//            if(contentType == null || !contentType.startsWith("multipart/form-data")) {
//                String response = "Bad request : Content-Type must be multipart/form-data";
//                httpExchange.sendResponseHeaders(400,response.getBytes().length);
//                try(OutputStream outputStream = httpExchange.getResponseBody()) {
//                    outputStream.write(response.getBytes());
//                }
//                return; // do not proceed ahead
//            }
//
//            // proceed if the method is post and content-Type is valid
//
//            try {
//
//                String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
//                boundary = boundary.replace("\"", "");
//                boundary = boundary.trim();
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//                IOUtils.copy(httpExchange.getRequestBody(), byteArrayOutputStream);
//                byte[] requestData = byteArrayOutputStream.toByteArray();
//                System.out.println("✅ Upload /upload called");
//                System.out.println("Content-Type: " + contentType);
//                System.out.println("Boundary: " + boundary);
//                System.out.println("Request size (bytes): " + requestData.length);
//                Multiparser parser = new Multiparser(requestData, boundary);
//                Multiparser.ParseResult result = parser.parse();
//
////                Multiparser multiParser = new Multiparser(requestData , boundary);
////                Multiparser.ParseResult result = multiParser.parse();
//                if(result == null) {
//                    String response = "Bad request : Could not parse File Content ";
//                    httpExchange.sendResponseHeaders(400,response.getBytes().length);
//                    try(OutputStream outputStream = httpExchange.getResponseBody() ) {
//                        outputStream.write(response.getBytes());
//                    }
//                    return;
//                }
//
//                String fileName = result.fileName;
//                if(fileName == null || fileName.trim().isEmpty()) {
//                    fileName = "unnamed-File";
//                }
//                String uniqueFilename = UUID.randomUUID().toString() +"_" + new File(fileName).getName();
//                String filePath = uploadDir + File.separator + uniqueFilename;
//
//                try(FileOutputStream fileOutputStream = new FileOutputStream(filePath) ) {
//                    fileOutputStream.write(result.fileContent);
//                }
//                //uploading to a port
//                int port = fileSharer.offerFile(filePath);
//                new Thread(() -> fileSharer.startFileServer(port)).start();
////                String jsonResponse = "{\"port\":}" + port + "}";
//                  String jsonResponse = "{\"port\":" + port + "}";
//                headers.add("Content-Type","application/json");
//                httpExchange.sendResponseHeaders(200,jsonResponse.getBytes().length);
//
//                try(OutputStream outputStream = httpExchange.getResponseBody()) {
//                    outputStream.write(jsonResponse.getBytes());
//                }
//            } catch (Exception e) {
//                    System.err.println("Error processing fileUpload" + e.getMessage());
//                    String response = "Sever Error : " + e.getMessage();
//                    httpExchange.sendResponseHeaders(500,response.getBytes().length);
//                    try(OutputStream outputStream = httpExchange.getResponseBody()) {
//                        outputStream.write(response.getBytes());
//                    }
//            }
//        }
//    }
//
//    //downloadHandler
//    private class DownloadHandler implements HttpHandler {
//        @Override
//        public void handle(HttpExchange httpExchange) throws IOException {
//            Headers headers = httpExchange.getResponseHeaders();
//            headers.add("Access-Control-Allow-Origin","*");
//
//            //method is not "GET"
//            if(!httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
//                String response = "Method Not Allowed";
//                httpExchange.sendResponseHeaders(405,response.getBytes().length);
//                try(OutputStream outputStream = httpExchange.getResponseBody()) {
//                    outputStream.write(response.getBytes());
//                }
//           return;
//            }
//
//            // now access port frm URL and create a socket from server to fileSharer that will eventually help us to send the data to ->
//            // -> the client via sockets (this is how we are handling multiple requests from multiple users)
//            String path = httpExchange.getRequestURI().getPath();
//            String portStr = path.substring(path.lastIndexOf("/")+1); // this may give error
//            try {
//                int port = Integer.parseInt(portStr);
//                //creation of socket now from
//                try (Socket socket = new Socket("localhost", port)) {
//                    InputStream socketInput = socket.getInputStream();
//                    File tempFile = File.createTempFile("download-", ".tmp");
//                    String fileName = "download-file";
//                    try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
//                        byte[] buffer = new byte[4096]; // size of file considered 4096 bytes 4KB
//                        int byteRead;
//                        ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
//                        int b;
//                        while ((b = socketInput.read()) != -1) {
//                            if (b == '\n') break;
//                            headerBaos.write(b);
//                        }
//                        //filename extraction
//                        String header = headerBaos.toString().trim();
//                        if (header.startsWith("Filename: ")) {
//                            fileName = header.substring("Filename: ".length()); // "Filename: hello.txt" so now it will have hello.txt
//                        }
//                        // read file content only 4KB ..... handle it for more size
//                        while ((byteRead = socketInput.read(buffer)) != -1) {
//                            fileOutputStream.write(buffer, 0, byteRead);
//                        }
//                    }
//                    //headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//                    headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//                    headers.add("Content-Type", "application/octet-stream");
//                    httpExchange.sendResponseHeaders(200, tempFile.length());
//                    // sending this temp file to the client
//                    try (OutputStream outputStream = httpExchange.getResponseBody()) {
//                        FileInputStream fileInputStream = new FileInputStream(tempFile); // sending this temp file to a client
//                        byte[] buffer = new byte[4096];
//                        int bytesRead;
//                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                            outputStream.write(buffer, 0, bytesRead);
//                        }
//                    }
//                    tempFile.delete();
//
//                } catch (Exception e) {
//                    System.out.println("Not able to download file" + e.getMessage());
//                    String response = "Error downloading file" + e.getMessage();
//                    headers.add("Content-Type" , "text/plain");
//                    httpExchange.sendResponseHeaders(404,response.getBytes().length);
//                    try(OutputStream outputStream = httpExchange.getResponseBody()) {
//                        outputStream.write(response.getBytes());
//                    }
//                }
//            } catch (NumberFormatException e) {
//                String response = "Bad Request: Invalid port number";
//                httpExchange.sendResponseHeaders(400, response.getBytes().length);
//                try (OutputStream os = httpExchange.getResponseBody()) {
//                    os.write(response.getBytes());
//                }
//            }
//        }
//    }
//    private static class Multiparser {
//
//        public final byte[] data;
//        public final String boundary;
//
//
//        private Multiparser(byte[] data, String boundary) {
//            this.data = data;
//            this.boundary = boundary;
//        }
//
////        public ParseResult parse() {
////            try {
////                String dataAsString = new String(data); // pdf json csv txt
////                String filenameMarker = "filename=\"";
////                int filenameStart = dataAsString.indexOf(filenameMarker);
////                if (filenameStart == -1) {
////                    return null; // no filename found;
////                }
////
////                int filenameEnd = dataAsString.indexOf("\"", filenameStart);
////                if (filenameEnd == -1) return null;
////                String filename = dataAsString.substring(filenameStart, filenameEnd);
////
////                String lower = dataAsString.toLowerCase();
////                String contentTypeMarker = "content-type: ";
////                int contentTypeStart = lower.indexOf(contentTypeMarker, filenameEnd);
////                String contentType = "application/octet-stream";
////
////                if (contentTypeStart != -1) {
////                    contentTypeStart += contentTypeMarker.length();
////                    int contentTypeEnd = dataAsString.indexOf("\r\n", contentTypeStart);
////                    contentType = dataAsString.substring(contentTypeStart, contentTypeEnd);
////                }
////
////                //data retrieval
////                String headerEndMarker = "\r\n\r\n";
////                int headerEnd = dataAsString.indexOf(headerEndMarker);
////                if (headerEnd == -1) { // no empty space found after content type
////                    return null;
////                }
////                int contentStart = headerEnd + headerEndMarker.length();
////
////                byte[] boundaryBytes = ("\r\n--" + boundary + "--").getBytes();
////                int contentEnd = findSequence(data, boundaryBytes, contentStart); // found the end point of data in file
////                if (contentEnd == -1) {
////                    boundaryBytes = ("\r\n--" + boundary).getBytes();
////                    contentEnd = findSequence(data, boundaryBytes, contentStart); // end format changed .. without "--"
////                }
////                if (contentEnd == -1 || contentEnd <= contentStart) {
////                    return null;
////                }
////
////                // finally getting raw data from file after file name content start and content End are successfully found out
////                byte[] fileContent = new byte[contentEnd - contentStart];
////                System.arraycopy(data, contentStart, fileContent, 0, fileContent.length);
////                return new ParseResult(filename, fileContent, contentType);
////
////            } catch (Exception e) {
////                System.out.println("Error fetching multiPartData : " + e.getMessage());
////                return null;
////            }
////        }
////
//public ParseResult parse() {
//    try {
//        // We only use String to search for headers (which are always text)
//        // but we keep the file content as raw bytes.
//        String dataHeaderPart = new String(data, 0, Math.min(data.length, 2048));
//
//        String filenameMarker = "filename=\"";
//        int filenameStart = dataHeaderPart.indexOf(filenameMarker);
//        if (filenameStart == -1) return null;
//        filenameStart += filenameMarker.length();
//
//        int filenameEnd = dataHeaderPart.indexOf("\"", filenameStart);
//        if (filenameEnd == -1) return null;
//        String filename = dataHeaderPart.substring(filenameStart, filenameEnd);
//
//        // Find where the real data starts (after \r\n\r\n)
//        byte[] headerEndMarker = "\r\n\r\n".getBytes();
//        int contentStart = findSequence(data, headerEndMarker, 0) + headerEndMarker.length;
//
//        // Find where the data ends (the boundary)
//        byte[] boundaryBytes = ("\r\n--" + boundary).getBytes();
//        int contentEnd = findSequence(data, boundaryBytes, contentStart);
//
//        if (contentEnd == -1 || contentEnd <= contentStart) return null;
//
//        // Extract bytes directly from the array
//        byte[] fileContent = new byte[contentEnd - contentStart];
//        System.arraycopy(data, contentStart, fileContent, 0, fileContent.length);
//
//        return new ParseResult(filename, fileContent, "application/octet-stream");
//
//    } catch (Exception e) {
//        System.err.println("Parsing error: " + e.getMessage());
//        return null;
//    }
//}
//
//        public static class ParseResult {
//            private final String fileName;
//            private final byte[] fileContent;
//            private final String contentType;
//
//            public ParseResult(String fileName, byte[] fileContent, String contentType) {
//                this.fileName = fileName;
//                this.fileContent = fileContent;
//                this.contentType = contentType;
//            }
//        }
//
//
//        private static int findSequence(byte[] data, byte[] seqeunce, int startPos) {
//            outer:
//            for (int i = startPos; i <= data.length - seqeunce.length; i++) {
//                for (int j = 0; j < seqeunce.length; j++) {
//                    if (data[i + j] != seqeunce[j]) {
//                        continue outer;
//                    }
//                }
//                return i;
//            }
//            return -1;
//        }
//    }
//}
package p2p.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import p2p.service.FileSharer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
    private final FileSharer fileSharer;
    public final HttpServer httpServer;
    private final String uploadDir;
    private final ExecutorService executorService;

    public FileController(int port) throws IOException {
        this.fileSharer = new FileSharer();

        // FIX 1: use java.io.tmpdir (not tempdir — that was a typo)
        this.uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "peerlink-uploads";
        this.executorService = Executors.newFixedThreadPool(10);

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/upload", new UploadHandler());
        httpServer.createContext("/download", new DownloadHandler());
        httpServer.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        httpServer.createContext("/", new CORSHandler());
        httpServer.setExecutor(executorService);
    }

    public void start() {
        httpServer.start();
        System.out.println("Server started at port: " + httpServer.getAddress().getPort());
    }

    public void stop() {
        httpServer.stop(0);
        executorService.shutdown();
    }

    // FIX 2: CORS helper — read FRONTEND_URL from env, default to * for dev
    private void addCORSHeaders(Headers headers) {
        String origin = System.getenv("FRONTEND_URL");
        headers.add("Access-Control-Allow-Origin", origin != null ? origin : "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private class CORSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCORSHeaders(ex.getResponseHeaders());
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            String response = "NOT FOUND";
            ex.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCORSHeaders(ex.getResponseHeaders());

            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                respond(ex, 405, "METHOD NOT ALLOWED");
                return;
            }

            String contentType = ex.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                respond(ex, 400, "Bad Request: Content-Type must be multipart/form-data");
                return;
            }

            try {
                String boundary = contentType.substring(contentType.indexOf("boundary=") + 9).replace("\"", "").trim();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(ex.getRequestBody(), baos);
                byte[] requestData = baos.toByteArray();

                Multiparser parser = new Multiparser(requestData, boundary);
                Multiparser.ParseResult result = parser.parse();

                if (result == null) {
                    respond(ex, 400, "Bad Request: Could not parse file content");
                    return;
                }

                String fileName = (result.fileName == null || result.fileName.trim().isEmpty())
                        ? "unnamed-file"
                        : new File(result.fileName).getName();

                String uniqueName = UUID.randomUUID() + "_" + fileName;
                String filePath = uploadDir + File.separator + uniqueName;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(result.fileContent);
                }

                // FIX 3: no more random ServerSocket — just store path keyed by code
                int code = fileSharer.offerFile(filePath);
                String json = "{\"port\":" + code + "}";

                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, json.getBytes().length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(json.getBytes());
                }

            } catch (Exception e) {
                System.err.println("Upload error: " + e.getMessage());
                respond(ex, 500, "Server Error: " + e.getMessage());
            }
        }
    }

    private class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCORSHeaders(ex.getResponseHeaders());

            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
                respond(ex, 405, "Method Not Allowed");
                return;
            }

            String path = ex.getRequestURI().getPath();
            String codeStr = path.substring(path.lastIndexOf("/") + 1);

            try {
                int code = Integer.parseInt(codeStr);

                // FIX 3 cont: look up file directly — no socket needed
                String filePath = fileSharer.getFilePath(code);
                if (filePath == null) {
                    respond(ex, 404, "File not found for code: " + code);
                    return;
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    respond(ex, 404, "File no longer available");
                    return;
                }

                // Extract original filename (strip UUID prefix)
                String storedName = file.getName();
                String originalName = storedName.contains("_")
                        ? storedName.substring(storedName.indexOf("_") + 1)
                        : storedName;

                ex.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + originalName + "\"");
                ex.getResponseHeaders().add("Content-Type", "application/octet-stream");
                ex.sendResponseHeaders(200, file.length());

                try (OutputStream os = ex.getResponseBody();
                     FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                // Optionally clean up after download
                // fileSharer.removeFile(code);
                // file.delete();

            } catch (NumberFormatException e) {
                respond(ex, 400, "Bad Request: Invalid code");
            }
        }
    }

    private void respond(HttpExchange ex, int status, String message) throws IOException {
        byte[] bytes = message.getBytes();
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --- Multiparser (unchanged from your version) ---
    private static class Multiparser {
        public final byte[] data;
        public final String boundary;

        private Multiparser(byte[] data, String boundary) {
            this.data = data;
            this.boundary = boundary;
        }

        public ParseResult parse() {
            try {
                String dataHeaderPart = new String(data, 0, Math.min(data.length, 2048));
                String filenameMarker = "filename=\"";
                int filenameStart = dataHeaderPart.indexOf(filenameMarker);
                if (filenameStart == -1) return null;
                filenameStart += filenameMarker.length();
                int filenameEnd = dataHeaderPart.indexOf("\"", filenameStart);
                if (filenameEnd == -1) return null;
                String filename = dataHeaderPart.substring(filenameStart, filenameEnd);

                byte[] headerEndMarker = "\r\n\r\n".getBytes();
                int contentStart = findSequence(data, headerEndMarker, 0) + headerEndMarker.length;

                byte[] boundaryBytes = ("\r\n--" + boundary).getBytes();
                int contentEnd = findSequence(data, boundaryBytes, contentStart);
                if (contentEnd == -1 || contentEnd <= contentStart) return null;

                byte[] fileContent = new byte[contentEnd - contentStart];
                System.arraycopy(data, contentStart, fileContent, 0, fileContent.length);
                return new ParseResult(filename, fileContent, "application/octet-stream");

            } catch (Exception e) {
                System.err.println("Parsing error: " + e.getMessage());
                return null;
            }
        }

        public static class ParseResult {
            public final String fileName;
            public final byte[] fileContent;
            public final String contentType;

            public ParseResult(String fileName, byte[] fileContent, String contentType) {
                this.fileName = fileName;
                this.fileContent = fileContent;
                this.contentType = contentType;
            }
        }

        private static int findSequence(byte[] data, byte[] sequence, int startPos) {
            outer:
            for (int i = startPos; i <= data.length - sequence.length; i++) {
                for (int j = 0; j < sequence.length; j++) {
                    if (data[i + j] != sequence[j]) continue outer;
                }
                return i;
            }
            return -1;
        }
    }
}