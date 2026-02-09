package p2p.service;

import p2p.utility.UploadUtils ;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileSharer {
    private HashMap<Integer , String> availableFiles;

    public FileSharer(){
        availableFiles = new HashMap<>(); // this will store files available at certain port as of now
    }

    // uploading end
    public int offerFile(String filePath) { // it is taking file path and returning port ->
        // -> after its loaded with file in available file map
        int port;
        while(true) {
           port = UploadUtils.genrateCode();
           if(!availableFiles.containsKey(port)) {
               availableFiles.put(port,filePath); // so basically map holds the file that appear to be at a port
               return port;
           }
        }
    }
    // download end point will handle all the cumputations of header and all and then it will go to filesharer
    //downloading end
    // client -> upload 
    // client2 -> download -> server -> downloadEndPt. -> filesharer -> socket -> outcome  
    public void startFileServer(int port) { //it seems that download handler will communicate to download...but filesharer is handling it all
        if(!availableFiles.containsKey(port)) {
            System.out.println("No file associated with port " +port);
            return;
        }
        String filePath = availableFiles.get(port);
        try(ServerSocket serverSocket = new ServerSocket(port) )  { //opening a socket here will automatically close it afterwards

            System.out.println("Serving file " + new File(filePath).getName() +" on port : " + port);
            Socket clientSocket = serverSocket.accept(); // socket creation at the time of client request
            System.out.println("ClientConnection : " +clientSocket.getInetAddress() );
            new Thread(new FileSenderHandler(clientSocket,filePath)).start();
        } catch (IOException e) {
            System.out.println("Error in finding file on port : " +port +" "+e.getMessage());
        }

    }


private static class FileSenderHandler implements Runnable{
        private final Socket clientSocket;
        private final String filePath;

    public FileSenderHandler(Socket clientSocket, String filePath) {
        this.clientSocket = clientSocket;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try(FileInputStream fis = new FileInputStream(filePath)){ // we took input stream here from file and
            OutputStream outputStream = clientSocket.getOutputStream(); // creation of output stream for sockets
            String filename = new File(filePath).getName();
            String header = "Filename: "+filename+"\n"; // will be passed with output stream
            outputStream.write(header.getBytes());

            byte[] buffer = new byte[4096];
            int byteread;
            while( (byteread = fis.read(buffer)) != -1  ) {
                outputStream.write(buffer,0,byteread);
            }

            System.out.println("File " +filename+ " sent to " +clientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println("Error sending file to the client  " + e.getMessage() );
        } finally {
            try{
                clientSocket.close();
            }catch (Exception e) {
                System.out.println("Error closing socket : " +e.getMessage());
            }
        }
    }
}
}
