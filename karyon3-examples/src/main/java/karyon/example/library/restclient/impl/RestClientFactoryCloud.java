package karyon.example.library.restclient.impl;

import javax.inject.Singleton;

import karyon.example.library.restclient.api.RestClient;
import karyon.example.library.restclient.api.RestClientFactory;

@Singleton
public class RestClientFactoryCloud implements RestClientFactory {
    @Override
    public RestClient getClientForService(String serviceName) {
        return new RestClient() {
            @Override
            public String get(String url) {
                return url + "-Cloud";
            }
        };
    }
}
