package org.ibp.api.java.keysequenceregister;

import org.ibp.api.domain.keysequenceregister.KeySequenceRegisterDeleteResponse;

import java.util.List;

public interface KeySequenceRegisterService {

	KeySequenceRegisterDeleteResponse deleteKeySequence(List<Integer> gids, List<String> prefixes);

}
