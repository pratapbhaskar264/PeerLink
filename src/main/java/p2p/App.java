package p2p;


import p2p.controller.FileController;

import java.io.IOException;


//fixation of bugs at last
public class App {
    public static void main(String[] args) {
      try{
          FileController fileController = new FileController(8080);
          fileController.start();
          System.out.println("Peerlink started at port 8080");
          System.out.println("UI available at http://localhost:3000");
          Runtime.getRuntime().addShutdownHook( new Thread( () -> {
              System.out.println("Shutting down the server");
              fileController.stop();
                  })
          );
          System.out.println("Press enter to stop the server");
          System.in.read(); //homework to stop the server when someone presses the enter key


      } catch (IOException e) {
          System.err.println("Failed to start server at port 8080");
          e.printStackTrace();
      }
    }
}
