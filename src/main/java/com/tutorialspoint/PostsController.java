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
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;


@Controller
@RequestMapping("/show-posts")
public class PostsController {
    private final int MAX_POSTS_CAN_GET = 100;//by vk api
    private final String ACCESS_TOKEN =
            "587ca5f3587ca5f3587ca5f3b858314fa85587c587ca5f30265146d92f285657d6f1c80";//from my old vk app ChatBox
    private final int SECONDS_IN_DAY = 86400;
    private final int MAX_ATTACHMENTS_PER_POST = 10;//вроде максимум 10 приложений может быть к посту

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
        /*ArrayList<Post> allPosts = new ArrayList<>();*/
        long currentDate = Instant.now().getEpochSecond();
        long searchPeriod = (years * 365 + months * 31 + days) *
                SECONDS_IN_DAY;
        ArrayList<Post> selectedPosts = new ArrayList<>();

        for (String address : addressesArray) {
            ArrayList<Post> allPostsForAddress = new ArrayList<>();
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
                    allPostsForAddress.addAll(posts);
                } else {
                    for (Post post : posts) {
                        if (currentDate - post.date < searchPeriod) {
                            allPostsForAddress.add(post);
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
            selectedPosts.addAll(selectPostsByPercentage(allPostsForAddress, percentage));
        }
        return selectedPosts;
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
            JsonNode idNode = itemNode.path("id");
            //Создаём пост с указанной датой и текстом. До настройки setenv.bat tomcat'a требовалась перекодировка.
            /*posts.add(new Post(dateNode.asLong(),
                    new String(textNode.asText().getBytes("cp1251"), "UTF-8")));*/
            JsonNode likesNode = itemNode.path("likes");
            JsonNode likesCountNode = likesNode.path("count");
/*            posts.add(new Post(idNode.asLong(), dateNode.asLong(),
                    textNode.asText(), likesCountNode.asLong()));*/
            Post post = new Post(idNode.asLong(), dateNode.asLong(),
                    textNode.asText(), likesCountNode.asLong());

            //Добавляем прикреплённые фото, видео, аудио.
            JsonNode attachmentsNode = itemNode.path("attachments");
            if (!attachmentsNode.isMissingNode()) {
                for (int i = 0; i < MAX_ATTACHMENTS_PER_POST; i++) {
                    JsonNode attachment = attachmentsNode.path(i);
                    //Если приложение со следующим номером есть, то добавляем к объекту Post, а если нет - завершаем
                    //добавление приложений.
                    if (!attachment.isMissingNode()){
                        JsonNode typeNode = attachment.path("type");
                        switch (typeNode.asText()) {
                            case "photo":
                                JsonNode photoNode = attachment.path("photo");
                                //В вк возможны варианты 75, 130, 604 и т.д. пикселей. ИМХО, 604 оптимально и есть почти для всех постов.
                                JsonNode photo_604 = photoNode.path("photo_604");
                                post.addPhoto(photo_604.asText());
                                break;
                            case "video":
                                //code
                                break;
                            case "audio":
                                //code
                                break;
                        }
                    } else
                        break;
                }
            }
            posts.add(post);
        }
        return posts;
    }

    private ArrayList<Post> selectPostsByPercentage(ArrayList<Post> allPosts, int percentage){
        //Записываем массив чисел лайков.
        long[] allPostsLikes = new long[allPosts.size()];
        for (int i = 0; i < allPosts.size(); i++)
            allPostsLikes[i] = allPosts.get(i).likesCount;
        //Сортируем массив числа лайков в порядке ВОЗРАСТАНИЯ.
        Arrays.sort(allPostsLikes);

        //число постов за период умножить на долю
        int numberOfPostsToSelect = Math.round(allPosts.size() * percentage / 100);
        long[] selectedPostsLikes = new long[numberOfPostsToSelect];
        //Массив с числом лайков от наибольшего до меньшего значений в пределах указанного процента от общего числа.
        for (int i = 0; i < numberOfPostsToSelect; i++)
            selectedPostsLikes[i] = allPostsLikes[(allPosts.size() - 1) - i];

        ArrayList<Post> selectedPosts = new ArrayList<>();
        for (Post post : allPosts) {
            //Если в массиве наибольших лайков есть значение, равное числу лайков у конкретного поста.
            for (int i = 0; i < numberOfPostsToSelect; i++) {
                if (post.likesCount == selectedPostsLikes[i]) {
                    selectedPosts.add(post);
                    break;
                }
            }
        }
        //Причём заметь, что по постам проходимся от новых до старых, т.е. сперва будут идти более
        //свежие посты, но не с бОльшим числом лайков.
        return selectedPosts;
    }
}

