This module enables WebSocket to play Kumite games.

## Motivation

Even simpler games may need to wait for other players. Instead of polling board states, we prefer to be notified for the game to be changed. It enables:

- lower bandwidth as less redundant information would transit on the network
- improved latency as the browser is notified by notification that something changed, instead of waiting of the delay between polls.

## WebSocket with or without Stomp

Stomp is a protocol over WebSocket. It can be compared to TCP and HTTP: HTTP provides its set of words (`GET`, `POST`, `200`, `401`).

### SpringBoot-WebFlux with Stomp: KO

While SpringBoot WebSocket supports WebSocket, SpringBoot-WebFlux does not support Stomp:

- https://stackoverflow.com/questions/55550768/why-does-stomp-functionality-in-spring-websocket-depend-on-spring-mvc : Webflux + WebSocket + STOMP is KO
- https://github.com/spring-projects/spring-boot/issues/12932: STOMP with Webflux is KO.
- https://github.com/markkoszykowski/Spring-WebFlux-STOMP-Handler/ : Some initiative offering STOMP in a Reactive environment.


We typically get an issue around `DelegatingWebSocketMessageBrokerConfiguration` which depends on `org.springframework.web.servlet.HandlerMapping`, which is `servlet`-oriented, hence not `reactive`-compatible.

### SpringBoot-WebFlux without Stomp: OK

Hence, we decided to rely on a simpler WebSocket protocol.

Some references (as WebSocket remains not a very common technology):

- https://developer.okta.com/blog/2018/09/25/spring-webflux-websockets-react : An example without Stomp
- https://msmechatronics.medium.com/websocket-wizardry-spring-webflux-edition-ca9d30346465 : propose a way to integrate WebSocket + WebFlux, by hand.
- https://www.baeldung.com/spring-5-reactive-websockets : suggests a simpler way to go with WebSocket+WebFlux, by leaving STOMP aside.
- https://github.com/kapilraina/spring-reactive-websocket-chat : another simple app with WebFlux + WebSocket.
- https://github.com/luis-moral/sample-webflux-websocket-netty: Another example without STOMP.
