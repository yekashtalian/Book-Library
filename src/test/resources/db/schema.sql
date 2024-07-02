DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;

CREATE TABLE reader
(
    id   BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT readers_PK PRIMARY KEY (id)
);

CREATE TABLE book
(
    id        BIGSERIAL,
    name      VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    reader_id BIGINT,
    CONSTRAINT book_PK PRIMARY KEY (id),
    CONSTRAINT book_readers_FK FOREIGN KEY (reader_id) REFERENCES reader (id) ON UPDATE CASCADE ON DELETE RESTRICT
);
