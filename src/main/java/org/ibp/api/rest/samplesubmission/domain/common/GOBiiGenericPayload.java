package org.ibp.api.rest.samplesubmission.domain.common;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
public abstract class GOBiiGenericPayload {

	@AutoProperty
	class LinkCollection {

		private List linksPerDataItem;

		private List exploreLinksPerDataItem;

		public List getLinksPerDataItem() {
			return linksPerDataItem;
		}

		public void setLinksPerDataItem(final List linksPerDataItem) {
			this.linksPerDataItem = linksPerDataItem;
		}

		public List getExploreLinksPerDataItem() {
			return exploreLinksPerDataItem;
		}

		public void setExploreLinksPerDataItem(final List exploreLinksPerDataItem) {
			this.exploreLinksPerDataItem = exploreLinksPerDataItem;
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
		public boolean equals(Object o) {
			return Pojomatic.equals(this, o);
		}

	}

	private LinkCollection linkCollection;

	public LinkCollection getLinkCollection() {
		return linkCollection;
	}

	public void setLinkCollection(final LinkCollection linkCollection) {
		this.linkCollection = linkCollection;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
