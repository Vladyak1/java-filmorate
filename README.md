# java-filmorate
Template repository for Filmorate project.

![ER-diagram](/assets/images/diagram.png)

Приложение позволяет хранить 2 основные сущности: films, users. 
Первичными ключами для этих сущностей является значение идентификатора\
Если необходимо увидеть выгрузку фильмов нынешнего 2024 года, то можно воспользоваться следующей формулой SQL

SELECT * \
FROM Film \
WHERE EXTRACT(YEAR FROM releaseDate) = 2024;