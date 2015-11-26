package karyon.example.library.restclient.api;

public interface RestClientFactory {
    RestClient getClientForService(String serviceName);
}
