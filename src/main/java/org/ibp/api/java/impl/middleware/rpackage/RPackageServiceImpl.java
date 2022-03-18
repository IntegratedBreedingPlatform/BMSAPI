package org.ibp.api.java.impl.middleware.rpackage;

import org.generationcp.middleware.domain.rpackage.RPackageDTO;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.rpackage.RPackageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RPackageServiceImpl implements RPackageService {

	@Autowired
	private org.generationcp.middleware.service.api.rpackage.RPackageService rPackageMiddlewareService;

	@Override
	public List<RCallDTO> getRCallsByPackageId(final Integer packageId) {

		final Optional<RPackageDTO> rPackage = this.rPackageMiddlewareService.getRPackageById(packageId);
		if (!rPackage.isPresent()) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("rpackage.does.not.exist", new Object[] {packageId}, "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final ModelMapper mapper = new ModelMapper();
		return this.rPackageMiddlewareService.getRCallsByPackageId(packageId).stream().map(o -> mapper.map(o, RCallDTO.class))
			.collect(Collectors.toList());
	}

}
