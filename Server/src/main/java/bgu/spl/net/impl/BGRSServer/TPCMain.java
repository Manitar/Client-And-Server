package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main (String [] args){

        Database database = Database.getInstance();
        database.initialize("./Courses.txt");
        Server.threadPerClient( 7777,
                ()->new ServerProtocol(),
                LineMessageEncoderDecoder::new).serve();
    }
}
