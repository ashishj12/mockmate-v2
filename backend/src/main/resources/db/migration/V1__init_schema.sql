-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- industry_insights table (referenced by users)
CREATE TABLE industry_insights (
    id              VARCHAR(30)  PRIMARY KEY DEFAULT 'ci_' || encode(gen_random_bytes(10), 'hex'),
    industry        VARCHAR(255) NOT NULL UNIQUE,
    salary_ranges   JSONB        NOT NULL DEFAULT '[]',
    growth_rate     DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    demand_level    VARCHAR(50)  NOT NULL DEFAULT 'Medium',
    top_skills      TEXT[]       NOT NULL DEFAULT '{}',
    market_outlook  VARCHAR(50)  NOT NULL DEFAULT 'neutral',
    key_trends      TEXT[]       NOT NULL DEFAULT '{}',
    recommended_skills TEXT[]    NOT NULL DEFAULT '{}',
    last_updated    TIMESTAMP    NOT NULL DEFAULT NOW(),
    next_update     TIMESTAMP    NOT NULL DEFAULT NOW() + INTERVAL '7 days'
);

CREATE INDEX idx_industry_insights_industry ON industry_insights(industry);


-- users table
CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    clerk_user_id   VARCHAR(255) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255),
    image_url       TEXT,
    industry        VARCHAR(255),
    bio             TEXT,
    experience      INTEGER,
    skills          TEXT[]       NOT NULL DEFAULT '{}',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_users_industry
        FOREIGN KEY (industry)
        REFERENCES industry_insights(industry)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE INDEX idx_users_clerk_user_id ON users(clerk_user_id);
CREATE INDEX idx_users_email ON users(email);

-- assessments table
CREATE TABLE assessments (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL,
    quiz_score      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    questions       JSONB        NOT NULL DEFAULT '[]',
    category        VARCHAR(100) NOT NULL,
    improvement_tip TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_assessments_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_assessments_user_id ON assessments(user_id);

-- resumes table
CREATE TABLE resumes (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID         NOT NULL UNIQUE,
    content     TEXT         NOT NULL DEFAULT '',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_resumes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- ats_analyses table
CREATE TABLE ats_analyses (
    id                  VARCHAR(30)  PRIMARY KEY DEFAULT 'ca_' || encode(gen_random_bytes(10), 'hex'),
    resume_id           UUID         NOT NULL UNIQUE,
    overall_score       INTEGER      NOT NULL DEFAULT 0,
    keyword_match_score INTEGER      NOT NULL DEFAULT 0,
    format_score        INTEGER      NOT NULL DEFAULT 0,
    skills_score        INTEGER      NOT NULL DEFAULT 0,
    experience_score    INTEGER      NOT NULL DEFAULT 0,
    matched_keywords    TEXT[]       NOT NULL DEFAULT '{}',
    missing_keywords    TEXT[]       NOT NULL DEFAULT '{}',
    job_description     TEXT         NOT NULL,
    job_title           VARCHAR(255),
    company_name        VARCHAR(255),
    improvements        JSONB        NOT NULL DEFAULT '[]',
    suggestions         JSONB        NOT NULL DEFAULT '[]',
    total_keywords      INTEGER      NOT NULL DEFAULT 0,
    matched_count       INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ats_analyses_resume
        FOREIGN KEY (resume_id)
        REFERENCES resumes(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ats_analyses_resume_id ON ats_analyses(resume_id);

-- cover_letters table
CREATE TABLE cover_letters (
    id              VARCHAR(30)  PRIMARY KEY DEFAULT 'cl_' || encode(gen_random_bytes(10), 'hex'),
    user_id         UUID         NOT NULL,
    content         TEXT         NOT NULL DEFAULT '',
    job_description TEXT,
    company_name    VARCHAR(255) NOT NULL,
    job_title       VARCHAR(255) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'draft',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cover_letters_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_cover_letters_user_id ON cover_letters(user_id);

-- Auto-update updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at        BEFORE UPDATE ON users         FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_assessments_updated_at  BEFORE UPDATE ON assessments   FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_resumes_updated_at      BEFORE UPDATE ON resumes       FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ats_updated_at          BEFORE UPDATE ON ats_analyses  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cover_letters_updated_at BEFORE UPDATE ON cover_letters FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_industry_updated_at     BEFORE UPDATE ON industry_insights FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();