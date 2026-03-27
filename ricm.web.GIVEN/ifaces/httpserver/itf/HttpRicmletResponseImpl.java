package httpserver.itf;

import httpserver.itf.impl.HttpServer;
import java.io.IOException;
import java.io.PrintStream;

public class HttpRicmletResponseImpl implements HttpRicmletResponse {
    private HttpServer m_hs;
    private PrintStream m_ps;
    private HttpRequest m_req;

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

    @Override
    public PrintStream beginBody() throws IOException {
        m_ps.print("\r\n");//empty line
        m_ps.flush();
        return m_ps;
    }

    @Override
    public void setCookie(String name, String value) {
    }
}