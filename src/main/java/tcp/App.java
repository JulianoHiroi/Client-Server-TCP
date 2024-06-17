package tcp;

import java.util.ArrayList;
import java.util.List;

import tcp.client.TCPClient;
import tcp.server.TCPServer;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 15; i++) {
            list.add(null);
        }
        int number = 0;
        boolean isServer = false;
        for (int i= 0 ;  i < 10 ; i++){
            list.set(i, number);
            number++;
            if(i == 5 && isServer == false){
                for (int j = 0; j < 5; j++) {
                    list.removeFirst();
                    list.add(null);
                }
                i = -1;
                isServer = true;
            }
        }
        System.out.println(list);
    }
    
}
