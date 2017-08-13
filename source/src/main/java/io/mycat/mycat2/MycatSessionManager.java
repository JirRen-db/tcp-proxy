package io.mycat.mycat2;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.mycat2.net.DefaultSQLHandler;
import io.mycat.mycat2.net.MySQLClientAuthHandler;
import io.mycat.proxy.BufferPool;
import io.mycat.proxy.Session;
import io.mycat.proxy.SessionManager;

/**
 * Mycat 2.0 Session Manager
 * 
 * @author wuzhihui
 *
 */
public class MycatSessionManager implements SessionManager<MySQLSession> {
	protected static Logger logger = LoggerFactory.getLogger(MycatSessionManager.class);
	private ArrayList<MySQLSession> allSessions = new ArrayList<MySQLSession>();

	@Override
	public MySQLSession createSession(BufferPool bufPool, Selector nioSelector, SocketChannel frontChannel,
			boolean isAcceptCon) throws IOException {

		logger.info("MySQL client connected  ." + frontChannel);

		MySQLSession session = new MySQLSession(bufPool, nioSelector, frontChannel);
		// 默认为透传命令模式
		session.curSQLCommand = DefaultSQLHandler.defaultSQLCmd;
		// 第一个IO处理器为Client Authorware
		session.setCurProxyHandler(MySQLClientAuthHandler.INSTANCE);
		// 向MySQL Client发送认证报文
		session.sendAuthPackge();
		session.setSessionManager(this);
		allSessions.add(session);
		return session;
	}

	@Override
	public Collection<MySQLSession> getAllSessions() {
		return this.allSessions;
	}

	
	public void removeSession(Session session) {
		this.allSessions.remove(session);

	}

}
