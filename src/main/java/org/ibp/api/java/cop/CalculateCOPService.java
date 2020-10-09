package org.ibp.api.java.cop;

import org.ibp.api.rest.cop.COPPermissions;
import org.ibp.api.rest.cop.COPPermissionsResponce;
import org.ibp.api.rest.cop.COPExportStudy;
import org.ibp.api.rest.cop.COPExportStudyResponse;

public interface CalculateCOPService {

	COPExportStudyResponse exportStudy(COPExportStudy COPExportStudy);

	COPPermissionsResponce permissions(COPPermissions COPPermissions);

}
