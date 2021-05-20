package org.ibp.api.java.impl.middleware.file;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.IOUtils;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SFTPFileStorageServiceImpl implements FileStorageService {

	@Value("${sftp.host}")
	private String host;

	@Value("${sftp.username}")
	private String username;

	@Value("${sftp.password}")
	private String password;

	@Value("${sftp.privateKey}")
	private String privateKey;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private JSch jsch;

	@Override
	public Map<String, String> upload(final MultipartFile file, final String key) {
		ChannelSftp channelSftp = null;
		try {
			channelSftp = this.setupJsch();
			channelSftp.connect();

			final String[] keyParts = key.split("/");
			if (keyParts.length > 1) {
				this.createFolders(channelSftp, Arrays.copyOfRange(keyParts, 0, keyParts.length - 1));
			}

			channelSftp.put(file.getInputStream(), keyParts[keyParts.length - 1]);
		} catch (final JSchException e) {
			throw new ApiRuntimeException("", e);
		} catch (final SftpException e) {
			throw new ApiRuntimeException("", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("", e);
		} finally {
			close(channelSftp);
		}
		return Collections.singletonMap("key", key);
	}

	@Override
	public byte[] getFile(final String key) {
		ChannelSftp channelSftp = null;
		byte[] bytes;
		try {
			channelSftp = this.setupJsch();
			channelSftp.connect();
			final InputStream inputStream = channelSftp.get(key);
			bytes = IOUtils.toByteArray(inputStream);
		} catch (final JSchException e) {
			throw new ApiRuntimeException("", e);
		} catch (final SftpException e) {
			throw new ApiRuntimeException("", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("", e);
		} finally {
			close(channelSftp);
		}
		return bytes;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	private static void close(final ChannelSftp channelSftp) {
		if (channelSftp != null) {
			try {
				Session session = null;
				session = channelSftp.getSession();
				channelSftp.disconnect();
				session.disconnect();
			} catch (final JSchException e) {
				// noop
			}
		}
	}

	private ChannelSftp setupJsch() throws JSchException, IOException {
		// TODO
		// jsch.setKnownHosts(this.knownhosts);
		this.jsch.setConfig("StrictHostKeyChecking", "no");
		if (isBlank(this.password)) {
			// TODO load only file path?
			final Resource resource = this.resourceLoader.getResource("classpath:" + this.privateKey);
			this.jsch.addIdentity(resource.getURI().getPath());
		}
		final Session jschSession = this.jsch.getSession(this.username, this.host);
		if (!isBlank(this.password)) {
			jschSession.setPassword(this.password);
		}
		jschSession.connect();
		return (ChannelSftp) jschSession.openChannel("sftp");
	}

	private void createFolders(final ChannelSftp channelSftp, final String[] folders) throws SftpException {
		for (final String folder : folders) {
			if (folder.length() > 0) {
				try {
					channelSftp.cd(folder);
				} catch (final SftpException e) {
					channelSftp.mkdir(folder);
					channelSftp.cd(folder);
				}
			}
		}
	}
}
