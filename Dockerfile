FROM ubuntu:16.04

COPY clj-holmes-ubuntu-latest /bin/clj-holmes

RUN chmod +x /bin/clj-holmes
