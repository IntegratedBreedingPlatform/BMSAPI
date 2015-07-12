
package org.ibp.api.rest;

import javax.servlet.http.HttpServletRequest;

import org.ibp.api.rest.germplasm.GermplasmListResource;
import org.ibp.api.rest.germplasm.GermplasmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResourceURLLinkProvider {

	@Autowired
	private HttpServletRequest httpServletRequest;

	public String getBaseUrl() {
		if (this.httpServletRequest == null) {
			return "";
		}
		return String.format("%s://%s:%s%s", this.httpServletRequest.getScheme(), this.httpServletRequest.getServerName(),
				this.httpServletRequest.getServerPort(), this.httpServletRequest.getContextPath());
	}

	public String getGermplasmListDetailsUrl(Integer germplasmListId, String cropname) {
		return String.format("%s%s/%s/%s", this.getBaseUrl(), GermplasmListResource.URL, cropname, germplasmListId);
	}

	public String getGermplasmByIDUrl(String germplasmId, String cropname) {
		return String.format("%s%s/%s/%s", this.getBaseUrl(), GermplasmResource.URL, cropname, germplasmId);
	}
}
