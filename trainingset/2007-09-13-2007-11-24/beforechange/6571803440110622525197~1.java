/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.BitSet;
import java.util.Enumeration;

/**
 * Implements common functionality for a lucene index.
 * <p/>
 * Note on synchronization: This class is not entirely thread-safe. Certain
 * concurrent access is however allowed. Read-only access on this index using
 * {@link #getReadOnlyIndexReader()} is thread-safe. That is, multiple threads
 * my call that method concurrently and use the returned IndexReader at the same
 * time.<br/>
 * Modifying threads must be synchronized externally in a way that only one
 * thread is using the returned IndexReader and IndexWriter instances returned
 * by {@link #getIndexReader()} and {@link #getIndexWriter()} at a time.<br/>
 * Concurrent access by <b>one</b> modifying thread and multiple read-only
 * threads is safe!
 */
abstract class AbstractIndex {

    /** The logger instance for this class */
    private static final Logger log = LoggerFactory.getLogger(AbstractIndex.class);

    /** PrintStream that pipes all calls to println(String) into log.info() */
    private static final LoggingPrintStream STREAM_LOGGER = new LoggingPrintStream();

    /** The currently set IndexWriter or <code>null</code> if none is set */
    private IndexWriter indexWriter;

    /** The currently set IndexReader or <code>null</code> if none is set */
    private CommittableIndexReader indexReader;

    /** The underlying Directory where the index is stored */
    private Directory directory;

    /** Analyzer we use to tokenize text */
    private Analyzer analyzer;

    /** Compound file flag */
    private boolean useCompoundFile = true;

    /** minMergeDocs config parameter */
    private int minMergeDocs = SearchIndex.DEFAULT_MIN_MERGE_DOCS;

    /** maxMergeDocs config parameter */
    private int maxMergeDocs = SearchIndex.DEFAULT_MAX_MERGE_DOCS;

    /** mergeFactor config parameter */
    private int mergeFactor = SearchIndex.DEFAULT_MERGE_FACTOR;

    /** maxFieldLength config parameter */
    private int maxFieldLength = SearchIndex.DEFAULT_MAX_FIELD_LENGTH;

    /**
     * The document number cache if this index may use one.
     */
    private DocNumberCache cache;

    /** The shared IndexReader for all read-only IndexReaders */
    private SharedIndexReader sharedReader;

    /**
     * The indexing queue.
     */
    private IndexingQueue indexingQueue;

    /**
     * Constructs an index with an <code>analyzer</code> and a
     * <code>directory</code>.
     *
     * @param analyzer      the analyzer for text tokenizing.
     * @param directory     the underlying directory.
     * @param cache         the document number cache if this index should use
     *                      one; otherwise <code>cache</code> is
     *                      <code>null</code>.
     * @param indexingQueue the indexing queue.
     * @throws IOException if the index cannot be initialized.
     */
    AbstractIndex(Analyzer analyzer,
                  Directory directory,
                  DocNumberCache cache,
                  IndexingQueue indexingQueue) throws IOException {
        this.analyzer = analyzer;
        this.directory = directory;
        this.cache = cache;
        this.indexingQueue = indexingQueue;

        if (!IndexReader.indexExists(directory)) {
            indexWriter = new IndexWriter(directory, analyzer, true);
            // immediately close, now that index has been created
            indexWriter.close();
            indexWriter = null;
        }
    }

    /**
     * Default implementation returns the same instance as passed
     * in the constructor.
     *
     * @return the directory instance passed in the constructor
     * @throws IOException
     */
    Directory getDirectory() throws IOException {
        return directory;
    }

    /**
     * Adds a document to this index and invalidates the shared reader.
     *
     * @param doc the document to add.
     * @throws IOException if an error occurs while writing to the index.
     */
    void addDocument(Document doc) throws IOException {
        // check if text extractor completed its work
        doc = getFinishedDocument(doc);
        getIndexWriter().addDocument(doc);
        invalidateSharedReader();
    }

    /**
     * Removes the document from this index. This call will not invalidate
     * the shared reader. If a subclass whishes to do so, it should overwrite
     * this method and call {@link #invalidateSharedReader()}.
     *
     * @param idTerm the id term of the document to remove.
     * @throws IOException if an error occurs while removing the document.
     * @return number of documents deleted
     */
    int removeDocument(Term idTerm) throws IOException {
        return getIndexReader().deleteDocuments(idTerm);
    }

    /**
     * Returns an <code>IndexReader</code> on this index. This index reader
     * may be used to delete documents.
     *
     * @return an <code>IndexReader</code> on this index.
     * @throws IOException if the reader cannot be obtained.
     */
    protected synchronized IndexReader getIndexReader() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
            log.debug("closing IndexWriter.");
            indexWriter = null;
        }
        if (indexReader == null) {
            indexReader = new CommittableIndexReader(IndexReader.open(getDirectory()));
        }
        return indexReader;
    }

    /**
     * Returns a read-only index reader, that can be used concurrently with
     * other threads writing to this index. The returned index reader is
     * read-only, that is, any attempt to delete a document from the index
     * will throw an <code>UnsupportedOperationException</code>.
     *
     * @return a read-only index reader.
     * @throws IOException if an error occurs while obtaining the index reader.
     */
    synchronized ReadOnlyIndexReader getReadOnlyIndexReader()
            throws IOException {
        // get current modifiable index reader
        IndexReader modifiableReader = getIndexReader();
        // capture snapshot of deleted documents
        BitSet deleted = new BitSet(modifiableReader.maxDoc());
        for (int i = 0; i < modifiableReader.maxDoc(); i++) {
            if (modifiableReader.isDeleted(i)) {
                deleted.set(i);
            }
        }
        if (sharedReader == null) {
            // create new shared reader
            CachingIndexReader cr = new CachingIndexReader(IndexReader.open(getDirectory()), cache);
            sharedReader = new SharedIndexReader(cr);
        }
        return new ReadOnlyIndexReader(sharedReader, deleted);
    }

    /**
     * Returns an <code>IndexWriter</code> on this index.
     * @return an <code>IndexWriter</code> on this index.
     * @throws IOException if the writer cannot be obtained.
     */
    protected synchronized IndexWriter getIndexWriter() throws IOException {
        if (indexReader != null) {
            indexReader.close();
            log.debug("closing IndexReader.");
            indexReader = null;
        }
        if (indexWriter == null) {
            indexWriter = new IndexWriter(getDirectory(), analyzer, false);
            // since lucene 2.0 setMaxBuffereDocs is equivalent to previous minMergeDocs attribute
            indexWriter.setMaxBufferedDocs(minMergeDocs);
            indexWriter.setMaxMergeDocs(maxMergeDocs);
            indexWriter.setMergeFactor(mergeFactor);
            indexWriter.setMaxFieldLength(maxFieldLength);
            indexWriter.setUseCompoundFile(useCompoundFile);
            indexWriter.setInfoStream(STREAM_LOGGER);
        }
        return indexWriter;
    }

    /**
     * Commits all pending changes to the underlying <code>Directory</code>.
     * @throws IOException if an error occurs while commiting changes.
     */
    protected void commit() throws IOException {
        commit(false);
    }

    /**
     * Commits all pending changes to the underlying <code>Directory</code>.
     *
     * @param optimize if <code>true</code> the index is optimized after the
     *                 commit.
     * @throws IOException if an error occurs while commiting changes.
     */
    protected synchronized void commit(boolean optimize) throws IOException {
        // if index is not locked there are no pending changes
        if (!IndexReader.isLocked(getDirectory())) {
            return;
        }

        if (indexReader != null) {
            indexReader.commitDeleted();
        }
        if (indexWriter != null) {
            log.debug("committing IndexWriter.");
            indexWriter.close();
            indexWriter = null;
        }
        // optimize if requested
        if (optimize) {
            IndexWriter writer = getIndexWriter();
            writer.optimize();
            writer.close();
            indexWriter = null;
        }
    }

    /**
     * Closes this index, releasing all held resources.
     */
    synchronized void close() {
        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.warn("Exception closing index writer: " + e.toString());
            }
            indexWriter = null;
        }
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (IOException e) {
                log.warn("Exception closing index reader: " + e.toString());
            }
            indexReader = null;
        }
        if (sharedReader != null) {
            try {
                sharedReader.close();
            } catch (IOException e) {
                log.warn("Exception closing index reader: " + e.toString());
            }
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (IOException e) {
                directory = null;
            }
        }
    }

    /**
     * Closes the shared reader.
     *
     * @throws IOException if an error occurs while closing the reader.
     */
    protected synchronized void invalidateSharedReader() throws IOException {
        // invalidate shared reader
        if (sharedReader != null) {
            sharedReader.close();
            sharedReader = null;
        }
    }

    /**
     * Returns a document that is finished with text extraction and is ready to
     * be added to the index.
     *
     * @param doc the document to check.
     * @return <code>doc</code> if it is finished already or a stripped down
     *         copy of <code>doc</code> without text extractors.
     * @throws IOException if the document cannot be added to the indexing
     *                     queue.
     */
    private Document getFinishedDocument(Document doc) throws IOException {
        if (!Util.isDocumentReady(doc)) {
            Document copy = new Document();
            for (Enumeration fields = doc.fields(); fields.hasMoreElements(); ) {
                Field f = (Field) fields.nextElement();
                Field field = null;
                Field.TermVector tv = getTermVectorParameter(f);
                Field.Store stored = getStoreParameter(f);
                Field.Index indexed = getIndexParameter(f);
                if (f.readerValue() != null) {
                    // replace all readers with empty string reader
                    field = new Field(f.name(), new StringReader(""), tv);
                } else if (f.stringValue() != null) {
                    field = new Field(f.name(), f.stringValue(),
                            stored, indexed, tv);
                } else if (f.isBinary()) {
                    field = new Field(f.name(), f.binaryValue(), stored);
                }
                if (field != null) {
                    field.setOmitNorms(f.getOmitNorms());
                    copy.add(field);
                }
            }
            // schedule the original document for later indexing
            Document existing = indexingQueue.addDocument(doc);
            if (existing != null) {
                // the queue already contained a pending document for this
                // node. -> dispose the document
                Util.disposeDocument(existing);
            }
            // use the stripped down copy for now
            doc = copy;
        }
        return doc;
    }

    //-------------------------< properties >-----------------------------------

    /**
     * The lucene index writer property: useCompountFile
     */
    void setUseCompoundFile(boolean b) {
        useCompoundFile = b;
        if (indexWriter != null) {
            indexWriter.setUseCompoundFile(b);
        }
    }

    /**
     * The lucene index writer property: minMergeDocs
     */
    void setMinMergeDocs(int minMergeDocs) {
        this.minMergeDocs = minMergeDocs;
        if (indexWriter != null) {
            // since lucene 2.0 setMaxBuffereDocs is equivalent to previous minMergeDocs attribute
            indexWriter.setMaxBufferedDocs(minMergeDocs);
        }
    }

    /**
     * The lucene index writer property: maxMergeDocs
     */
    void setMaxMergeDocs(int maxMergeDocs) {
        this.maxMergeDocs = maxMergeDocs;
        if (indexWriter != null) {
            indexWriter.setMaxMergeDocs(maxMergeDocs);
        }
    }

    /**
     * The lucene index writer property: mergeFactor
     */
    void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
        if (indexWriter != null) {
            indexWriter.setMergeFactor(mergeFactor);
        }
    }

    /**
     * The lucene index writer property: maxFieldLength
     */
    void setMaxFieldLength(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
        if (indexWriter != null) {
            indexWriter.setMaxFieldLength(maxFieldLength);
        }
    }

    //------------------------------< internal >--------------------------------

    /**
     * Returns the index parameter set on <code>f</code>.
     *
     * @param f a lucene field.
     * @return the index parameter on <code>f</code>.
     */
    private Field.Index getIndexParameter(Field f) {
        if (!f.isIndexed()) {
            return Field.Index.NO;
        } else if (f.isTokenized()) {
            return Field.Index.TOKENIZED;
        } else {
            return Field.Index.UN_TOKENIZED;
        }
    }

    /**
     * Returns the store parameter set on <code>f</code>.
     *
     * @param f a lucene field.
     * @return the store parameter on <code>f</code>.
     */
    private Field.Store getStoreParameter(Field f) {
        if (f.isCompressed()) {
            return Field.Store.COMPRESS;
        } else if (f.isStored()) {
            return Field.Store.YES;
        } else {
            return Field.Store.NO;
        }
    }

    /**
     * Returns the term vector parameter set on <code>f</code>.
     *
     * @param f a lucene field.
     * @return the term vector parameter on <code>f</code>.
     */
    private Field.TermVector getTermVectorParameter(Field f) {
        if (f.isStorePositionWithTermVector() && f.isStoreOffsetWithTermVector()) {
            return Field.TermVector.WITH_POSITIONS_OFFSETS;
        } else if (f.isStorePositionWithTermVector()) {
            return Field.TermVector.WITH_POSITIONS;
        } else if (f.isStoreOffsetWithTermVector()) {
            return Field.TermVector.WITH_OFFSETS;
        } else if (f.isTermVectorStored()) {
            return Field.TermVector.YES;
        } else {
            return Field.TermVector.NO;
        }
    }

    /**
     * Adapter to pipe info messages from lucene into log messages.
     */
    private static final class LoggingPrintStream extends PrintStream {

        /** Buffer print calls until a newline is written */
        private StringBuffer buffer = new StringBuffer();

        public LoggingPrintStream() {
            super(new OutputStream() {
                public void write(int b) {
                    // do nothing
                }
            });
        }

        public void print(String s) {
            buffer.append(s);
        }

        public void println(String s) {
            buffer.append(s);
            log.debug(buffer.toString());
            buffer.setLength(0);
        }
    }
}
