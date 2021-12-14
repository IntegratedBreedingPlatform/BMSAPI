package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AWSS3FileStorageServiceImplTest {

	private static final String BUCKET_NAME = "bucket";
	private static final String SUB_FOLDER = "sub-folder";
	private static final String FILE_PATH = "germplasm/4/3/5/2/c/germplasmuuid-ZGQAG70384dcd/download (2).jpeg";
	private static final String FOLDER_SEPARATOR = AWSS3FileStorageServiceImpl.FOLDER_SEPARATOR;


	static class AWSMock {

		public GetObjectRequest.Builder getObjectRequestBuilder;
		public DeleteObjectRequest.Builder deleteObjectRequestBuilder;
		public PutObjectRequest.Builder putObjectRequestBuilder;
		public DeleteObjectsRequest.Builder deleteObjectsRequestBuilder;
	}


	private AWSS3FileStorageServiceImpl awss3FileStorageService = new AWSS3FileStorageServiceImpl();

	@Test
	public void testIsConfigured() {
		assertThat(this.awss3FileStorageService.isConfigured(), is(true));
	}

	@Test
	public void testUpload_SubFolder_Upload() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME + FOLDER_SEPARATOR + SUB_FOLDER);

			final MultipartFile file = mock(MultipartFile.class);
			this.awss3FileStorageService.upload(file, FILE_PATH);

			verify(awsMock.putObjectRequestBuilder).bucket(BUCKET_NAME);
			verify(awsMock.putObjectRequestBuilder).key(SUB_FOLDER + FOLDER_SEPARATOR + FILE_PATH);
		});
	}

	@Test
	public void testUpload_SubFolder_Upload_NoSubfolder() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME);

			final MultipartFile file = mock(MultipartFile.class);
			this.awss3FileStorageService.upload(file, FILE_PATH);

			verify(awsMock.putObjectRequestBuilder).bucket(BUCKET_NAME);
			verify(awsMock.putObjectRequestBuilder).key(FILE_PATH);
		});
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testUpload_SubFolder_Upload_MultipleSubfolder() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME
				+ FOLDER_SEPARATOR + SUB_FOLDER
				+ FOLDER_SEPARATOR + SUB_FOLDER);

			final MultipartFile file = mock(MultipartFile.class);
			this.awss3FileStorageService.upload(file, FILE_PATH);
		});
	}

	@Test
	public void testUpload_SubFolder_Delete() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME + FOLDER_SEPARATOR + SUB_FOLDER);

			this.awss3FileStorageService.deleteFile(FILE_PATH);

			verify(awsMock.deleteObjectRequestBuilder).bucket(BUCKET_NAME);
			verify(awsMock.deleteObjectRequestBuilder).key(SUB_FOLDER + FOLDER_SEPARATOR + FILE_PATH);
		});
	}

	@Test
	public void testUpload_SubFolder_Delete_Multiple() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME + FOLDER_SEPARATOR + SUB_FOLDER);

			final List<String> paths = newArrayList(FILE_PATH, FILE_PATH);
			this.awss3FileStorageService.deleteFiles(paths);

			verify(awsMock.deleteObjectsRequestBuilder).bucket(BUCKET_NAME);
			final List<ObjectIdentifier> objIds = paths.stream().map(s -> ObjectIdentifier.builder().key(
				SUB_FOLDER + FOLDER_SEPARATOR + s).build()
			).collect(toList());
			verify(awsMock.deleteObjectsRequestBuilder).delete(Delete.builder().objects(objIds).build());
		});
	}

	@Test
	public void testUpload_SubFolder_Get() {
		withAWSMocked(awsMock -> {
			this.awss3FileStorageService.setBucketName(BUCKET_NAME + FOLDER_SEPARATOR + SUB_FOLDER);

			this.awss3FileStorageService.getFile(FILE_PATH);

			verify(awsMock.getObjectRequestBuilder).bucket(BUCKET_NAME);
			verify(awsMock.getObjectRequestBuilder).key(SUB_FOLDER + FOLDER_SEPARATOR + FILE_PATH);
		});
	}

	private static void withAWSMocked(final Consumer<AWSMock> assertions) {
		try (
			final MockedStatic<S3Client> s3ClientMockedStatic = mockStatic(S3Client.class);
			final MockedStatic<AwsBasicCredentials> awsBasicCredentials = mockStatic(AwsBasicCredentials.class);
			final MockedStatic<StaticCredentialsProvider> staticCredentialsProviderMockedStatic = mockStatic(
				StaticCredentialsProvider.class);
			final MockedStatic<PutObjectRequest> putObjectRequestMockedStatic = mockStatic(PutObjectRequest.class);
			final MockedStatic<Region> region = mockStatic(Region.class);
			final MockedStatic<UrlConnectionHttpClient> urlConnectionHttpClientMockedStatic = mockStatic(
				UrlConnectionHttpClient.class
			);
			final MockedStatic<RequestBody> requestBodyMockedStatic = mockStatic(RequestBody.class);
			final MockedStatic<IoUtils> ioUtilsMockedStatic = mockStatic(IoUtils.class);
			final MockedStatic<GetObjectRequest> getObjectRequestMockedStatic = mockStatic(GetObjectRequest.class);
			final MockedStatic<DeleteObjectRequest> deleteObjectRequestMockedStatic = mockStatic(DeleteObjectRequest.class);
			final MockedStatic<DeleteObjectsRequest> deleteObjectsRequestMockedStatic = mockStatic(DeleteObjectsRequest.class)
		) {

			// build s3 client
			final S3ClientBuilder s3ClientBuilder = mock(S3ClientBuilder.class);
			s3ClientMockedStatic.when(S3Client::builder).thenReturn(s3ClientBuilder);

			region.when(() -> Region.of(anyString())).thenReturn(Region.US_EAST_1);
			when(s3ClientBuilder.region(any())).thenReturn(s3ClientBuilder);

			final UrlConnectionHttpClient.Builder urlConnectionBuilder = mock(UrlConnectionHttpClient.Builder.class);
			urlConnectionHttpClientMockedStatic.when(UrlConnectionHttpClient::builder).thenReturn(urlConnectionBuilder);
			final UrlConnectionHttpClient urlConnectionHttpClient = mock(UrlConnectionHttpClient.class);
			when(urlConnectionBuilder.build()).thenReturn(urlConnectionHttpClient);
			when(s3ClientBuilder.httpClient(any())).thenReturn(s3ClientBuilder);

			final StaticCredentialsProvider staticCredentialsProvider = mock(StaticCredentialsProvider.class);
			staticCredentialsProviderMockedStatic.when(() -> StaticCredentialsProvider.create(any())).thenReturn(staticCredentialsProvider);
			when(s3ClientBuilder.credentialsProvider(staticCredentialsProvider)).thenReturn(s3ClientBuilder);

			// build final client
			final S3Client s3Client = mock(S3Client.class);
			when(s3ClientBuilder.build()).thenReturn(s3Client);

			// mock request builders
			final PutObjectRequest.Builder putObjectRequestBuilder = mock(PutObjectRequest.Builder.class, RETURNS_SELF);
			putObjectRequestMockedStatic.when(PutObjectRequest::builder).thenReturn(putObjectRequestBuilder);

			final DeleteObjectRequest.Builder deleteObjectRequestBuilder = mock(DeleteObjectRequest.Builder.class, RETURNS_SELF);
			deleteObjectRequestMockedStatic.when(DeleteObjectRequest::builder).thenReturn(deleteObjectRequestBuilder);

			final GetObjectRequest.Builder getObjectRequestBuilder = mock(GetObjectRequest.Builder.class, RETURNS_SELF);
			getObjectRequestMockedStatic.when(GetObjectRequest::builder).thenReturn(getObjectRequestBuilder);

			final DeleteObjectsRequest.Builder deleteObjectsRequestBuilder = mock(DeleteObjectsRequest.Builder.class, RETURNS_SELF);
			deleteObjectsRequestMockedStatic.when(DeleteObjectsRequest::builder).thenReturn(deleteObjectsRequestBuilder);
			final DeleteObjectsResponse deleteObjectsResponse = mock(DeleteObjectsResponse.class);
			final DeleteObjectsRequest deleteObjectsRequest = mock(DeleteObjectsRequest.class);
			when(deleteObjectsRequestBuilder.build()).thenReturn(deleteObjectsRequest);
			when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(deleteObjectsResponse);

			// RequestBody
			final RequestBody requestBody = mock(RequestBody.class);
			requestBodyMockedStatic.when(() -> RequestBody.fromBytes(any())).thenReturn(requestBody);

			// use mocks
			final AWSMock awsMock = new AWSMock();
			awsMock.putObjectRequestBuilder = putObjectRequestBuilder;
			awsMock.deleteObjectRequestBuilder = deleteObjectRequestBuilder;
			awsMock.getObjectRequestBuilder = getObjectRequestBuilder;
			awsMock.deleteObjectsRequestBuilder = deleteObjectsRequestBuilder;

			assertions.accept(awsMock);
		}
	}

}
