package com.example.course.model.services;


import com.example.course.model.converters.TypeBank;
import com.example.course.model.exchange.Exchange;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExchangeRatesSearchIn {
   Map<TypeBank.typeBank, List<Exchange>> searcExcange(List<String> param) throws JSONException, IOException;
   String bestCurseDay(List<String> param);
   String bestCurseWeek(List<String> param);
   String validDate(List<String> param);
}
