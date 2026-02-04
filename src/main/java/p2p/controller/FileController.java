package p2p.controller;

import com.sun.net.httpserver.HttpServer;
import org.springframework.web.bind.annotation.RestController;
import p2p.service.FileSharer;

@RestController
public class FileController {
    private final FileSharer fileSharer;
    public final HttpServer httpServer;
    private final String uploadDir;
    private final ExcecutorService excecutorService;

    public FileController(FileSharer fileSharer, HttpServer httpServer, String uploadDir, ExcecutorService excecutorService) {
        this.fileSharer = fileSharer;
        this.httpServer = httpServer;
        this.uploadDir = uploadDir;
        this.excecutorService = excecutorService;
    }

}
