import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

@ServerEndpoint("/socketHandler")
public class handler {
	 @OnOpen
	 public void onOpen() {
		 System.out.println("Connection Found!");
	 }
	 
	 @OnClose
	 public void onClose() {
		 System.out.println("Connection Ended");
	 }
	 
	 @OnMessage
	 public String onMessage(String message) {
		 System.out.println(message);
		 RequestHandler.handleRequest(message);
		 return "blah";
	 }
	 
	 @OnError
	 public void onError(Throwable e) {
		 e.printStackTrace();
	 }
}
