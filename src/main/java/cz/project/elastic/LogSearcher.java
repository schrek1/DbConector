package cz.project.elastic;

import cz.project.Const;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Vyhledavani logu z elasticu
 */
public class LogSearcher {

    private static final int WAIT_TIME_TO_SCROLL = 60000; //ms
    public static final int RESULT_WINDOW_SIZE = 5;

    private TransportClient client;

    public LogSearcher() {
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(
                    new TransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<String> findInAllFields(String text) {
        List<String> results = new ArrayList<>();
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("*\"" + text + "\"*")
                .defaultOperator(Operator.AND)
                .field("_all");


        SearchResponse scrollResponse = client
                .prepareSearch(Const.ELASTIC_LOG_INDEX_NAME)
                .addSort("time", SortOrder.DESC)
                .setScroll(new TimeValue(WAIT_TIME_TO_SCROLL))
                .setQuery(queryBuilder)
                .setSize(RESULT_WINDOW_SIZE).execute().actionGet();

        //Scroll until no hits are returned
        while (true) {
            SearchHit[] hits = scrollResponse.getHits().getHits();

            //Break condition: No hits are returned
            if (hits.length == 0) {
                break;
            }

            // otherwise read results
            for (SearchHit hit : hits) {
                results.add(hit.getSourceAsString());
            }

            // prepare next query
            scrollResponse = client
                    .prepareSearchScroll(scrollResponse.getScrollId())
                    .setScroll(new TimeValue(WAIT_TIME_TO_SCROLL))
                    .execute()
                    .actionGet();
        }


        return results;
    }
}
