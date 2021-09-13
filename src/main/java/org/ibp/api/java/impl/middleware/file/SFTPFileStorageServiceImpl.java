package org.ibp.api.java.impl.middleware.file;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.IOUtils;
import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SFTPFileStorageServiceImpl implements FileStorageService {

	@Value("${sftp.host}")
	private String host;

	@Value("${sftp.username}")
	private String username;

	@Value("${sftp.password}")
	private String password;

	@Value("${sftp.privateKeyPath}")
	private String privateKeyPath;

	@Autowired
	private JSch jsch;

	@Override
	public void upload(final MultipartFile file, final String path) {
		ChannelSftp channelSftp = null;
		try {
			channelSftp = this.setupJsch();
			channelSftp.connect();

			final String[] pathParts = path.split("/");
			if (pathParts.length > 1) {
				this.createFolders(channelSftp, Arrays.copyOfRange(pathParts, 0, pathParts.length - 1));
			}

			channelSftp.put(file.getInputStream(), pathParts[pathParts.length - 1]);
		} catch (final JSchException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.connection");
		} catch (final SftpException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.upload");
		} catch (final IOException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.format");
		} finally {
			close(channelSftp);
		}
	}

	@Override
	public byte[] getFile(final String path) {
		ChannelSftp channelSftp = null;
		byte[] bytes;
		try {
			channelSftp = this.setupJsch();
			channelSftp.connect();
			final InputStream inputStream = channelSftp.get(path);
			bytes = IOUtils.toByteArray(inputStream);
		} catch (final JSchException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.connection");
		} catch (final SftpException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.get");
		} catch (final IOException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.get");
		} finally {
			close(channelSftp);
		}
		return bytes;
	}

	@Override
	public void deleteFile(final String path) {

		try {
			final ChannelSftp channelSftp = this.setupJsch();
			channelSftp.connect();
			channelSftp.rm(path);
		} catch (final JSchException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.connection");
		} catch (final SftpException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.delete");
		} catch (final IOException e) {
			throw new ApiRuntime2Exception(e.getMessage(), "file.storage.sftp.error.file.delete");
		}
	}

	@Override
	public void deleteFiles(final List<String> paths) {
		// TODO
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
			this.jsch.addIdentity(this.privateKeyPath);
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
