package org.ibp.api.rest.role;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.RoleTypePermission;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collection;
import java.util.HashMap;

@Component
public class RoleValidator {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	public void validateRoleGeneratorInput(final RoleGeneratorInput roleGeneratorInput) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());
		Preconditions.checkNotNull(roleGeneratorInput.getName(), "name cannot be null");
		Preconditions.checkNotNull(roleGeneratorInput.getRoleType() ,"role type cannot be null");

		if (roleGeneratorInput.getName().length() > 100) {
			errors.reject("role.name.lenght");
		}

		if (this.workbenchDataManager.getRoleByName(roleGeneratorInput.getName()) != null) {
			errors.reject("role.name.already.exists");
		}

		if (roleGeneratorInput.getDescription() != null && roleGeneratorInput.getDescription().length() > 255) {
				errors.reject("role.description.lenght");
		}

		if (roleGeneratorInput.getRoleType() == null) {
			errors.reject("role.role.type.null");
		}

		if (this.workbenchDataManager.getRoleType(roleGeneratorInput.getRoleType()) == null) {
			errors.reject("role.role.type.does.not.exist");
		}
		else {
			for (final Integer permissionId : roleGeneratorInput.getPermissions()) {
				final Permission permission = this.workbenchDataManager.getPermission(permissionId);
				if (permission == null) {
					errors.reject("role.permission.does.not.exist");
				}

				final Collection result = CollectionUtils.collect(permission.getRoleTypePermissions(), new Transformer() {
					@Override
					public Integer transform(final Object input) {
						final RoleTypePermission roleTypePermission = (RoleTypePermission) input;
						return Integer.valueOf(roleTypePermission.getRoleType().getId());
					}
				});
				if (!result.contains(roleGeneratorInput.getRoleType())) {
					errors.reject("role.role.type.does.not.correspond");
				}

			}
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}
}
