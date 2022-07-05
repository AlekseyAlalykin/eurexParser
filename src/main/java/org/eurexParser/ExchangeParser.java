package org.eurexParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ExchangeParser {
    public static String filePath = "settings.properties";

    public static String url;
    public static String contractType;
    //Загрузка сайта из файла
    static {
        try {
            Properties props = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            props.load(is);
            url = props.getProperty("url");
            contractType = props.getProperty("contractType");
            is.close();
        } catch (IOException exception){
            exception.printStackTrace();
        }
    }

    //Данные погашения из выпадающего списка
    public List<String> getMaturityDate() throws IOException{
        Document doc = Jsoup.connect(url).get();
        Element element = doc.getElementById("maturityDate");
        List<String> maturityDates = new ArrayList<>();
        for(Element choice: element.children()){
            if (!choice.attr("value").equals("")){
                maturityDates.add(choice.attr("value"));
            }
        }
        return maturityDates;
    }

    public String getAddress() throws IOException{
        Document doc = Jsoup.connect(url).get();
        Element element = doc.getElementsByAttributeValue("name","productId").get(0);

        return url.substring(0,url.lastIndexOf("/")+1) +
                            element.attr("value") +
                            "!quotesSingleViewOption";
    }

    public void parse() throws IOException {
        List<String> maturityDates = getMaturityDate();
        String address = getAddress();

        //Для записи названия столбцов
        boolean isFirstRowWritten = false;

        CSVWriter writer = new CSVWriter();
        writer.open();

        for (String date:maturityDates){
            Document doc = Jsoup.connect(address + "?callPut=" + contractType + "&maturityDate=" + date)
                                                                                    .timeout(100*1000).get();
            //System.out.println(address + "?callPut=" + contractType + "&maturityDate=" + date);
            Elements elements = doc.getElementsByAttributeValueStarting("id","time");
            Element tbody = elements.get(0);

            String[] line = new String[tbody.child(0).children().size()];

            if (!isFirstRowWritten){
                Element theadRow = tbody.parent().child(0).child(0);
                for (int i=0; i < theadRow.children().size(); i++){
                    line[i] = theadRow.child(i).child(0).ownText();
                }
                writer.writeLine(line);

                isFirstRowWritten = true;
            }

            for (Element tr: tbody.children()){
                //sum это последняя строка в таблице без данных
                if (!tr.hasClass("sum")) {
                    for (int i = 0; i < tr.children().size(); i++) {
                        line[i] = tr.child(i).child(0).ownText();
                    }
                    writer.writeLine(line);
                }
            }

        }

        writer.close();




        /*
        Elements elements = doc.getElementsByAttributeValueStarting("id","time");
        Element table = elements.get(0);
        */


    }

    public static void main(String[] args) {
        ExchangeParser parser = new ExchangeParser();
        try {
            parser.parse();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
