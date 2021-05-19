package org.ibp.api.java.impl.middleware.common;

import com.jcraft.jsch.JSch;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.file.AWSS3FileStorageServiceImpl;
import org.ibp.api.java.impl.middleware.file.NoneFileStorageServiceImpl;
import org.ibp.api.java.impl.middleware.file.SFTPFileStorageServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import static org.apache.commons.lang.StringUtils.isBlank;

@Configuration
public class FileServiceFactory {

	// AWS

	@Value("${aws.bucketName}")
	private String bucketName;

	@Value("${aws.accessKeyId}")
	private String accessKey;

	@Value("${aws.secretAccessKey}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	// SFTP

	@Value("${sftp.host}")
	private String host;

	@Value("${sftp.username}")
	private String username;

	@Value("${sftp.password}")
	private String password;

	@Value("${sftp.privateKey}")
	private String privateKey;

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FileStorageService getFileStorageService() {
		if (this.hasAWSProperties()) {
			return new AWSS3FileStorageServiceImpl();
		} else if (this.hasSFTPProperties()) {
			return new SFTPFileStorageServiceImpl();
		} else {
			// I've tried some alternatives with Optional but couldn't make it to work
			return new NoneFileStorageServiceImpl();
		}
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public JSch getJsch() {
		return new JSch();
	}

	private boolean hasAWSProperties() {
		return !isBlank(this.bucketName) && !isBlank(this.accessKey) && !isBlank(this.secretKey) && !isBlank(this.region);
	}

	private boolean hasSFTPProperties() {
		return !isBlank(this.host) && !isBlank(this.username)
			&& (!isBlank(this.password) || !isBlank(this.privateKey));
	}

}
