package org.ibp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
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
				.antMatchers("/")
				.and()
				.authorizeRequests()
				.anyRequest()
				.access("#oauth2.clientHasRole('USER') or #oauth2.hasScope('read')");
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
		
		@Autowired
		private TokenStore tokenStore;
		
		@Autowired
		private AuthorizationServerTokenServices tokenServices;

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
			endpoints.tokenStore(tokenStore);
			endpoints.tokenServices(tokenServices);
			endpoints.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
		}
		
		@Override
	    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
	        oauthServer.allowFormAuthenticationForClients();
	    }

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		 	clients.inMemory()
		        .withClient("my-trusted-client")
		            .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
		            .authorities("USER", "ADMIN")
		            .scopes("read", "write")
		            .resourceIds("bmsapi")
		            .accessTokenValiditySeconds(60)
 		    .and()
		        .withClient("my-client-with-registered-redirect")
		            .authorizedGrantTypes("authorization_code")
		            .authorities("USER")
		            .scopes("read")
		            .resourceIds("bmsapi")
		            .redirectUris("http://anywhere?key=value")
 		    .and()
		        .withClient("my-client-with-secret")
		            .authorizedGrantTypes("client_credentials", "password")
		            .authorities("USER")
		            .scopes("read")
		            .resourceIds("bmsapi")	
		            .secret("secret");
		}
		
		@Bean
		public TokenStore tokenStore() {
			return new InMemoryTokenStore();
		}
		
		@Bean
		public AuthorizationServerTokenServices tokenServices() {
			return new AuthorizationServerTokenServices() {
				
				@Override
				public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
					OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
					OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
					return OAuth2Config.this.tokenStore.getAccessToken(authentication);
				}
				
				@Override
				public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
					return OAuth2Config.this.tokenStore.getAccessToken(authentication);
				}
				
				@Override
				public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
					return OAuth2Config.this.tokenStore.getAccessToken(authentication);
				}
			};
		}
	}
}
