package org.ibp.api.java.impl.middleware.design.util;

import org.ibp.api.domain.design.ListItem;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

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

    @Test
    public void testConvertToListItemList() {

        final List<String> listOfString = new LinkedList<>();

        final String sampleText1 = "sample text 1";
        final String sampleText2 = "sample text 2";

        listOfString.add(sampleText1);
        listOfString.add(sampleText2);

        final List<ListItem> listItems = ExperimentalDesignUtil.convertToListItemList(listOfString);

        Assert.assertEquals(2, listItems.size());
        Assert.assertEquals(sampleText1, listItems.get(0).getValue());
        Assert.assertEquals(sampleText2, listItems.get(1).getValue());

    }

}
