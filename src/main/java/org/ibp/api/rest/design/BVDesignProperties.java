package org.ibp.api.rest.design;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class BVDesignProperties {
	private String bvDesignPath;

	private Integer bvDesignRunnerTimeout;

	private String uploadDirectory;

	@Resource
	private Environment environment;

	@PostConstruct
	public void init() {
		this.bvDesignPath = this.environment.getProperty("bv.design.path");
		this.bvDesignRunnerTimeout = Integer.parseInt(this.environment.getProperty("bv.design.runner.timeout"));
		this.uploadDirectory = this.environment.getProperty("upload.directory");
	}



	public String getUploadDirectory() {
		return this.uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public String getBvDesignPath() {
		return bvDesignPath;
	}

	public void setBvDesignPath(final String bvDesignPath) {
		this.bvDesignPath = bvDesignPath;
	}

	public Integer getBvDesignRunnerTimeout() {
		return bvDesignRunnerTimeout;
	}

	public void setBvDesignRunnerTimeout(final Integer bvDesignRunnerTimeout) {
		this.bvDesignRunnerTimeout = bvDesignRunnerTimeout;
	}
}
