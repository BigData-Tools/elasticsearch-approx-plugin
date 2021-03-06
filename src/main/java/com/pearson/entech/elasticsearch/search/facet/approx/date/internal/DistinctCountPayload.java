package com.pearson.entech.elasticsearch.search.facet.approx.date.internal;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.trove.ExtTHashMap;
import org.elasticsearch.common.trove.map.TLongObjectMap;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import com.clearspring.analytics.stream.cardinality.HyperLogLog.Builder;

public class DistinctCountPayload {

    private final Builder _stdBuilder = new HyperLogLog.Builder(0.0025);

    private long _count;

    private CountThenEstimateBytes _cardinality;

    public DistinctCountPayload(final int entryLimit) {
        _count = 0;
        _cardinality = new CountThenEstimateBytes(entryLimit, _stdBuilder);
    }

    DistinctCountPayload(final StreamInput in) throws IOException {
        _count = in.readVLong();
        final int entryLimit = in.readVInt();
        final int payloadSize = in.readVInt();
        final byte[] payloadBytes = new byte[payloadSize];
        in.readBytes(payloadBytes, 0, payloadSize);
        try {
            _cardinality = new CountThenEstimateBytes(payloadBytes, entryLimit, _stdBuilder);
        } catch(final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    DistinctCountPayload(final long count, final CountThenEstimateBytes cardinality) {
        _count = count;
        _cardinality = cardinality;
    }

    public boolean update(final BytesRef ref) {
        _count++;
        return _cardinality.offerBytesRef(ref);
    }

    byte[] cardinalityBytes() throws IOException {
        return _cardinality.getBytes();
    }

    public long getCount() {
        return _count;
    }

    public CountThenEstimateBytes getCardinality() {
        return _cardinality;
    }

    DistinctCountPayload merge(final DistinctCountPayload other) throws CardinalityMergeException {
        _count += other._count;
        _cardinality = CountThenEstimateBytes.mergeEstimators(this._cardinality, other._cardinality);
        return this;
    }

    DistinctCountPayload mergeInto(final TLongObjectMap<DistinctCountPayload> map, final long key) {
        if(map.containsKey(key))
            try {
                map.put(key, this.merge(map.get(key)));
            } catch(final CardinalityMergeException e) {
                throw new ElasticSearchException("Unable to merge two facet cardinality objects", e);
            }
        else
            map.put(key, this);
        return this;
    }

    <K> DistinctCountPayload mergeInto(final ExtTHashMap<K, DistinctCountPayload> map, final K key) {
        if(map.containsKey(key))
            try {
                map.put(key, this.merge(map.get(key)));
            } catch(final CardinalityMergeException e) {
                throw new ElasticSearchException("Unable to merge two facet cardinality objects", e);
            }
        else
            map.put(key, this);
        return this;
    }

    @Override
    public String toString() {
        final String descr = _cardinality.sizeof() == -1 ?
                "Set" : "Estimator";
        return String.format(
                "%s of %d distinct elements (%d total elements)",
                descr, _cardinality.cardinality(), _count);
    }

    public void writeTo(final StreamOutput output) throws IOException {
        output.writeVLong(_count);
        output.writeVInt(_cardinality.getTippingPoint());
        final byte[] bytes = _cardinality.getBytes();
        output.writeVInt(bytes.length);
        output.writeBytes(bytes);
    }

}