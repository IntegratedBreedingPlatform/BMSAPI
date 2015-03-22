package org.generationcp.bms;

import com.google.common.base.Function;
import org.generationcp.middleware.domain.oms.Term;

/**
 * TODO: Move this to middleware
 */
public class OntologyProjections {
    public static final Function<Term, String> termNameProjection = new Function<Term, String>()
    {
        public String apply(Term x)
        {
            return x.getName();
        }
    };

    public static final Function<Term, Integer> termIdProjection = new Function<Term, Integer>()
    {
        public Integer apply(Term x)
        {
            return x.getId();
        }
    };
}
