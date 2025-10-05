 CREATE TABLE IF NOT EXISTS admins (
     id UUID PRIMARY KEY,
     name VARCHAR(255) NOT NULL UNIQUE,
     password VARCHAR(255) NOT NULL
 );

-- INSERT INTO admins (id, name, password)
-- VALUES (gen_random_uuid(), 'Sahil', '4444');

 
 
