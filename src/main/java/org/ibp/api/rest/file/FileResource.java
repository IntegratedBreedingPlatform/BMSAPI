package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import org.ibp.api.exception.ApiRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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

@Api("File services")
@RestController
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

	@Value("${aws.bucketName}")
	private String bucketName;

	@Value("${aws.accessKeyId}")
	private String accessKey;

	@Value("${aws.secretAccessKey}")
	private String secretKey;

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> upload(@RequestPart("file") final MultipartFile file) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final String fileName = file.getOriginalFilename();
			final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(this.bucketName)
				.key(fileName)
				.build();
			final PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

			return new ResponseEntity<>(fileName, HttpStatus.CREATED);
		} catch (final SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			throw new ApiRuntimeException("Something went wrong while contacting Amazon S3, please contact your administrator", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("Your file format is not supported", e);
		}
	}

	@RequestMapping(value = "/files/{fileName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable final String fileName) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(this.bucketName)
				.key(fileName)
				.build();
			final ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

			final InputStreamResource resource = new InputStreamResource(response);

			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
			return ResponseEntity.ok()
				.headers(headers)
				.contentLength(response.response().contentLength())
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
		} catch (final SdkClientException e) {
			throw new ApiRuntimeException("Something went wrong while contacting Amazon S3, please contact your administrator", e);
		}
	}

	@RequestMapping(value = "/images/{imageName}", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getImage(@PathVariable final String imageName) {
		try {
			final S3Client s3Client = this.buildS3Client();

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(this.bucketName)
				.key(imageName)
				.build();
			final ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

			return IoUtils.toByteArray(response);
		} catch (final SdkClientException e) {
			throw new ApiRuntimeException("Something went wrong while contacting Amazon S3, please contact your administrator", e);
		} catch (final IOException e) {
			throw new ApiRuntimeException("Something was wrong while retrieving the image", e);
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
			.region(Region.US_EAST_1)
			// https://github.com/aws/aws-sdk-java-v2/issues/1786#issuecomment-706542582
			.httpClient(UrlConnectionHttpClient.builder().build())
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}
}
