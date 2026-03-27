package httpserver.itf.impl;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";

	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
	}

	public void process(HttpResponse resp) throws Exception {
		String path = m_ressname;

		if (path == null || path.equals("/")) {
			path = "/FILES/" + DEFAULT_FILE;
		} else if (path.endsWith("/")) {
			path = path + DEFAULT_FILE;
		} else {
			// Remove leading slash to verify in filesystem (example: 8080/FILES instead of 8080/FILES/)
			String fsPath = path.startsWith("/") ? path.substring(1) : path;
			File f = new File(fsPath);
			if (f.isDirectory()) {
				path = path + "/" + DEFAULT_FILE;
			}
		}

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		File file = new File(m_hs.getFolder(), path);

		if (!file.exists() || !file.isFile()) {
			resp.setReplyError(404, "Not Found");
			return;
		}

		resp.setReplyOk();
		resp.setContentType(HttpRequest.getContentType(file.getName()));
		resp.setContentLength((int) file.length());

		PrintStream ps = resp.beginBody();

		FileInputStream fis = new FileInputStream(file);
		try {
			byte[] buffer = new byte[8192];
			int n;
			while ((n = fis.read(buffer)) != -1) {
				ps.write(buffer, 0, n);
			}
			ps.flush();
		} finally {
			fis.close();
		}
	}

}
