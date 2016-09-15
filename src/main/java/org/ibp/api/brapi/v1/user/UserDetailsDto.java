
package org.ibp.api.brapi.v1.user;

public class UserDetailsDto extends UserDetailDto{

	private Boolean sendEmail;
	
	public Boolean getSendEmail() {
		return sendEmail;
	}

	
	public void setSendEmail(Boolean sendEmail) {
		this.sendEmail = sendEmail;
	}
	
	public String toString(){
		StringBuffer str= new StringBuffer();
		str.append("UserDetails ")
		.append("[ ").append(super.toString())
		.append(" ,[ UserDetails ")
		.append(" sendEmail= ").append(sendEmail)		
		.append(" ]").append(" ]");
		return str.toString();
	}

}
