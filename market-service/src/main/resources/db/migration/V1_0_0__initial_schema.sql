CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS market;

CREATE TABLE market (
      id uuid NOT NULL,
      item1_id character varying COLLATE pg_catalog."default" NOT NULL,
      item1_name character varying COLLATE pg_catalog."default" NOT NULL,
      item2_id character varying COLLATE pg_catalog."default" NOT NULL,
      item2_name character varying COLLATE pg_catalog."default" NOT NULL,
      item_type int NOT NULL,
      status character varying COLLATE pg_catalog."default" NOT NULL,
      created_at TIMESTAMP NOT NULL,
      updated_at TIMESTAMP NOT NULL,
      closes_at TIMESTAMP NOT NULL,
      open BOOLEAN NOT NULL DEFAULT TRUE,
      result int,
      CONSTRAINT market_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_market_status ON market (status);
CREATE INDEX idx_market_status_closes_at ON market (status, closes_at);
CREATE INDEX idx_market_closes_at ON market (closes_at);

DROP TABLE IF EXISTS market_check;

CREATE TABLE market_check (
                        id uuid NOT NULL,
                        check_id int NOT NULL,
                        last_check_at TIMESTAMP NOT NULL,
                        CONSTRAINT market_check_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_market_check_id ON market_check (check_id);