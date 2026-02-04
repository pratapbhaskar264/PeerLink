package p2p.controller;

import org.springframework.web.bind.annotation.RestController;
import p2p.service.FileSharer;

@RestController
public class FileController {
    private final FileSharer fileSharer;

    public FileController(FileSharer fileSharer) {
        this.fileSharer = fileSharer;
    }

}
