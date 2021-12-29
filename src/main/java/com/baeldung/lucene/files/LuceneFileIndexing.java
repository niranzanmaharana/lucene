package com.baeldung.lucene.files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class LuceneFileIndexing {
    private static final String INPUT_DIR = "C:\\Users\\SG0313239\\project\\workspace\\poc\\lucene\\src\\main\\resources\\input";
    private static final String INDEX_DIR = System.getProperty("java.io.tmpdir");

    public static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    public static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try {
            //Create lucene Document
            Document document = new Document();

            document.add(new StringField("path", file.toString(), Field.Store.YES));
            document.add(new LongPoint("modified", lastModified));
            document.add(new TextField("contents", new String(Files.readAllBytes(file)), Field.Store.YES));

            //Updates a document by first deleting the document(s)
            //containing <code>term</code> and then adding the new
            //document.  The delete and then add are atomic as seen
            //by a reader on the same index
            writer.updateDocument(new Term("path", file.toString()), document);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
        return searcher.search(query, 10);
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

    public static void writeDocs() {
        final Path docDir = Paths.get(INPUT_DIR);
        try {
            Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
            Analyzer analyzer = new StandardAnalyzer();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void searchDocs() throws Exception {
        IndexSearcher searcher = createSearcher();
        TopDocs foundDocs = searchInContent("SELECT ", searcher);
        System.out.println("Total Results :: " + foundDocs.totalHits);
        for (ScoreDoc scoreDoc : foundDocs.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            System.out.println("Path : "+ document.get("path") + ", Score : " + scoreDoc.score);
        }
    }

    public static void main(String[] args) throws Exception {
        writeDocs();
        searchDocs();
    }
}
