package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.ibp.api.java.file.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api("File services")
@RestController
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

	@Autowired
	private FileStorageService fileStorageService;

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, String>> upload(
		@RequestPart("file") final MultipartFile file,
		@ApiParam("store file under this name / key") @RequestParam final String key
	) {
		return new ResponseEntity<>(this.fileStorageService.upload(file, key), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Resource> getFile(final HttpServletRequest request) {
		final String key = getKey(request);
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"");
		return ResponseEntity.ok()
			.headers(headers)
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(this.fileStorageService.getFile(key));
	}

	@RequestMapping(value = "/images/**", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getImage(final HttpServletRequest request) {
		final String key = getKey(request);
		return this.fileStorageService.getImage(key);
	}

	private static String getKey(final HttpServletRequest request) {
		return request.getRequestURI().split(request.getContextPath() + "/images/")[1];
	}
}
