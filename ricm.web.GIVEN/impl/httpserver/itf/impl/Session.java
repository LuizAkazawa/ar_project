package httpserver.itf.impl;

import java.util.HashMap;
import java.util.Map;

import httpserver.itf.HttpSession;

/**
 * Server-side HTTP session: a named attribute store keyed by a unique id.
 */
public class Session implements HttpSession {

	private final String m_id;
	private final Map<String, Object> m_values = new HashMap<>();
	//cleanup thread reads this, so has to be volatile
	private volatile long m_lastAccessTime;

	public Session(String id) {
		m_id = id;
		touch();
	}

	void touch() {
		m_lastAccessTime = System.currentTimeMillis();
	}

	long getLastAccessTime() {
		return m_lastAccessTime;
	}

	@Override
	public String getId() {
		return m_id;
	}

	//thread safety pour le map m_values
	@Override
	public synchronized Object getValue(String key) {
		return m_values.get(key);
	}

	@Override
	public synchronized void setValue(String key, Object value) {
		m_values.put(key, value);
	}
}
