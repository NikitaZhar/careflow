INSERT INTO users (username, email, password, role)
SELECT 'admin', 'nikita.zharinov@gmail.com', '123', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);