package httpserver.itf;

import httpserver.itf.impl.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
header with cookies
GET /ricmlets/examples/HelloRicmlet HTTP/1.1
Cookie: myFirstCookie=123;mySecondCookie=Hello
Cookie: anotherCookie=45678
EMPTY LINE
* */

public class HttpRicmletRequestImpl extends HttpRicmletRequest {
    private String m_className;
    private Map<String, String> m_args = new HashMap<>();
    private Map<String, String> m_cookies = new HashMap<>();
    private HttpSession m_session;
    /** If non-null, response must emit Set-Cookie for a newly created session. */
    private String m_newSessionCookieValue;

    public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
        super(hs, method, ressname, br);
        parseResources(ressname);
        parseHeaders(br);
    }

    private void parseHeaders(BufferedReader br) throws IOException {
        String line;
        //while we don't find the empty line we need to search for cookies
        while ((line = br.readLine()) != null && !line.equals("")) {
            if (line.startsWith("Cookie:")) {
                // removes "Cookie:"
                String content = line.substring(8);

                //parse different cookies
                String[] parts = content.split(";");
                for (String part : parts) {
                    String[] kv = part.split("=");
                    if (kv.length == 2) {
                        m_cookies.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }
        }
    }

    private void parseResources(String ressname) {
        //removing ricmlet (hardcoded)
        String path = ressname.substring(10);

        //searching for arguments
        int queryIndex = path.indexOf('?');
        String classPath;
        if (queryIndex != -1) {
            classPath = path.substring(0, queryIndex);
            String queryString = path.substring(queryIndex + 1);
            parseArgs(queryString);
        } else {
            classPath = path;
        }
        m_className = classPath.replace('/', '.');
    }

    private void parseArgs(String queryString) {
        //splitting arguments
        //e.g name=Bob&surname=Marley -> ["name=Bob", "surname=Marley"]
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                m_args.put(kv[0], kv[1]); // hashmap: {"name" : "Bob", "surname" : "Marley"}
            }
        }
    }

    @Override
    public String getArg(String name) {
        return m_args.get(name); //if no args, return null
    }

    @Override
    public void process(HttpResponse resp) throws Exception {
        try {
            //confirm singleton -> asks the server for the object that corresponds this class name and verify if any instance exists
            HttpRicmlet ricmlet = m_hs.getInstance(m_className);
            ricmlet.doGet(this, (HttpRicmletResponse) resp);

        } catch (ClassNotFoundException e) { //problems to find class
            resp.setReplyError(404, "Ricmlet not found");
        } catch (Exception e) {
            resp.setReplyError(500, "Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    public HttpSession getSession() {
        if (m_session != null) {
            return m_session;
        }
        String sid = getCookie(HttpServer.SESSION_COOKIE_NAME);
        if (sid != null) {
            HttpSession existing = m_hs.getSessionById(sid);
            if (existing != null) {
                //renovate timer
                m_hs.touchSession(existing);
                m_session = existing;
                return m_session;
            }
        }
        //session not found, create a new one
        m_session = m_hs.createSession();
        m_newSessionCookieValue = m_session.getId(); //send session id to browser
        return m_session;
    }

    /**
     * New session id to send as Set-Cookie, or null if the session was already known.
     */
    public String getNewSessionCookieValue() {
        return m_newSessionCookieValue;
    }

    @Override
    public String getCookie(String name) {
        return m_cookies.get(name);
    }
}