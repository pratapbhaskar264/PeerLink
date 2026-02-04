package p2p.controller;

import com.sun.net.httpserver.HttpServer;
import org.springframework.web.bind.annotation.RestController;
import p2p.service.FileSharer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class FileController {
    private final FileSharer fileSharer;
    public final HttpServer httpServer;
    //temp dir which will store files on temp basis
    private final String uploadDir;
    private final ExecutorService excecutorService;

    public FileController( int port) throws IOException {
        this.fileSharer = new FileSharer();
        this.httpServer = HttpServer.create(new InetSocketAddress(port) , 0);

        this.uploadDir = System.getProperty("java.io.tempdir") + File.separator + "peerlink-uploads";
        this.excecutorService = Executors.newFixedThreadPool(10);
    }

}
