/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.pearson.entech.elasticsearch.search.facet.approx.date.external;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilderException;
import org.elasticsearch.search.facet.FacetBuilder;

import com.google.common.collect.Maps;

/**
 * A facet builder of date facets.
 */
public class DateFacetBuilder extends FacetBuilder {

    private String keyFieldName;
    private String valueFieldName;
    private String sliceFieldName;
    private String distinctFieldName;
    private String interval = null;
    private String preZone = null;
    private String postZone = null;
    private Boolean preZoneAdjustLargeInterval;
    private int exactThreshold = -1;
    private long preOffset = 0;
    private long postOffset = 0;
    private float factor = 1.0f;

    private String valueScript;
    private Map<String, Object> params;
    private String lang;

    /**
     * Constructs a new date histogram facet with the provided facet logical name.
     *
     * @param name The logical name of the facet
     */
    public DateFacetBuilder(final String name) {
        super(name);
    }

    /**
     * The field name to use in order to control where the hit will "fall into" within the histogram
     * entries -- synonym for keyField(). Essentially, using the key field numeric value, the hit
     * will be "rounded" into the relevant bucket controlled by the interval.
     * 
     * @param field 
     * @return the builder
     */
    public DateFacetBuilder field(final String field) {
        this.keyFieldName = field;
        return this;
    }

    /**
     * The field name to use in order to control where the hit will "fall into" within the histogram
     * entries -- synonym for field(). Essentially, using the key field numeric value, the hit
     * will be "rounded" into the relevant bucket controlled by the interval.
     * 
     * @param keyField 
     * @return the builder
     */
    public DateFacetBuilder keyField(final String keyField) {
        this.keyFieldName = keyField;
        return this;
    }

    /**
     * The field name to use as the value of the hit to compute data based on values within the interval
     * (for example, total).
     * 
     * @param valueField 
     * @return the builder 
     */
    public DateFacetBuilder valueField(final String valueField) {
        this.valueFieldName = valueField;
        return this;
    }

    /**
     * The field name to count distinct values of.
     * 
     * @param distinctField 
     * @return the builder 
     */
    public DateFacetBuilder distinctField(final String distinctField) {
        this.distinctFieldName = distinctField;
        return this;
    }

    /**
     * The field name to slice the output by.
     * 
     * @param sliceField 
     * @return the builder
     */
    public DateFacetBuilder sliceField(final String sliceField) {
        this.sliceFieldName = sliceField;
        return this;
    }

    /**
     * A value script to apply -- NOT YET IMPLEMENTED.
     * 
     * @param valueScript
     * @return the builder
     */
    public DateFacetBuilder valueScript(final String valueScript) {
        this.valueScript = valueScript;
        return this;
    }

    /**
     * Arbitrary additional parameters -- currently none are used.
     * 
     * @param name the parameter name
     * @param value the parameter value
     * @return the builder
     */
    public DateFacetBuilder param(final String name, final Object value) {
        if(params == null) {
            params = Maps.newHashMap();
        }
        params.put(name, value);
        return this;
    }

    /**
     * The language of the value script -- NOT YET IMPLEMENTED.
     * 
     * @param lang the language
     * @return the builder
     */
    public DateFacetBuilder lang(final String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * The interval used to control the bucket "size" where each key value of a hit will fall into. Check
     * the docs for all available values.
     * 
     * @param interval the interval setting 
     * @return the builder
     */
    public DateFacetBuilder interval(final String interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Should pre zone be adjusted for large (day and above) intervals. Defaults to <tt>false</tt>.
     * 
     * @param preZoneAdjustLargeInterval true/false 
     * @return the builder
     */
    public DateFacetBuilder preZoneAdjustLargeInterval(final boolean preZoneAdjustLargeInterval) {
        this.preZoneAdjustLargeInterval = preZoneAdjustLargeInterval;
        return this;
    }

    /**
     * Sets the pre time zone to use when bucketing the values. This timezone will be applied before
     * rounding off the result.
     * <p/>
     * Can either be in the form of "-10:00" or
     * one of the values listed here: http://joda-time.sourceforge.net/timezones.html.
     * 
     * @param preZone a timezone string 
     * @return the builder
     */
    public DateFacetBuilder preZone(final String preZone) {
        this.preZone = preZone;
        return this;
    }

    /**
     * Sets the post time zone to use when bucketing the values. This timezone will be applied after
     * rounding off the result.
     * <p/>
     * Can either be in the form of "-10:00" or
     * one of the values listed here: http://joda-time.sourceforge.net/timezones.html.
     * 
     * @param postZone a timezone string 
     * @return the builder
     */
    public DateFacetBuilder postZone(final String postZone) {
        this.postZone = postZone;
        return this;
    }

    /**
     * Sets a pre offset that will be applied before rounding the results.
     * 
     * @param preOffset a valid time value
     * @return the builder
     */
    public DateFacetBuilder preOffset(final TimeValue preOffset) {
        this.preOffset = preOffset.millis();
        return this;
    }

    /**
     * Sets a post offset that will be applied after rounding the results.
     * 
     * @param postOffset a valid time value
     * @return the builder
     */
    public DateFacetBuilder postOffset(final TimeValue postOffset) {
        this.postOffset = postOffset.millis();
        return this;
    }

    /**
     * Sets the factor that will be used to multiply the value with before and divided
     * by after the rounding of the results.
     * 
     * @param factor the rounding factor
     * @return the builder
     */
    public DateFacetBuilder factor(final float factor) {
        this.factor = factor;
        return this;
    }

    /**
     * How many distinct terms can we collect in each result bucket before
     * flipping over to approximate counting? (Distinct mode only)
     * 
     * @param threshold the threshold
     * @return the builder
     */
    public DateFacetBuilder exactThreshold(final int threshold) {
        this.exactThreshold = threshold;
        return this;
    }

    /**
     * Should the facet run in global mode (not bounded by the search query) or not (bounded by
     * the search query). Defaults to <tt>false</tt>.
     * 
     * @param global true/false
     * @return the builder
     */
    @Override
    public DateFacetBuilder global(final boolean global) {
        super.global(global);
        return this;
    }

    /**
     * An additional filter used to further filter down the set of documents the facet will run on.
     * 
     * @param the filter to use
     * @return the builder
     */
    @Override
    public DateFacetBuilder facetFilter(final FilterBuilder filter) {
        this.facetFilter = filter;
        return this;
    }

    /**
     * Sets the nested path the facet will execute on. A match (root object) will then cause all the
     * nested objects matching the path to be computed into the facet.
     * 
     * @param nested a nested path using dot notation
     * @return the builder
     */
    @Override
    public DateFacetBuilder nested(final String nested) {
        this.nested = nested;
        return this;
    }

    @Override
    public XContentBuilder toXContent(final XContentBuilder builder, final Params params) throws IOException {
        if(keyFieldName == null) {
            throw new SearchSourceBuilderException("field must be set on date histogram facet for facet [" + name + "]");
        }
        if(interval == null) {
            throw new SearchSourceBuilderException("interval must be set on date histogram facet for facet [" + name + "]");
        }
        builder.startObject(name);

        builder.startObject("date_facet");

        builder.field("key_field", keyFieldName);

        if(valueFieldName != null)
            builder.field("value_field", valueFieldName);

        if(distinctFieldName != null)
            builder.field("distinct_field", distinctFieldName);

        if(sliceFieldName != null)
            builder.field("slice_field", sliceFieldName);

        if(valueScript != null) {
            builder.field("value_script", valueScript);
            if(lang != null) {
                builder.field("lang", lang);
            }
            if(this.params != null) {
                builder.field("params", this.params);
            }
        }
        builder.field("interval", interval);
        if(preZone != null) {
            builder.field("pre_zone", preZone);
        }
        if(preZoneAdjustLargeInterval != null) {
            builder.field("pre_zone_adjust_large_interval", preZoneAdjustLargeInterval);
        }
        if(postZone != null) {
            builder.field("post_zone", postZone);
        }
        if(preOffset != 0) {
            builder.field("pre_offset", preOffset);
        }
        if(postOffset != 0) {
            builder.field("post_offset", postOffset);
        }
        if(factor != 1.0f) {
            builder.field("factor", factor);
        }
        if(exactThreshold != -1) {
            builder.field("exact_threshold", exactThreshold);
        }
        builder.endObject();

        addFilterFacetAndGlobal(builder, params);

        builder.endObject();
        return builder;
    }
}
