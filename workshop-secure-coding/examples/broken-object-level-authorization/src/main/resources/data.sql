-- Workshop-only accounts. Both entries use BCrypt hashes, not plaintext passwords.
INSERT INTO user_account (id, username, password, enabled) VALUES
    (1, 'alice', '$2y$10$WlVnIj63TKNmJi9T75ohwuAj1N7IW3R8Kfd9kdZ.DTk6mxSmBKEYe', TRUE),
    (2, 'bob',   '$2y$10$wjgLpVE/v2QRqeEQFYCNk.ETnYxKNN.pWTQUJ1soXzU5KexgWk2ry', TRUE);

-- Alice's orders are 1001 and 1003. Bob's order is 1002.
INSERT INTO customer_order (id, description, owner_id) VALUES
    (1001, 'Alice: Notebook', 1),
    (1002, 'Bob: Camera',     2),
    (1003, 'Alice: Pencil',   1);
