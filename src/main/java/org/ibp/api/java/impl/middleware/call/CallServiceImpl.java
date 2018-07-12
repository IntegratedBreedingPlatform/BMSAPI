package org.ibp.api.java.impl.middleware.call;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.BrapiCall;
import org.ibp.api.java.call.CallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CallServiceImpl implements CallService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<org.ibp.api.brapi.v1.calls.BrapiCall> getAllCalls(final String dataType, final Integer pageSize, final Integer pageNumber) {
		final List<BrapiCall> brapiCalls = this.workbenchDataManager.getBrapiCalls(dataType, pageSize, pageNumber);

		return this.map(brapiCalls);
	}

	void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	private List<org.ibp.api.brapi.v1.calls.BrapiCall> map(final List<BrapiCall> brapiCalls) throws MiddlewareQueryException {
		final List<org.ibp.api.brapi.v1.calls.BrapiCall> calls = new ArrayList<>();

		if (brapiCalls == null) {
			return calls;
		}

		for (final BrapiCall brapiCall : brapiCalls) {
			final org.ibp.api.brapi.v1.calls.BrapiCall call = new org.ibp.api.brapi.v1.calls.BrapiCall();
			call.setCall(brapiCall.getCall());
			call.setDatatypes(Arrays.asList(brapiCall.getDatatypes().split(",")));
			call.setVersions(Arrays.asList(brapiCall.getVersions().split(",")));
			final List<String> methods = Arrays.asList(brapiCall.getMethods().split(","));
			for (int i = 0; i < methods.size() ; i++) {
				call.addMethods(RequestMethod.valueOf(methods.get(i).trim()));
			}

			calls.add(call);
		}
		return calls;
	}

}
