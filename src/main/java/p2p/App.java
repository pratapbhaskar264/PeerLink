package p2p;

import p2p.controller.FileController;

public class App {
    public static void main(String[] args) throws Exception {
        // FIX 1: Read PORT from Render's environment variable
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        FileController controller = new FileController(port);
        controller.start();
    }
}