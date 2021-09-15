package org.ibp.api.java.impl.middleware.file;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SFTPFileStorageServiceImplTest {

	private static final String FOLDER_1 = "folder 1";
	private static final String FOLDER_2 = "folder 2";
	private static final String FILENAME_PNG = "filename.png";
	private static final String KEY = FOLDER_1 + "/" + FOLDER_2 + "/" + FILENAME_PNG;

	@Mock
	private JSch jsch;

	@InjectMocks
	private final SFTPFileStorageServiceImpl sftpFileStorageService = new SFTPFileStorageServiceImpl();

	private Session session;
	private ChannelSftp channel;

	@Before
	public void setup() throws JSchException, SftpException {
		ReflectionTestUtils.setField(this.sftpFileStorageService, "host", "localhost");
		ReflectionTestUtils.setField(this.sftpFileStorageService, "username", "username");
		ReflectionTestUtils.setField(this.sftpFileStorageService, "password", "password");

		this.session = mock(Session.class);
		this.channel = mock(ChannelSftp.class);
		when(this.jsch.getSession(anyString(), anyString())).thenReturn(this.session);
		when(this.session.openChannel(anyString())).thenReturn(this.channel);
		when(this.channel.getSession()).thenReturn(this.session);
		Mockito.doThrow(SftpException.class)
			.doNothing()
			.when(this.channel).cd(FOLDER_2);
	}

	@Test
	public void testUpload() throws SftpException {
		final MultipartFile file = mock(MultipartFile.class);
		this.sftpFileStorageService.upload(file, KEY);
		verify(this.channel).cd(FOLDER_1);
		verify(this.channel, times(2)).cd(FOLDER_2);
		verify(this.channel).mkdir(FOLDER_2);
		verify(this.channel).put(nullable(InputStream.class), eq(FILENAME_PNG));
	}

	@Test
	public void testIsConfigured() {
		assertThat(this.sftpFileStorageService.isConfigured(), is(true));
	}
}
