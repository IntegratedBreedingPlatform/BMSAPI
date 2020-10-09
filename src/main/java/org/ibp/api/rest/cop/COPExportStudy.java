package org.ibp.api.rest.cop;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class COPExportStudy {

	@AutoProperty
	public static class Studydata {
		private Integer sid;
		private Integer listid;
		private List<String> germplasmids;
		private String crop;

		public Studydata(){

		}

		public Integer getSid() {
			return sid;
		}

		public void setSid(Integer sid) {
			this.sid = sid;
		}

		public Integer getListid() {
			return listid;
		}

		public void setListid(Integer listid) {
			this.listid = listid;
		}

		public List<String> getGermplasmids() {
			return germplasmids;
		}

		public void setGermplasmids(List<String> germplasmids) {
			this.germplasmids = germplasmids;
		}

		public String getCrop() {
			return crop;
		}

		public void setCrop(String crop) {
			this.crop = crop;
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

	private String source;
	private Integer breederid;
	private String breedermail;
	private String breedername;
	private Studydata studydata;
	private String tenantid;
	private String pollination_type;
	private Integer generations;
	private Boolean skip_cache;
	private String last_modified_date;

	public COPExportStudy(){

	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getBreederid() {
		return breederid;
	}

	public void setBreederid(Integer breederid) {
		this.breederid = breederid;
	}

	public String getBreedermail() {
		return breedermail;
	}

	public void setBreedermail(String breedermail) {
		this.breedermail = breedermail;
	}

	public String getBreedername() {
		return breedername;
	}

	public void setBreedername(String breedername) {
		this.breedername = breedername;
	}

	public Studydata getStudydata() {
		return studydata;
	}

	public void setStudydata(Studydata studydata) {
		this.studydata = studydata;
	}

	public String getTenantid() {
		return tenantid;
	}

	public COPExportStudy setTenantid(String tenantid) {
		this.tenantid = tenantid;
		return this;
	}

	public String getPollination_type() {
		return pollination_type;
	}

	public COPExportStudy setPollination_type(String pollination_type) {
		this.pollination_type = pollination_type;
		return this;
	}

	public Integer getGenerations() {
		return generations;
	}

	public COPExportStudy setGenerations(Integer generations) {
		this.generations = generations;
		return this;
	}

	public Boolean getSkip_cache() {
		return skip_cache;
	}

	public COPExportStudy setSkip_cache(Boolean skip_cache) {
		this.skip_cache = skip_cache;
		return this;
	}

	public String getLast_modified_date() {
		return last_modified_date;
	}

	public COPExportStudy setLast_modified_date(String last_modified_date) {
		this.last_modified_date = last_modified_date;
		return this;
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
