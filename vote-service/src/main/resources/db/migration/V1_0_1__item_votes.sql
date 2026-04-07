DROP TABLE IF EXISTS user_item_votes;

CREATE TABLE user_item_votes (
                                  id uuid NOT NULL,
                                  user_id character varying COLLATE pg_catalog."default" NOT NULL,
                                  item_id character varying COLLATE pg_catalog."default" NOT NULL,
                                  item_type int NOT NULL,
                                  votes integer NOT NULL,
                                  CONSTRAINT user_item_votes_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_item_votes_unique ON user_item_votes (user_id, item_id, item_type);
CREATE INDEX idx_user_item_votes_1 ON user_item_votes (user_id);
CREATE INDEX idx_user_item_votes_2 ON user_item_votes (item_id);

DROP TABLE IF EXISTS user_friend_weight;

CREATE TABLE user_friend_weight (
                                    id uuid NOT NULL,
                                    user_id character varying COLLATE pg_catalog."default" NOT NULL,
                                    friend_id character varying COLLATE pg_catalog."default" NOT NULL,
                                    weight integer NOT NULL,
                                    CONSTRAINT user_friend_weight_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_friend_weight_unique_1 ON user_friend_weight (user_id, friend_id);
CREATE INDEX idx_user_friend_weight_1 ON user_friend_weight (user_id);
CREATE INDEX idx_user_friend_weight_2 ON user_friend_weight (friend_id);

DROP TABLE IF EXISTS user_item_status;

CREATE TABLE user_item_status (
                                   id uuid NOT NULL,
                                   user_id character varying COLLATE pg_catalog."default" NOT NULL,
                                   item_id character varying COLLATE pg_catalog."default" NOT NULL,
                                   item_name character varying COLLATE pg_catalog."default" NOT NULL,
                                   item_type int NOT NULL,
                                   status int NOT NULL,
                                   CONSTRAINT user_item_status_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_item_status_unique_1 ON user_item_status (user_id, item_id, item_type);
CREATE INDEX idx_user_item_status_1 ON user_item_status (user_id);
CREATE INDEX idx_user_item_status_2 ON user_item_status (item_id);
