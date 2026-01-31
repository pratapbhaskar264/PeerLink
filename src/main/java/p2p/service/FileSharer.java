package p2p.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class FileSharer {
    private HashMap<Integer , String> availableFiles;

    public FileSharer(){
        availableFiles = new HashMap<>();
    }

}
