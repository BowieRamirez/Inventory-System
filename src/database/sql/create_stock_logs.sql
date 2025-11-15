-- Create stock_logs table to store all stock activity logs
CREATE TABLE IF NOT EXISTS stock_logs (
    ts DATETIME,
    actor VARCHAR(200),
    item_code INT,
    item_name VARCHAR(255),
    size VARCHAR(20),
    delta VARCHAR(20),
    action_type VARCHAR(50),
    details VARCHAR(400),
    INDEX idx_ts (ts)
);

-- Insert sample data to verify table works
INSERT INTO stock_logs (ts, actor, item_code, item_name, size, delta, action_type, details)
VALUES (NOW(), 'system', 0, 'Table Created', 'N/A', '0/0', 'TABLE_CREATED', 'Stock logs table initialized');
