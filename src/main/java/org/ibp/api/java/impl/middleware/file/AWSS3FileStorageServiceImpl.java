package org.ibp.api.java.impl.middleware.file;

import org.generationcp.middleware.api.file.FileMetadataServiceImpl;
import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.springframework.util.CollectionUtils.isEmpty;

public class AWSS3FileStorageServiceImpl implements FileStorageService {

	private static final Logger LOG = LoggerFactory.getLogger(AWSS3FileStorageServiceImpl.class);

	static final String FOLDER_SEPARATOR = FileMetadataServiceImpl.FILE_PATH_SLASH;

	@Value("${aws.bucketName}")
	private String bucketName;

	@Value("${aws.accessKeyId}")
	private String accessKey;

	@Value("${aws.secretAccessKey}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	@Override
	public void upload(final MultipartFile file, final String path) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(resolveBucketName())
				.key(resolvePath(path))
				.build();
			final PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
		} catch (final SdkClientException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.connection");
		} catch (final IOException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.file.format");
		}
	}

	@Override
	public byte[] getFile(final String path) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(resolveBucketName())
				.key(resolvePath(path))
				.build();
			final ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

			return IoUtils.toByteArray(response);
		} catch (final SdkClientException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.connection");
		} catch (final IOException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.file.get");
		}
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public void deleteFile(final String path) {

		try {
			final S3Client s3Client = this.buildS3Client();

			final DeleteObjectRequest deleteObjectResponse = DeleteObjectRequest.builder()
				.bucket(resolveBucketName())
				.key(resolvePath(path))
				.build();
			s3Client.deleteObject(deleteObjectResponse);
		} catch (final SdkClientException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.connection");
		}
	}

	@Override
	public void deleteFiles(final List<String> paths) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final List<ObjectIdentifier> objIds = paths.stream().map(s -> ObjectIdentifier.builder().key(resolvePath(s)).build())
				.collect(toList());
			final DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
				.bucket(resolveBucketName())
				.delete(Delete.builder().objects(objIds).build())
				.build();
			final DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);

			if (!isEmpty(response.errors())) {
				throw new ApiRuntime2Exception("", "file.storage.aws.error.delete.multi");
			}
			if (response.deleted().size() < objIds.size() && LOG.isWarnEnabled()) {
				LOG.warn(String.format("%d of %d objects deleted", response.deleted().size(), objIds.size()));
			}

		} catch (final SdkClientException e) {
			throw new ApiRuntime2Exception("", "file.storage.aws.error.connection");
		}
	}

	/*
	 * TODO
	 *  - forcing credentials for now (Unable to load credentials from any of the providers in the chain AwsCredentialsProviderChain)
	 *  - configurable region
	 */
	private S3Client buildS3Client() {
		final AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
		return S3Client.builder()
			.region(Region.of(this.region))
			// https://github.com/aws/aws-sdk-java-v2/issues/1786#issuecomment-706542582
			.httpClient(UrlConnectionHttpClient.builder().build())
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	private String resolveBucketName() {
		return resolveSubfolder()[0];
	}

	private String resolvePath(final String path) {
		final String[] subfolder = resolveSubfolder();
		return subfolder.length == 2
			? subfolder[1] + FOLDER_SEPARATOR + path
			: path;
	}

	private String[] resolveSubfolder() {
		final String[] parts = this.bucketName.split("\\" + FOLDER_SEPARATOR);
		checkArgument(parts.length <= 2, "file.storage.aws.error.subfolder.multi");
		return parts;
	}

	void setBucketName(final String bucketName) {
		this.bucketName = bucketName;
	}
}
