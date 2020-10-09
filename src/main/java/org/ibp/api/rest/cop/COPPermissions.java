package org.ibp.api.rest.cop;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;

@AutoProperty
public class COPPermissions {

	@AutoProperty
	public static class Api {

		private String api_name;
		private String url;
		private String api_token;

		public Api(){

		}

		public String getApi_name() {
			return api_name;
		}

		public void setApi_name(String api_name) {
			this.api_name = api_name;
		}

		public String getApi_token() {
			return api_token;
		}

		public void setApi_token(String api_token) {
			this.api_token = api_token;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}


		@Override
		public int hashCode() {
			return Pojomatic.hashCode(this);
		}

		@Override
		public String toString() {
			return Pojomatic.toString(this);
		}

		@Override
		public boolean equals(final Object o) {
			return Pojomatic.equals(this, o);
		}
	}

	private List<Api> apis = new ArrayList<>();
	private String token;
	private String platform_url;

	public COPPermissions(){

	}

	public List<Api> getApis() {
		return apis;
	}

	public void setApis(List<Api> apis) {
		this.apis = apis;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPlatform_url() {
		return platform_url;
	}

	public void setPlatform_url(String platform_url) {
		this.platform_url = platform_url;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
