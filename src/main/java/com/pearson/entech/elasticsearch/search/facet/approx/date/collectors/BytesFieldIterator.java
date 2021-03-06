package com.pearson.entech.elasticsearch.search.facet.approx.date.collectors;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.index.fielddata.AtomicFieldData;
import org.elasticsearch.index.fielddata.BytesValues;
import org.elasticsearch.index.fielddata.BytesValues.Iter;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.ScriptDocValues;

/**
 * An iterator over BytesRef values for a given type of field data.
 * 
 * @param <B> the field data type
 */
public class BytesFieldIterator<B extends AtomicFieldData<? extends ScriptDocValues>>
        extends CollectableIterator<BytesRef> {

    private final IndexFieldData<B> _bytesFieldData;
    private BytesValues _bytesFieldValues;
    private Iter _docIter;

    /**
     * Create a new iterator.
     * 
     * @param bytesFieldData the field data to iterate over
     */
    public BytesFieldIterator(final IndexFieldData<B> bytesFieldData) {
        _bytesFieldData = bytesFieldData;
    }

    @Override
    public void collect(final int doc) throws IOException {
        _docIter = _bytesFieldValues.getIter(doc);
    }

    @Override
    public void setNextReader(final AtomicReaderContext context) throws IOException {
        _bytesFieldValues = _bytesFieldData.load(context).getBytesValues();
    }

    @Override
    public boolean hasNext() {
        return _docIter.hasNext();
    }

    @Override
    public BytesRef next() {
        return _docIter.next();
    }

    @Override
    public void postCollection() {
        _docIter = null;
    }

}
