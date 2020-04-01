package com.example.course.model.services;


import com.example.course.model.exchange.Exchange;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public interface ExchangeRatesSearchIn {
   String searcExcange(List<String> param) throws JSONException, IOException;
}
