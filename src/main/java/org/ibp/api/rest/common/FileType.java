package org.ibp.api.rest.common;

/**
 * Created by clarysabel on 2/7/19.
 */
public enum FileType {

	CSV ("csv"),
	PDF ("pdf"),
	XLS ("xls");

	private String extension;

	FileType(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(final String extension) {
		this.extension = extension;
	}

	public static FileType getEnum(final String extension) {
		for (FileType e : FileType.values()) {
			if (extension.equals(e.getExtension()))
				return e;
		}
		return null;
	}
}
