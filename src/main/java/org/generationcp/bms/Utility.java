package org.generationcp.bms;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/***
 * * TODO: Move this to middleware or common utility project for sharing
 */
public class Utility {

    /**
     * @param source source list
     * @param projection projection function
     * @param <Source> Source Type
     * @param <Result> Result Type
     * @return List<Result> Projected data
     */
    public static <Source, Result> List<Result> convertAll(List<Source> source, Function<Source, Result> projection)
    {
        ArrayList<Result> results = new ArrayList<>();
        for (Source element : source)
        {
            results.add(projection.apply(element));
        }
        return results;
    }

    /**
     * @param source source list
     * @param projection projection function
     * @param <Key> Key Type
     * @param <Source> Source Type
     * @return List<Result> Projected data
     */
    public static <Key, Source> Map<Key, Source> mapAll(List<Source> source, Function<Source, Key> projection)
    {
        Map<Key, Source> results = new HashMap<>();
        for (Source element : source)
        {
            results.put(projection.apply(element), element);
        }
        return results;
    }
}

