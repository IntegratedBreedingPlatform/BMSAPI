package org.ibp.api.brapi.v1.sample;

import org.generationcp.middleware.service.api.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Prototype BrAPI call to support sample tracking and related analysis in systems such as GOBII and phenotype/genotype correlation tools.
 * 
 * @author Naymesh Mistry
 *
 */
@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired
	private SampleService sampleService;

	@ApiOperation(value = "Get detailed information about the sample.",
			notes = "Get detailed information about the sample such as the study, plot it belongs to, the germplasm etc.")
	@RequestMapping(value = "/{crop}/brapi/v1/sample/{sampleId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional
	public ResponseEntity<Sample> getSampleDetails(@PathVariable final String crop,
			@ApiParam(value = "Id of the sample to get details for.") @PathVariable String sampleId) {

		final Sample sample = new Sample();

		/*final org.generationcp.middleware.pojos.Sample mwSample = this.sampleService.getSample(sampleId);
		if (mwSample != null) {

			sample.setLocationId(mwSample.getLocationId());
			sample.setPlotId(mwSample.getPlotId());
			sample.setPlotNumber(mwSample.getPlotNumber());
			sample.setPlantId(mwSample.getPlant());
			sample.setSampleId(mwSample.getSampleId());
			sample.setTakenBy(mwSample.getTakenBy());
			sample.setSampleDate(mwSample.getSampleDate());
			sample.setNotes(mwSample.getNotes());
			sample.setPlantingDate(mwSample.getPlantingDate());
			sample.setHarvestDate(mwSample.getHarvestDate());
			sample.setLocationName(mwSample.getLocationName());
			sample.setGermplasmId(mwSample.getGermplasmId());
			sample.setEntryNumber(mwSample.getEntryNumber());
			sample.setSeason(mwSample.getSeason());

			// TODO in middleware query
			sample.setStudyId(mwSample.getStudyId());
			sample.setStudyName(mwSample.getStudyName());
			sample.setYear(mwSample.getYear());

			sample.setFieldId(mwSample.getFieldId());
			sample.setFieldName(mwSample.getFieldName());

			sample.setSeedSource(mwSample.getSeedSource());
			sample.setPedigree(mwSample.getPedigree());

		}*/

		return new ResponseEntity<Sample>(sample, HttpStatus.OK);
	}

	@ApiOperation(value = "Create (take) sample from a plant in a plot at a location within a study.",
			notes = "Returns sample ID assigned to the newly created sample.")
	@RequestMapping(value = "/{crop}/brapi/v1/sample", method = RequestMethod.PUT)
	@ResponseBody
	@Transactional
	public ResponseEntity<String> createSample(@PathVariable final String crop, @RequestBody Sample sample) {
		
		org.generationcp.middleware.pojos.Sample mwSample =
				new org.generationcp.middleware.pojos.Sample();

		final String sampleId = "";//this.sampleService.createSample(mwSample);
		return new ResponseEntity<String>(sampleId, HttpStatus.CREATED);
	}
}
