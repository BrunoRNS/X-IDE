package org.xide.server;
public interface MessageRepository { void save(String requestId, String payload) throws Exception; }
