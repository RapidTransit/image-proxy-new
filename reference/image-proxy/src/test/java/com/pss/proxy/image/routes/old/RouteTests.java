package com.pss.proxy.image.routes.old;//package com.pss.proxy.image.routes.old;
//import com.pss.proxy.image.config.properties.ProxyConfig;
//import com.pss.proxy.image.service.QueryParamService;
//import io.vertx.core.MultiMap;
//import io.vertx.core.http.*;
//import io.vertx.ext.web.RoutingContext;
//import io.vertx.junit5.VertxExtension;
//import io.vertx.junit5.VertxTestContext;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith({VertxExtension.class, MockitoExtension.class})
//public class RouteTests {
//
//    @Mock RoutingContext event;
//
//    @Mock HttpServerRequest request;
//
//    @Mock HttpServerResponse response;
//
//    @Mock ProxyConfig config;
//
//    @Mock HttpClient client;
//
//    @Mock HttpClientResponse clientResponse;
//
//    @Mock QueryParamService queryParamService;
//
//    @InjectMocks
//    JwtRouteHandler jwtRoute;
//
//    @Test
//    @Tag("200")
//    @DisplayName("Test Success 200")
//    public void testParameterizedRouteWith200(VertxTestContext context){
//        when(clientResponse.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//        when(clientResponse.statusCode()).thenReturn(200);
//        when(response.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//        jwtRoute.handleResponse(event, request, response, "/test-path.png", clientResponse);
//        verify(clientResponse).pipeTo(response);
//        context.completeNow();
//    }
//
//
//    @Tag("403")
//    @DisplayName("Test Error Codes")
//    @ParameterizedTest
//    @ValueSource(ints = {403, 404, 500})
//    public void testParameterizedRouteWithError(int code, VertxTestContext context){
//        when(config.noImage()).thenReturn("/images/no-image.png");
//        when(request.params()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//
//        when(clientResponse.statusCode()).thenReturn(code);
//        jwtRoute.handleResponse(event, request, response, "/test-path.png", clientResponse);
//        verify(event).reroute(anyString());
//        context.completeNow();
//    }
//}
