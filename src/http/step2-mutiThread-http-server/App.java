import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class App 
{
    public static void main( String[] args ) throws IOException {
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer();
        simpleHttpServer.start();
    }
}