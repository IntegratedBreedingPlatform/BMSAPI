package org.ibp.api.java.impl.middleware.design.util;

import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

public class ExperimentalDesignUtilTest {

    @Test
    public void testSetReplatinGroups() {
        final int replicationsCount = 3;
        ExperimentalDesignInput input = new ExperimentalDesignInput();
        input.setReplicationsCount(replicationsCount);
        input.setReplicationsArrangement(1);

        // non-latinized
        ExperimentalDesignUtil.setReplatinGroups(input);
        Assert.assertNull(input.getReplatinGroups());


        // latinized, columns arrangement
        input = new ExperimentalDesignInput();
        input.setReplicationsCount(replicationsCount);
        input.setUseLatenized(true);
        input.setReplicationsArrangement(1);
        ExperimentalDesignUtil.setReplatinGroups(input);
        Assert.assertEquals(String.valueOf(replicationsCount), input.getReplatinGroups());


        // latinized, rows arrangement
        input = new ExperimentalDesignInput();
        input.setReplicationsCount(replicationsCount);
        input.setUseLatenized(true);
        input.setReplicationsArrangement(2);
        ExperimentalDesignUtil.setReplatinGroups(input);
        Assert.assertEquals("1,1,1", input.getReplatinGroups());

        // invalid reps arrangement
        input = new ExperimentalDesignInput();
        input.setReplicationsCount(replicationsCount);
        input.setUseLatenized(true);
        input.setReplicationsArrangement(3);
        ExperimentalDesignUtil.setReplatinGroups(input);
        Assert.assertNull(input.getReplatinGroups());

    }
}
