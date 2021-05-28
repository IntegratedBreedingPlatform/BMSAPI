package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

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
	public Map<String, String> upload(final MultipartFile file, final String key) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(this.bucketName)
				.key(key)
				.build();
			final PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
			return Collections.singletonMap("key", key);
		} catch (final SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			throw new ApiRuntimeException("Something went wrong while contacting Amazon S3, please contact your administrator", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("Your file format is not supported", e);
		}
	}

	@Override
	public byte[] getFile(final String key) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(this.bucketName)
				.key(key)
				.build();
			final ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

			return IoUtils.toByteArray(response);
		} catch (final SdkClientException e) {
			throw new ApiRuntimeException("Something went wrong while contacting Amazon S3, please contact your administrator", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("Something was wrong while retrieving the image", e);
		}
	}

	@Override
	public boolean isConfigured() {
		return true;
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
