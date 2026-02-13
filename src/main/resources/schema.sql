DROP TABLE IF EXISTS status_history;
DROP TABLE IF EXISTS job_application;
DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS user_details;

CREATE TABLE company (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL DEFAULT 'Germany',
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE user_details (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE job_application (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_details(id),
    company_id UUID NOT NULL REFERENCES company(id),
    title VARCHAR(255) NOT NULL,
    source VARCHAR(50),
    status VARCHAR(50) CHECK (status IN ('APPLIED','INTERVIEW','OFFER','REJECTED','WITHDRAWN')),
    applied_date DATE,
    job_url TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE job_application_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES job_application(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES user_details(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    notes TEXT,
    updated_date TIMESTAMP NOT NULL DEFAULT NOW()
);

