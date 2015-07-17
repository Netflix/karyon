package com.netflix.karyon.rxnetty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AdminView {
    String render(Map<String, List<String>> queryParameters) throws IOException;
}
