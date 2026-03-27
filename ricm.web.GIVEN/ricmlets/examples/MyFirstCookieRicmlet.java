package examples;

import java.io.IOException;
import java.io.PrintStream;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;

public class MyFirstCookieRicmlet implements HttpRicmlet {

    @Override
    public void doGet(HttpRicmletRequest req, HttpRicmletResponse resp) throws IOException {
        String cookieName = "MyFirstCookie";
        String val = req.getCookie(cookieName);

        int count;
        String message;

        if (val == null) {
            count = 1;
            message = "First Access";
        } else {
            count = Integer.parseInt(val) + 1;
            message = "Visite nombre " + count + " - via cookie";
        }


        resp.setCookie(cookieName, String.valueOf(count));
        //create header
        resp.setReplyOk();
        resp.setContentType("text/html");
        PrintStream ps = resp.beginBody();

        ps.println("<HTML><HEAD><TITLE>Test Cookies</TITLE></HEAD>");
        ps.println("<BODY>");
        ps.println("<H2>Gestion de Cookies</H2>");
        ps.println("<P>" + message + "</P>");
        ps.println("</BODY></HTML>");
    }
}