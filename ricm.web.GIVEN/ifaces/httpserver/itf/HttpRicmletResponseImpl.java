package httpserver.itf;

import httpserver.itf.impl.HttpServer;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRicmletResponseImpl implements HttpRicmletResponse {
    private HttpServer m_hs;
    private PrintStream m_ps;
    private HttpRequest m_req;
    private Map<String, String> m_cookiesToSet = new HashMap<>();

    public HttpRicmletResponseImpl(HttpServer hs, HttpRequest req, PrintStream ps) {
        m_hs = hs;
        m_req = req;
        m_ps = ps;
    }

    @Override
    public void setReplyOk() throws IOException {
        m_ps.print("HTTP/1.1 200 OK\r\n");
    }

    @Override
    public void setReplyError(int codeRet, String msg) throws IOException {
        // error line example:  HTTP/1.1 404 Not Found
        m_ps.print("HTTP/1.1 " + codeRet + " " + msg + "\r\n");
    }

    @Override
    public void setContentLength(int length) throws IOException {
        m_ps.print("Content-Length: " + length + "\r\n");
    }

    @Override
    public void setContentType(String type) throws IOException {
        m_ps.print("Content-Type: " + type + "\r\n");
    }

    /*EXAMPLE
    HTTP/1.1 200 OK
    Server: Ricm4HttpServer
    Set-Cookie: myFirstCookie=123;mySecondCookie=Hello
    Set-Cookie: anotherCookie=45678
    ...
    */
    @Override
    public PrintStream beginBody() throws IOException {
        if (m_req instanceof HttpRicmletRequestImpl) {
            String v = ((HttpRicmletRequestImpl) m_req).getNewSessionCookieValue();
            if (v != null) {
                m_cookiesToSet.put(HttpServer.SESSION_COOKIE_NAME, v);
            }
        }
        for (Map.Entry<String, String> entry : m_cookiesToSet.entrySet()) {
            m_ps.print("Set-Cookie: " + entry.getKey() + "=" + entry.getValue() + "\r\n");
        }

        m_ps.print("\r\n");//empty line
        m_ps.flush();
        return m_ps;
    }

    @Override
    public void setCookie(String name, String value) {
        m_cookiesToSet.put(name, value);
    }
}