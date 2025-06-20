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