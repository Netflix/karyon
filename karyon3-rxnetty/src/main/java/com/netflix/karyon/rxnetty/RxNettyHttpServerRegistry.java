package com.netflix.karyon.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map;

import javax.inject.Provider;

import com.netflix.karyon.http.ServerConfig;

/**
 * Repository for multiple RxNetty HTTP servers.  This repo is used mostly
 * for bootstrapping an application where each server needs to be started
 * as well for the rxnetty admin page.
 * 
 * @author elandau
 *
 */
public interface RxNettyHttpServerRegistry {
    /**
     * Return a mapping of server name to Provider to the actual server.
     * The key maps to the classname of the qualifier associated
     * with the server binding.
     * 
     * @return
     */
    Map<String, Provider<HttpServer<ByteBuf, ByteBuf>>> getServers();

    /**
     * Return a mapping of ServerConfig to the actual server config
     * where the key maps to the classname of the qualifier associated
     * with the configuration binding. 
     * @return
     */
    Map<String, ServerConfig> getServerConfigs();
}
