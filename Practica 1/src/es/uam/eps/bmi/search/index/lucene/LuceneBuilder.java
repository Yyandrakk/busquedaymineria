package es.uam.eps.bmi.search.index.lucene;

import es.uam.eps.bmi.search.index.IndexBuilder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LuceneBuilder implements IndexBuilder{

    private IndexWriter m_indexWriter;
    private FieldType typeUrl = new FieldType();
    private FieldType typeText  = new FieldType();

    public LuceneBuilder() {
        typeText.setStored(true);
        typeText.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        typeUrl.setStored(true);
    }

    @Override
    public void build(String collectionPath, String indexPath) throws IOException {

        File filePath = new File(collectionPath);

        if (!filePath.exists()) {
            throw new IOException();
        }

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        Directory directory = FSDirectory.open(Paths.get(indexPath));
        this.m_indexWriter = new IndexWriter(directory,config);


        if(collectionPath.endsWith(".txt")){
            List<String> strings = Files.readAllLines(filePath.toPath());
            strings.parallelStream().forEach(s -> {
                try {
                    org.jsoup.nodes.Document d =Jsoup.connect(s).timeout(20000).get();
                    addDocumento(d);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if(collectionPath.endsWith(".zip")){
            ZipFile zipFile = new ZipFile(collectionPath);
            List<? extends ZipEntry> files = zipFile.stream().parallel().filter((f)->{return !f.isDirectory() ;}).collect(Collectors.toList());
            files.parallelStream().forEach(f -> {
                InputStream is = null;
                try {
                    is = zipFile.getInputStream(f);
                    org.jsoup.nodes.Document d = Jsoup.parse(is,"UTF-8",collectionPath+f.getName());
                    if(is != null)is.close();
                    addDocumento(d);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if(filePath.isDirectory()){

            File[] files = filePath.listFiles();
            Arrays.stream(files).parallel().filter((f)->{return !f.isDirectory() ;}).forEach((f) -> {
                try{
                    org.jsoup.nodes.Document d = Jsoup.parse(f, "UTF-8");
                    addDocumento(d);
                }catch (IOException e){
                    throw new RuntimeException(e);
                }

            });
        }else{
            throw new IOException();
        }

        this.m_indexWriter.close();

    }

    private void addDocumento(org.jsoup.nodes.Document d ) throws IOException {

        if(d==null)return;

        Document doc = new Document();
        Field urlField = new Field("path", d.baseUri(), typeUrl);
        Field contField = new Field("texto", d.normalise().text(), typeText);
        doc.add(urlField);
        doc.add(contField);
        this.m_indexWriter.addDocument(doc);
    }
}