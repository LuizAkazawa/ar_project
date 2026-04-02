package httpserver.itf.impl;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import httpserver.itf.*;

import java.util.HashMap;


/**
 * Basic HTTP Server Implementation 
 * 
 * Only manages static requests
 * The url for a static ressource is of the form: "http//host:port/<path>/<ressource name>"
 * For example, try accessing the following urls from your brower:
 *    http://localhost:<port>/
 *    http://localhost:<port>/voile.jpg
 *    ...
 */
public class HttpServer {

	/** Cookie name used to carry the server-side session identifier. */
	public static final String SESSION_COOKIE_NAME = "session-id";

	/** Inactivity period after which a session is discarded (no request referencing it). */
	private static final long SESSION_TIMEOUT_MS = 30L * 60L * 1000L;

	private int m_port;
	private File m_folder;  // default folder for accessing static resources (files)
	private ServerSocket m_ssoc;
	private HashMap<String, HttpRicmlet> m_ricmlets = new HashMap<>();
	private final ConcurrentHashMap<String, Session> m_sessions = new ConcurrentHashMap<>();

	protected HttpServer(int port, String folderName) {
		m_port = port;
		if (!folderName.endsWith(File.separator)) 
			folderName = folderName + File.separator;
		m_folder = new File(folderName);
		try {
			m_ssoc=new ServerSocket(m_port);
			System.out.println("HttpServer started on port " + m_port);
		} catch (IOException e) {
			System.out.println("HttpServer Exception:" + e );
			System.exit(1);
		}
		startSessionCleanupThread();
	}

	private void startSessionCleanupThread() {
		Thread t = new Thread(() -> {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(60_000L);
				} catch (InterruptedException e) {
					break;
				}
				removeExpiredSessions();
			}
		}, "http-session-cleanup");
		t.setDaemon(true);
		t.start();
	}

	private boolean isExpired(Session s) {
		return System.currentTimeMillis() - s.getLastAccessTime() > SESSION_TIMEOUT_MS;
	}

	/**
	 * Removes sessions that have not been touched within the timeout window.
	 */
	public void removeExpiredSessions() {
		long now = System.currentTimeMillis();
		m_sessions.entrySet().removeIf(e -> now - e.getValue().getLastAccessTime() > SESSION_TIMEOUT_MS);
	}

	/**
	 * Returns the live session for this id, or null if unknown or expired.
	 */
	public HttpSession getSessionById(String id) {
		if (id == null) {
			return null;
		}
		Session s = m_sessions.get(id);
		if (s == null) {
			return null;
		}
		if (isExpired(s)) {
			m_sessions.remove(id, s);
			return null;
		}
		return s;
	}

	public void touchSession(HttpSession session) {
		((Session) session).touch();
	}

	public HttpSession createSession() {
		String id = UUID.randomUUID().toString();
		Session s = new Session(id);
		m_sessions.put(id, s);
		return s;
	}
	
	public File getFolder() {
		return m_folder;
	}
	
	

	public HttpRicmlet getInstance(String clsname)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException, 
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		//Check if we can get an instance of "clsname"
		HttpRicmlet instance = m_ricmlets.get(clsname);
		if (instance == null) {
			Class<?> c = Class.forName(clsname);
			instance = (HttpRicmlet) c.getDeclaredConstructor().newInstance();
			m_ricmlets.put(clsname, instance);
		}

		return instance;
		//throw new Error("No Support for Ricmlets");
	}




	/*
	 * Reads a request on the given input stream and returns the corresponding HttpRequest object
	 */
	public HttpRequest getRequest(BufferedReader br) throws IOException {
		HttpRequest request = null;
		
		String startline = br.readLine();
		StringTokenizer parseline = new StringTokenizer(startline);
		String method = parseline.nextToken().toUpperCase(); 
		String ressname = parseline.nextToken();

		if (method.equals("GET")) {
			if (ressname.startsWith("/ricmlets")) {
				request = new HttpRicmletRequestImpl(this, method, ressname, br);
			} else{
				request = new HttpStaticRequest(this, method, ressname);
			}
		} else
			request = new UnknownRequest(this, method, ressname);

		return request;
	}


	/*
	 * Returns an HttpResponse object associated to the given HttpRequest object
	 */
	public HttpResponse getResponse(HttpRequest req, PrintStream ps) {
		if (req instanceof HttpRicmletRequest) {
			return new HttpRicmletResponseImpl(this, req, ps);
		}
		return new HttpResponseImpl(this, req, ps);
	}


	/*
	 * Server main loop
	 */
	protected void loop() {
		try {
			while (true) {
				Socket soc = m_ssoc.accept();
				(new HttpWorker(this, soc)).start();
			}
		} catch (IOException e) {
			System.out.println("HttpServer Exception, skipping request");
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args) {
		int port = 0;
		if (args.length != 2) {
			System.out.println("Usage: java Server <port-number> <file folder>");
		} else {
			port = Integer.parseInt(args[0]);
			String foldername = args[1];
			HttpServer hs = new HttpServer(port, foldername);
			hs.loop();
		}
	}

}

