package examples;


import java.io.IOException;
import java.io.PrintStream;

import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;

public class CookieRicmlet implements httpserver.itf.HttpRicmlet{
	boolean f = true;

	@Override
	public void doGet(HttpRicmletRequest req,  HttpRicmletResponse resp) throws IOException {

		String myFirstCookie = req.getCookie("MyFirstCookie");
		int nextValue = 1;

		if (myFirstCookie != null) {
			try {
				nextValue = Integer.parseInt(myFirstCookie) + 1;
			} catch (NumberFormatException e) {
				nextValue = 1;
			}
		}
		resp.setCookie("MyFirstCookie", String.valueOf(nextValue));
	
		resp.setReplyOk();
		resp.setContentType("text/html");
		PrintStream ps = resp.beginBody();
		ps.println("<HTML><HEAD><TITLE> Ricmlet processing </TITLE></HEAD>");
		ps.print("<BODY><H4> MyFirstCookie " + req.getCookie("MyFirstCookie") + "<br>");
		ps.println("</H4></BODY></HTML>");
		ps.println();
}
}
