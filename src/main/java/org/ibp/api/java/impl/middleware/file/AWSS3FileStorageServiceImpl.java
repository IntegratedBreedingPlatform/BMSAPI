package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;

public class AWSS3FileStorageServiceImpl implements FileStorageService {

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
				.bucket(this.bucketName)
				.key(path)
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
				.bucket(this.bucketName)
				.key(path)
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
				.bucket(this.bucketName)
				.key(path)
				.build();
			s3Client.deleteObject(deleteObjectResponse);
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
}
