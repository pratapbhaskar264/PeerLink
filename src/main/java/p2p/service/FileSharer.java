package p2p.service;

import org.springframework.stereotype.Service;
import p2p.utility.UploadUtils;

import java.util.HashMap;

@Service
public class FileSharer {
    private HashMap<Integer , String> availableFiles;

    public FileSharer(){
        availableFiles = new HashMap<>(); // this will store files available at certain port as of now
    }

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

}
