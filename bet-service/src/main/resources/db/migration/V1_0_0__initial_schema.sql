CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS bet;

CREATE TABLE bet(
    id uuid NOT NULL,
    customer_id character varying COLLATE pg_catalog."default" NOT NULL,
    market_id uuid NOT NULL,
    item1_id character varying COLLATE pg_catalog."default" NOT NULL,
    item1_name character varying COLLATE pg_catalog."default" NOT NULL,
    item2_id character varying COLLATE pg_catalog."default" NOT NULL,
    item2_name character varying COLLATE pg_catalog."default" NOT NULL,
    item_type int NOT NULL,
    stake int NOT NULL,
    result int NOT NULL,
    status character varying COLLATE pg_catalog."default" NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    bet_won BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT bet_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_bet_1 ON bet (market_id);
CREATE INDEX idx_bet_2 ON bet (customer_id);

CREATE INDEX idx_bet_3 ON bet (market_id, result);
CREATE INDEX idx_bet_4 ON bet (market_id, status);
CREATE INDEX idx_bet_5 ON bet (market_id, status, result);
CREATE UNIQUE INDEX idx_bet_unique_1 ON bet (customer_id, market_id);

DROP TABLE IF EXISTS market_open_status;

CREATE TABLE market_open_status(id uuid NOT NULL,
                                  market_id uuid NOT NULL,
                                  status character varying COLLATE pg_catalog."default" NOT NULL,
                                  item1_id character varying COLLATE pg_catalog."default" NOT NULL,
                                  item2_id character varying COLLATE pg_catalog."default" NOT NULL,
                                  item_type int NOT NULL,
                                  CONSTRAINT market_open_status_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_market_open_status_unique_1 ON market_open_status (market_id);
CREATE INDEX idx_market_open_status_1 ON market_open_status (market_id, status, item1_id, item2_id, item_type);

DROP TABLE IF EXISTS market_settle_status;

CREATE TABLE market_settle_status(id uuid NOT NULL,
                               market_id uuid NOT NULL,
                               expected_count int NOT NULL,
                               finished_count int NOT NULL,
                               CONSTRAINT market_settle_status_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_market_settle_status_unique_1 ON market_settle_status (market_id);

DROP TABLE IF EXISTS bet_settle_request;

CREATE TABLE bet_settle_request(
                               id uuid NOT NULL,
                               request_id uuid NOT NULL,
                               market_id uuid NOT NULL,
                               CONSTRAINT bet_settle_request_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_bet_settle_request_1 ON bet_settle_request (request_id);
CREATE INDEX idx_bet_settle_request_2 ON bet_settle_request (market_id);