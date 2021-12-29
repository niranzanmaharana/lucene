package com.baeldung.lucene.inmemory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class LuceneInMemoryIndexing {
    static void writeIndex(RAMDirectory ramDir, Analyzer analyzer) {
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(ramDir, iwc);

            indexDoc(writer, "document-1", "hello world");
            indexDoc(writer, "document-2", "hello happy world");
            indexDoc(writer, "document-3", "hello happy world");
            indexDoc(writer, "document-4", "hello hello world");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void indexDoc(IndexWriter writer, String name, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name", name, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        writer.addDocument(doc);
    }

    static void searchIndex(RAMDirectory ramDir, Analyzer analyzer) {
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(ramDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser qp = new QueryParser("content", analyzer);
            Query query = qp.parse("happy");
            TopDocs foundDocs = searcher.search(query, 10);
            System.out.println("Total Results :: " + foundDocs.totalHits);
            for (ScoreDoc sd : foundDocs.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                System.out.println("Document Name : " + d.get("name")
                        + "  :: Content : " + d.get("content")
                        + "  :: Score : " + sd.score);
            }
            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        writeIndex(ramDir, analyzer);
        searchIndex(ramDir, analyzer);
    }
}
