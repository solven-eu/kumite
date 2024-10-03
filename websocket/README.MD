This module tried enabling WebSocket to play Kumite games.

Though, it seems :

- WebFlux + WebSocket are OK with SpringBoot
- WebFlux + WebSocket_STOMP are KO with SpringBoot

We typically get an issue around `DelegatingWebSocketMessageBrokerConfiguration` which depends on `org.springframework.web.servlet.HandlerMapping`, which is `servlet`-oriented, hence not `reactive`-compatible.

https://msmechatronics.medium.com/websocket-wizardry-spring-webflux-edition-ca9d30346465 : propose a way to integrate WebSocket + WebFlux, by hand.

https://www.baeldung.com/spring-5-reactive-websockets : suggests a simpler way to go with WebSocket+WebFlux, by leaving STOMP aside.

https://github.com/kapilraina/spring-reactive-websocket-chat : another simple app with WebFlux + WebSocket.

https://stackoverflow.com/questions/55550768/why-does-stomp-functionality-in-spring-websocket-depend-on-spring-mvc : Webflux + WebSocket + STOMP is KO

https://github.com/markkoszykowski/Spring-WebFlux-STOMP-Handler/ : Some initiative offering STOMP in a Reactive environment.

https://github.com/luis-moral/sample-webflux-websocket-netty: Another example without STOMP.

https://github.com/spring-projects/spring-boot/issues/12932: STOMP with Webflux is KO.

https://developer.okta.com/blog/2018/09/25/spring-webflux-websockets-react : An example without Stomp