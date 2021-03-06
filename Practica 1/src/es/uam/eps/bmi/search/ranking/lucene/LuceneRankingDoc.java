package es.uam.eps.bmi.search.ranking.lucene;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;

/**
 *
 * @author pablo
 */
public class LuceneRankingDoc extends SearchRankingDoc {
    Index index;
    ScoreDoc rankedDoc;
    
    LuceneRankingDoc (Index idx, ScoreDoc r) {
        super(r);
        index = idx;
        rankedDoc = r;
    }

    @Override
    public double getScore() {
        return rankedDoc.score;
    }

    @Override
    public String getPath() throws IOException {
        return index.getDocPath(rankedDoc.doc);
    }
}
