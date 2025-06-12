-- Заполнение справочников
MERGE INTO RATINGS AS r USING
(SELECT q.id,
          q.full_name
     FROM (SELECT 1 AS id,
                  'G' AS full_name
           UNION
           SELECT 2 AS id,
                  'PG' AS full_name
           UNION
           SELECT 3 AS id,
                  'PG-13' AS full_name
           UNION
           SELECT 4 AS id,
                  'R' AS full_name
           UNION
           SELECT 5 AS id,
                  'NC-17' AS full_name) q
    ORDER BY q.id) AS pd ON r.ID = pd.id
WHEN MATCHED THEN UPDATE SET FULL_NAME = pd.full_name
WHEN NOT MATCHED THEN INSERT (ID, FULL_NAME) VALUES (pd.id, pd.full_name);

MERGE INTO GENRES AS g USING
 (SELECT q.id,
         q.full_name
    FROM (SELECT 1 AS id,
                 'Комедия' AS full_name
          UNION
          SELECT 2 AS id,
                 'Драма' AS full_name
          UNION
          SELECT 3 AS id,
                 'Мультфильм' AS full_name
          UNION
          SELECT 4 AS id,
                 'Триллер' AS full_name
          UNION
          SELECT 5 AS id,
                 'Документальный' AS full_name
          UNION
          SELECT 6 AS id,
                 'Боевик' AS full_name) q
   ORDER BY q.id) AS pd ON g.ID = pd.id
WHEN matched THEN UPDATE SET FULL_NAME = pd.full_name
WHEN NOT matched THEN INSERT (ID, FULL_NAME) VALUES (pd.id, pd.full_name);

MERGE INTO RELATIONSHIP_ATTRIBUTES AS ra USING
 (SELECT q.id,
         q.attribute_type,
         q.full_name
    FROM (SELECT 1 AS id,
                 1 AS attribute_type,
                 'Справочники атрибутов' AS full_name
          UNION
          SELECT 2 AS id,
       	         1 AS attribute_type,
       	         'Типы отношений' AS full_name
          UNION
          SELECT 3 AS id,
       	         1 AS attribute_type,
       	         'Статусы отношений' AS full_name
          UNION
          SELECT 4 AS id,
       	         2 AS attribute_type,
       	         'Дружба' AS full_name
          UNION
          SELECT 5 AS id,
       	         2 AS attribute_type,
       	         'Вражда' AS full_name
          UNION
          SELECT 6 AS id,
       	         2 AS attribute_type,
       	         'Семья' AS full_name
          UNION
          SELECT 7 AS id,
       	         2 AS attribute_type,
       	         'Любовь' AS full_name
          UNION
          SELECT 8 AS id,
       	         3 AS attribute_type,
       	         'Запрошено' AS full_name
          UNION
          SELECT 9 AS id,
       	         3 AS attribute_type,
       	         'Одобрено' AS full_name
          UNION
          SELECT 10 AS id,
       	         3 AS attribute_type,
       	         'Отклонено' AS full_name) q
   ORDER BY q.id) AS pd ON ra.ID = pd.id AND ra.ATTRIBUTE_TYPE = pd.attribute_type
WHEN MATCHED THEN UPDATE SET FULL_NAME = pd.full_name
WHEN NOT MATCHED THEN INSERT (ID, ATTRIBUTE_TYPE, FULL_NAME) VALUES (pd.id, pd.attribute_type, pd.full_name);