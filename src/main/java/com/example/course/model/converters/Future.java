package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.json.JSONException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Future {
    public Future(Date date) {

    }

/*    public void test(String url, String dateCh, List<Exchange> array) throws Exception {
        CompletableFuture<List<Exchange>> future = CompletableFuture.supplyAsync(() ->  arrayExchange(url, dateCh, array););

    public List<Exchange> arrayExchange(String url, String dateCh, List<Exchange> array) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + dateCh, String.class);
        ParsJson parsJson = new ParsJson(resultJson);
        Exchange result  = parsJson.parsJson();
        array.add(result);
        return array;
    }*/
}
