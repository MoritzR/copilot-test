ALTER TABLE customers ADD COLUMN github_username VARCHAR(255);
CREATE UNIQUE INDEX idx_customers_github_username ON customers(github_username);