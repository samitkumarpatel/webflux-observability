ALTER TABLE person
    ADD COLUMN created_by text NOT NULL DEFAULT current_user,
    ADD COLUMN updated_by text NOT NULL DEFAULT current_user,
    ADD COLUMN created_at timestamp DEFAULT current_timestamp,
    ADD COLUMN updated_at timestamp DEFAULT current_timestamp;