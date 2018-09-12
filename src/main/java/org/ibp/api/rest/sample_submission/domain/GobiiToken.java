package org.ibp.api.rest.sample_submission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
public class GOBiiToken {

	private String userName;

	private String password;

	private String token;

	private String gobiiCropType;

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public String getGobiiCropType() {
		return gobiiCropType;
	}

	public void setGobiiCropType(final String gobiiCropType) {
		this.gobiiCropType = gobiiCropType;
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
