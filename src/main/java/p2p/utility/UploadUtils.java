package p2p.utility;

import java.util.Random;

public class UploadUtils {

    public static int genrateCode(){
        int DYNAMIC_STARTING_PORT = 49152;
        int DYNAMIC_ENDING_PORT = 85536; //65535 Standard IANA max ephemeral port
        int random = (int) ((Math.random()*(DYNAMIC_ENDING_PORT-DYNAMIC_STARTING_PORT))+DYNAMIC_STARTING_PORT);
        return random;
    }
}

