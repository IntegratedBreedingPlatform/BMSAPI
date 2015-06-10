package org.ibp.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
public class OAuthConfig {
	
	@Configuration
	@EnableResourceServer
	protected static class ResourceServer extends ResourceServerConfigurerAdapter {

		@Override
		public void configure(HttpSecurity http) throws Exception {
			
			http.requestMatchers()
				.and()
				.authorizeRequests()
					.antMatchers(HttpMethod.GET, "/ontology/**").access("#oauth2.hasScope('read') or hasRole('USER')")
					.antMatchers(HttpMethod.PUT, "/ontology/**").access("#oauth2.hasScope('write') or hasRole('ADMIN')")
					.antMatchers(HttpMethod.POST, "/ontology/**").access("#oauth2.hasScope('write') or hasRole('ADMIN')")
					.antMatchers(HttpMethod.DELETE, "/ontology/**").access("#oauth2.hasScope('write') or hasRole('ADMIN')");
		}

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
			resources.resourceId("bmsapi");
		}
	}

	@Configuration
	@EnableAuthorizationServer
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;
		
		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
			endpoints.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
		}
		
		@Override
	    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
	        oauthServer.allowFormAuthenticationForClients();
	    }

		/**
		 * Setup registered clients of the API here. In memory for demo purposes only.
		 * 
		 * <p> Grant types:
		 * 
		 * <ul>
		 * <li><strong>Authorization code</strong> (authorization_code) This grant type is most appropriate for server-side web
		 * applications. After the resource owner has authorized access to their data, they are redirected back to the web application with
		 * an authorization code as a query parameter in the URL. This code must be exchanged for an access token by the client application.
		 * This exchange is done server-to-server and requires both the client_id and client_secret, preventing even the resource owner
		 * from obtaining the access token. This grant type also allows for long-lived access to an API by using refresh tokens.
		 * 
		 * <li><strong>Implicit grant</strong> (implicit) Used for browser-based client-side applications. The implicit grant is the most
		 * simplistic of all flows, and is optimized for clientside web applications running in a browser. The resource owner grants access
		 * to the application, and a new access token is immediately minted and passed back to the application using a #hash fragment in the
		 * URL. The application can immediately extract the access token from the hash fragment (using JavaScript) and make API requests.
		 * This grant type does not require the intermediary “authorization code,” but it also doesn’t make available refresh tokens for
		 * long-lived access.
		 * 
		 * <li><strong>Resource owner password-based grant</strong> (password) This grant type enables a resource owner’s username and
		 * password to be exchanged for an OAuth access token. It is used for only highly-trusted clients, such as a mobile application
		 * written by the API provider. While the user’s password is still exposed to the client, it does not need to be stored on the
		 * device. After the initial authentication, only the OAuth token needs to be stored. Because the password is not stored, the user
		 * can revoke access to the app without changing the password, and the token is scoped to a limited set of data, so this grant type
		 * still provides enhanced security over traditional username/password authentication.
		 * 
		 * <li><strong>Client credentials</strong> (client_credentials) The client credentials grant type allows an application to obtain
		 * an access token for resources owned by the client or when authorization has been “previously arranged with an authorization
		 * server.” This grant type is appropriate for applications that need to access APIs, such as storage services or databases, on
		 * behalf of themselves rather than on behalf of a specific user.
		 * </ul>
		 * 
		 */
		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		 	clients.inMemory()
		        .withClient("bms-official-app")
		            .authorizedGrantTypes("password", "client_credentials", "authorization_code", "refresh_token", "implicit")
		            .authorities(Role.ADMIN.toString(), Role.USER.toString())
		            .scopes("read", "write")
		            .resourceIds("bmsapi")
		            .secret("topsecret")
		            .accessTokenValiditySeconds(3600)
		            .redirectUris("http://localhost:19080/bmsapi")
		        .and()
		        .withClient("third-party-client-app")
		            .authorizedGrantTypes("client_credentials")
		            .authorities(Role.USER.toString())
		            .scopes("read")
		            .resourceIds("bmsapi")	
		            .secret("secretsecret")
		            .accessTokenValiditySeconds(3600)
		            .redirectUris("http://localhost:19080/bmsapi");
		}
		
		@Bean
		public TokenStore tokenStore() {
			return new InMemoryTokenStore();
		}
	}
}
