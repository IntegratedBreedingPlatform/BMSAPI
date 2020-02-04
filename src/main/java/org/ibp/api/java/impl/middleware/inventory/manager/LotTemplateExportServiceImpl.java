package org.ibp.api.java.impl.middleware.inventory.manager;

import com.google.common.io.Files;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.inventory.manager.LotTemplateExportService;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class LotTemplateExportServiceImpl implements LotTemplateExportService {

	@Resource
	private LotExcelTemplateGenerator lotExcelTemplateGenerator;

	@Override
	public File export(final List<LocationDto> locations, final List<VariableDetails> units) {
		try {
			final File temporaryFolder = Files.createTempDir();
			final String sanitizedFileName = "template_import_lots.xls";
			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
			return lotExcelTemplateGenerator.generateTemplateFile(fileNameFullPath, locations, units);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.lot.template", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

}
