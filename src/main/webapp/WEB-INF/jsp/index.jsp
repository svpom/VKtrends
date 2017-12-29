<html>
<%@ page pageEncoding="UTF-8" %>
<head>
    <title>VKtrends</title>
</head>

<body>
<form action="/VKtrends/get-posts">
    <h2><b>Введите полные URL-адреса сообществ, каждый с новой строки:</b></h2>
    <p><textarea name="adresses"></textarea></p>

    <h2>Фильтры:</h2>
        <h3>Выбирать по числу...</h3>
        <input type="radio" name="by-likes">лайков<Br>
        <input type="radio" name="by-reposts">репостов

        <h3>Доля выбираемых записей:</h3>
        <input type="number" name="percentage"> %<Br>

        <h3>За последние:</h3>
        <input type="number" name="years"> лет<Br>
        <input type="number" name="months"> месяцев<Br>
        <input type="number" name="days"> дней<Br>

    <p><input type="submit"></p>
</form>
</body>
</html>