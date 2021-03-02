package org.ibp.api.java.impl.middleware.keysequenceregister;

import com.google.common.collect.Lists;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.ibp.api.domain.keysequenceregister.KeySequenceRegisterDeleteResponse;
import org.ibp.api.java.keysequenceregister.KeySequenceRegisterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class KeySequenceRegisterServiceImpl implements KeySequenceRegisterService {

	private static final String SEQUENCE_NUMBER_REGEX = ")\\s?(\\d+).*";

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private org.generationcp.middleware.service.api.KeySequenceRegisterService keySequenceRegisterMiddlewareService;

	@Override
	public KeySequenceRegisterDeleteResponse deleteKeySequenceReqister(final List<Integer> gids, final List<String> prefixes) {

		final List<String> names = this.germplasmDataManager.getNamesByGidsAndPrefixes(gids, prefixes);
		final Set<String> prefixesToBeDeleted = new HashSet<>();
		for (final String prefix : prefixes) {
			final Pattern namePattern =
				Pattern.compile("^(" + Pattern.quote(prefix) + KeySequenceRegisterServiceImpl.SEQUENCE_NUMBER_REGEX);
			for (String name : names) {
				name = name.trim().toUpperCase();
				final Matcher nameMatcher = namePattern.matcher(name);
				if (nameMatcher.find()) {
					prefixesToBeDeleted.add(prefix);
					break;
				}
			}
		}

		if (!prefixesToBeDeleted.isEmpty()) {
			this.keySequenceRegisterMiddlewareService.deleteKeySequences(new ArrayList<>(prefixesToBeDeleted));
		}

		final List<String> undeletedPrefixes =
			prefixes.stream().filter(prefix -> !prefixesToBeDeleted.contains(prefix)).collect(Collectors.toList());
		return new KeySequenceRegisterDeleteResponse(Lists.newArrayList(prefixesToBeDeleted), undeletedPrefixes);
	}

}
