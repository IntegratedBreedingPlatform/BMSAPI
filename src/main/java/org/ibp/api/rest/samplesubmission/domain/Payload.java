package org.ibp.api.rest.samplesubmission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
public class Payload<T extends GOBiiGenericData> {

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

	private T data;

	private Header header;

	public T getData() {
		return data;
	}

	public LinkCollection getLinkCollection() {
		return linkCollection;
	}

	public void setLinkCollection(final LinkCollection linkCollection) {
		this.linkCollection = linkCollection;
	}

	public void setData(final T data) {
		this.data = data;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(final Header header) {
		this.header = header;
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
