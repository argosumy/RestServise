package com.example.course.controller;

import com.example.course.model.converters.TypeBank;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import com.example.course.model.services.ExchangeRatesSearch;
import org.json.JSONException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Component
@RestController
public class CourseController {
    private final ExchangeRatesSearch service;

    public CourseController(ExchangeRatesSearch service) {
        this.service = service;
    }
/*Вид запроса
*http://localhost:8888//date=22.03.2020/cur=USD/xml
* */
    @GetMapping(value = "date={date}/cur={cur}/xml", produces = {MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Object> exchangeRateDayXML(@PathVariable("date") String date,@PathVariable(value = "cur") String cur) throws JSONException, IOException {
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.ok(service.searcExcangeBanks(date,"json",cur));
    }
    @GetMapping(value = "date={date}/cur={cur}/json", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> exchangeRateDayJson(@PathVariable("date") String date,@PathVariable(value = "cur") String cur) throws JSONException, IOException {
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.ok(service.searcExcangeBanks(date,"json",cur));
    }

    //Лучший курс по банкам на указаную дату
    @GetMapping(value = "best/date={date}/cur={cur}")
    public ResponseEntity<Object> exchangeRateDayBest(@PathVariable String date, @PathVariable String cur) throws ParserConfigurationException, SAXException {
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.ok(service.bestCurseDay(date,"json",cur));
    }

    //Лучший курс по банкам за прошедшую неделю
    @GetMapping(value = "best/week={cur}")
    public String exchangeRateWeek(@PathVariable String cur) {
                    return service.bestCurseWeek(cur);
    }

    @GetMapping(value = "/download/date={date}/cur={cur}")
    public ResponseEntity<?> getFile(@PathVariable("date") String date,@PathVariable(value = "cur") String cur) throws Exception {
        WordDoc wordDoc = new WordDoc();
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return ResponseEntity.ok(answer);
        }
        Map<TypeBank.typeBank,List<Exchange>> typeBankListMap = service.searcExcangeBanks(date,"json",cur);
        byte[] byteArray = wordDoc.wordWrite(typeBankListMap);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(byteArray));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=exchange.docx")
                .body(resource);
    }




}
