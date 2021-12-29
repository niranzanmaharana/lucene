package com.baeldung.lucene.directory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LuceneDirectoryIndexing {
    private static final String INDEX_DIR = System.getProperty("java.io.tmpdir");

    private static IndexWriter createWriter() throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);
        return writer;
    }

    private static Document createDocument(Integer id, String firstName, String lastName, String website) {
        Document document = new Document();
        document.add(new StringField("id", id.toString() , Field.Store.YES));
        document.add(new TextField("firstName", firstName , Field.Store.YES));
        document.add(new TextField("lastName", lastName , Field.Store.YES));
        document.add(new TextField("website", website , Field.Store.YES));
        return document;
    }

    public static void writeIndex() throws IOException {
        IndexWriter writer = createWriter();
        List<Document> documents = new ArrayList<>();

        Document document1 = createDocument(1, "Lokesh", "Gupta", "howtodoinjava.com");
        documents.add(document1);

        Document document2 = createDocument(2, "Brian", "Schultz", "example.com");
        documents.add(document2);

        writer.deleteAll();
        writer.addDocuments(documents);
        writer.commit();
        writer.close();
    }

    private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
        Query firstNameQuery = qp.parse(firstName);
        TopDocs hits = searcher.search(firstNameQuery, 10);
        return hits;
    }

    private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("id", new StandardAnalyzer());
        Query idQuery = qp.parse(id.toString());
        TopDocs hits = searcher.search(idQuery, 10);
        return hits;
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }

    public static void searchIndex() throws Exception {
        IndexSearcher searcher = createSearcher();
        TopDocs docFoundById = searchById(1, searcher);
        System.out.println("Search By ID(1) Total Results :: " + docFoundById.totalHits);

        for (ScoreDoc scoreDoc : docFoundById.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            System.out.println(String.format("ID: %s,\t Name: %s %s,\t Website: %s", document.get("id"), document.get("firstName"), document.get("lastName"), document.get("website")));
        }

        TopDocs docsFoundByFirstName = searchByFirstName("Brian", searcher);
        System.out.println("\nSearch By First Name(Brian) Total Results :: " + docsFoundByFirstName.totalHits);
        for (ScoreDoc scoreDoc : docsFoundByFirstName.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            System.out.println(String.format("ID: %s,\t Name: %s %s,\t Website: %s", document.get("id"), document.get("firstName"), document.get("lastName"), document.get("website")));
        }
    }

    public static void main(String[] args) throws Exception {
        writeIndex();
        searchIndex();
    }
}
