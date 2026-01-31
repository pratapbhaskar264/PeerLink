package p2p.service;

import org.springframework.stereotype.Service;
import p2p.utility.UploadUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

@Service
public class FileSharer {
    private HashMap<Integer , String> availableFiles;

    public FileSharer(){
        availableFiles = new HashMap<>(); // this will store files available at certain port as of now
    }

    // uploading end
    private int offerFile(String filePath) { // it is taking file path and returning port ->
        // -> after its loaded with file in available file map
        int port;
        while(true) {
           port = UploadUtils.genrateCode();
           if(!availableFiles.containsKey(port)) {
               availableFiles.put(port,filePath);
               return port;
           }
        }
    }

    //downloading end
    public void startFileSharer(int port) {
        if(!availableFiles.containsKey(port)) {
            System.out.println("no such file associated with port " +port);
            return;
        }
        String filePath = availableFiles.get(port);
        try(ServerSocket serverSocket = new ServerSocket(port) ) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
