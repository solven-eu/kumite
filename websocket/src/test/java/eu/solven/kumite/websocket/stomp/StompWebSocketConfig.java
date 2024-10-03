package eu.solven.kumite.websocket.stomp;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// https://docs.spring.io/spring-framework/reference/web/websocket.html
// https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html
@Configuration
// `@EnableWebSocketMessageBroker` is valid only in WebMvc, not Reactive
@EnableWebSocketMessageBroker
@AllArgsConstructor
@Slf4j
@Deprecated(since = "WebSocketMessageBrokerConfigurer would be interpreter by @EnableWebSocketMessageBroker")
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	final ApplicationContext appContext;

	// https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-sameorigin-disable
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
	}

	// https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-sameorigin-disable
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// AuthorizationManager<Message<?>> myAuthorizationRules = AuthenticatedAuthorizationManager.authenticated();
		// AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(myAuthorizationRules);
		// AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(this.appContext);
		// authz.setAuthorizationEventPublisher(publisher);
		// registration.interceptors(new SecurityContextChannelInterceptor(), authz);

		// https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html
		// https://stackoverflow.com/questions/30887788/json-web-token-jwt-with-spring-based-sockjs-stomp-web-socket
		// https://medium.com/@poojithairosha/spring-boot-3-authenticate-websocket-connections-with-jwt-tokens-2b4ff60532b6
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				log.info("Headers: {}", accessor);

				assert accessor != null;
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {

					String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
					assert authorizationHeader != null;
					String token = authorizationHeader.substring("Bearer ".length());

					String username = ""; // jwtTokenUtil.getUsername(token);
					UserDetails userDetails = null; // userDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

					accessor.setUser(usernamePasswordAuthenticationToken);
				}

				return message;
			}

		});
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");

		// Message received with one of those below destinationPrefixes will be automatically routed to controllers
		// @MessageMapping
		config.setApplicationDestinationPrefixes("/kumite");
	}

	// `registerStompEndpoints` is valid only in WebMvc, not Reactive
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/gs-guide-websocket");
	}

}