
package org.ibp.api.rest;

import javax.servlet.http.HttpServletRequest;

import org.ibp.api.rest.germplasm.GermplasmListResource;
import org.ibp.api.rest.germplasm.GermplasmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// FIXME replace with http://en.wikipedia.org/wiki/HATEOAS
@Component
public class ResourceURLLinkProvider {

	@Autowired
	private HttpServletRequest httpServletRequest;

	public String getBaseUrl() {
		if (httpServletRequest == null) {
			return "";
		}
		return String.format("%s://%s:%s%s", httpServletRequest.getScheme(), httpServletRequest.getServerName(), httpServletRequest.getServerPort(), httpServletRequest.getContextPath());
	}

	public String getGermplasmListDetailsUrl(Integer germplasmListId, String cropname) {
		return String.format("%s%s/%s/%s", getBaseUrl(), GermplasmListResource.URL, cropname, germplasmListId);
	}
	
	public String getGermplasmByIDUrl(String germplasmId, String cropname) {
		return String.format("%s%s/%s/%s", getBaseUrl(), GermplasmResource.URL, cropname, germplasmId);
	}
}
