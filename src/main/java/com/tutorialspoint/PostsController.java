package com.tutorialspoint;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

/*import javax.servlet.http.*;*/
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;


@Controller
@RequestMapping("/show-posts")
public class PostsController {
    private final int MAX_POSTS_CAN_GET = 100;//by vk api
    private final String ACCESS_TOKEN =
            "587ca5f3587ca5f3587ca5f3b858314fa85587c587ca5f30265146d92f285657d6f1c80";//from my old vk app ChatBox
    private final int SECONDS_IN_DAY = 86400;

    @RequestMapping(method = RequestMethod.GET)
    public String showPosts(@RequestParam("addresses") String addresses,
                            @RequestParam(value = "by-likes",
                                    required = false) boolean byLikes,
                            @RequestParam(value = "by-reposts",
                                    required = false) boolean byReposts,
                            @RequestParam("percentage") int percentage,
                            @RequestParam(value = "years", defaultValue = "0") int years,
                            @RequestParam(value = "months", defaultValue = "0") int months,
                            @RequestParam(value = "days", defaultValue = "0") int days,
                            ModelMap model) {
        //TODO: ВЫбрать место для обработки exceptions и обработать.
        ArrayList<Post> posts;
        String resultText = new String();
        try {
            posts = getPosts(addresses, byLikes, byReposts,
                    percentage, years, months, days);
            for (Post post : posts)
                resultText += post.toString();
        } catch (Exception ex){
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            resultText += errors.toString();
        }

        model.addAttribute("message", resultText);
        return "posts";
    }

    private ArrayList<Post> getPosts(String addresses, boolean byLikes,
                                     boolean byReposts, int percentage,
                                     int years, int months, int days) throws Exception{
        String[] addressesArray = addresses.split("\\r\\n");

        //Объявляем список постов и вычисляем текущее время и период поиска постов в unixtime
        ArrayList<Post> allPosts = new ArrayList<>();
        long currentDate = Instant.now().getEpochSecond();
        long searchPeriod = (years * 365 + months * 31 + days) *
                SECONDS_IN_DAY;

        for (String address : addressesArray) {
            boolean postDateReachesTimeBorder = false;
            int offsetOfPosts = 0;

            /*TODO: вынеси следующий код в отдельный метод*/
            while (!postDateReachesTimeBorder) {
                ArrayList<Post> posts = toPosts(makeRequest(address,
                        offsetOfPosts));
                //Если пост получен самым первым из сообщества, то он, скорее всего, прикреплён,
                //а значит старый и нечего на него смотреть.
                if (offsetOfPosts == 0) posts.remove(0);

                if (currentDate - posts.get(posts.size() - 1).date < searchPeriod) {
                    allPosts.addAll(posts);
                } else {
                    for (Post post : posts) {
                        if (currentDate - post.date < searchPeriod) {
                            allPosts.add(post);
                        } else {
                            postDateReachesTimeBorder = true;
                            break;
                        }
                    }
                }

                offsetOfPosts += MAX_POSTS_CAN_GET;//=100 by vk api
/*                } catch (Exception ex) {
*//*                    TODO: Добавить обработку исключения при ошибке во время
                    выполнения запроса к vk. Как вариант, выводить пользователю
                    сообщение о том, что произошла ошибка и запрос будет повторен*//*

                }*/
            }
        }
        return allPosts;
    }


    private String makeRequest(String addressOfCommunity, int offset) throws Exception {
        //Из адреса сообщества составляем URL запроса к API.
        String addressForApi = composeApiUrl(addressOfCommunity, offset);

        //Открываем соединение.
        URL url = new URL(addressForApi);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //Создаём объекты для последующего чтения из потока данных.
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        //Считываем https-ответ.
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        //Здесь \n есть в response, а после парсинга JSON'a  - нет, парсер их почему-то удаляет как-то. Ниже - выход из ситуации.
        String responseWithBrTag = response.toString().replace("\\n", "<br>");
        return responseWithBrTag;
    }

    private String composeApiUrl(String addressOfCommunity, int offset) {
        String communityDomain = addressOfCommunity.replace("https://vk.com/", "");

        String addressForApi = "https://api.vk.com/method/wall.get?domain=" +
                communityDomain + "&offset=" + offset +
                "&count=" + MAX_POSTS_CAN_GET +
                "&access_token=" +
                ACCESS_TOKEN + "&v=5.69"; //нормальная версия апи вк

        return addressForApi;
    }

    private ArrayList<Post> toPosts (String response) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        JsonNode responseNode = rootNode.path("response");
        JsonNode itemsNode = responseNode.path("items");

        Iterator<JsonNode> items = itemsNode.elements();
        ArrayList<Post> posts = new ArrayList<Post>();
        while(items.hasNext()) {
            JsonNode itemNode = items.next();

            JsonNode dateNode = itemNode.path("date");
            JsonNode textNode = itemNode.path("text");
            //Создаём пост с указанной датой и текстом. До настройки setenv.bat tomcat'a требовалась перекодировка.
            /*posts.add(new Post(dateNode.asLong(),
                    new String(textNode.asText().getBytes("cp1251"), "UTF-8")));*/
            posts.add(new Post(dateNode.asLong(), textNode.asText()));
        }
        return posts;
    }
}

